package org.egov.edcr.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.OccupancyDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.egov.edcr.service.LayerNames;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class ServiceRoomExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(ServiceRoomExtract.class);

    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail pl) {
        Map<String, Integer> roomOccupancyFeature = pl.getSubFeatureColorCodesMaster().get("serviceRoom");
        Set<String> roomOccupancyTypes = new HashSet<>();
        roomOccupancyTypes.addAll(roomOccupancyFeature.keySet());
        for (Block block : pl.getBlocks()) {
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                for (Floor floor : block.getBuilding().getFloors()) {

//                    List<ServiceRoom> serviceRooms = new ArrayList<>();
//
//                    String serviceroomlayerPattern = "BLK_" + block.getNumber() + "_SERVICEROOM";
////          List<String> serviceroomLayers = Util.getLayerNamesLike(planDetail.getDoc(), serviceroomlayerPattern);
//                    String serviceLayer = String.format(layerNames.getLayerName(serviceroomlayerPattern), block.getNumber());
//
//                    List<DXFLWPolyline> serviceRoomPolyLines = Util.getPolyLinesByLayer(planDetail.getDoc(), serviceLayer);
//                    List<BigDecimal> dimensions = Util.getListOfDimensionValueByLayer(planDetail, serviceLayer);
//                    if (!dimensions.isEmpty() || !serviceRoomPolyLines.isEmpty()) {
//                        ServiceRoom serviceRoom = new ServiceRoom();
//
//                        serviceRoom.setNumber("1");
//                        serviceRoom.setName("acroom");
//                        serviceRoom.setHeight(BigDecimal.valueOf(3));
//                    }
//
////                block.getBuilding().setServiceRoom(serviceRooms);
//                }
//
//            return planDetail;

                    if(floor.getUnits() != null && !floor.getUnits().isEmpty())
                        for (FloorUnit floorUnit : floor.getUnits()) {
                            Map<Integer, List<BigDecimal>> serviceRoomHeightMap = new HashMap<>();
                            String serviceRoomLayerName = String.format(layerNames.getLayerName("LAYER_NAME_UNIT_SERVICEROOM"), block.getNumber(), floor.getNumber(), floorUnit.getUnitNumber(), "+\\d");
                            List<String> serviceRoomLayers = Util.getLayerNamesLike(pl.getDoc(), serviceRoomLayerName);

                            if (!serviceRoomLayers.isEmpty()) {
                                for (String serviceRoomLayer : serviceRoomLayers) {
                                    for (String type : roomOccupancyTypes) {
                                        Integer colorCode = roomOccupancyFeature.get(type);
                                        List<BigDecimal> serviceRoomheights = Util.getListOfDimensionByColourCode(pl, serviceRoomLayer, colorCode);
                                        if (!serviceRoomheights.isEmpty())
                                            serviceRoomHeightMap.put(colorCode, serviceRoomheights);
                                    }

                                    List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), serviceRoomLayer);

                                    if (!serviceRoomHeightMap.isEmpty() || !roomPolyLines.isEmpty()) {

                                        boolean isClosed = roomPolyLines.stream().allMatch(dxflwPolyline -> dxflwPolyline.isClosed());

                                        ServiceRoom room = new ServiceRoom();
                                        String[] roomNo = serviceRoomLayer.split("_");
                                        if (roomNo != null && roomNo.length == 7) {
                                            room.setNumber(roomNo[6]);
                                        }
                                        room.setClosed(isClosed);

                                        List<RoomHeight> roomHeights = new ArrayList<>();
                                        if (!roomPolyLines.isEmpty()) {
                                            List<Measurement> rooms = new ArrayList<Measurement>();
                                            roomPolyLines.stream().forEach(rp -> {
                                                Measurement m = new MeasurementDetail(rp, true);
                                                if (!serviceRoomHeightMap.isEmpty() && serviceRoomHeightMap.containsKey(m.getColorCode())) {
                                                    for (BigDecimal value : serviceRoomHeightMap.get(m.getColorCode())) {
                                                        RoomHeight roomHeight = new RoomHeight();
                                                        roomHeight.setColorCode(m.getColorCode());
                                                        roomHeight.setHeight(value);
                                                        roomHeights.add(roomHeight);
                                                    }
                                                    room.setHeights(roomHeights);
                                                }
                                                rooms.add(m);
                                            });
                                            room.setRooms(rooms);
                                        }
                                        floorUnit.addServiceRoom(room);
                                    }
                                }
                            }
                        }
                }
        }
        return pl;
    }
}
