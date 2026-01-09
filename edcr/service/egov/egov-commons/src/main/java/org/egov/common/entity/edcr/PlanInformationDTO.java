/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
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

package org.egov.common.entity.edcr;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

//These are the declarations of the applicant in the plan using PLAN_INFO layer.
public class PlanInformationDTO implements Serializable {

//    private static final String NA = "NA";
	public static final String SEQ_EDCR_PLANINFO = "SEQ_EDCR_PLANINFO";
	private static final long serialVersionUID = 4L;
//
	@Id
	@GeneratedValue(generator = SEQ_EDCR_PLANINFO, strategy = GenerationType.SEQUENCE)
	private Long id;
	// Plot area defined in PLAN_INFO layer. Using the same to measure coverage and
	// small plot condition.This is the declared plot area in the plan.
	private BigDecimal plotArea = BigDecimal.ZERO;

	// Temporary field used to auto populate occupancy detail.
	private String occupancy;
//    
//    private String liftType;
//    //Temporary field used for service type.
	private String serviceType;

	private String materialType;
	
	private transient String landUseZone;

	private BigDecimal roadWidth = BigDecimal.ZERO;

	private BigDecimal proposedRoadWidth = BigDecimal.ZERO;
	private String wardNo;
	private String dagNo;

	private String pattaNo;

	public BigDecimal getProposedRoadWidth() {
		return proposedRoadWidth;
	}

	public void setProposedRoadWidth(BigDecimal proposedRoadWidth) {
		this.proposedRoadWidth = proposedRoadWidth;
	}

	public BigDecimal getRoadWidth() {
		return roadWidth;
	}

	public void setRoadWidth(BigDecimal roadWidth) {
		this.roadWidth = roadWidth;
	}

	public String getDagNo() {
		return dagNo;
	}

	public void setDagNo(String dagNo) {
		this.dagNo = dagNo;
	}

	public String getPattaNo() {
		return pattaNo;
	}

	public void setPattaNo(String pattaNo) {
		this.pattaNo = pattaNo;
	}

	public String getWardNo() {
		return wardNo;
	}

	public void setWardNo(String wardNo) {
		this.wardNo = wardNo;
	}

	public String getMaterialType() {
		return materialType;
	}

	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}

	public BigDecimal getPlotArea() {
		return plotArea;
	}

	public void setPlotArea(BigDecimal plotArea) {
		this.plotArea = plotArea;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getOccupancy() {
		return occupancy;
	}

	public void setOccupancy(String occupancy) {
		this.occupancy = occupancy;
	}

	public String getLandUseZone() {
        return landUseZone;
    }

    public void setLandUseZone(String landUseZone) {
        this.landUseZone = landUseZone;
    }

}
