package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Toilet;
import org.egov.common.entity.edcr.TypicalFloor;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ToiletDetailsExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(ToiletDetailsExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    /**
     * Extracts toilets and ventilation details for all units in the given PlanDetail.
     * Iterates through blocks, floors, and units to perform extraction.
     *
     * @param planDetail the PlanDetail object containing building data
     * @return the updated PlanDetail object with extracted toilet and ventilation information
     */
    @Override
    public PlanDetail extract(PlanDetail planDetail) {
        LOG.debug("Starting extraction for PlanDetail with [{}] blocks", 
                  planDetail.getBlocks() != null ? planDetail.getBlocks().size() : 0);

        for (Block block : planDetail.getBlocks()) {
            if (block.getBuilding() != null && block.getBuilding().getFloors() != null) {
                LOG.debug("Processing Block [{}] with [{}] floors", 
                          block.getNumber(), block.getBuilding().getFloors().size());

                outside: for (Floor floor : block.getBuilding().getFloors()) {
                    LOG.debug("Processing Floor [{}] with [{}] units", 
                              floor.getNumber(), floor.getUnits() != null ? floor.getUnits().size() : 0);

                    // Handle typical floors for toilets
                    if (block.getTypicalFloor() != null && !block.getTypicalFloor().isEmpty()) {
                        for (TypicalFloor tp : block.getTypicalFloor()) {
                            if (tp.getRepetitiveFloorNos().contains(floor.getNumber())) {
                                for (Floor modelFloor : block.getBuilding().getFloors()) {
                                    if (modelFloor.getNumber().equals(tp.getModelFloorNo())) {
                                        if (modelFloor.getToilet() != null) {
                                            floor.setToilet(modelFloor.getToilet());
                                            LOG.debug("Copied toilets from model floor [{}] to floor [{}]", 
                                                      modelFloor.getNumber(), floor.getNumber());
                                        }
                                        continue outside;
                                    }
                                }
                            }
                        }
                    }

                    // Extract toilets for each unit
                    if (floor.getUnits() != null) {
                        for (FloorUnit unit : floor.getUnits()) {
                            LOG.debug("Processing FloorUnit [{}] in Floor [{}], Block [{}]", 
                                      unit.getUnitNumber(), floor.getNumber(), block.getNumber());

                            List<Toilet> toilets = extractToilets(planDetail, block, floor, unit);
                            LOG.debug("Extracted [{}] toilets for Unit [{}]", toilets.size(), unit.getUnitNumber());

                            extractVentilation(planDetail, block, floor, unit, toilets);
                            LOG.debug("Ventilation extraction completed for Unit [{}]", unit.getUnitNumber());

                            unit.setToilet(toilets);
                            LOG.debug("Set toilets for Unit [{}]", unit.getUnitNumber());
                        }
                    }
                }
            }
        }

        LOG.debug("Completed extraction for PlanDetail");
        return planDetail;
    }


  
    /**
     * Extracts toilet measurement details from CAD layers.
     *
     * @param planDetail the plan detail object
     * @param block      the block being processed
     * @param floor      the floor being processed
     * @return list of toilets found on this floor
     */
    private List<Toilet> extractToilets(PlanDetail planDetail, Block block, Floor floor, FloorUnit unit) {
        LOG.debug("Starting toilet extraction for Unit [{}] in Floor [{}], Block [{}]",
                  unit.getUnitNumber(), floor.getNumber(), block.getNumber());

        List<Toilet> toilets = new ArrayList<>();
        String layerName = String.format(layerNames.getLayerName("LAYER_NAME_BLK_FLR_UNIT_TOILET"),
                                         block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        LOG.debug("Constructed toilet layer pattern: {}", layerName);

        List<String> names = Util.getLayerNamesLike(planDetail.getDoc(), layerName);
        LOG.debug("Found [{}] toilet layers for Unit [{}]", names.size(), unit.getUnitNumber());

        for (String toiletLayer : names) {
            LOG.debug("Processing toilet layer: {}", toiletLayer);

            List<DXFLWPolyline> toiletMeasurements = Util.getPolyLinesByLayer(planDetail.getDoc(), toiletLayer);
            LOG.debug("Found [{}] measurements in layer [{}]", toiletMeasurements.size(), toiletLayer);
            
            List<BigDecimal> toiletWidth = Util.getListOfDimensionByColourCode(planDetail, toiletLayer, DxfFileConstants.INDEX_COLOR_TWO);

            if (!toiletMeasurements.isEmpty()) {
                Toilet toiletObj = new Toilet();
                List<Measurement> toiletMeasurementList = new ArrayList<>();
                toiletMeasurements.forEach(toilet -> {
                    Measurement measurementToilet = new MeasurementDetail(toilet, true);
                    toiletMeasurementList.add(measurementToilet);
                });
                
                if (toiletWidth != null && !toiletWidth.isEmpty()) {
                	toiletObj.setToiletWidth(toiletWidth);
				}

                toiletObj.setToilets(toiletMeasurementList);
                toilets.add(toiletObj);

                LOG.debug("Added Toilet object with [{}] measurements for layer [{}]",
                          toiletMeasurementList.size(), toiletLayer);
            }
        }

        LOG.debug("Completed toilet extraction for Unit [{}]. Total toilets extracted: [{}]",
                  unit.getUnitNumber(), toilets.size());

        return toilets;
    }
    
    /**
     * Extracts ventilation details (window height and width) and maps them to toilets.
     *
     * @param planDetail the plan detail object
     * @param block      the block being processed
     * @param floor      the floor being processed
     * @param toilets    the list of toilets on this floor
     */
    private void extractVentilation(PlanDetail planDetail, Block block, Floor floor, FloorUnit unit, List<Toilet> toilets) {
        LOG.debug("Starting ventilation extraction for Unit [{}] in Floor [{}], Block [{}]",
                  unit.getUnitNumber(), floor.getNumber(), block.getNumber());

        String toiletVentilationLayer = String.format(
                layerNames.getLayerName("LAYER_NAME_BLK_FLR_UNIT_TOILET_VENTILATION"),
                block.getNumber(), floor.getNumber(), unit.getUnitNumber(), "+\\d");

        LOG.debug("Constructed toilet ventilation layer pattern: {}", toiletVentilationLayer);

        List<String> ventilationList = Util.getLayerNamesLike(planDetail.getDoc(), toiletVentilationLayer);
        LOG.debug("Found [{}] ventilation layers for Unit [{}]", ventilationList.size(), unit.getUnitNumber());

        int index = 0;
        for (String ventilationHeightLayer : ventilationList) {
            LOG.debug("Processing ventilation layer: {}", ventilationHeightLayer);

            // get height from layer
            String windowHeightStr = Util.getMtextByLayerName(planDetail.getDoc(), ventilationHeightLayer);
            BigDecimal windowHeight = windowHeightStr != null
                    ? new BigDecimal(windowHeightStr.replaceAll("WINDOW_HT_M=", ""))
                    : BigDecimal.ZERO;

            LOG.debug("Extracted ventilation height: {} for layer {}", windowHeight, ventilationHeightLayer);

            // get widths from that same layer
            List<BigDecimal> windowWidths =
                    Util.getListOfDimensionByColourCode(planDetail, ventilationHeightLayer, DxfFileConstants.INDEX_COLOR_TWO);

            LOG.debug("Extracted [{}] ventilation widths for layer {}", windowWidths.size(), ventilationHeightLayer);

            if (index < toilets.size()) {
                Toilet toiletObj = toilets.get(index);
                toiletObj.setToiletVentilation(windowHeight);
                if (!windowWidths.isEmpty()) {
                    toiletObj.setToiletWindowWidth(windowWidths);
                }
                LOG.debug("Updated Toilet object [{}] with ventilation height and widths", index);
            } else {
                LOG.warn("More ventilation layers [{}] than toilets [{}] for Unit [{}]",
                         ventilationList.size(), toilets.size(), unit.getUnitNumber());
            }

            index++;
        }

        LOG.debug("Completed ventilation extraction for Unit [{}]", unit.getUnitNumber());
    }
}