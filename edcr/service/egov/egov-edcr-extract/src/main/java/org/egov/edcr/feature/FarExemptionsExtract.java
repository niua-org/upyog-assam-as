package org.egov.edcr.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.springframework.stereotype.Service;

@Service
public class FarExemptionsExtract extends FeatureExtract{
    private static final Logger LOG = LogManager.getLogger(FarExemptionsExtract.class);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail planDetail) {
        LOG.info("Extract for FarExemptions");
        return planDetail;
    }

}
