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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*All the details extracted from the plan are referred in this object*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanBpa implements Serializable {

    private static final long serialVersionUID = 7276648029097296311L;
    private VirtualBuildingDTO virtualBuilding = new VirtualBuildingDTO();

    private PlotDTO plot;

    /**
     * Planinformation captures the declarations of the plan.Plan information captures the boundary, building location
     * details,surrounding building NOC's etc. User will assert the details about the plot. The same will be used to print in plan
     * report.
     */
    private PlanInformationDTO planInformation;
    
    Map<String, String> planInfoProperties = new HashMap<>();
 

    // Single plan contain multiple block/building information. Records Existing and proposed block information.
    private List<BlockDTO> blocks = new ArrayList<>();
    
    private FarDetailsDTO farDetails;
    
    private BigDecimal totalKitchens = BigDecimal.ZERO;
    private BigDecimal totalBathrooms = BigDecimal.ZERO;
    private BigDecimal totalLatrines = BigDecimal.ZERO;
    private BigDecimal totalUrinals = BigDecimal.ZERO;
    private transient List<ElectricLine> electricLine = new ArrayList<>();
    private ReportOutput reportOutput = new ReportOutput();

   public ReportOutput getReportOutput() {
        return reportOutput;
    }

    public void setReportOutput(ReportOutput reportOutput) {
        this.reportOutput = reportOutput;
    }
    
    public List<ElectricLine> getElectricLine() {
        return electricLine;
    }

    public void setElectricLine(List<ElectricLine> electricLine) {
        this.electricLine = electricLine;
    }


 public BigDecimal getTotalKitchens() {
        return totalKitchens;
    }

    public void setTotalKitchens(BigDecimal totalKitchens) {
        this.totalKitchens = totalKitchens;
    }

    public BigDecimal getTotalBathrooms() {
        return totalBathrooms;
    }

    public void setTotalBathrooms(BigDecimal totalBathrooms) {
        this.totalBathrooms = totalBathrooms;
    }

    public BigDecimal getTotalLatrines() {
        return totalLatrines;
    }

    public void setTotalLatrines(BigDecimal totalLatrines) {
        this.totalLatrines = totalLatrines;
    }

    public BigDecimal getTotalUrinals() {
        return totalUrinals;
    }

    public void setTotalUrinals(BigDecimal totalUrinals) {
        this.totalUrinals = totalUrinals;
    }
    
	public FarDetailsDTO getFarDetails() {
	    return farDetails;
	}
	
	public void setFarDetails(FarDetailsDTO farDetails) {
	    this.farDetails = farDetails;
	}

   
    public List<BlockDTO> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<BlockDTO> blocks) {
        this.blocks = blocks;
    }

    public BlockDTO getBlockByName(String blockName) {
        for (BlockDTO block : getBlocks()) {
            if (block.getName().equalsIgnoreCase(blockName))
                return block;
        }
        return null;
    }


    public PlanInformationDTO getPlanInformation() {
        return planInformation;
    }

    public void setPlanInformation(PlanInformationDTO planInformation) {
        this.planInformation = planInformation;
    }

    public PlotDTO getPlot() {
      
		return plot;
    }

    public void setPlot(PlotDTO plot) {
        this.plot = plot;
    }

  
    public VirtualBuildingDTO getVirtualBuilding() {
        return virtualBuilding;
    }

    public void setVirtualBuilding(VirtualBuildingDTO virtualBuilding) {
        this.virtualBuilding = virtualBuilding;
    }
    public void sortBlockByName() {
        if (!blocks.isEmpty())
            Collections.sort(blocks, Comparator.comparing(BlockDTO::getNumber));
    }

    public Map<String, String> getPlanInfoProperties() {
        return planInfoProperties;
    }

    public void setPlanInfoProperties(Map<String, String> planInfoProperties) {
        this.planInfoProperties = planInfoProperties;
    }

}
