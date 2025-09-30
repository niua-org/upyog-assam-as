package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Door;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.Room;
import org.egov.common.entity.edcr.RoomHeight;
import org.egov.common.entity.edcr.TypicalFloor;
import org.egov.common.entity.edcr.Window;
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

                        }

						// Code Added by Neha for roomwise windows extract
						 for (Room room : floor.getRegularRooms()) {
                            
						String windowLayerName = String.format(layerNames.getLayerName("LAYER_NAME_REGULAR_ROOM_WINDOW"),
								block.getNumber(), floor.getNumber(), room.getNumber(), "+\\d");

						List<String> windowLayers = Util.getLayerNamesLike(pl.getDoc(), windowLayerName);

						if (!windowLayers.isEmpty()) {

							for (String windowLayer : windowLayers) {
								String windowHeight = Util.getMtextByLayerName(pl.getDoc(), windowLayer);

//                            	List<DXFLWPolyline> doorPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
//                            			doorLayer);

//                            	BigDecimal doorWidth=BigDecimal.ZERO;

								List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), windowLayer);
								if (dimensionList != null && !dimensionList.isEmpty()) {
									Window window = new Window();
									BigDecimal windowHeight1 = windowHeight != null
											? BigDecimal.valueOf(
													Double.valueOf(windowHeight.replaceAll("WINDOW_HT_M=", "")))
											: BigDecimal.ZERO;
									window.setWindowHeight(windowHeight1);
									for (Object dxfEntity : dimensionList) {
										DXFDimension dimension = (DXFDimension) dxfEntity;
										List<BigDecimal> values = new ArrayList<>();
										Util.extractDimensionValue(pl, values, dimension, windowLayer);

										if (!values.isEmpty()) {
											for (BigDecimal minDis : values) {
//                                            	doorWidth=minDis;
												window.setWindowWidth(minDis);
											}
										} else {
											window.setWindowWidth(BigDecimal.ZERO);
										}
									}
									room.addWindow(window);
								}
//								else {
//									window.setWindowWidth(BigDecimal.ZERO);
//								}

							}
						}
						 }
						 
						
						
						// Code Added by Neha for windows extract

						String windowLayerName = String.format(layerNames.getLayerName("LAYER_NAME_WINDOW"),
								block.getNumber(), floor.getNumber(), "+\\d");

						List<String> windowLayers = Util.getLayerNamesLike(pl.getDoc(), windowLayerName);

						if (!windowLayers.isEmpty()) {

							for (String windowLayer : windowLayers) {
								String windowHeight = Util.getMtextByLayerName(pl.getDoc(), windowLayer);

//                            	List<DXFLWPolyline> doorPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
//                            			doorLayer);

//                            	BigDecimal doorWidth=BigDecimal.ZERO;

								List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), windowLayer);
								if (dimensionList != null && !dimensionList.isEmpty()) {
									Window window = new Window();
									BigDecimal windowHeight1 = windowHeight != null
											? BigDecimal.valueOf(
													Double.valueOf(windowHeight.replaceAll("WINDOW_HT_M=", "")))
											: BigDecimal.ZERO;
									window.setWindowHeight(windowHeight1);
									for (Object dxfEntity : dimensionList) {
										DXFDimension dimension = (DXFDimension) dxfEntity;
										List<BigDecimal> values = new ArrayList<>();
										Util.extractDimensionValue(pl, values, dimension, windowLayer);

										if (!values.isEmpty()) {
											for (BigDecimal minDis : values) {
//                                            	doorWidth=minDis;
												window.setWindowWidth(minDis);
											}
										} else {
											window.setWindowWidth(BigDecimal.ZERO);
										}
									}
									floor.addWindow(window);
								}
//								else {
//									window.setWindowWidth(BigDecimal.ZERO);
//								}

							}
						}
					//}
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

		Map<Integer, List<BigDecimal>> acRoomHeightMaps = new HashMap<>();

		String acRoomLayerNames = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_AC_ROOM"), block.getNumber(),
				floor.getNumber(), unit.getUnitNumber(), "+\\d");

		List<String> acRoomLayerss = Util.getLayerNamesLike(pl.getDoc(), acRoomLayerNames);

		if (acRoomLayerss.isEmpty()) {
			return;
		}

		for (String acRoomLayer : acRoomLayerss) {

			// Collect heights by occupancy type
			for (String type : roomOccupancyTypes) {
				Integer colorCode = roomOccupancyFeature.get(type);
				List<BigDecimal> acRoomHeights = Util.getListOfDimensionByColourCode(pl, acRoomLayer, colorCode);
				if (!acRoomHeights.isEmpty()) {
					acRoomHeightMaps.put(colorCode, acRoomHeights);
				}
			}

			List<DXFLWPolyline> acRoomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), acRoomLayer);

			if (!acRoomHeightMaps.isEmpty() || !acRoomPolyLines.isEmpty()) {
				boolean isClosed = acRoomPolyLines.stream().allMatch(DXFLWPolyline::isClosed);

				Room acRoom = new Room();
				String[] roomNo = acRoomLayer.split("_");
				if (roomNo != null && roomNo.length == 9) {
					acRoom.setNumber(roomNo[8]);
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
							layerNames.getLayerName("LAYER_NAME_UNIT_MEZZANINE_AT_ACROOM"), block.getNumber(),
							floor.getNumber(), acRoom.getNumber(), "+\\d");

					List<String> acRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(), acRoomMezzLayerRegExp);

					if (!acRoomMezzLayers.isEmpty()) {
					    for (String layerName : acRoomMezzLayers) {
					        List<Occupancy> roomMezzanines = extractMezzanineAreas(pl, layerName);
					        acRoom.setMezzanineAreas(roomMezzanines);
					    }
					}


					acRoom.setRooms(acRooms);
				}
				unit.addAcRoom(acRoom);
			}
		}
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

		String regularRoomLayerNames = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_REGULAR_ROOM"),
				block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

		List<String> regularRoomLayerss = Util.getLayerNamesLike(pl.getDoc(), regularRoomLayerNames);

		if (!regularRoomLayerss.isEmpty()) {
			for (String regularRoomLayer : regularRoomLayerss) {

				for (String type : roomOccupancyTypes) {
					Integer colorCode = roomOccupancyFeature.get(type);
					List<BigDecimal> regularRoomHeights = Util.getListOfDimensionByColourCode(pl, regularRoomLayer,
							colorCode);
					if (!regularRoomHeights.isEmpty()) {
						roomHeightMaps.put(colorCode, regularRoomHeights);
					}
				}

				List<BigDecimal> roomWidth = Util.getListOfDimensionByColourCode(pl, regularRoomLayer,
						DxfFileConstants.INDEX_COLOR_TWO);

				List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), regularRoomLayer);

				if (!roomHeightMaps.isEmpty() || !roomPolyLines.isEmpty()) {

					boolean isClosed = roomPolyLines.stream().allMatch(DXFLWPolyline::isClosed);

					Room room = new Room();
					String[] roomNo = regularRoomLayer.split("_");
					if (roomNo != null && roomNo.length == 9) {
						room.setNumber(roomNo[8]);
					}
					room.setClosed(isClosed);

					if (!roomWidth.isEmpty()) {
						room.setRoomWidth(roomWidth);
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
							}
							rooms.add(m);
						});

                  // Extract mezzanine at regular room level
						String regularRoomMezzLayerRegExp = String.format(
								layerNames.getLayerName("LAYER_NAME_UNIT_MEZZANINE_AT_ROOM"), block.getNumber(),
								floor.getNumber(), unit.getUnitNumber(), room.getNumber(), "+\\d");

						
						List<String> regularRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(), regularRoomMezzLayerRegExp);
						if (!regularRoomMezzLayers.isEmpty()) {
						    for (String layerName : regularRoomMezzLayers) {
						        List<Occupancy> roomMezzanines = extractMezzanineAreas(pl, layerName);
						        room.setMezzanineAreas(roomMezzanines);
						    }
						}


						room.setRooms(rooms);
					}

					unit.addRegularRoom(room);
                   // floor.addRegularRoom(room); 
				}
			}
		}
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
	    List<Occupancy> roomMezzanines = new ArrayList<>();
	    String[] array = mezzanineLayerName.split("_");
	    String mezzanineNo = array[array.length - 1];

	    List<DXFLWPolyline> mezzaninePolyLines = Util.getPolyLinesByLayer(pl.getDoc(), mezzanineLayerName);

	    if (!mezzaninePolyLines.isEmpty()) {
	        for (DXFLWPolyline polyline : mezzaninePolyLines) {
	            OccupancyDetail occupancy = new OccupancyDetail();
	            occupancy.setColorCode(polyline.getColor());
	            occupancy.setMezzanineNumber(mezzanineNo);
	            occupancy.setIsMezzanine(true);
	            occupancy.setBuiltUpArea(Util.getPolyLineArea(polyline));
	            occupancy.setTypeHelper(Util.findOccupancyType(polyline, pl));

	            List<BigDecimal> heights = Util.getListOfDimensionValueByLayer(pl, mezzanineLayerName);
	            if (!heights.isEmpty()) {
	                occupancy.setHeight(Collections.max(heights));
	            }
	            roomMezzanines.add(occupancy);
	        }
	    }
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

		Map<Integer, List<BigDecimal>> nonInhabitableRoomHeightMap = new HashMap<>();

		String roomLayerName = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_NON_INHABITABLE_ROOM"),
				block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

		List<String> roomLayers = Util.getLayerNamesLike(pl.getDoc(), roomLayerName);

		if (!roomLayers.isEmpty()) {
			for (String roomLayer : roomLayers) {

				for (String type : roomOccupancyTypes) {
					Integer colorCode = roomOccupancyFeature.get(type);
					List<BigDecimal> roomheights = Util.getListOfDimensionByColourCode(pl, roomLayer, colorCode);
					if (!roomheights.isEmpty()) {
						nonInhabitableRoomHeightMap.put(colorCode, roomheights);
					}
				}

				List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), roomLayer);

				if (!nonInhabitableRoomHeightMap.isEmpty() || !roomPolyLines.isEmpty()) {
					boolean isClosed = roomPolyLines.stream().allMatch(DXFLWPolyline::isClosed);

					Room room = new Room();
					String[] roomNo = roomLayer.split("_");
					if (roomNo != null && roomNo.length == 9) {
						room.setNumber(roomNo[8]);
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
						if (!regularRoomMezzLayers.isEmpty()) {
						    for (String layerName : regularRoomMezzLayers) {
						        List<Occupancy> roomMezzanines = extractMezzanineAreas(pl, layerName);
						        room.setMezzanineAreas(roomMezzanines);
						    }
						}

						room.setRooms(rooms);
					}
					unit.addNonInhabitationalRooms(room);
				}
			}
		}
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
	    String hillyAreaRoomHeightLayerName = String.format(
	        layerNames.getLayerName("LAYER_NAME_UNIT_HILLY_ROOM_HEIGHT"),
	        block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d"
	    );

	    List<String> hillyHeightLayers = Util.getLayerNamesLike(pl.getDoc(), hillyAreaRoomHeightLayerName);

	    if (!hillyHeightLayers.isEmpty()) {
	        for (String hillyLayer : hillyHeightLayers) {
	            String hillyHeight = Util.getMtextByLayerName(pl.getDoc(), hillyLayer);

	            List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), hillyLayer);
	            if (dimensionList != null && !dimensionList.isEmpty()) {
	                Room room = new Room();

	                BigDecimal hillyHeightValue = hillyHeight != null
	                        ? BigDecimal.valueOf(Double.valueOf(hillyHeight.replaceAll("HILLY_ROOM_HT_M=", "")))
	                        : BigDecimal.ZERO;

	                room.setHillyAreaRoomHeight(hillyHeightValue);

	               
	                unit.addRegularRoom(room);
	               
	                // floor.addRegularRoom(room);
	            }
	        }
	    }
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
	    String doorLayerName = String.format(
	        layerNames.getLayerName("LAYER_NAME_UNIT_DOOR"),
	        block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d"
	    );

	    List<String> doorLayers = Util.getLayerNamesLike(pl.getDoc(), doorLayerName);

	    if (!doorLayers.isEmpty()) {
	        for (String doorLayer : doorLayers) {
	            String doorHeight = Util.getMtextByLayerName(pl.getDoc(), doorLayer);

	            List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), doorLayer);
	            if (dimensionList != null && !dimensionList.isEmpty()) {
	                Door door = new Door();

	                BigDecimal doorHeightValue = doorHeight != null
	                        ? BigDecimal.valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
	                        : BigDecimal.ZERO;
	                door.setDoorHeight(doorHeightValue);

	                for (Object dxfEntity : dimensionList) {
	                    DXFDimension dimension = (DXFDimension) dxfEntity;
	                    List<BigDecimal> values = new ArrayList<>();
	                    Util.extractDimensionValue(pl, values, dimension, doorLayer);

	                    if (!values.isEmpty()) {
	                        for (BigDecimal minDis : values) {
	                            door.setDoorWidth(minDis);
	                        }
	                    } else {
	                        door.setDoorWidth(BigDecimal.ZERO);
	                    }
	                }

	                unit.addDoor(door);
	            }
	        }
	    }
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
	    String nonHabitationaldoorLayer = String.format(
	        layerNames.getLayerName("LAYER_NAME_UNIT_NON_HABITATIONAL_DOOR"),
	        block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d"
	    );

	    List<String> nonHabitationaldoorLayers = Util.getLayerNamesLike(pl.getDoc(), nonHabitationaldoorLayer);

	    if (!nonHabitationaldoorLayers.isEmpty()) {
	        for (String doorLayer : nonHabitationaldoorLayers) {
	            String doorHeight = Util.getMtextByLayerName(pl.getDoc(), doorLayer);

	            List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), doorLayer);
	            if (dimensionList != null && !dimensionList.isEmpty()) {
	                Door door = new Door();

	                BigDecimal doorHeightValue = doorHeight != null
	                        ? BigDecimal.valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
	                        : BigDecimal.ZERO;
	                door.setNonHabitationDoorHeight(doorHeightValue);

	                for (Object dxfEntity : dimensionList) {
	                    DXFDimension dimension = (DXFDimension) dxfEntity;
	                    List<BigDecimal> values = new ArrayList<>();
	                    Util.extractDimensionValue(pl, values, dimension, doorLayer);

	                    if (!values.isEmpty()) {
	                        for (BigDecimal minDis : values) {
	                            door.setNonHabitationDoorWidth(minDis);
	                        }
	                    } else {
	                        door.setNonHabitationDoorWidth(BigDecimal.ZERO);
	                    }
	                }

	                unit.addNonaHabitationalDoors(door);
	            }
	        }
	    }
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
	    for (Room room : unit.getRegularRooms()) {
	        String roomDoorLayerName = String.format(
	            layerNames.getLayerName("LAYER_NAME_UNIT_REGULAR_ROOM_DOOR"),
	            block.getNumber(), floor.getNumber(), unit.getUnitNumber(), room.getNumber(), "+\\d"
	        );

	        List<String> roomDoorLayers = Util.getLayerNamesLike(pl.getDoc(), roomDoorLayerName);

	        if (!roomDoorLayers.isEmpty()) {
	            for (String doorLayer : roomDoorLayers) {
	                String doorHeight = Util.getMtextByLayerName(pl.getDoc(), doorLayer);

	                List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(), doorLayer);
	                if (dimensionList != null && !dimensionList.isEmpty()) {
	                    Door door = new Door();

	                    BigDecimal doorHeightValue = doorHeight != null
	                            ? BigDecimal.valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
	                            : BigDecimal.ZERO;
	                    door.setDoorHeight(doorHeightValue);

	                    for (Object dxfEntity : dimensionList) {
	                        DXFDimension dimension = (DXFDimension) dxfEntity;
	                        List<BigDecimal> values = new ArrayList<>();
	                        Util.extractDimensionValue(pl, values, dimension, doorLayer);

	                        if (!values.isEmpty()) {
	                            for (BigDecimal minDis : values) {
	                                door.setDoorWidth(minDis);
	                            }
	                        } else {
	                            door.setDoorWidth(BigDecimal.ZERO);
	                        }
	                    }
	                    room.addDoors(door);
	                }
	            }
	        }
	    }
	}

	 /*
     * Extract AC room
     */
	/*
	 * Map<Integer, List<BigDecimal>> acRoomHeightMap = new HashMap<>();
	 * 
	 * String acRoomLayerName =
	 * String.format(layerNames.getLayerName("LAYER_NAME_AC_ROOM"),
	 * block.getNumber(), floor.getNumber(), "+\\d");
	 * 
	 * List<String> acRoomLayers = Util.getLayerNamesLike(pl.getDoc(),
	 * acRoomLayerName);
	 * 
	 * if (!acRoomLayers.isEmpty()) {
	 * 
	 * for (String acRoomLayer : acRoomLayers) {
	 * 
	 * for (String type : roomOccupancyTypes) { Integer colorCode =
	 * roomOccupancyFeature.get(type); List<BigDecimal> acRoomheights =
	 * Util.getListOfDimensionByColourCode(pl, acRoomLayer, colorCode); if
	 * (!acRoomheights.isEmpty()) acRoomHeightMap.put(colorCode, acRoomheights); }
	 * 
	 * List<DXFLWPolyline> acRoomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
	 * acRoomLayer);
	 * 
	 * if (!acRoomHeightMap.isEmpty() || !acRoomPolyLines.isEmpty()) { boolean
	 * isClosed = acRoomPolyLines.stream() .allMatch(dxflwPolyline ->
	 * dxflwPolyline.isClosed());
	 * 
	 * Room acRoom = new Room(); String[] roomNo = acRoomLayer.split("_"); if
	 * (roomNo != null && roomNo.length == 7) { acRoom.setNumber(roomNo[6]); }
	 * acRoom.setClosed(isClosed);
	 * 
	 * List<RoomHeight> acRoomHeights = new ArrayList<>(); if
	 * (!acRoomPolyLines.isEmpty()) { List<Measurement> acRooms = new
	 * ArrayList<Measurement>(); acRoomPolyLines.stream().forEach(arp -> {
	 * Measurement m = new MeasurementDetail(arp, true); if
	 * (!acRoomHeightMap.isEmpty() && acRoomHeightMap.containsKey(m.getColorCode()))
	 * { for (BigDecimal value : acRoomHeightMap.get(m.getColorCode())) { RoomHeight
	 * roomHeight = new RoomHeight(); roomHeight.setColorCode(m.getColorCode());
	 * roomHeight.setHeight(value); acRoomHeights.add(roomHeight); }
	 * acRoom.setHeights(acRoomHeights); } acRooms.add(m); });
	 * 
	 * // Extract the Mezzanine Area if is declared at ac room level String
	 * acRoomMezzLayerRegExp = String
	 * .format(layerNames.getLayerName("LAYER_NAME_MEZZANINE_AT_ACROOM"),
	 * block.getNumber(), floor.getNumber(), acRoom.getNumber(), "+\\d");
	 * List<String> acRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(),
	 * acRoomMezzLayerRegExp); if (!acRoomMezzLayers.isEmpty()) { for (String
	 * layerName : acRoomMezzLayers) { List<Occupancy> roomMezzanines = new
	 * ArrayList<>(); String[] array = layerName.split("_"); String mezzanineNo =
	 * array[8]; List<DXFLWPolyline> mezzaninePolyLines =
	 * Util.getPolyLinesByLayer(pl.getDoc(), layerName); if
	 * (!mezzaninePolyLines.isEmpty()) for (DXFLWPolyline polyline :
	 * mezzaninePolyLines) { OccupancyDetail occupancy = new OccupancyDetail();
	 * occupancy.setColorCode(polyline.getColor());
	 * occupancy.setMezzanineNumber(mezzanineNo); occupancy.setIsMezzanine(true);
	 * occupancy.setBuiltUpArea(Util.getPolyLineArea(polyline));
	 * occupancy.setTypeHelper(Util.findOccupancyType(polyline, pl));
	 * List<BigDecimal> heights = Util.getListOfDimensionValueByLayer(pl,
	 * layerName); if (!heights.isEmpty())
	 * occupancy.setHeight(Collections.max(heights)); roomMezzanines.add(occupancy);
	 * } acRoom.setMezzanineAreas(roomMezzanines); } }
	 * 
	 * acRoom.setRooms(acRooms); } floor.addAcRoom(acRoom); }
	 * 
	 * }
	 * 
	 * }
	 */
    

    /*
     * Extract regular room
     */
    
    /*   ------------------------------ 
    original code */
	/*
	 * Map<Integer, List<BigDecimal>> roomHeightMap = new HashMap<>();
	 * 
	 * String regularRoomLayerName =
	 * String.format(layerNames.getLayerName("LAYER_NAME_REGULAR_ROOM"),
	 * block.getNumber(), floor.getNumber(), "+\\d");
	 * 
	 * List<String> regularRoomLayers = Util.getLayerNamesLike(pl.getDoc(),
	 * regularRoomLayerName);
	 * 
	 * if (!regularRoomLayers.isEmpty()) {
	 * 
	 * for (String regularRoomLayer : regularRoomLayers) {
	 * 
	 * for (String type : roomOccupancyTypes) { Integer colorCode =
	 * roomOccupancyFeature.get(type); List<BigDecimal> regularRoomheights =
	 * Util.getListOfDimensionByColourCode(pl, regularRoomLayer, colorCode); if
	 * (!regularRoomheights.isEmpty()) roomHeightMap.put(colorCode,
	 * regularRoomheights); }
	 * 
	 * List<BigDecimal> roomWidth = Util.getListOfDimensionByColourCode(pl,
	 * regularRoomLayer, DxfFileConstants.INDEX_COLOR_TWO);
	 * 
	 * 
	 * List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
	 * regularRoomLayer);
	 * 
	 * if (!roomHeightMap.isEmpty() || !roomPolyLines.isEmpty()) {
	 * 
	 * boolean isClosed = roomPolyLines.stream() .allMatch(dxflwPolyline ->
	 * dxflwPolyline.isClosed());
	 * 
	 * Room room = new Room(); String[] roomNo = regularRoomLayer.split("_"); if
	 * (roomNo != null && roomNo.length == 7) { room.setNumber(roomNo[6]); }
	 * room.setClosed(isClosed);
	 * 
	 * if (!roomWidth.isEmpty()) { room.setRoomWidth(roomWidth); } List<RoomHeight>
	 * roomHeights = new ArrayList<>(); if (!roomPolyLines.isEmpty()) {
	 * List<Measurement> rooms = new ArrayList<Measurement>();
	 * roomPolyLines.stream().forEach(rp -> { Measurement m = new
	 * MeasurementDetail(rp, true); if (!roomHeightMap.isEmpty() &&
	 * roomHeightMap.containsKey(m.getColorCode())) { for (BigDecimal value :
	 * roomHeightMap.get(m.getColorCode())) { RoomHeight roomHeight = new
	 * RoomHeight(); roomHeight.setColorCode(m.getColorCode());
	 * roomHeight.setHeight(value); roomHeights.add(roomHeight); }
	 * room.setHeights(roomHeights); } rooms.add(m); }); // Extract the Mezzanine
	 * Area if is declared at ac room level String regularRoomMezzLayerRegExp =
	 * String .format(layerNames.getLayerName("LAYER_NAME_MEZZANINE_AT_ROOM"),
	 * block.getNumber(), floor.getNumber(), room.getNumber(), "+\\d"); List<String>
	 * regularRoomMezzLayers = Util.getLayerNamesLike(pl.getDoc(),
	 * regularRoomMezzLayerRegExp); if (!regularRoomMezzLayers.isEmpty()) { for
	 * (String layerName : regularRoomMezzLayers) { List<Occupancy> roomMezzanines =
	 * new ArrayList<>(); String[] array = layerName.split("_"); String mezzanineNo
	 * = array[8]; List<DXFLWPolyline> mezzaninePolyLines =
	 * Util.getPolyLinesByLayer(pl.getDoc(), layerName); if
	 * (!mezzaninePolyLines.isEmpty()) for (DXFLWPolyline polyline :
	 * mezzaninePolyLines) { OccupancyDetail occupancy = new OccupancyDetail();
	 * occupancy.setColorCode(polyline.getColor());
	 * occupancy.setMezzanineNumber(mezzanineNo); occupancy.setIsMezzanine(true);
	 * occupancy.setBuiltUpArea(Util.getPolyLineArea(polyline));
	 * occupancy.setTypeHelper(Util.findOccupancyType(polyline, pl));
	 * List<BigDecimal> heights = Util.getListOfDimensionValueByLayer(pl,
	 * layerName); if (!heights.isEmpty())
	 * occupancy.setHeight(Collections.max(heights)); roomMezzanines.add(occupancy);
	 * } room.setMezzanineAreas(roomMezzanines); } } room.setRooms(rooms); }
	 * floor.addRegularRoom(room); } } }
	 */
    
    
    /*
     * Extract regular room (unit-wise)
     * 
     * 
     */
    
  
   
                            /*
//     * Extract Non Inhabitable room
//     */
  
							/*
							 * Map<Integer, List<BigDecimal>> nonInhabitableRoomHeightMap = new
							 * HashMap<>();
							 * 
							 * String roomLayerName = String.format(layerNames.getLayerName(
							 * "LAYER_NAME_NON_INHABITABLE_ROOM"), block.getNumber(),
							 * floor.getNumber(), "+\\d");
							 * 
							 * List<String> roomLayers = Util.getLayerNamesLike(pl.getDoc(),
							 * roomLayerName);
							 * 
							 * if (!roomLayers.isEmpty()) {
							 * 
							 * for (String roomLayer : roomLayers) {
							 * 
							 * for (String type : roomOccupancyTypes) { Integer colorCode =
							 * roomOccupancyFeature.get(type); List<BigDecimal> roomheights =
							 * Util.getListOfDimensionByColourCode(pl, roomLayer, colorCode); if
							 * (!roomheights.isEmpty()) nonInhabitableRoomHeightMap.put(colorCode,
							 * roomheights); }
							 * 
							 * List<DXFLWPolyline> roomPolyLines =
							 * Util.getPolyLinesByLayer(pl.getDoc(), roomLayer);
							 * 
							 * if (!nonInhabitableRoomHeightMap.isEmpty() ||
							 * !roomPolyLines.isEmpty()) { boolean isClosed = roomPolyLines.stream()
							 * .allMatch(dxflwPolyline -> dxflwPolyline.isClosed());
							 * 
							 * Room room = new Room(); String[] roomNo = roomLayer.split("_"); if
							 * (roomNo != null && roomNo.length == 7) { room.setNumber(roomNo[6]); }
							 * room.setClosed(isClosed);
							 * 
							 * List<RoomHeight> roomHeights = new ArrayList<>(); if
							 * (!roomPolyLines.isEmpty()) { List<Measurement> rooms = new
							 * ArrayList<Measurement>(); roomPolyLines.stream().forEach(arp -> {
							 * Measurement m = new MeasurementDetail(arp, true); if
							 * (!nonInhabitableRoomHeightMap.isEmpty() &&
							 * nonInhabitableRoomHeightMap.containsKey(m.getColorCode())) { for
							 * (BigDecimal value :
							 * nonInhabitableRoomHeightMap.get(m.getColorCode())) { RoomHeight
							 * roomHeight = new RoomHeight();
							 * roomHeight.setColorCode(m.getColorCode());
							 * roomHeight.setHeight(value); roomHeights.add(roomHeight); }
							 * room.setHeights(roomHeights); } rooms.add(m); });
							 * 
							 * // Extract the Mezzanine Area if is declared at non inhabitable room
							 * level String roomMezzLayerRegExp = String
							 * .format(layerNames.getLayerName(
							 * "LAYER_NAME_MEZZANINE_AT_NON_INHABITABLE_ROOM"), block.getNumber(),
							 * floor.getNumber(), room.getNumber(), "+\\d"); List<String>
							 * roomMezzLayers = Util.getLayerNamesLike(pl.getDoc(),
							 * roomMezzLayerRegExp); if (!roomMezzLayers.isEmpty()) { for (String
							 * layerName : roomMezzLayers) { List<Occupancy> roomMezzanines = new
							 * ArrayList<>(); String[] array = layerName.split("_"); String
							 * mezzanineNo = array[8]; List<DXFLWPolyline> mezzaninePolyLines =
							 * Util.getPolyLinesByLayer(pl.getDoc(), layerName); if
							 * (!mezzaninePolyLines.isEmpty()) for (DXFLWPolyline polyline :
							 * mezzaninePolyLines) { OccupancyDetail occupancy = new
							 * OccupancyDetail(); occupancy.setColorCode(polyline.getColor());
							 * occupancy.setMezzanineNumber(mezzanineNo);
							 * occupancy.setIsMezzanine(true);
							 * occupancy.setBuiltUpArea(Util.getPolyLineArea(polyline));
							 * occupancy.setTypeHelper(Util.findOccupancyType(polyline, pl));
							 * List<BigDecimal> heights = Util.getListOfDimensionValueByLayer(pl,
							 * layerName); if (!heights.isEmpty())
							 * occupancy.setHeight(Collections.max(heights));
							 * roomMezzanines.add(occupancy); }
							 * room.setMezzanineAreas(roomMezzanines); } }
							 * 
							 * room.setRooms(rooms); } floor.addNonInhabitationalRooms(room); }
							 * 
							 * }
							 * 
							 * }
							 */
  

  
	// Code Added by Neha for Doors extract
	/*
	 * String doorLayerName =
	 * String.format(layerNames.getLayerName("LAYER_NAME_DOOR"), block.getNumber(),
	 * floor.getNumber(), "+\\d");
	 * 
	 * List<String> doorLayers = Util.getLayerNamesLike(pl.getDoc(), doorLayerName);
	 * 
	 * if (!doorLayers.isEmpty()) {
	 * 
	 * for (String doorLayer : doorLayers) { String doorHeight =
	 * Util.getMtextByLayerName(pl.getDoc(), doorLayer);
	 * 
	 * // List<DXFLWPolyline> doorPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
	 * // doorLayer);
	 * 
	 * // BigDecimal doorWidth=BigDecimal.ZERO;
	 * 
	 * List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(),
	 * doorLayer); if (dimensionList != null && !dimensionList.isEmpty()) { Door
	 * door = new Door(); BigDecimal doorHeight1 = doorHeight != null ? BigDecimal
	 * .valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", ""))) :
	 * BigDecimal.ZERO; door.setDoorHeight(doorHeight1); for (Object dxfEntity :
	 * dimensionList) { DXFDimension dimension = (DXFDimension) dxfEntity;
	 * List<BigDecimal> values = new ArrayList<>(); Util.extractDimensionValue(pl,
	 * values, dimension, doorLayer);
	 * 
	 * if (!values.isEmpty()) { for (BigDecimal minDis : values) { //
	 * doorWidth=minDis; door.setDoorWidth(minDis); } } else {
	 * door.setDoorWidth(BigDecimal.ZERO); } } floor.addDoor(door); } // else { //
	 * door.setDoorWidth(BigDecimal.ZERO); // }
	 * 
	 * } }
	 */
    
	// Code Added by Neha for non-habitational Doors extract

	/*
	 * String nonHabitationaldoorLayer =
	 * String.format(layerNames.getLayerName("LAYER_NAME_NON_HABITATIONAL_DOOR"),
	 * block.getNumber(), floor.getNumber(), "+\\d");
	 * 
	 * List<String> nonHabitationaldoorLayers = Util.getLayerNamesLike(pl.getDoc(),
	 * nonHabitationaldoorLayer);
	 * 
	 * if (!nonHabitationaldoorLayers.isEmpty()) {
	 * 
	 * for (String doorLayer : nonHabitationaldoorLayers) { String doorHeight =
	 * Util.getMtextByLayerName(pl.getDoc(), doorLayer);
	 * 
	 * // List<DXFLWPolyline> doorPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
	 * // doorLayer);
	 * 
	 * // BigDecimal doorWidth=BigDecimal.ZERO;
	 * 
	 * List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(),
	 * doorLayer); if (dimensionList != null && !dimensionList.isEmpty()) { Door
	 * door = new Door(); BigDecimal doorHeight1 = doorHeight != null ? BigDecimal
	 * .valueOf(Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", ""))) :
	 * BigDecimal.ZERO; door.setNonHabitationDoorHeight(doorHeight1); for (Object
	 * dxfEntity : dimensionList) { DXFDimension dimension = (DXFDimension)
	 * dxfEntity; List<BigDecimal> values = new ArrayList<>();
	 * Util.extractDimensionValue(pl, values, dimension, doorLayer);
	 * 
	 * if (!values.isEmpty()) { for (BigDecimal minDis : values) { //
	 * doorWidth=minDis; door.setNonHabitationDoorWidth(minDis); } } else {
	 * door.setNonHabitationDoorWidth(BigDecimal.ZERO); } }
	 * floor.addNonaHabitationalDoors(door); } // else { //
	 * door.setDoorWidth(BigDecimal.ZERO); // }
	 * 
	 * } }
	 */


	// Code Added by Neha for roomwise doors extract
	/*
	 * for (Room room : floor.getRegularRooms()) {
	 * 
	 * String roomDoorLayerName =
	 * String.format(layerNames.getLayerName("LAYER_NAME_REGULAR_ROOM_DOOR"),
	 * block.getNumber(), floor.getNumber(), room.getNumber(), "+\\d");
	 * 
	 * List<String> roomDoorLayers = Util.getLayerNamesLike(pl.getDoc(),
	 * roomDoorLayerName);
	 * 
	 * if (!roomDoorLayers.isEmpty()) {
	 * 
	 * for (String doorLayer : roomDoorLayers) { String doorHeight =
	 * Util.getMtextByLayerName(pl.getDoc(), doorLayer);
	 * 
	 * // List<DXFLWPolyline> doorPolyLines = Util.getPolyLinesByLayer(pl.getDoc(),
	 * // doorLayer);
	 * 
	 * // BigDecimal doorWidth=BigDecimal.ZERO;
	 * 
	 * List<DXFDimension> dimensionList = Util.getDimensionsByLayer(pl.getDoc(),
	 * doorLayer); if (dimensionList != null && !dimensionList.isEmpty()) { Door
	 * door = new Door(); BigDecimal doorHeight1 = doorHeight != null ?
	 * BigDecimal.valueOf( Double.valueOf(doorHeight.replaceAll("DOOR_HT_M=", "")))
	 * : BigDecimal.ZERO; door.setDoorHeight(doorHeight1); for (Object dxfEntity :
	 * dimensionList) { DXFDimension dimension = (DXFDimension) dxfEntity;
	 * List<BigDecimal> values = new ArrayList<>(); Util.extractDimensionValue(pl,
	 * values, dimension, doorLayer);
	 * 
	 * if (!values.isEmpty()) { for (BigDecimal minDis : values) { //
	 * doorWidth=minDis; door.setDoorWidth(minDis); } } else {
	 * door.setDoorWidth(BigDecimal.ZERO); } } room.addDoors(door); } // else { //
	 * window.setWindowWidth(BigDecimal.ZERO); // }
	 * 
	 * } } }
	 */
    
    /*
     * Extract Unitwise AC room
     */
    


    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

}