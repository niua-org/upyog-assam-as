package org.egov.edcr.feature;

import java.math.BigDecimal;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.egov.common.entity.edcr.Block;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoofTankExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(RoofTankExtract.class);
    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail planDetail) {

        for (Block block : planDetail.getBlocks()) {

            block.setRoofTanks(Util.getListOfDimensionValueByLayer(planDetail,
                    String.format(layerNames.getLayerName("LAYER_NAME_ROOF_TANK"), block.getNumber())));

            if (block.getRoofTanks() != null && !block.getRoofTanks().isEmpty()) {

                BigDecimal minHeight = block.getRoofTanks().stream()
                        .reduce(BigDecimal::min)
                        .orElse(BigDecimal.ZERO);

                BigDecimal declaredHeight = block.getBuilding().getDeclaredBuildingHeight();

                //  Only subtract if roof tank height < 2m
				/*
				 * if (minHeight.compareTo(new BigDecimal(2)) < 0) {
				 * 
				 * BigDecimal newHeight = declaredHeight.subtract(minHeight);
				 * 
				 * if (newHeight.compareTo(BigDecimal.ZERO) < 0) { newHeight = BigDecimal.ZERO;
				 * }
				 * 
				 * block.getBuilding().setBuildingHeight(newHeight);
				 * block.getBuilding().setHeightIncreasedBy("Roof Tank (<2m) Subtracted"); }
				 * else { // ≥ 2m → do NOT add and do NOT subtract
				 * block.getBuilding().setBuildingHeight(declaredHeight);
				 * block.getBuilding().setHeightIncreasedBy(null); }
				 */
            }

            // High rise check
            if (block.getBuilding().getBuildingHeight().compareTo(new BigDecimal(15)) > 0) {
                block.getBuilding().setIsHighRise(true);
            }
        }

        return planDetail;
    }



}
