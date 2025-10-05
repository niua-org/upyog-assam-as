package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BathRoomExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(BathRoomExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail planDetail) {
        List<DXFLWPolyline> rooms;
        List<Measurement> roomMeasurements;
        List<BigDecimal> roomHeights;
        List<RoomHeight> roomHeightsList;
        List<DXFLWPolyline> ventilationBS;
        List<Measurement> ventilationMeasurements;
        RoomHeight height;
        for (Block block : planDetail.getBlocks())
            if (block.getBuilding() != null && block.getBuilding().getFloors() != null)
                for (Floor f : block.getBuilding().getFloors()) {

//                    String layerName = String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_BATH"), block.getNumber(),
//                            f.getNumber());
//                    String ventilationLayerName = layerNames.getLayerName("LAYER_NAME_BLOCK_NAME_PREFIX") + block.getNumber() + "_"
//                            + layerNames.getLayerName("LAYER_NAME_FLOOR_NAME_PREFIX") + f.getNumber() + "_"
//                            + layerNames.getLayerName("LAYER_NAME_BATH_STORE_VENTILATION");
//                    ventilationBS = Util.getPolyLinesByLayer(planDetail.getDoc(), ventilationLayerName);
//                    if (ventilationBS != null) {
//                        ventilationMeasurements = ventilationBS.stream()
//                                .map(flightPolyLine -> new MeasurementDetail(flightPolyLine, true)).collect(Collectors.toList());
//                    } else {
//                        ventilationMeasurements = new ArrayList<>();
//                    }
//                    rooms = Util.getPolyLinesByLayer(planDetail.getDoc(), layerName);
//
//                    // Setting total no of bathrooms in plan detail
//                    int noOfRooms = rooms.size();
//                    planDetail.setTotalBathrooms(BigDecimal.valueOf(noOfRooms));
//
//                    roomMeasurements = rooms.stream()
//                            .map(flightPolyLine -> new MeasurementDetail(flightPolyLine, true)).collect(Collectors.toList());
//                    f.setBathRoom(new Room());
//                    f.getBathRoom().setBathVentilation(ventilationMeasurements);
//                    f.getBathRoom().setRooms(roomMeasurements);
//                    roomHeights = Util.getListOfDimensionValueByLayer(planDetail,
//                            String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_BATH_HT"), block.getNumber(),
//                                    f.getNumber()));
//                    roomHeightsList = new ArrayList<>();
//                    for (BigDecimal h : roomHeights) {
//                        height = new RoomHeight();
//                        height.setHeight(h);
//                        roomHeightsList.add(height);
//                    }
//                    f.getBathRoom().setHeights(roomHeightsList);


                    if (f.getUnits() != null || !f.getUnits().isEmpty())
                        for (FloorUnit floorUnit : f.getUnits()) {
                            String layerName = String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_UNIT_BATH"), block.getNumber(),
                                    f.getNumber(), floorUnit.getUnitNumber());

                            String ventilationLayerName = layerNames.getLayerName("LAYER_NAME_BLOCK_NAME_PREFIX") + block.getNumber() + "_"
                                    + layerNames.getLayerName("LAYER_NAME_FLOOR_NAME_PREFIX") + f.getNumber() + "_"
                                    + layerNames.getLayerName("LAYER_NAME_UNIT_NAME_PREFIX") + floorUnit.getUnitNumber() + "_"
                                    + layerNames.getLayerName("LAYER_NAME_BATH_STORE_VENTILATION");
                            ventilationBS = Util.getPolyLinesByLayer(planDetail.getDoc(), ventilationLayerName);
                            if (ventilationBS != null) {
                                ventilationMeasurements = ventilationBS.stream()
                                        .map(flightPolyLine -> new MeasurementDetail(flightPolyLine, true)).collect(Collectors.toList());
                            } else {
                                ventilationMeasurements = new ArrayList<>();
                            }
                            rooms = Util.getPolyLinesByLayer(planDetail.getDoc(), layerName);

                            // Setting total no of bathrooms in plan detail
                            int noOfRooms = rooms.size();
                            planDetail.setTotalBathrooms(BigDecimal.valueOf(noOfRooms));

                            roomMeasurements = rooms.stream()
                                    .map(flightPolyLine -> new MeasurementDetail(flightPolyLine, true)).collect(Collectors.toList());
                            floorUnit.setBathRoom(new Room());
                            floorUnit.getBathRoom().setBathVentilation(ventilationMeasurements);
                            floorUnit.getBathRoom().setRooms(roomMeasurements);
                            roomHeights = Util.getListOfDimensionValueByLayer(planDetail,
                                    String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_UNIT_BATH_HT"), block.getNumber(),
                                            f.getNumber(), floorUnit.getUnitNumber()));
                            roomHeightsList = new ArrayList<>();
                            for (BigDecimal h : roomHeights) {
                                height = new RoomHeight();
                                height.setHeight(h);
                                roomHeightsList.add(height);
                            }
                            floorUnit.getBathRoom().setHeights(roomHeightsList);
                        }
                }

        return planDetail;
    }

}
