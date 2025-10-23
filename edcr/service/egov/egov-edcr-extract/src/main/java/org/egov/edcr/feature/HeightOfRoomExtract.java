package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.OccupancyDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeightOfRoomExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(HeightOfRoomExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail extract(PlanDetail pl) {

        Map<String, Integer> roomOccupancyFeature = pl.getSubFeatureColorCodesMaster().get("HeightOfRoom");
        Set<String> roomOccupancyTypes = new HashSet<>();
        roomOccupancyTypes.addAll(roomOccupancyFeature.keySet());
        if (LOG.isDebugEnabled())
            LOG.debug("Starting of Height Of Room Extract......");
        if (pl != null && !pl.getBlocks().isEmpty())
            for (Block block : pl.getBlocks())
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                    outside: for (Floor floor : block.getBuilding().getFloors()) {
                        if (!block.getTypicalFloor().isEmpty())
                            for (TypicalFloor tp : block.getTypicalFloor())
                                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                    for (Floor allFloors : block.getBuilding().getFloors())
                                        if (allFloors.getNumber().equals(tp.getModelFloorNo())) {
                                            if (allFloors.getAcRooms() != null)
                                                floor.setAcRooms(allFloors.getAcRooms());
                                            if (allFloors.getRegularRooms() != null)
                                                floor.setRegularRooms(allFloors.getRegularRooms());
                                            if (allFloors.getUnits() != null)
                                                floor.setUnits(allFloors.getUnits());                                         
                                            continue outside;
                                        }

                        for (FloorUnit unit : floor.getUnits()) {
                        	
                            extractAcRoomsForUnit(pl, block, floor, unit, layerNames, roomOccupancyTypes, roomOccupancyFeature);
                        
                            extractRegularRoomsForUnit(pl, block, floor, unit, roomOccupancyFeature, roomOccupancyTypes, layerNames);

                            extractNonInhabitableRoomsForUnit(pl, block, floor, unit, roomOccupancyFeature, roomOccupancyTypes, layerNames);
                        
                            extractHillyAreaRoomHeights(pl, block, floor, unit);
                        
                            extractDoorDetails(pl, block, floor, unit);
                            
                            extractNonHabitationalDoorDetails(pl, block, floor, unit);
                            
                            extractRoomWiseDoors(pl, block, floor, unit);
                            
                            extractWindows(pl, block, floor, unit, layerNames);
                            
                            extractWindowsForUnitLevel(pl, block, floor, unit, layerNames);

                            extractProjectionForUnitLevel(pl, block, floor, unit);

                        }
                    }
                    
        if (LOG.isDebugEnabled())
            LOG.debug("End of Height Of Room Extract......");
        return pl;
    }

    /**
     * Extracts AC (Air-Conditioned) rooms for a given floor unit in a building plan.
     * <p>
     * This method performs the following operations:
     * <ul>
     *     <li>Builds the CAD layer name pattern for AC rooms in the floor unit.</li>
     *     <li>Retrieves all matching layers from the CAD document using {@link Util#getLayerNamesLike}.</li>
     *     <li>For each AC room layer:</li>
     *         <ul>
     *             <li>Collects room heights for all occupancy types defined in {@code roomOccupancyTypes}.</li>
     *             <li>Retrieves polylines representing the room boundaries using {@link Util#getPolyLinesByLayer}.</li>
     *             <li>Checks if all polylines are closed and sets the room as closed if true.</li>
     *             <li>Creates a {@link Room} object and sets its number, heights, and polylines.</li>
     *             <li>Extracts mezzanine areas at AC room level using {@link #extractMezzanineAreas}, if present.</li>
     *         </ul>
     *     <li>Adds the fully populated AC room to the {@link FloorUnit} using {@link FloorUnit#addAcRoom}.</li>
     * </ul>
     *
     * @param pl                   the {@link PlanDetail} containing the CAD drawing and plan metadata
     * @param block                the {@link Block} to which the floor unit belongs
     * @param floor                the {@link Floor} containing the unit
     * @param unit                 the {@link FloorUnit} for which AC rooms are being extracted
     * @param layerNames           the {@link LayerNames} object containing CAD layer naming conventions
     * @param roomOccupancyTypes   a set of occupancy type names relevant for height extraction
     * @param roomOccupancyFeature a map of occupancy type names to color codes used in the CAD drawing
     */
    
    private void extractAcRoomsForUnit(PlanDetail pl, Block block, Floor floor, FloorUnit unit, LayerNames layerNames,
            Set<String> roomOccupancyTypes, Map<String, Integer> roomOccupancyFeature) {

        LOG.debug("---- Starting extractAcRoomsForUnit for Block [{}], Floor [{}], Unit [{}] ----",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        Map<Integer, List<BigDecimal>> acRoomHeightMaps = new HashMap<>();

        String acRoomLayerNames = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_AC_ROOM"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        LOG.debug("Generated AC Room layer name pattern: {}", acRoomLayerNames);

        List<String> acRoomLayerss = Util.getLayerNamesLike(pl.getDoc(), acRoomLayerNames);

        if (acRoomLayerss.isEmpty()) {
            LOG.debug("No AC Room layers found for Unit [{}]", unit.getUnitNumber());
            return;
        }

        LOG.debug("Found {} AC Room layers for Unit [{}]: {}", acRoomLayerss.size(), unit.getUnitNumber(), acRoomLayerss);

        for (String acRoomLayer : acRoomLayerss) {
            LOG.debug("Processing AC Room layer: {}", acRoomLayer);

            // Collect heights by occupancy type
            for (String type : roomOccupancyTypes) {
                Integer colorCode = roomOccupancyFeature.get(type);
                List<BigDecimal> acRoomHeights = Util.getListOfDimensionByColourCode(pl, acRoomLayer, colorCode);
                if (!acRoomHeights.isEmpty()) {
                    acRoomHeightMaps.put(colorCode, acRoomHeights);
                    LOG.debug("Added AC Room heights for color [{}]: {}", colorCode, acRoomHeights);
                }
            }

            List<DXFLWPolyline> acRoomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), acRoomLayer);
            LOG.debug("Found {} polyline(s) in AC Room layer [{}]", acRoomPolyLines.size(), acRoomLayer);

            if (!acRoomHeightMaps.isEmpty() || !acRoomPolyLines.isEmpty()) {
                boolean isClosed = acRoomPolyLines.stream().allMatch(DXFLWPolyline::isClosed);
                LOG.debug("All polylines closed: {}", isClosed);

                Room acRoom = new Room();
                String[] roomNo = acRoomLayer.split("_");
                if (roomNo != null && roomNo.length == 9) {
                    acRoom.setNumber(roomNo[8]);
                    LOG.debug("Extracted AC Room number: {}", acRoom.getNumber());
                }
                acRoom.setClosed(isClosed);

                List<RoomHeight> acRoomHeightsList = new ArrayList<>();
                if (!acRoomPolyLines.isEmpty()) {
                    List<Measurement> acRooms = new ArrayList<>();
                    acRoomPolyLines.forEach(arp -> {
                        Measurement m = new MeasurementDetail(arp, true);
                        if (!acRoomHeightMaps.isEmpty() && acRoomHeightMaps.containsKey(m.getColorCode())) {
                            for (BigDecimal value : acRoomHeightMaps.get(m.getColorCode())) {
                                RoomHeight roomHeight = new RoomHeight();
                                roomHeight.setColorCode(m.getColorCode());
                                roomHeight.setHeight(value);
                                acRoomHeightsList.add(roomHeight);
                            }
                            acRoom.setHeights(acRoomHeightsList);
                        }
                        acRooms.add(m);
                    });

                    // Extract mezzanine area (if declared at AC room level)
                    String acRoomMezzLayerRegExp = String.format(
                            layerNames.getLayerName("LAYER_NAME_UNIT_MEZZANINE_AT_ACROOM"),
                            block.getNumber(), floor.getNumber(), acRoom.getNumber(), "+\\d");

                    LOG.debug("Checking mezzanine layers using pattern: {}", acRoomMezzLayerRegExp);

                    List<String> acRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(), acRoomMezzLayerRegExp);
                    LOG.debug("Found {} mezzanine layer(s) for AC Room [{}]: {}",
                            acRoomMezzLayers.size(), acRoom.getNumber(), acRoomMezzLayers);

                    if (!acRoomMezzLayers.isEmpty()) {
                        for (String layerName : acRoomMezzLayers) {
                            List<Occupancy> roomMezzanines = extractMezzanineAreas(pl, layerName);
                            acRoom.setMezzanineAreas(roomMezzanines);
                            LOG.debug("Extracted {} mezzanine areas from layer [{}]",
                                    roomMezzanines.size(), layerName);
                        }
                    }

                    acRoom.setRooms(acRooms);
                    LOG.debug("Added {} AC Room measurement(s) for Room [{}]",
                            acRooms.size(), acRoom.getNumber());
                }

                unit.addAcRoom(acRoom);
                LOG.debug("AC Room [{}] added to Unit [{}]", acRoom.getNumber(), unit.getUnitNumber());
            }
        }

        LOG.debug("---- Completed extractAcRoomsForUnit for Block [{}], Floor [{}], Unit [{}] ----",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());
    }

	
	/**
	 * Extracts regular rooms for a given floor unit in a building plan.
	 * <p>
	 * This method performs the following steps:
	 * <ul>
	 *     <li>Builds the layer name pattern for regular rooms in the unit.</li>
	 *     <li>Retrieves all matching CAD layers using {@link Util#getLayerNamesLike}.</li>
	 *     <li>For each layer:</li>
	 *         <ul>
	 *             <li>Collects room heights for all occupancy types defined in {@code roomOccupancyTypes}.</li>
	 *             <li>Retrieves room width values based on color codes.</li>
	 *             <li>Retrieves polylines representing the room boundaries using {@link Util#getPolyLinesByLayer}.</li>
	 *             <li>Checks if all polylines are closed and sets the room as closed if true.</li>
	 *             <li>Creates a {@link Room} object and sets room number, widths, heights, and polylines.</li>
	 *             <li>Extracts mezzanine areas (if any) at room level using {@link #extractMezzanineAreas}.</li>
	 *         </ul>
	 *     <li>Adds the fully populated {@link Room} object to the {@link FloorUnit} using {@link FloorUnit#addRegularRoom}.</li>
	 * </ul>
	 *
	 * @param pl                  the {@link PlanDetail} containing the CAD drawing and plan metadata
	 * @param block               the {@link Block} to which the floor unit belongs
	 * @param floor               the {@link Floor} containing the unit
	 * @param unit                the {@link FloorUnit} for which regular rooms are being extracted
	 * @param roomOccupancyFeature a map of occupancy type names to color codes used in the CAD drawing
	 * @param roomOccupancyTypes  a set of occupancy type names relevant for height extraction
	 * @param layerNames          the {@link LayerNames} object containing CAD layer naming conventions
	 */
    
    private void extractRegularRoomsForUnit(PlanDetail pl, Block block, Floor floor, FloorUnit unit,
            Map<String, Integer> roomOccupancyFeature, Set<String> roomOccupancyTypes, LayerNames layerNames) {

        Map<Integer, List<BigDecimal>> roomHeightMaps = new HashMap<>();

        String regularRoomLayerNames = String.format(
                layerNames.getLayerName("LAYER_NAME_UNITWISE_REGULAR_ROOM"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        LOG.debug("Extracting Regular Rooms for Unit [{}] on Floor [{}] in Block [{}] using layer pattern [{}]",
                unit.getUnitNumber(), floor.getNumber(), block.getNumber(), regularRoomLayerNames);

        List<String> regularRoomLayerss = Util.getLayerNamesLike(pl.getDoc(), regularRoomLayerNames);

        if (regularRoomLayerss.isEmpty()) {
            LOG.debug("No Regular Room layers found for Unit [{}] on Floor [{}] in Block [{}]",
                    unit.getUnitNumber(), floor.getNumber(), block.getNumber());
            return;
        }

        for (String regularRoomLayer : regularRoomLayerss) {
            LOG.debug("Processing Regular Room Layer: [{}]", regularRoomLayer);

            for (String type : roomOccupancyTypes) {
                Integer colorCode = roomOccupancyFeature.get(type);
                List<BigDecimal> regularRoomHeights = Util.getListOfDimensionByColourCode(pl, regularRoomLayer, colorCode);
                LOG.debug("Found {} height values for type [{}] (colorCode={}) in layer [{}]",
                        regularRoomHeights.size(), type, colorCode, regularRoomLayer);

                if (!regularRoomHeights.isEmpty()) {
                    roomHeightMaps.put(colorCode, regularRoomHeights);
                }
            }

            List<BigDecimal> roomWidth = Util.getListOfDimensionByColourCode(pl, regularRoomLayer,
                    DxfFileConstants.INDEX_COLOR_TWO);
            LOG.debug("Room width values extracted: {}", roomWidth);

            List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), regularRoomLayer);
            LOG.debug("Number of Room Polylines found: {}", roomPolyLines.size());

            if (!roomHeightMaps.isEmpty() || !roomPolyLines.isEmpty()) {

                boolean isClosed = roomPolyLines.stream().allMatch(DXFLWPolyline::isClosed);
                LOG.debug("Room Layer [{}] closed status: {}", regularRoomLayer, isClosed);

                Room room = new Room();
                String[] roomNo = regularRoomLayer.split("_");
                if (roomNo != null && roomNo.length == 9) {
                    room.setNumber(roomNo[8]);
                    LOG.debug("Room Number extracted: {}", roomNo[8]);
                }
                room.setClosed(isClosed);

                if (!roomWidth.isEmpty()) {
                    room.setRoomWidth(roomWidth);
                    LOG.debug("Room Width set: {}", roomWidth);
                }

                List<RoomHeight> roomHeights = new ArrayList<>();
                if (!roomPolyLines.isEmpty()) {
                    List<Measurement> rooms = new ArrayList<>();
                    roomPolyLines.forEach(rp -> {
                        Measurement m = new MeasurementDetail(rp, true);
                        if (!roomHeightMaps.isEmpty() && roomHeightMaps.containsKey(m.getColorCode())) {
                            for (BigDecimal value : roomHeightMaps.get(m.getColorCode())) {
                                RoomHeight roomHeight = new RoomHeight();
                                roomHeight.setColorCode(m.getColorCode());
                                roomHeight.setHeight(value);
                                roomHeights.add(roomHeight);
                            }
                            room.setHeights(roomHeights);
                            LOG.debug("Heights set for Room [{}]: {}", room.getNumber(), roomHeights);
                        }
                        rooms.add(m);
                    });

                    // Extract mezzanine at regular room level
                    String regularRoomMezzLayerRegExp = String.format(
                            layerNames.getLayerName("LAYER_NAME_UNIT_MEZZANINE_AT_ROOM"),
                            block.getNumber(), floor.getNumber(), unit.getUnitNumber(),
                            room.getNumber(), "+\\d");

                    LOG.debug("Searching for mezzanine layers for Room [{}] using pattern [{}]",
                            room.getNumber(), regularRoomMezzLayerRegExp);

                    List<String> regularRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(), regularRoomMezzLayerRegExp);
                    if (!regularRoomMezzLayers.isEmpty()) {
                        LOG.debug("Found [{}] mezzanine layers for Room [{}]", regularRoomMezzLayers.size(), room.getNumber());
                        for (String layerName : regularRoomMezzLayers) {
                            List<Occupancy> roomMezzanines = extractMezzanineAreas(pl, layerName);
                            room.setMezzanineAreas(roomMezzanines);
                            LOG.debug("Extracted mezzanine areas for Room [{}] from layer [{}]: {}",
                                    room.getNumber(), layerName, roomMezzanines);
                        }
                    }

                    room.setRooms(rooms);
                }

                unit.addRegularRoom(room);
                LOG.debug("Regular Room [{}] added to Unit [{}] on Floor [{}], Block [{}]",
                        room.getNumber(), unit.getUnitNumber(), floor.getNumber(), block.getNumber());
            }
        }

        LOG.debug("Completed extracting Regular Rooms for Unit [{}] on Floor [{}] in Block [{}]",
                unit.getUnitNumber(), floor.getNumber(), block.getNumber());
    }

	/**
	 * Extracts mezzanine areas from a specified CAD layer.
	 * <p>
	 * This method performs the following steps:
	 * <ul>
	 *   <li>Splits the layer name to identify the mezzanine number.</li>
	 *   <li>Retrieves all polylines from the specified layer using {@link Util#getPolyLinesByLayer}.</li>
	 *   <li>For each polyline, creates an {@link OccupancyDetail} object with:</li>
	 *       <ul>
	 *           <li>Color code from the polyline.</li>
	 *           <li>Mezzanine number derived from the layer name.</li>
	 *           <li>Built-up area calculated using {@link Util#getPolyLineArea}.</li>
	 *           <li>Occupancy type determined by {@link Util#findOccupancyType}.</li>
	 *           <li>Height set as the maximum value obtained from {@link Util#getListOfDimensionValueByLayer}.</li>
	 *       </ul>
	 *   <li>All created {@link OccupancyDetail} objects are added to a list and returned.</li>
	 * </ul>
	 *
	 * @param pl                   the {@link PlanDetail} containing the drawing document and related metadata
	 * @param mezzanineLayerName   the name of the CAD layer containing mezzanine polylines
	 * @return a list of {@link Occupancy} objects representing mezzanine areas extracted from the layer
	 */

    private List<Occupancy> extractMezzanineAreas(PlanDetail pl, String mezzanineLayerName) {
        LOG.debug("Starting extraction of mezzanine areas for layer: [{}]", mezzanineLayerName);

        List<Occupancy> roomMezzanines = new ArrayList<>();

        if (mezzanineLayerName == null || mezzanineLayerName.isEmpty()) {
            LOG.debug("Mezzanine layer name is null or empty. Skipping extraction.");
            return roomMezzanines;
        }

        String[] array = mezzanineLayerName.split("_");
        String mezzanineNo = array[array.length - 1];
        LOG.debug("Extracted mezzanine number [{}] from layer name [{}]", mezzanineNo, mezzanineLayerName);

        List<DXFLWPolyline> mezzaninePolyLines = Util.getPolyLinesByLayer(pl.getDoc(), mezzanineLayerName);
        LOG.debug("Found [{}] mezzanine polylines for layer [{}]", mezzaninePolyLines.size(), mezzanineLayerName);

        if (!mezzaninePolyLines.isEmpty()) {
            for (DXFLWPolyline polyline : mezzaninePolyLines) {
                LOG.debug("Processing mezzanine polyline with color code [{}]", polyline.getColor());

                OccupancyDetail occupancy = new OccupancyDetail();
                occupancy.setColorCode(polyline.getColor());
                occupancy.setMezzanineNumber(mezzanineNo);
                occupancy.setIsMezzanine(true);

                BigDecimal area = Util.getPolyLineArea(polyline);
                occupancy.setBuiltUpArea(area);
                LOG.debug("Set mezzanine built-up area: [{}] for mezzanine number [{}]", area, mezzanineNo);

                occupancy.setTypeHelper(Util.findOccupancyType(polyline, pl));

                List<BigDecimal> heights = Util.getListOfDimensionValueByLayer(pl, mezzanineLayerName);
                LOG.debug("Found [{}] height values for mezzanine layer [{}]", heights.size(), mezzanineLayerName);

                if (!heights.isEmpty()) {
                    BigDecimal maxHeight = Collections.max(heights);
                    occupancy.setHeight(maxHeight);
                    LOG.debug("Set mezzanine height to [{}] for mezzanine number [{}]", maxHeight, mezzanineNo);
                }

                roomMezzanines.add(occupancy);
                LOG.debug("Added mezzanine occupancy detail for mezzanine number [{}]", mezzanineNo);
            }
        } else {
            LOG.debug("No mezzanine polylines found for layer [{}]", mezzanineLayerName);
        }

        LOG.debug("Completed extraction of [{}] mezzanine areas for layer [{}]",
                roomMezzanines.size(), mezzanineLayerName);

        return roomMezzanines;
    }

	
	/**
	 * Extracts non-inhabitable rooms for a given floor unit.
	 * <p>
	 * This method performs the following steps:
	 * <ul>
	 *   <li>Finds all CAD layers corresponding to non-inhabitable rooms in the unit.</li>
	 *   <li>For each layer, retrieves room heights based on color codes mapped in {@code roomOccupancyFeature}.</li>
	 *   <li>Extracts polylines representing the room geometry and determines if the room is closed.</li>
	 *   <li>Creates a {@link Room} object, sets its number, closed status, and heights.</li>
	 *   <li>Extracts mezzanine areas within the room (if present) using {@link #extractMezzanineAreas(PlanDetail, String)}.</li>
	 *   <li>Adds the created room to the unit's list of non-inhabitable rooms via {@link FloorUnit#addNonInhabitationalRooms(Room)}.</li>
	 * </ul>
	 * <p>
	 * Default behavior:
	 * <ul>
	 *   <li>If a room has no height data, the height list is left empty.</li>
	 *   <li>If a room has no polylines, it will still be created but will have no geometry.</li>
	 * </ul>
	 *
	 * @param pl                  the {@link PlanDetail} containing the drawing document and related metadata
	 * @param block               the {@link Block} in which the floor unit exists
	 * @param floor               the {@link Floor} containing the unit
	 * @param unit                the {@link FloorUnit} for which non-inhabitable rooms are to be extracted
	 * @param roomOccupancyFeature a map of occupancy types to their associated color codes
	 * @param roomOccupancyTypes   a set of occupancy types to be considered
	 * @param layerNames           the {@link LayerNames} instance used to resolve layer naming patterns
	 */

    private void extractNonInhabitableRoomsForUnit(PlanDetail pl, Block block, Floor floor, FloorUnit unit,
            Map<String, Integer> roomOccupancyFeature, Set<String> roomOccupancyTypes, LayerNames layerNames) {

        LOG.info("Starting extraction of Non-Inhabitable Rooms for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        Map<Integer, List<BigDecimal>> nonInhabitableRoomHeightMap = new HashMap<>();

        String roomLayerName = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_NON_INHABITABLE_ROOM"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        List<String> roomLayers = Util.getLayerNamesLike(pl.getDoc(), roomLayerName);
        LOG.debug("Found {} non-inhabitable room layers matching pattern [{}]", roomLayers.size(), roomLayerName);

        if (!roomLayers.isEmpty()) {
            for (String roomLayer : roomLayers) {
                LOG.debug("Processing non-inhabitable room layer: {}", roomLayer);

                for (String type : roomOccupancyTypes) {
                    Integer colorCode = roomOccupancyFeature.get(type);
                    List<BigDecimal> roomHeights = Util.getListOfDimensionByColourCode(pl, roomLayer, colorCode);
                    if (!roomHeights.isEmpty()) {
                        nonInhabitableRoomHeightMap.put(colorCode, roomHeights);
                        LOG.debug("Captured height values {} for color code {} in layer {}", roomHeights, colorCode, roomLayer);
                    }
                }

                List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), roomLayer);
                LOG.debug("Found {} polylines in non-inhabitable room layer {}", roomPolyLines.size(), roomLayer);

                if (!nonInhabitableRoomHeightMap.isEmpty() || !roomPolyLines.isEmpty()) {
                    boolean isClosed = roomPolyLines.stream().allMatch(DXFLWPolyline::isClosed);
                    LOG.debug("All room polylines closed: {}", isClosed);

                    Room room = new Room();
                    String[] roomNo = roomLayer.split("_");
                    if (roomNo != null && roomNo.length == 9) {
                        room.setNumber(roomNo[8]);
                        LOG.debug("Set room number: {}", roomNo[8]);
                    }
                    room.setClosed(isClosed);

                    List<RoomHeight> roomHeights = new ArrayList<>();
                    if (!roomPolyLines.isEmpty()) {
                        List<Measurement> rooms = new ArrayList<>();
                        roomPolyLines.forEach(arp -> {
                            Measurement m = new MeasurementDetail(arp, true);
                            if (!nonInhabitableRoomHeightMap.isEmpty()
                                    && nonInhabitableRoomHeightMap.containsKey(m.getColorCode())) {
                                for (BigDecimal value : nonInhabitableRoomHeightMap.get(m.getColorCode())) {
                                    RoomHeight roomHeight = new RoomHeight();
                                    roomHeight.setColorCode(m.getColorCode());
                                    roomHeight.setHeight(value);
                                    roomHeights.add(roomHeight);
                                }
                                room.setHeights(roomHeights);
                            }
                            rooms.add(m);
                        });

                        // Extract mezzanine at non-inhabitable room level
                        String roomMezzLayerRegExp = String.format(
                                layerNames.getLayerName("LAYER_NAME_UNIT_MEZZANINE_AT_NON_INHABITABLE_ROOM"),
                                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), room.getNumber(), "+\\d");

                        List<String> regularRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(), roomMezzLayerRegExp);
                        LOG.debug("Found {} mezzanine layers for non-inhabitable room [{}]: {}",
                                regularRoomMezzLayers.size(), room.getNumber(), regularRoomMezzLayers);

                        if (!regularRoomMezzLayers.isEmpty()) {
                            for (String layerName : regularRoomMezzLayers) {
                                List<Occupancy> roomMezzanines = extractMezzanineAreas(pl, layerName);
                                room.setMezzanineAreas(roomMezzanines);
                                LOG.debug("Extracted mezzanine areas for layer [{}] - count: {}",
                                        layerName, roomMezzanines.size());
                            }
                        }

                        room.setRooms(rooms);
                    }

                    unit.addNonInhabitationalRooms(room);
                    LOG.info("Added Non-Inhabitable Room [{}] to Unit [{}], Floor [{}], Block [{}]",
                            room.getNumber(), unit.getUnitNumber(), floor.getNumber(), block.getNumber());
                }
            }
        } else {
            LOG.debug("No non-inhabitable room layers found for Unit [{}], Floor [{}], Block [{}]",
                    unit.getUnitNumber(), floor.getNumber(), block.getNumber());
        }

        LOG.info("Completed extraction of Non-Inhabitable Rooms for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());
    }

	
	/**
	 * Extracts and assigns hilly area room heights for a given floor unit.
	 * <p>
	 * This method scans CAD layers representing hilly area room heights for the specified 
	 * {@link FloorUnit}. For each matching layer:
	 * <ul>
	 *   <li>It retrieves the room height from the MText entity (if available).</li>
	 *   <li>Creates a {@link Room} object and sets its hilly area room height.</li>
	 *   <li>Adds the created room to the unit's list of regular rooms via {@link FloorUnit#addRegularRoom(Room)}.</li>
	 * </ul>
	 * <p>
	 * Default behavior:
	 * <ul>
	 *   <li>If no MText value is found for the room height, it defaults to {@code BigDecimal.ZERO}.</li>
	 * </ul>
	 *
	 * @param pl    the {@link PlanDetail} object containing the drawing document and related metadata
	 * @param block the {@link Block} in which the floor unit exists
	 * @param floor the {@link Floor} that contains the unit
	 * @param unit  the {@link FloorUnit} for which hilly area room heights need to be extracted
	 */
	
    private void extractHillyAreaRoomHeights(PlanDetail pl, Block block, Floor floor, FloorUnit unit) {
        LOG.debug("Starting extraction of hilly area room heights for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        String hillyAreaRoomHeightLayerName = String.format(
                layerNames.getLayerName("LAYER_NAME_UNIT_HILLY_ROOM_HEIGHT"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d"
        );
        LOG.debug("Generated hilly area room height layer name pattern: {}", hillyAreaRoomHeightLayerName);

        List<String> hillyHeightLayers = Util.getLayerNamesLike(pl.getDoc(), hillyAreaRoomHeightLayerName);
        LOG.debug("Found [{}] hilly area room height layers", hillyHeightLayers.size());

        if (!hillyHeightLayers.isEmpty()) {
            for (String hillyLayer : hillyHeightLayers) {
                LOG.debug("Processing hilly area room height layer: {}", hillyLayer);

                String hillyHeight = Util.getMtextByLayerName(pl.getDoc(), hillyLayer);
                LOG.debug("Extracted hilly height text: {}", hillyHeight);

                List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), hillyLayer);
                LOG.debug("Found [{}] dimensions for layer [{}]", 
                        dimensionList != null ? dimensionList.size() : 0, hillyLayer);

                if (dimensionList != null && !dimensionList.isEmpty()) {
                    Room room = new Room();

                    BigDecimal hillyHeightValue = hillyHeight != null
                            ? BigDecimal.valueOf(Double.valueOf(hillyHeight.replaceAll("HILLY_ROOM_HT_M=", "")))
                            : BigDecimal.ZERO;

                    LOG.debug("Parsed hilly room height value: {}", hillyHeightValue);

                    room.setHillyAreaRoomHeight(hillyHeightValue);
                    unit.addRegularRoom(room);

                    LOG.debug("Added hilly area room height [{}] to unit [{}]", hillyHeightValue, unit.getUnitNumber());
                } else {
                    LOG.debug("No dimensions found for hilly layer [{}]; skipping room creation", hillyLayer);
                }
            }
        } else {
            LOG.debug("No hilly area room height layers found for Unit [{}]", unit.getUnitNumber());
        }

        LOG.debug("Completed extraction of hilly area room heights for Unit [{}]", unit.getUnitNumber());
    }

	
	/**
	 * Extracts and assigns door details for a given floor unit.
	 * <p>
	 * This method identifies CAD layers corresponding to doors for the specified 
	 * {@link FloorUnit}. For each matching door layer:
	 * <ul>
	 *   <li>Retrieves the door height from the MText entity, if available.</li>
	 *   <li>Retrieves the door width values from the dimension entities.</li>
	 *   <li>Creates a {@link Door} object and sets its height and width.</li>
	 *   <li>Adds the created door to the unit's list of doors via {@link FloorUnit#addDoor(Door)}.</li>
	 * </ul>
	 * <p>
	 * Default behavior:
	 * <ul>
	 *   <li>If no door height is found, it defaults to {@code BigDecimal.ZERO}.</li>
	 *   <li>If no valid dimensions are found, the door width defaults to {@code BigDecimal.ZERO}.</li>
	 * </ul>
	 *
	 * @param pl    the {@link PlanDetail} object containing the drawing document and related metadata
	 * @param block the {@link Block} in which the floor unit exists
	 * @param floor the {@link Floor} that contains the unit
	 * @param unit  the {@link FloorUnit} for which door details need to be extracted
	 */
	
    private void extractDoorDetails(PlanDetail pl, Block block, Floor floor, FloorUnit unit) {
        LOG.debug("Starting extraction of door details for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        String doorLayerName = String.format(
                layerNames.getLayerName("LAYER_NAME_UNIT_DOOR"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d"
        );
        LOG.debug("Generated door layer name pattern: {}", doorLayerName);

        List<String> doorLayers = Util.getLayerNamesLike(pl.getDoc(), doorLayerName);
        LOG.debug("Found [{}] door layers for Unit [{}]", doorLayers.size(), unit.getUnitNumber());

        if (!doorLayers.isEmpty()) {
            for (String doorLayer : doorLayers) {
                LOG.debug("Processing door layer: {}", doorLayer);

                String doorHeight = Util.getMtextByLayerName(pl.getDoc(), doorLayer);
                LOG.debug("Extracted door height text: {}", doorHeight);

                List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), doorLayer);
                LOG.debug("Found [{}] dimensions for layer [{}]",
                        dimensionList != null ? dimensionList.size() : 0, doorLayer);

                if (dimensionList != null && !dimensionList.isEmpty()) {
                    Door door = new Door();

                    BigDecimal doorHeightValue = doorHeight != null
                            ? BigDecimal.valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
                            : BigDecimal.ZERO;

                    door.setDoorHeight(doorHeightValue);
                    LOG.debug("Parsed and set door height: {}", doorHeightValue);

                    for (Object dxfEntity : dimensionList) {
                        DXFDimension dimension = (DXFDimension) dxfEntity;
                        List<BigDecimal> values = new ArrayList<>();
                        Util.extractDimensionValue(pl, values, dimension, doorLayer);

                        if (!values.isEmpty()) {
                            for (BigDecimal minDis : values) {
                                door.setDoorWidth(minDis);
                                LOG.debug("Extracted and set door width: {}", minDis);
                            }
                        } else {
                            door.setDoorWidth(BigDecimal.ZERO);
                            LOG.debug("No width values found for layer [{}], defaulting to 0", doorLayer);
                        }
                    }

                    unit.addDoor(door);
                    LOG.debug("Added door with height [{}] and width [{}] to Unit [{}]",
                            door.getDoorHeight(), door.getDoorWidth(), unit.getUnitNumber());
                } else {
                    LOG.debug("No dimensions found for door layer [{}], skipping door creation", doorLayer);
                }
            }
        } else {
            LOG.debug("No door layers found for Unit [{}]", unit.getUnitNumber());
        }

        LOG.debug("Completed extraction of door details for Unit [{}]", unit.getUnitNumber());
    }


	/**
	 * Extracts and assigns non-habitational doors for a given floor unit. 
	 * <p>
	 * This method searches CAD layers that represent non-habitational doors 
	 * for the specified {@link FloorUnit}. For each matching door layer:
	 * <ul>
	 *   <li>It retrieves the door height from the MText entity (if present).</li>
	 *   <li>It retrieves the door width values from the dimension entities.</li>
	 *   <li>A {@link Door} object is created and populated with the extracted 
	 *       non-habitational door height and width.</li>
	 *   <li>The created door is then added to the unit's list of non-habitational doors.</li>
	 * </ul>
	 * <p>
	 * If no MText value is found, the door height defaults to {@code BigDecimal.ZERO}.  
	 * If no valid dimensions are found, the door width defaults to {@code BigDecimal.ZERO}.
	 *
	 * @param pl    the {@link PlanDetail} object containing the drawing document and related metadata
	 * @param block the {@link Block} in which the floor unit exists
	 * @param floor the {@link Floor} that contains the unit
	 * @param unit  the {@link FloorUnit} for which non-habitational door details need to be extracted
	 */
	
    private void extractNonHabitationalDoorDetails(PlanDetail pl, Block block, Floor floor, FloorUnit unit) {
        LOG.debug("Starting extraction of non-habitational door details for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        String nonHabitationaldoorLayer = String.format(
                layerNames.getLayerName("LAYER_NAME_UNIT_NON_HABITATIONAL_DOOR"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d"
        );
        LOG.debug("Generated non-habitational door layer name pattern: {}", nonHabitationaldoorLayer);

        List<String> nonHabitationaldoorLayers = Util.getLayerNamesLike(pl.getDoc(), nonHabitationaldoorLayer);
        LOG.debug("Found [{}] non-habitational door layers for Unit [{}]", nonHabitationaldoorLayers.size(), unit.getUnitNumber());

        if (!nonHabitationaldoorLayers.isEmpty()) {
            for (String doorLayer : nonHabitationaldoorLayers) {
                LOG.debug("Processing non-habitational door layer: {}", doorLayer);

                String doorHeight = Util.getMtextByLayerName(pl.getDoc(), doorLayer);
                LOG.debug("Extracted door height text: {}", doorHeight);

                List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), doorLayer);
                LOG.debug("Found [{}] dimensions for layer [{}]",
                        dimensionList != null ? dimensionList.size() : 0, doorLayer);

                if (dimensionList != null && !dimensionList.isEmpty()) {
                    Door door = new Door();

                    BigDecimal doorHeightValue = doorHeight != null
                            ? BigDecimal.valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
                            : BigDecimal.ZERO;
                    door.setNonHabitationDoorHeight(doorHeightValue);
                    LOG.debug("Parsed and set non-habitational door height: {}", doorHeightValue);

                    for (Object dxfEntity : dimensionList) {
                        DXFDimension dimension = (DXFDimension) dxfEntity;
                        List<BigDecimal> values = new ArrayList<>();
                        Util.extractDimensionValue(pl, values, dimension, doorLayer);

                        if (!values.isEmpty()) {
                            for (BigDecimal minDis : values) {
                                door.setNonHabitationDoorWidth(minDis);
                                LOG.debug("Extracted and set non-habitational door width: {}", minDis);
                            }
                        } else {
                            door.setNonHabitationDoorWidth(BigDecimal.ZERO);
                            LOG.debug("No width values found for layer [{}], defaulting to 0", doorLayer);
                        }
                    }

                    unit.addNonaHabitationalDoors(door);
                    LOG.debug("Added non-habitational door with height [{}] and width [{}] to Unit [{}]",
                            door.getNonHabitationDoorHeight(), door.getNonHabitationDoorWidth(), unit.getUnitNumber());
                } else {
                    LOG.debug("No dimensions found for non-habitational door layer [{}], skipping door creation", doorLayer);
                }
            }
        } else {
            LOG.debug("No non-habitational door layers found for Unit [{}]", unit.getUnitNumber());
        }

        LOG.debug("Completed extraction of non-habitational door details for Unit [{}]", unit.getUnitNumber());
    }


	/**
	 * Extracts and assigns doors for each regular room inside a given floor unit. 
	 * <p>
	 * This method scans CAD layers for door definitions corresponding to each room 
	 * in the given {@link FloorUnit}. It identifies door layers by matching their 
	 * names using the configured naming convention. For each door layer:
	 * <ul>
	 *   <li>It retrieves the door height from the MText entity (if available).</li>
	 *   <li>It retrieves the door width values from dimension entities in the layer.</li>
	 *   <li>A {@link Door} object is created and populated with the extracted height and width.</li>
	 *   <li>The door is then added to the respective {@link Room} in the unit.</li>
	 * </ul>
	 * If no valid dimensions are found, the door width defaults to {@code BigDecimal.ZERO}.
	 * If no door height MText is found, the height defaults to {@code BigDecimal.ZERO}.
	 *
	 * @param pl    the {@link PlanDetail} object containing the drawing document and related metadata
	 * @param block the {@link Block} in which the floor unit exists
	 * @param floor the {@link Floor} that contains the unit
	 * @param unit  the {@link FloorUnit} for which room-wise doors need to be extracted
	 */
    
    private void extractRoomWiseDoors(PlanDetail pl, Block block, Floor floor, FloorUnit unit) {
        LOG.debug("Starting extractRoomWiseDoors for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        for (Room room : unit.getRegularRooms()) {
            LOG.debug("Processing doors for Room [{}] in Unit [{}]", room.getNumber(), unit.getUnitNumber());

            String roomDoorLayerName = String.format(
                    layerNames.getLayerName("LAYER_NAME_UNIT_REGULAR_ROOM_DOOR"),
                    block.getNumber(), floor.getNumber(), unit.getUnitNumber(), room.getNumber(), "+\\d"
            );

            List<String> roomDoorLayers = Util.getLayerNamesLike(pl.getDoc(), roomDoorLayerName);
            LOG.debug("Found [{}] matching door layers for room [{}]: {}", roomDoorLayers.size(), room.getNumber(), roomDoorLayers);

            if (!roomDoorLayers.isEmpty()) {
                for (String doorLayer : roomDoorLayers) {
                    LOG.debug("Processing door layer [{}] for Room [{}]", doorLayer, room.getNumber());

                    String doorHeight = Util.getMtextByLayerName(pl.getDoc(), doorLayer);
                    LOG.debug("Extracted door height text [{}] from layer [{}]", doorHeight, doorLayer);

                    List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), doorLayer);
                    LOG.debug("Found [{}] dimensions for door layer [{}]", 
                            dimensionList != null ? dimensionList.size() : 0, doorLayer);

                    if (dimensionList != null && !dimensionList.isEmpty()) {
                        Door door = new Door();

                        BigDecimal doorHeightValue = doorHeight != null
                                ? BigDecimal.valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
                                : BigDecimal.ZERO;
                        door.setDoorHeight(doorHeightValue);
                        LOG.debug("Set door height to [{}] for Room [{}]", doorHeightValue, room.getNumber());

                        for (Object dxfEntity : dimensionList) {
                            DXFDimension dimension = (DXFDimension) dxfEntity;
                            List<BigDecimal> values = new ArrayList<>();
                            Util.extractDimensionValue(pl, values, dimension, doorLayer);

                            if (!values.isEmpty()) {
                                for (BigDecimal minDis : values) {
                                    door.setDoorWidth(minDis);
                                    LOG.debug("Set door width to [{}] for Room [{}]", minDis, room.getNumber());
                                }
                            } else {
                                door.setDoorWidth(BigDecimal.ZERO);
                                LOG.debug("No dimension values found, setting default door width = 0 for Room [{}]", room.getNumber());
                            }
                        }
                        room.addDoors(door);
                        LOG.debug("Added door to Room [{}] in Unit [{}]", room.getNumber(), unit.getUnitNumber());
                    } else {
                        LOG.debug("No dimension entities found for layer [{}]", doorLayer);
                    }
                }
            } else {
                LOG.debug("No door layers found for Room [{}] in Unit [{}]", room.getNumber(), unit.getUnitNumber());
            }
        }

        LOG.debug("Completed extractRoomWiseDoors for Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), unit.getUnitNumber());
    }

	
	/**
	 * Extracts window details (height and width) for all regular rooms in a given floor
	 * and associates the extracted windows with their respective rooms.
	 *
	 * <p>
	 * This method scans each regular room on the floor, constructs window layer names,
	 * retrieves window dimensions and height annotations from the drawing layers,
	 * and creates {@link Window} objects that are added to the corresponding {@link Room}.
	 * </p>
	 *
	 * @param pl          the {@link PlanDetail} containing document details
	 * @param block       the {@link Block} in which the floor exists
	 * @param floor       the {@link Floor} for which windows are to be extracted
	 * @param layerNames  the {@link LayerNames} utility for resolving CAD layer naming conventions
	 */
    private void extractWindows(PlanDetail pl, Block block, Floor floor, FloorUnit unit, LayerNames layerNames) {
        LOG.debug("Starting window extraction for Block [{}], Floor [{}], Unit [{}]", 
                  block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        for (Room room : unit.getRegularRooms()) {
            LOG.debug("Processing windows for Room [{}] in Unit [{}]", room.getNumber(), unit.getUnitNumber());

            String windowLayerName = String.format(
                    layerNames.getLayerName("LAYER_NAME_UNIT_ROOM_WINDOW"),
                    block.getNumber(),
                    floor.getNumber(),
                    unit.getUnitNumber(),
                    room.getNumber(),
                    "+\\d"
            );

            LOG.debug("Generated window layer name pattern: {}", windowLayerName);

            List<String> windowLayers = Util.getLayerNamesLike(pl.getDoc(), windowLayerName);
            LOG.debug("Found [{}] window layers matching pattern [{}]", windowLayers.size(), windowLayerName);

            if (!windowLayers.isEmpty()) {
                for (String windowLayer : windowLayers) {
                    LOG.debug("Processing window layer: {}", windowLayer);

                    String windowHeight = Util.getMtextByLayerName(pl.getDoc(), windowLayer);
                    LOG.debug("Extracted window height text: {}", windowHeight);

                    List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), windowLayer);
                    LOG.debug("Found [{}] dimensions for window layer [{}]", 
                              dimensionList != null ? dimensionList.size() : 0, windowLayer);

                    if (dimensionList != null && !dimensionList.isEmpty()) {
                        Window window = new Window();

                        BigDecimal windowHeight1 = windowHeight != null
                                ? BigDecimal.valueOf(Double.valueOf(windowHeight.replaceAll("WINDOW_HT_M=", "")))
                                : BigDecimal.ZERO;
                        window.setWindowHeight(windowHeight1);
                        LOG.debug("Set window height to [{}]", windowHeight1);

                        for (Object dxfEntity : dimensionList) {
                            DXFDimension dimension = (DXFDimension) dxfEntity;
                            List<BigDecimal> values = new ArrayList<>();
                            Util.extractDimensionValue(pl, values, dimension, windowLayer);
                            LOG.debug("Extracted dimension values: {}", values);

                            if (!values.isEmpty()) {
                                for (BigDecimal minDis : values) {
                                    window.setWindowWidth(minDis);
                                    LOG.debug("Set window width to [{}]", minDis);
                                }
                            } else {
                                window.setWindowWidth(BigDecimal.ZERO);
                                LOG.debug("No dimension values found; setting window width to 0");
                            }
                        }
                        room.addWindow(window);
                        LOG.debug("Added window to Room [{}]", room.getNumber());
                    }
                }
            } else {
                LOG.debug("No window layers found for Room [{}]", room.getNumber());
            }
        }

        LOG.debug("Completed window extraction for Block [{}], Floor [{}], Unit [{}]", 
                  block.getNumber(), floor.getNumber(), unit.getUnitNumber());
    }

	/**
	 * Extracts window details (height and width) for a given floor and
	 * associates the extracted windows with the {@link Floor}.
	 *
	 * <p>
	 * This method constructs window layer names for the floor, retrieves window
	 * dimensions and height annotations from the drawing layers, and creates
	 * {@link Window} objects that are added directly to the {@link Floor}.
	 * </p>
	 *
	 * @param pl          the {@link PlanDetail} containing document details
	 * @param block       the {@link Block} in which the floor exists
	 * @param floor       the {@link Floor} for which windows are to be extracted
	 * @param layerNames  the {@link LayerNames} utility for resolving CAD layer naming conventions
	 */
    private void extractWindowsForUnitLevel(PlanDetail pl, Block block, Floor floor, FloorUnit unit, LayerNames layerNames) {
        LOG.debug("Starting window extraction for Unit Level - Block [{}], Floor [{}], Unit [{}]", 
                  block.getNumber(), floor.getNumber(), unit.getUnitNumber());

        String windowLayerName = String.format(
                layerNames.getLayerName("LAYER_NAME_UNIT_WINDOW"),
                block.getNumber(),
                floor.getNumber(),
                unit.getUnitNumber(),
                "+\\d"
        );
        LOG.debug("Generated window layer name pattern: {}", windowLayerName);

        List<String> windowLayers = Util.getLayerNamesLike(pl.getDoc(), windowLayerName);
        LOG.debug("Found [{}] window layers matching pattern [{}]", windowLayers.size(), windowLayerName);

        if (!windowLayers.isEmpty()) {
            for (String windowLayer : windowLayers) {
                LOG.debug("Processing window layer: {}", windowLayer);

                String windowHeight = Util.getMtextByLayerName(pl.getDoc(), windowLayer);
                LOG.debug("Extracted window height text: {}", windowHeight);

                List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), windowLayer);
                LOG.debug("Found [{}] dimensions for window layer [{}]", 
                          dimensionList != null ? dimensionList.size() : 0, windowLayer);

                if (dimensionList != null && !dimensionList.isEmpty()) {
                    Window window = new Window();

                    BigDecimal windowHeight1 = windowHeight != null
                            ? BigDecimal.valueOf(Double.valueOf(windowHeight.replaceAll("WINDOW_HT_M=", "")))
                            : BigDecimal.ZERO;
                    window.setWindowHeight(windowHeight1);
                    LOG.debug("Set window height to [{}]", windowHeight1);

                    for (Object dxfEntity : dimensionList) {
                        DXFDimension dimension = (DXFDimension) dxfEntity;
                        List<BigDecimal> values = new ArrayList<>();
                        Util.extractDimensionValue(pl, values, dimension, windowLayer);
                        LOG.debug("Extracted dimension values: {}", values);

                        if (!values.isEmpty()) {
                            for (BigDecimal minDis : values) {
                                window.setWindowWidth(minDis);
                                LOG.debug("Set window width to [{}]", minDis);
                            }
                        } else {
                            window.setWindowWidth(BigDecimal.ZERO);
                            LOG.debug("No dimension values found; setting window width to 0");
                        }
                    }

                    unit.addWindow(window);
                    LOG.debug("Added window to Unit [{}]", unit.getUnitNumber());
                }
            }
        } else {
            LOG.debug("No window layers found for Unit [{}]", unit.getUnitNumber());
        }

        LOG.debug("Completed window extraction for Unit Level - Block [{}], Floor [{}], Unit [{}]", 
                  block.getNumber(), floor.getNumber(), unit.getUnitNumber());
    }

    /**
     * Extracts projection details for a specific floor unit.
     * Processes CAD layers to identify and extract building projections
     * (balconies, bay windows, etc.) at the unit level.
     *
     * @param pl the PlanDetail containing the CAD drawing and plan metadata
     * @param block the Block to which the floor unit belongs
     * @param floor the Floor containing the unit
     * @param floorUnit the FloorUnit for which projections are being extracted
     */
    private void extractProjectionForUnitLevel(PlanDetail pl, Block block, Floor floor, FloorUnit floorUnit) {
        LOG.debug("Starting projection extraction for Unit Level - Block [{}], Floor [{}], Unit [{}]",
                block.getNumber(), floor.getNumber(), floorUnit.getUnitNumber());

        if (!floorUnit.getRegularRooms().isEmpty()) {
            for (Room room : floorUnit.getRegularRooms()) {
                String projectionLayer = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_PROJECTION"), block.getNumber(), floor.getNumber(), floorUnit.getUnitNumber(), room.getNumber(), "+\\d");
                List<String> projectionRoomLayers = Util.getLayerNamesLike(pl.getDoc(), projectionLayer);

                List<Projections> projection = new ArrayList<>();
                for (String projectionlayer : projectionRoomLayers) {
                    List<DXFLWPolyline> polylines = Util.getPolyLinesByLayer(pl.getDoc(), projectionlayer);
                    List<BigDecimal> projectionLength = Util.getListOfDimensionByColourCode(pl, projectionlayer,
                            DxfFileConstants.INDEX_COLOR_ONE);
                    List<Projections> projectionMeasurements = polylines.stream()
                            .map(pline -> {
                                Projections proj = new Projections();
                                MeasurementDetail measurement = new MeasurementDetail(pline, true);
                                proj.setArea(measurement.getArea());
                                proj.setWidth(measurement.getWidth());
                                proj.setHeight(measurement.getHeight());
                                proj.setLength(projectionLength.get(0));  // Assuming first value is the required length, because there will always be only one length for projection
                                return proj;
                            }).collect(Collectors.toList());

                    projection.addAll(projectionMeasurements);

                }
                room.setRoomProjections(projection);
            }
        }

    }


    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

}