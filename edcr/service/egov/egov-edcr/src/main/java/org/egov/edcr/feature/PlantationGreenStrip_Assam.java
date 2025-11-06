package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_6;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

@Service
public class PlantationGreenStrip_Assam extends FeatureProcess {
    private static final Logger LOG = LogManager.getLogger(PlantationGreenStrip_Assam.class);

    @Autowired
    MDMSCacheManager cache;

    @Override
    public Plan validate(Plan pl) {
        return null;
    }

    @Override
    public Plan process(Plan pl) {
        Optional<PlantationGreenStripRequirement> ruleOpt = getPlantationGreenStripRule(pl);

        // Get min % (default 10–15%) from MDMS or use defaults
        BigDecimal minPercentage = ruleOpt.map(PlantationGreenStripRequirement::getMinPercentage)
                                          .orElse(new BigDecimal("10"));
        BigDecimal maxPercentage = ruleOpt.map(PlantationGreenStripRequirement::getMaxPercentage)
                                          .orElse(new BigDecimal("15"));

        // We’ll take minimum threshold as 10–15% depending on rule
        BigDecimal requiredMinPercentage = maxPercentage; // if range exists, use higher end (15%)

        BigDecimal landscapeArea = pl.getPlot().getLandscapingArea(); // 15% of landscaping area (which h 60 percnt of open area)
        
     // Calculate Unpaved Area = 50% of Landscaping Area
        BigDecimal unpavedArea = landscapeArea
                .multiply(new BigDecimal("0.50"))
                .setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
        
        BigDecimal minRequiredArea = landscapeArea.multiply(requiredMinPercentage).divide(BigDecimal.valueOf(100));

        for (Block block : pl.getBlocks()) {
            processBlock(pl, block, minRequiredArea, requiredMinPercentage, landscapeArea, unpavedArea);
        }

        return pl;
    }

    private Optional<PlantationGreenStripRequirement> getPlantationGreenStripRule(Plan pl) {
        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.PLANTATION_GREEN_STRIP.getValue(), false);
        return rules.stream()
                .filter(PlantationGreenStripRequirement.class::isInstance)
                .map(PlantationGreenStripRequirement.class::cast)
                .findFirst();
    }

    private void processBlock(Plan pl, Block block, BigDecimal minRequiredArea, BigDecimal minPercentage, BigDecimal landscapeArea, BigDecimal unpavedArea) {
        ScrutinyDetail scrutinyDetail = createScrutinyDetailForBlock(block);

        List<BigDecimal> areas = block.getPlantationGreenStripes().stream()
                .map(greenStrip -> greenStrip.getArea())
                .collect(Collectors.toList());

        if (!areas.isEmpty()) {
            BigDecimal totalGreenArea = areas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            //  Only minimum check (no max restriction)
            boolean valid = totalGreenArea.compareTo(minRequiredArea) >= 0;

            String permissible = "Minimum " + minPercentage + "% of Landscaping Area (≥ "
                    + minRequiredArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
                    + " sq.m)";

            buildResult(pl, scrutinyDetail, valid,
                    "Area of Organised Open Space:  (Minimum " + minPercentage + "% of Landscaping Area)",
                    permissible,
                    totalGreenArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
                            .toString());
            
            ///  Landscaping Area (should be ≥ 60% of Total Open Area)
            if (landscapeArea != null && block.getLandscapingArea() != null && !block.getLandscapingArea().isEmpty()) {
                BigDecimal providedLandscaping = block.getLandscapingArea().stream()
                        .map(Measurement::getArea)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);

                boolean validLandscape = providedLandscaping.compareTo(landscapeArea) >= 0;

                buildResult(pl, scrutinyDetail, validLandscape,
                        "Landscaping Area (60% of Total Open Area)",
                        landscapeArea + " sq.m", providedLandscaping + " sq.m");

                LOG.info("Landscaping for Block {} => Provided: {} sq.m | Required ≥ {} sq.m | Status: {}",
                        block.getNumber(), providedLandscaping, landscapeArea, valid ? "Accepted" : "Not Accepted");
            } else {
                LOG.info("Skipping Landscaping Area validation for Block {} (no data found).", block.getNumber());
            }

            // Unpaved Area (should be ≥ 50% of Landscaping Area)
            if (unpavedArea != null && block.getUnpavedArea() != null && !block.getUnpavedArea().isEmpty()) {
                BigDecimal providedUnpaved = block.getUnpavedArea().stream()
                        .map(Measurement::getArea)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);

                boolean validUnpaved = providedUnpaved.compareTo(unpavedArea) >= 0;

                buildResult(pl, scrutinyDetail, validUnpaved,
                        "Unpaved Area (50% of Landscaping Area)",
                        unpavedArea + " sq.m", providedUnpaved + " sq.m");

                LOG.info("Unpaved Area for Block {} => Provided: {} sq.m | Required ≥ {} sq.m | Status: {}",
                        block.getNumber(), providedUnpaved, unpavedArea, valid ? "Accepted" : "Not Accepted");
            } else {
                LOG.info("Skipping Unpaved Area validation for Block {} (no data found).", block.getNumber());
            }
        }
    }

    private ScrutinyDetail createScrutinyDetailForBlock(Block block) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey(BLOCK + block.getNumber() + CONTINUOUS_GREEN_PLANTING_STRIP);
        return scrutinyDetail;
    }

    private void buildResult(Plan pl, ScrutinyDetail scrutinyDetail, boolean valid, String description, String permited,
                             String provided) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(RULE_37_6);
        detail.setDescription(description);
        detail.setPermissible(permited);
        detail.setProvided(provided);
        detail.setStatus(valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
