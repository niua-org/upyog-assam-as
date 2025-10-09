package org.egov.user.domain.model;

import lombok.*;
import org.egov.user.domain.exception.InvalidUserSearchCriteriaException;
import org.egov.user.domain.model.enums.GuardianRelation;
import org.egov.user.domain.model.enums.UserType;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class UserSearchCriteria {

    private List<Long> id;
    private List<String> uuid;
    private String userName;
    private String name;
    private String pan;
    private String guardian;
    private String gender;
    private GuardianRelation guardianRelation;
    private String altContactNumber;
    private String aadhaarNumber;
    private String mobileNumber;
    private String emailId;
    private boolean fuzzyLogic;
    private Boolean active;
    private Integer offset;
    private Integer limit;
    private List<String> sort;
    private UserType type;
    private String tenantId;
    private List<String> roleCodes;
    private String alternatemobilenumber;
    private Boolean excludeAddressDetails = false; // This fields is added to exclude address details from user search response V2
    private String addressId; // This field is added to search user with particular address using addressId
    private String addressStatus; // This field is used to search address with particular status

    public void validate(boolean isInterServiceCall) {
        if (validateIfEmptySearch(isInterServiceCall) || validateIfTenantIdExists(isInterServiceCall)) {
            throw new InvalidUserSearchCriteriaException(this);
        }
    }

    private boolean validateIfEmptySearch(boolean isInterServiceCall) {
        /*
            for "InterServiceCall" ->
                at least one is compulsory --> id, uuid, userName, name, pan, guardian, gender, guardianRelation, altContactNumber, aadhaarNumber, mobileNumber, emailId, fuzzyLogic, active, offset, limit, sort, type, tenantId, roleCodes, alternatemobilenumber, excludeAddressDetails, addressId, addressStatus

            and for calls from outside->
                at least one is compulsory --> id, uuid, userName, name, pan, guardian, gender, guardianRelation, altContactNumber, aadhaarNumber, mobileNumber, emailId, fuzzyLogic, active, offset, limit, sort, type, tenantId, roleCodes, alternatemobilenumber, excludeAddressDetails, addressId, addressStatus
         */
        if (isInterServiceCall)
            return isEmpty(userName) && isEmpty(name) && isEmpty(mobileNumber) && isEmpty(emailId) && isEmpty(aadhaarNumber) && isEmpty(pan) &&
                    isEmpty(altContactNumber) && isEmpty(guardian) && isEmpty(gender) &&
                    CollectionUtils.isEmpty(uuid) && CollectionUtils.isEmpty(id) && CollectionUtils.isEmpty(roleCodes);
        else
            return isEmpty(userName) && isEmpty(name) && isEmpty(mobileNumber) && isEmpty(emailId) &&
                    isEmpty(altContactNumber) && isEmpty(guardian) && isEmpty(gender) &&
                    CollectionUtils.isEmpty(uuid) && isEmpty(aadhaarNumber) && isEmpty(pan);
    }

    private boolean validateIfTenantIdExists(boolean isInterServiceCall) {
        /*
         * Validates if tenantId is compulsory and missing, based on the type of call.
         *
         * Rules:
         * 1. For InterServiceCall (internal service-to-service calls):
         *    - tenantId is required if ANY of these fields are provided:
         *        userName, name, mobileNumber, aadhaarNumber, pan,
         *        altContactNumber, guardian, guardianRelation, gender, roleCodes
         *
         * 2. For external calls (from outside):
         *    - tenantId is required if ANY of these fields are provided:
         *        userName, name, mobileNumber, aadhaarNumber, pan,
         *        altContactNumber, guardian, guardianRelation, gender
         *    - (Note: roleCodes is NOT considered here)
         *
         * @param isInterServiceCall true if request is from another service, false if external call
         * @return true if tenantId is compulsory but missing, otherwise false
         */
        if (isInterServiceCall)
            return (!isEmpty(userName) || !isEmpty(name) || !isEmpty(mobileNumber) || !isEmpty(aadhaarNumber) || !isEmpty(pan) ||
                    !isEmpty(altContactNumber) || !isEmpty(guardian) || !isEmpty(gender) ||
                    !CollectionUtils.isEmpty(roleCodes))
                    && isEmpty(tenantId);
        else
            return (!isEmpty(userName) || !isEmpty(name) || !isEmpty(mobileNumber) || !isEmpty(aadhaarNumber) || !isEmpty(pan) ||
                    !isEmpty(altContactNumber) || !isEmpty(guardian) || !isEmpty(gender))
                    && isEmpty(tenantId);

    }
}
