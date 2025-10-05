package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
public class VentilationExtract extends FeatureExtract {

	private static final Logger LOG = LogManager.getLogger(VentilationExtract.class);
	@Autowired
	private LayerNames layerNames;

	@Override
	public PlanDetail extract(PlanDetail pl) {
		for (Block b : pl.getBlocks()) {
			if (b.getBuilding() != null && b.getBuilding().getFloors() != null
					&& !b.getBuilding().getFloors().isEmpty()) {
				for (Floor f : b.getBuilding().getFloors()) {

//					/*
//					 * Adding general light and ventilation at floor level
//					 */
//					List<DXFLWPolyline> lightAndVentilations = Util.getPolyLinesByLayer(pl.getDoc(), String.format(
//							layerNames.getLayerName("LAYER_NAME_LIGHT_VENTILATION"), b.getNumber(), f.getNumber()));
//					if (!lightAndVentilations.isEmpty()) {
//						List<Measurement> lightAndventilationMeasurements = lightAndVentilations.stream()
//								.map(polyline -> new MeasurementDetail(polyline, true)).collect(Collectors.toList());
//						f.getLightAndVentilation().setMeasurements(lightAndventilationMeasurements);
//
//						f.getLightAndVentilation()
//								.setHeightOrDepth((Util.getListOfDimensionValueByLayer(pl,
//										String.format(layerNames.getLayerName("LAYER_NAME_LIGHT_VENTILATION"),
//												b.getNumber(), f.getNumber()))));
//
//					}
//					/*
//					 * Adding regular room wise light and ventilation
//					 */
//					for (Room room : f.getRegularRooms()) {
//						String regularRoomLayerName = String.format(
//								layerNames.getLayerName("LAYER_NAME_ROOM_LIGHT_VENTILATION"), b.getNumber(),
//								f.getNumber(), room.getNumber(), "+\\d");
//
//						List<String> regularRoomLayers = Util.getLayerNamesLike(pl.getDoc(), regularRoomLayerName);
//						if (!regularRoomLayers.isEmpty()) {
//							for (String regularRoomLayer : regularRoomLayers) {
//								List<DXFLWPolyline> lightAndventilations = Util.getPolyLinesByLayer(pl.getDoc(),
//										regularRoomLayer);
//								if (!lightAndventilations.isEmpty()) {
//									List<Measurement> lightAndventilationMeasurements = lightAndventilations.stream()
//											.map(polyline -> new MeasurementDetail(polyline, true))
//											.collect(Collectors.toList());
//									room.getLightAndVentilation().setMeasurements(lightAndventilationMeasurements);
//
//									room.getLightAndVentilation().setHeightOrDepth(
//											(Util.getListOfDimensionValueByLayer(pl, regularRoomLayer)));
//								}
//							}
//						}
//					}
//					/*
//					 * Adding AC room wise light and ventilation
//					 */
//					for (Room room : f.getAcRooms()) {
//						String acRoomLayerName = String.format(
//								layerNames.getLayerName("LAYER_NAME_ACROOM_LIGHT_VENTILATION"), b.getNumber(),
//								f.getNumber(), room.getNumber(), "+\\d");
//
//						List<String> acRoomLayers = Util.getLayerNamesLike(pl.getDoc(), acRoomLayerName);
//						if (!acRoomLayers.isEmpty()) {
//							for (String acRoomLayer : acRoomLayers) {
//
//								List<DXFLWPolyline> lightAndventilations = Util.getPolyLinesByLayer(pl.getDoc(),
//										acRoomLayer);
//								if (!lightAndventilations.isEmpty()) {
//									List<Measurement> lightAndventilationMeasurements = lightAndventilations.stream()
//											.map(polyline -> new MeasurementDetail(polyline, true))
//											.collect(Collectors.toList());
//									room.getLightAndVentilation().setMeasurements(lightAndventilationMeasurements);
//
//									room.getLightAndVentilation()
//											.setHeightOrDepth((Util.getListOfDimensionValueByLayer(pl, acRoomLayer)));
//
//								}
//
//							}
//						}
//					}
//
//					// Kitchen and dining ventilation handling via new method
//                    handleKitchenDiningVentilation(pl, b, f);
//
//                    // Laundry and recreation ventilation handling via new method
//                    handleLaundryRecreationVentilation(pl, b, f);


                    if(f.getUnits() != null && !f.getUnits().isEmpty())
                        for(FloorUnit floorUnit : f.getUnits()) {

                            /*
                             * Adding general light and ventilation at floor level
                             */
                            List<DXFLWPolyline> lightAndVentilations = Util.getPolyLinesByLayer(pl.getDoc(), String.format(
                                    layerNames.getLayerName("LAYER_NAME_UNIT_LIGHT_VENTILATION"), b.getNumber(), f.getNumber(), floorUnit.getUnitNumber()));
                            if (!lightAndVentilations.isEmpty()) {
                                List<Measurement> lightAndventilationMeasurements = lightAndVentilations.stream()
                                        .map(polyline -> new MeasurementDetail(polyline, true)).collect(Collectors.toList());
                                floorUnit.getLightAndVentilation().setMeasurements(lightAndventilationMeasurements);

                                floorUnit.getLightAndVentilation()
                                        .setHeightOrDepth((Util.getListOfDimensionValueByLayer(pl,
                                                String.format(layerNames.getLayerName("LAYER_NAME_UNIT_LIGHT_VENTILATION"),
                                                        b.getNumber(), f.getNumber(), floorUnit.getUnitNumber()))));

                            }
                            /*
                             * Adding regular room wise light and ventilation
                             */
                            for (Room room : floorUnit.getRegularRooms()) {
                                String regularRoomLayerName = String.format(
                                        layerNames.getLayerName("LAYER_NAME_UNIT_ROOM_LIGHT_VENTILATION"), b.getNumber(),
                                        f.getNumber(), floorUnit.getUnitNumber(), room.getNumber(), "+\\d");

                                List<String> regularRoomLayers = Util.getLayerNamesLike(pl.getDoc(), regularRoomLayerName);
                                if (!regularRoomLayers.isEmpty()) {
                                    for (String regularRoomLayer : regularRoomLayers) {
                                        List<DXFLWPolyline> lightAndventilations = Util.getPolyLinesByLayer(pl.getDoc(),
                                                regularRoomLayer);
                                        if (!lightAndventilations.isEmpty()) {
                                            List<Measurement> lightAndventilationMeasurements = lightAndventilations.stream()
                                                    .map(polyline -> new MeasurementDetail(polyline, true))
                                                    .collect(Collectors.toList());
                                            room.getLightAndVentilation().setMeasurements(lightAndventilationMeasurements);

                                            room.getLightAndVentilation().setHeightOrDepth(
                                                    (Util.getListOfDimensionValueByLayer(pl, regularRoomLayer)));
                                        }
                                    }
                                }
                            }
                            /*
                             * Adding AC room wise light and ventilation
                             */
                            for (Room room : floorUnit.getAcRooms()) {
                                String acRoomLayerName = String.format(
                                        layerNames.getLayerName("LAYER_NAME_UNIT_ACROOM_LIGHT_VENTILATION"), b.getNumber(),
                                        f.getNumber(), floorUnit.getUnitNumber(), room.getNumber(), "+\\d");

                                List<String> acRoomLayers = Util.getLayerNamesLike(pl.getDoc(), acRoomLayerName);
                                if (!acRoomLayers.isEmpty()) {
                                    for (String acRoomLayer : acRoomLayers) {

                                        List<DXFLWPolyline> lightAndventilations = Util.getPolyLinesByLayer(pl.getDoc(),
                                                acRoomLayer);
                                        if (!lightAndventilations.isEmpty()) {
                                            List<Measurement> lightAndventilationMeasurements = lightAndventilations.stream()
                                                    .map(polyline -> new MeasurementDetail(polyline, true))
                                                    .collect(Collectors.toList());
                                            room.getLightAndVentilation().setMeasurements(lightAndventilationMeasurements);

                                            room.getLightAndVentilation()
                                                    .setHeightOrDepth((Util.getListOfDimensionValueByLayer(pl, acRoomLayer)));

                                        }

                                    }
                                }
                            }

                            // Kitchen and dining ventilation handling via new method
                            handleKitchenDiningVentilation(pl, b, f, floorUnit);

                            // Laundry and recreation ventilation handling via new method
                            handleLaundryRecreationVentilation(pl, b, f, floorUnit);
                        }
                }
			}
		}

		return pl;
	}
	
	private void handleKitchenDiningVentilation(PlanDetail pl, Block b, Floor f, FloorUnit floorUnit) {
        Room kitchen = floorUnit.getKitchen();
        if (kitchen != null) {
            String kitchenAndDining = String.format(
                    layerNames.getLayerName("LAYER_NAME_UNIT_KITCHEN_DINING_VENTILATION"),
                    b.getNumber(), f.getNumber(), floorUnit.getUnitNumber(), "+\\d");

            List<String> ventilationLayers = Util.getLayerNamesLike(pl.getDoc(), kitchenAndDining);
            if (!ventilationLayers.isEmpty()) {
            	List<BigDecimal> allWindowWidths = new ArrayList<>();
            	List<BigDecimal> allDoorWidths = new ArrayList<>();
            	List<BigDecimal> allDoorHeights = new ArrayList<>();
                for (String ventLayer : ventilationLayers) {
                    List<DXFLWPolyline> lightAndVentilations = Util.getPolyLinesByLayer(pl.getDoc(), ventLayer);
                    if (!lightAndVentilations.isEmpty()) {
                        List<Measurement> lightAndVentilationMeasurements = lightAndVentilations.stream()
                            .map(polyline -> new MeasurementDetail(polyline, true))
                            .collect(Collectors.toList());
                        kitchen.getLightAndVentilation().setMeasurements(lightAndVentilationMeasurements);

                        kitchen.getLightAndVentilation().setHeightOrDepth(
                            Util.getListOfDimensionValueByLayer(pl, ventLayer));
                    }
                    List<BigDecimal> kitchenWindowWidth = 
                            Util.getListOfDimensionByColourCode(pl, ventLayer, DxfFileConstants.INDEX_COLOR_TWO);

                        if (!kitchenWindowWidth.isEmpty()) {
                            allWindowWidths.addAll(kitchenWindowWidth);
                        }
                        
                        List<BigDecimal> kitchenDoorWidth = 
                                Util.getListOfDimensionByColourCode(pl, ventLayer, DxfFileConstants.INDEX_COLOR_THREE);

                            if (!kitchenDoorWidth.isEmpty()) {
                            	allDoorWidths.addAll(kitchenDoorWidth);
                            }
                            
                            
                            List<BigDecimal> kitchenDoorHeight = 
                                    Util.getListOfDimensionByColourCode(pl, ventLayer, DxfFileConstants.INDEX_COLOR_FOUR);

                                if (!kitchenDoorHeight.isEmpty()) {
                                	allDoorHeights.addAll(kitchenDoorHeight);
                                }


                        
                    String kitchenWindowHeight = Util.getMtextByLayerName(pl.getDoc(), ventLayer);

                    if (StringUtils.isNotBlank(kitchenWindowHeight)) {
                        try {
                            // Clean unwanted characters (=, m, space, etc.)
                            String sanitized = kitchenWindowHeight;
                            if (sanitized.contains("=")) {
                                sanitized = sanitized.split("=")[1] != null
                                        ? sanitized.split("=")[1].replaceAll("[^\\d.]", "")
                                        : "";
                            } else {
                                sanitized = sanitized.replaceAll("[^\\d.]", "");
                            }

                            if (StringUtils.isNotBlank(sanitized)) {
                                BigDecimal height = new BigDecimal(sanitized);
                                kitchen.setKitchenWindowHeight(height);
                            } else {
                                pl.addError(ventLayer + "_WINDOW_HT", 
                                    "Kitchen window height is not defined in layer " + ventLayer);
                            }
                        } catch (NumberFormatException e) {
                            LOG.error("Invalid kitchen window height value: {}", kitchenWindowHeight, e);
                        }
                    }

                }
                
                if (!allWindowWidths.isEmpty()) {
                    kitchen.setKitchenWindowWidth(allWindowWidths);
                }
                
                if (!allDoorHeights.isEmpty()) {
                    kitchen.setKitchenDoorHeight(allDoorHeights);
                }
                
                if (!allDoorWidths.isEmpty()) {
                    kitchen.setKitchenDoorWidth(allDoorWidths);
                }
                
                
            }
        }
    }

    private void handleLaundryRecreationVentilation(PlanDetail pl, Block b, Floor f, FloorUnit floorUnit) {
        MeasurementWithHeight laundryVentilation = floorUnit.getLaundryOrRecreationalVentilation();
        if (laundryVentilation != null) {
            String laundryVentLayerPattern = String.format(
                    layerNames.getLayerName("LAYER_NAME_UNIT_LAUNDRY_RECREATION_VENTILATION"),
                    b.getNumber(), f.getNumber(), floorUnit.getUnitNumber(), "+\\d");

            List<String> ventilationLayers = Util.getLayerNamesLike(pl.getDoc(), laundryVentLayerPattern);
            if (!ventilationLayers.isEmpty()) {
                List<Measurement> ventilationMeasurements = new ArrayList<>();
                List<BigDecimal> heightsOrDepths = new ArrayList<>();

                for (String ventLayer : ventilationLayers) {
                    List<DXFLWPolyline> ventilations = Util.getPolyLinesByLayer(pl.getDoc(), ventLayer);
                    if (!ventilations.isEmpty()) {
                        List<Measurement> measurements = ventilations.stream()
                            .map(polyline -> new MeasurementDetail(polyline, true))
                            .collect(Collectors.toList());
                        ventilationMeasurements.addAll(measurements);

                        List<BigDecimal> dimensionValues = Util.getListOfDimensionValueByLayer(pl, ventLayer);
                        heightsOrDepths.addAll(dimensionValues);
                    }
                }

                laundryVentilation.setMeasurements(ventilationMeasurements);
                laundryVentilation.setHeightOrDepth(heightsOrDepths);
            }
        }
    }

	@Override
	public PlanDetail validate(PlanDetail pl) {
		return pl;
	}

}
