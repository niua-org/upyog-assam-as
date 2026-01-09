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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BlockDTO extends Measurement {

	private static final String SIDE_YARD1_DESC = "Side Yard1";
    private static final String SIDE_YARD2_DESC = "Side Yard2";
   
    private static final long serialVersionUID = 12L;
    private String name;
    private String number;
    private BuildingDTO building = new BuildingDTO();  
    private String numberOfLifts;
    private List<SetBack> setBacks = new ArrayList<>();
    private List<BigDecimal> plinthHeight;

    @Override
	public String toString() {
		return "BlockDTO [name=" + name + ", number=" + number + ", building=" + building + ", numberOfLifts="
				+ numberOfLifts + ", setBacks=" + setBacks + ", plinthHeight=" + plinthHeight + "]";
	}


    public List<BigDecimal> getPlinthHeight() {
        return plinthHeight;
    }

    public void setPlinthHeight(List<BigDecimal> plinthHeight) {
        this.plinthHeight = plinthHeight;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumberOfLifts() {
        return numberOfLifts;
    }

    public void setNumberOfLifts(String numberOfLifts) {
        this.numberOfLifts = numberOfLifts;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BuildingDTO getBuilding() {
        return building;
    }

    public void setBuilding(BuildingDTO building) {
        this.building = building;
    }

    public List<SetBack> getSetBacks() {
        return setBacks;
    }

    public SetBack getLevelZeroSetBack() {
        SetBack setBack = null;

        for (SetBack setback : getSetBacks()) {
            if (setback.getLevel() == 0)
                return setback;
        }
        return setBack;
    }
    public SetBack getLowerLevelSetBack(Integer level, String yardDesc) {

        SetBack setBack = null;
        if (level == 0)
            return null;

        while (level > 0) {
            level--;
            for (SetBack setback : getSetBacks()) {
                if (setback.getLevel() == level && yardDesc.equalsIgnoreCase(SIDE_YARD1_DESC)
                        && setback.getSideYard1() != null)
                    return setback;
                else if (setback.getLevel() == level && yardDesc.equalsIgnoreCase(SIDE_YARD2_DESC)
                        && setback.getSideYard2() != null)
                    return setback;
            }

        }
        return setBack;

    }
    public void setSetBacks(List<SetBack> setBacks) {
        this.setBacks = setBacks;
    }
    
    public SetBack getSetBackByLevel(String level) {

        SetBack setBack = null;
        Integer lvl = Integer.valueOf(level);
        for (SetBack setback : getSetBacks()) {
            if (setback.getLevel() == lvl)
                return setback;
        }
        return setBack;
    }

}
