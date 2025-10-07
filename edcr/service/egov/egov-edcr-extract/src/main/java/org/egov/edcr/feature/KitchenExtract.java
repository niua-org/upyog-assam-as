package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KitchenExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(KitchenExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail extract(PlanDetail pl) {
//        if (LOG.isDebugEnabled())
            LOG.info("Starting of Kitchen room Extract......");
        if (pl != null && !pl.getBlocks().isEmpty())
            for (Block block : pl.getBlocks())
                if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                    outside:for (Floor floor : block.getBuilding().getFloors()) {
/*
                        if (!block.getTypicalFloor().isEmpty())
                            for (TypicalFloor tp : block.getTypicalFloor())
                                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                    for (Floor allFloors : block.getBuilding().getFloors())
                                        if (allFloors.getNumber().equals(tp.getModelFloorNo()))
                                            if (allFloors.getKitchen() != null) {
                                                floor.setKitchen(allFloors.getKitchen());
                                                continue outside;
                                            }

                        List<DXFLWPolyline> kitchenPolyLines = new ArrayList<>();

                        String kitchenLayer = String.format(layerNames.getLayerName("LAYER_NAME_KITCHEN"), block.getNumber(),
                                floor.getNumber());
                       // List<BigDecimal> kitchenHeight = Util.getListOfDimensionValueByLayer(pl, kitchenLayer);

                        List<BigDecimal> kitchenHeight = Util.getListOfDimensionByColourCode(pl, kitchenLayer, DxfFileConstants.KITCHEN_HEIGHT_COLOR);

                        List<BigDecimal> kitchenWidth = Util.getListOfDimensionByColourCode(pl, kitchenLayer,
        						DxfFileConstants.KITCHEN_WIDTH_COLOR);

                        List<DXFLWPolyline> residentialKitchenPolyLines = Util.getPolyLinesByLayerAndColor(pl.getDoc(),
                                kitchenLayer, DxfFileConstants.RESIDENTIAL_KITCHEN_ROOM_COLOR, pl);
                        List<DXFLWPolyline> residentialKitchenStorePolyLines = Util.getPolyLinesByLayerAndColor(
                                pl.getDoc(), kitchenLayer, DxfFileConstants.RESIDENTIAL_KITCHEN_STORE_ROOM_COLOR, pl);
                        List<DXFLWPolyline> residentialKitchenDiningPolyLines = Util.getPolyLinesByLayerAndColor(
                                pl.getDoc(), kitchenLayer, DxfFileConstants.RESIDENTIAL_KITCHEN_DINING_ROOM_COLOR, pl);
                        List<DXFLWPolyline> commercialKitchenPolyLines = Util.getPolyLinesByLayerAndColor(pl.getDoc(),
                                kitchenLayer, DxfFileConstants.COMMERCIAL_KITCHEN_ROOM_COLOR, pl);
                        List<DXFLWPolyline> commercialKitchenStorePolyLines = Util.getPolyLinesByLayerAndColor(
                                pl.getDoc(), kitchenLayer, DxfFileConstants.COMMERCIAL_KITCHEN_STORE_ROOM_COLOR, pl);
                        List<DXFLWPolyline> commercialKitchenDiningPolyLines = Util.getPolyLinesByLayerAndColor(
                                pl.getDoc(), kitchenLayer, DxfFileConstants.COMMERCIAL_KITCHEN_DINING_ROOM_COLOR, pl);

                        if (!residentialKitchenPolyLines.isEmpty())
                            kitchenPolyLines.addAll(residentialKitchenPolyLines);
                        if (!residentialKitchenStorePolyLines.isEmpty())
                            kitchenPolyLines.addAll(residentialKitchenStorePolyLines);
                        if (!residentialKitchenDiningPolyLines.isEmpty())
                            kitchenPolyLines.addAll(residentialKitchenDiningPolyLines);
                        if (!commercialKitchenPolyLines.isEmpty())
                            kitchenPolyLines.addAll(commercialKitchenPolyLines);
                        if (!commercialKitchenStorePolyLines.isEmpty())
                            kitchenPolyLines.addAll(commercialKitchenStorePolyLines);
                        if (!commercialKitchenDiningPolyLines.isEmpty())
                            kitchenPolyLines.addAll(commercialKitchenDiningPolyLines);

                        if (!kitchenHeight.isEmpty() || !kitchenPolyLines.isEmpty()) {
                            Room kitchen = new Room();
                            List<RoomHeight> kitchenHeights = new ArrayList<>();
                            if (!kitchenHeight.isEmpty()) {
                                for (BigDecimal height : kitchenHeight) {
                                    RoomHeight roomHeight = new RoomHeight();
                                    roomHeight.setHeight(height);
                                    kitchenHeights.add(roomHeight);
                                }
                                kitchen.setHeights(kitchenHeights);
                            }
                            if (!kitchenWidth.isEmpty()) {
                                kitchen.setKitchenWidth(kitchenWidth);
                            }

                            if (kitchenPolyLines != null && !kitchenPolyLines.isEmpty()) {
                                List<Measurement> kitchens = kitchenPolyLines.stream()
                                        .map(acRoomPolyLine -> new MeasurementDetail(acRoomPolyLine, true))
                                        .collect(Collectors.toList());
                                kitchen.setRooms(kitchens);
                            }
                            floor.setKitchen(kitchen);
                        }
*/
                        if (!block.getTypicalFloor().isEmpty())
                            for (TypicalFloor tp : block.getTypicalFloor())
                                if (tp.getRepetitiveFloorNos().contains(floor.getNumber()))
                                    for (Floor allFloors : block.getBuilding().getFloors())
                                        if (allFloors.getNumber().equals(tp.getModelFloorNo())) {
                                            if (allFloors.getKitchen() != null) {
                                                floor.setKitchen(allFloors.getKitchen());
                                            }
                                            if (allFloors.getUnits() != null) {
                                                floor.setUnits(allFloors.getUnits());
                                            }
                                            continue outside;
                                        }

                        for (FloorUnit floorUnit : floor.getUnits()) {
                            LOG.info("Extracting Kitchen data for Block: " + block.getNumber() + " Floor: " + floor.getNumber() + " Unit: " + floorUnit.getUnitNumber());
                            extractKitchensData(pl, block, floor, floorUnit);
                        }
                    }

//        if (LOG.isDebugEnabled())
            LOG.info("End of Kitchen Room Extract......");
        return pl;
    }

    /**
     * Extracts kitchen data from DXF layers for a specific floor unit including dimensions,
     * polylines for different kitchen types (residential/commercial kitchen, store, dining areas)
     * and populates the floor unit's kitchen room information.
     *
     * @param pl PlanDetail containing the DXF document and plan data
     * @param block Building block containing the floor unit
     * @param floor Floor containing the unit
     * @param floorUnit Specific unit to extract kitchen data for
     */
    public void extractKitchensData(PlanDetail pl, Block block, Floor floor, FloorUnit floorUnit) {
        List<DXFLWPolyline> kitchenPolyLines = new ArrayList<>();

        String kitchenLayer = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_KITCHEN"), block.getNumber(),
                floor.getNumber(), floorUnit.getUnitNumber());
        // List<BigDecimal> kitchenHeight = Util.getListOfDimensionValueByLayer(plLAYER_NAME_UNIT_KITCHEN, kitchenLayer);

        List<BigDecimal> kitchenHeight = Util.getListOfDimensionByColourCode(pl, kitchenLayer, DxfFileConstants.KITCHEN_HEIGHT_COLOR);
        List<BigDecimal> kitchenWidth = Util.getListOfDimensionByColourCode(pl, kitchenLayer, DxfFileConstants.KITCHEN_WIDTH_COLOR);
        LOG.info("Kitchen Layer: " + kitchenLayer + " Kitchen Heights: " + kitchenHeight + " Kitchen Widths: " + kitchenWidth);

        List<DXFLWPolyline> residentialKitchenPolyLines = Util.getPolyLinesByLayerAndColor(pl.getDoc(),
                kitchenLayer, DxfFileConstants.RESIDENTIAL_KITCHEN_ROOM_COLOR, pl);
        List<DXFLWPolyline> residentialKitchenStorePolyLines = Util.getPolyLinesByLayerAndColor(
                pl.getDoc(), kitchenLayer, DxfFileConstants.RESIDENTIAL_KITCHEN_STORE_ROOM_COLOR, pl);
        List<DXFLWPolyline> residentialKitchenDiningPolyLines = Util.getPolyLinesByLayerAndColor(
                pl.getDoc(), kitchenLayer, DxfFileConstants.RESIDENTIAL_KITCHEN_DINING_ROOM_COLOR, pl);
        List<DXFLWPolyline> commercialKitchenPolyLines = Util.getPolyLinesByLayerAndColor(pl.getDoc(),
                kitchenLayer, DxfFileConstants.COMMERCIAL_KITCHEN_ROOM_COLOR, pl);
        List<DXFLWPolyline> commercialKitchenStorePolyLines = Util.getPolyLinesByLayerAndColor(
                pl.getDoc(), kitchenLayer, DxfFileConstants.COMMERCIAL_KITCHEN_STORE_ROOM_COLOR, pl);
        List<DXFLWPolyline> commercialKitchenDiningPolyLines = Util.getPolyLinesByLayerAndColor(
                pl.getDoc(), kitchenLayer, DxfFileConstants.COMMERCIAL_KITCHEN_DINING_ROOM_COLOR, pl);

        LOG.info("Found - Residential Kitchens: " + residentialKitchenPolyLines.size() +
                ", Residential Stores: " + residentialKitchenStorePolyLines.size() +
                ", Residential Dining: " + residentialKitchenDiningPolyLines.size() +
                ", Commercial Kitchens: " + commercialKitchenPolyLines.size() +
                ", Commercial Stores: " + commercialKitchenStorePolyLines.size() +
                ", Commercial Dining: " + commercialKitchenDiningPolyLines.size());

        if (!residentialKitchenPolyLines.isEmpty())
            kitchenPolyLines.addAll(residentialKitchenPolyLines);
        if (!residentialKitchenStorePolyLines.isEmpty())
            kitchenPolyLines.addAll(residentialKitchenStorePolyLines);
        if (!residentialKitchenDiningPolyLines.isEmpty())
            kitchenPolyLines.addAll(residentialKitchenDiningPolyLines);
        if (!commercialKitchenPolyLines.isEmpty())
            kitchenPolyLines.addAll(commercialKitchenPolyLines);
        if (!commercialKitchenStorePolyLines.isEmpty())
            kitchenPolyLines.addAll(commercialKitchenStorePolyLines);
        if (!commercialKitchenDiningPolyLines.isEmpty())
            kitchenPolyLines.addAll(commercialKitchenDiningPolyLines);

        if (!kitchenHeight.isEmpty() || !kitchenPolyLines.isEmpty()) {
            Room kitchen = new Room();
            List<RoomHeight> kitchenHeights = new ArrayList<>();
            if (!kitchenHeight.isEmpty()) {
                for (BigDecimal height : kitchenHeight) {
                    RoomHeight roomHeight = new RoomHeight();
                    roomHeight.setHeight(height);
                    kitchenHeights.add(roomHeight);
                }
                kitchen.setHeights(kitchenHeights);
            }
            if (!kitchenWidth.isEmpty()) {
                kitchen.setKitchenWidth(kitchenWidth);
            }

            if (kitchenPolyLines != null && !kitchenPolyLines.isEmpty()) {
                List<Measurement> kitchens = kitchenPolyLines.stream()
                        .map(acRoomPolyLine -> new MeasurementDetail(acRoomPolyLine, true))
                        .collect(Collectors.toList());
                kitchen.setRooms(kitchens);
            }
            floorUnit.setKitchen(kitchen);
        }

        int noOfResidentialKitchens = residentialKitchenPolyLines.size();
        LOG.info("No of Residential Kitchens found: " + noOfResidentialKitchens);
        pl.setTotalKitchens(BigDecimal.valueOf(noOfResidentialKitchens));
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        return pl;
    }

}