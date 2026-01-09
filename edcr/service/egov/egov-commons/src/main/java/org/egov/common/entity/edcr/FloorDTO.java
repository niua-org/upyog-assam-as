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
import java.util.stream.Collectors;

public class FloorDTO extends Measurement {

    private static final long serialVersionUID = 26L;

    private List<OccupancyDTO> occupancies = new ArrayList<>();

    private String name;
    private Integer number;
    private List<FloorUnitDTO> units = new ArrayList<>();

    public void addBuiltUpArea(OccupancyDTO occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else if (occupancies.contains(occupancy)) {
            occupancies.get(occupancies.indexOf(occupancy))
                    .setBuiltUpArea((occupancies.get(occupancies.indexOf(occupancy)).getBuiltUpArea() == null
                            ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getBuiltUpArea())
                                    .add(occupancy.getBuiltUpArea()));
            occupancies.get(occupancies.indexOf(occupancy)).setExistingBuiltUpArea(
                    (occupancies.get(occupancies.indexOf(occupancy)).getExistingBuiltUpArea() == null ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getExistingBuiltUpArea())
                                    .add(occupancy.getExistingBuiltUpArea()));

        } else
            occupancies.add(occupancy);

    }

    public void addCarpetArea(OccupancyDTO occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else if (occupancies.contains(occupancy)) {
            occupancies.get(occupancies.indexOf(occupancy))
                    .setCarpetArea((occupancies.get(occupancies.indexOf(occupancy)).getCarpetArea() == null
                            ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getCarpetArea())
                                    .add(occupancy.getCarpetArea()));

            occupancies.get(occupancies.indexOf(occupancy)).setExistingCarpetArea(
                    (occupancies.get(occupancies.indexOf(occupancy)).getExistingCarpetArea() == null ? BigDecimal.ZERO
                            : occupancies.get(occupancies.indexOf(occupancy)).getExistingCarpetArea())
                                    .add(occupancy.getExistingCarpetArea()));
        } else
            occupancies.add(occupancy);

    }

    public void addDeductionArea(OccupancyDTO occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else {
            List<OccupancyDTO> collect = occupancies.stream().filter(o -> o.getTypeHelper() != null
                    && (occupancy.getTypeHelper() != null && o.getTypeHelper().getType() != null
                            && o.getTypeHelper().getType().getCode()
                                    .equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())))
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                collect.get(0)
                        .setDeduction(collect.get(0).getDeduction() == null
                                ? BigDecimal.ZERO
                                : collect.get(0).getDeduction()
                                        .add(occupancy.getDeduction()));
                collect.get(0).setExistingDeduction(
                        (collect.get(0).getExistingDeduction() == null ? BigDecimal.ZERO
                                : collect.get(0).getExistingDeduction())
                                        .add(occupancy.getExistingDeduction()));
            } else
                occupancies.add(occupancy);
        }

    }

    public void addCarpetDeductionArea(OccupancyDTO occupancy) {
        if (occupancies == null) {
            occupancies = new ArrayList<>();
            occupancies.add(occupancy);
        } else {
            List<OccupancyDTO> collect = occupancies.stream().filter(o -> o.getTypeHelper() != null
                    && (o.getTypeHelper().getType().getCode()
                            .equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())))
                    .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                collect.get(0)
                        .setCarpetAreaDeduction(collect.get(0).getCarpetAreaDeduction() == null
                                ? BigDecimal.ZERO
                                : collect.get(0).getCarpetAreaDeduction()
                                        .add(occupancy.getCarpetAreaDeduction()));
                collect.get(0).setExistingCarpetAreaDeduction(
                        (collect.get(0).getExistingCarpetAreaDeduction() == null ? BigDecimal.ZERO
                                : collect.get(0).getExistingCarpetAreaDeduction())
                                        .add(occupancy.getExistingCarpetAreaDeduction()));
            } else
                occupancies.add(occupancy);
        }

    }

    public List<OccupancyDTO> getOccupancies() {
        return occupancies;
    }

    public void setOccupancies(List<OccupancyDTO> occupancies) {
        this.occupancies = occupancies;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {

        return "Floor :" + number;

    }
    public List<FloorUnitDTO> getUnits() {
        return units;
    }

    public void setUnits(List<FloorUnitDTO> units) {
        this.units = units;
    }


}
