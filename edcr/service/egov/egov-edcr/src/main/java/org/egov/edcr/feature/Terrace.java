/*
 * UPYOG  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.service.MDMSCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.egov.edcr.constants.CommonKeyConstants.*;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

@Service
public class Terrace extends FeatureProcess {

    /** Logger for this class */
    private static final Logger LOG = LogManager.getLogger(Terrace.class);

    @Autowired
    MDMSCacheManager cache;

    /**
     * Validates the given plan object.
     * <p>No terrace-specific validation rules implemented currently.</p>
     *
     * @param pl Plan object to validate
     * @return same plan without modification
     */
    @Override
    public Plan validate(Plan pl) {
        LOG.info("Terrace.validate() called — no specific validation rules implemented.");
        return pl;
    }

    /**
     * Processes the terrace feature for each block in the plan.
     * <p>
     * This method:
     * <ul>
     *     <li>Iterates over all blocks</li>
     *     <li>Checks if terrace exists on the block</li>
     *     <li>Triggers validation and scrutiny report creation</li>
     * </ul>
     * </p>
     *
     * @param pl The plan object containing blocks and terrace details
     * @return The updated plan object with scrutiny details added
     */
    @Override
    public Plan process(Plan pl) {

        LOG.info("Terrace: Starting process for {} blocks", pl.getBlocks().size());

        for (Block block : pl.getBlocks()) {
            LOG.debug("Terrace: Processing block number {}", block.getNumber());
            processBlock(pl, block);
        }

        LOG.info("Terrace: Completed terrace processing for all blocks.");
        return pl;
    }

    /**
     * Processes a single block for terrace validation.
     *
     * @param pl The plan object
     * @param block The block to process
     */
    private void processBlock(Plan pl, Block block) {

        if (block.getTerrace() == null) {
            LOG.debug("Terrace: No terrace found for block {} — skipping.", block.getNumber());
            return;
        }

        LOG.info("Terrace: Terrace found for block {} — validating.", block.getNumber());
        validate(pl, block);
    }

    /**
     * Performs validation for a block’s terrace and populates the scrutiny report.
     *
     * @param pl The plan object that holds report output
     * @param block Block for which terrace validation is performed
     */
    private void validate(Plan pl, Block block) {

        BigDecimal terraceArea = block.getTerrace().getArea();

        LOG.debug("Terrace: Block {} — Terrace area = {}", block.getNumber(), terraceArea);

        ScrutinyDetail scrutinyDetail = createScrutinyDetail();
        Map<String, String> row = createResultRow(Result.Accepted.getResultVal(), terraceArea);

        LOG.debug("Terrace: Adding scrutiny detail row for block {}", block.getNumber());
        scrutinyDetail.getDetail().add(row);

        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

        LOG.info("Terrace: Validation added to report for block {}", block.getNumber());
    }

    /**
     * Creates a scrutiny detail section for terrace reporting.
     *
     * @return A configured ScrutinyDetail object
     */
    private ScrutinyDetail createScrutinyDetail() {

        LOG.debug("Terrace: Creating ScrutinyDetail section.");

        ScrutinyDetail sd = new ScrutinyDetail();
        sd.setKey(Common_Terrace);
        sd.addColumnHeading(1, RULE_NO);
        sd.addColumnHeading(2, DESCRIPTION);
        sd.addColumnHeading(3, REQUIRED);
        sd.addColumnHeading(4, PROVIDED);
        sd.addColumnHeading(5, STATUS);

        return sd;
    }

    /**
     * Creates a single scrutiny detail row for terrace area reporting.
     *
     * @param status The validation status (Accepted/Rejected/etc.)
     * @param terraceArea Terrace area provided in plan
     * @return Map representing a row in the scrutiny detail report
     */
    private Map<String, String> createResultRow(String status, BigDecimal terraceArea) {

        LOG.debug("Terrace: Creating result row — Status={}, Area={}", status, terraceArea);

        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(RULE_NO_TERRACE);
        detail.setDescription(TERRACE);
        detail.setRequired("-");
        detail.setProvided(terraceArea != null ? terraceArea.toString() : "-");
        detail.setStatus(status);

        return mapReportDetails(detail);
    }

    /**
     * Returns amendments for the Terrace feature.
     * <p>No amendments defined yet.</p>
     *
     * @return Empty LinkedHashMap
     */
    @Override
    public Map<String, Date> getAmendments() {
        LOG.debug("Terrace: getAmendments() called — no amendments defined.");
        return new LinkedHashMap<>();
    }
}