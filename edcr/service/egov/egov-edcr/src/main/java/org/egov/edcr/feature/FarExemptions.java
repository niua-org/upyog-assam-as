package org.egov.edcr.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.CommonKeyConstants.FAR_EXEMPTIONS;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

@Service
public class FarExemptions extends FeatureProcess {
    private static final Logger LOG = LogManager.getLogger(FarExemptions.class);

    @Override
    public Plan validate(Plan plan) {
        return plan;
    }

    @Override
    public Plan process(Plan plan) {
        LOG.info("Processing Far Exemptions");

        for (Block block : plan.getBlocks()) {
            ScrutinyDetail scrutinyDetail = createScrutinyDetail(BLOCK + block.getNumber() + UNDERSCORE, FAR_EXEMPTIONS, DESCRIPTION, PROVIDED, STATUS);

//            for(Floor floor: block.getBuilding().getFloors())
                createReportDetails(plan, block, scrutinyDetail);
        }
        return plan;
    }

    private void createReportDetails(Plan pl, Block b, ScrutinyDetail scrutinyDetail){
        LOG.info("Creating Report Details for Far Exemptions");
        org.egov.common.entity.edcr.FarExemption exemptions = pl.getFarExemptions();

        if(exemptions.getBasementParking() != null && exemptions.getBasementParking().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_BASEMENT_PARKING, exemptions.getBasementParking());

        if(exemptions.getBasementServiceFloor() != null && exemptions.getBasementServiceFloor().compareTo(BigDecimal.ZERO) > 0){
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_BASEMENT_SERVICE_FLOOR, exemptions.getBasementServiceFloor());
        }

        if(exemptions.getEntranceLobby() != null && exemptions.getEntranceLobby().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_ENTRANCE_LOBBY, exemptions.getEntranceLobby());

        if(exemptions.getBalcony() != null && exemptions.getBalcony().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_BALCONY, exemptions.getBalcony());

        if(exemptions.getCorridor() != null && exemptions.getCorridor().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_CORRIDOR, exemptions.getCorridor());

        if(exemptions.getProjection() != null && exemptions.getProjection().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_TOTAL_PROJECTIONS, exemptions.getProjection());

        if(exemptions.getGuardRoom() != null && exemptions.getGuardRoom().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_GUARDROOM, exemptions.getGuardRoom());

        if(exemptions.getCareTakerRoom() != null && exemptions.getCareTakerRoom().compareTo(BigDecimal.ZERO) > 0)
            addDetailsInReport(pl, scrutinyDetail, DEDUCTED_CARETAKERROOM, exemptions.getCareTakerRoom());
    }

    private void addDetailsInReport(Plan pl, ScrutinyDetail scrutinyDetail, String description, BigDecimal deductedValue){
        String truncatedProvidedValue = deductedValue.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setDescription(description);
        detail.setProvided(truncatedProvidedValue);
        detail.setStatus(Result.Accepted.getResultVal());

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    // Method to create ScrutinyDetail
    private ScrutinyDetail createScrutinyDetail(String key, String... headings) {
        LOG.info("Creating scrutiny detail with key: {}", key);
        ScrutinyDetail detail = new ScrutinyDetail();
        detail.setKey(key);
        for (int i = 0; i < headings.length; i++) {
            detail.addColumnHeading(i + 1, headings[i]);
        }
        return detail;
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
