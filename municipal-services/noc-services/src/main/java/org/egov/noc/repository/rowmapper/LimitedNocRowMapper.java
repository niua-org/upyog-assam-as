package org.egov.noc.repository.rowmapper;

import com.google.gson.Gson;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.enums.ApplicationType;
import org.egov.noc.web.model.enums.Status;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * extracts the limited data from the resultSet and populate the NOC Objects
 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
 */
@Component
public class LimitedNocRowMapper implements ResultSetExtractor<List<Noc>> {

    @Override
    public List<Noc> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<String, Noc> nocListMap = new HashMap<>();
        Noc noc;
        while (rs.next()) {
            String Id = rs.getString("noc_Id");
            if (nocListMap.getOrDefault(Id, null) == null) {
                noc = new Noc();
                noc.setTenantId(rs.getString("tenantid"));
                noc.setId(rs.getString("noc_Id"));
                noc.setApplicationNo(rs.getString("applicationNo"));
                noc.setNocNo(rs.getString("nocNo"));
                noc.setNocType(rs.getString("nocType"));
                noc.setApplicationStatus(rs.getString("applicationStatus"));
                noc.setApplicationType(ApplicationType.fromValue(rs.getString("applicationType")));
                noc.setStatus(Status.fromValue(rs.getString("status")));
                noc.setLandId(rs.getString("landId"));
                noc.setSource(rs.getString("source"));
                noc.setSourceRefId(rs.getString("sourceRefId"));
                noc.setAccountId(rs.getString("AccountId"));

                Object additionalDetails = new Gson().fromJson(rs.getString("additionalDetails").equals("{}")
                                || rs.getString("additionalDetails").equals("null") ? null : rs.getString("additionalDetails"),
                        Object.class);
                noc.setAdditionalDetails(additionalDetails);

                nocListMap.put(Id, noc);
            }
        }
        return new ArrayList<>(nocListMap.values());
    }


}
