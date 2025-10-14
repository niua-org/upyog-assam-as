package org.egov.land.web.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OwnerInfo
 */
@Validated
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OwnerInfoV2 {

    private Long id;

    private String uuid;

    /**
     * Tenant ID of the owner
     */
    @SafeHtml
    @JsonProperty("tenantId")
    private String tenantId = null;

    /**
     * Name of the owner
     */
    @SafeHtml
    @JsonProperty("name")
    private String name = null;

    /**
     * Unique ID of the owner
     */
    @SafeHtml
    @JsonProperty("ownerId")
    private String ownerId = null;

    /**
     * Mobile number of the owner
     */
    @SafeHtml
    @JsonProperty("mobileNumber")
    private String mobileNumber = null;

    /**
     * Gender of the owner
     */
    @SafeHtml
    private String gender;

    /**
     * Mother's name of the owner
     */
    @SafeHtml
    private String motherName = null;

    /**
     * Father's or husband's name of the owner
     */
    @SafeHtml
    @JsonProperty("fatherOrHusbandName")
    private String fatherOrHusbandName = null;

    /**
     * Email ID of the owner
     */
    @Size(max = 128)
    @SafeHtml
    @JsonProperty("emailId")
    private String emailId;

    /**
     * Alternate contact number of the owner
     */
    @Size(max = 50)
    @SafeHtml
    @JsonProperty("altContactNumber")
    private String altContactNumber;

    /**
     * PAN number of the owner
     */
    @Size(max = 10)
    @SafeHtml
    @JsonProperty("pan")
    private String panNumber;

    /**
     * Aadhaar number of the owner
     */
  //  @Pattern(regexp = "^[0-9]{12}$", message = "AdharNumber should be 12 digit number")
    @SafeHtml
    @JsonProperty("aadhaarNumber")
    private String aadhaarNumber;

    /**
     * Permanent address of the owner
     */
    @JsonProperty("permanentAddress")
    private Address permanentAddress;

    /**
     * Correspondence address of the owner
     */
    @JsonProperty("correspondenceAddress")
    private Address correspondenceAddress = null;

    /**
     * Indicates if the owner is the primary owner
     */
    @JsonProperty("isPrimaryOwner")
    private Boolean isPrimaryOwner = null;

    /**
     * Ownership percentage of the owner
     */
    @JsonProperty("ownerShipPercentage")
    private BigDecimal ownerShipPercentage = null;

    /**
     * Type of the owner
     */
    @SafeHtml
    @JsonProperty("ownerType")
    private String ownerType = null;

    /**
     * Status of the owner
     */
    @JsonProperty("status")
    private Boolean status = null;

    /**
     * Institution ID associated with the owner
     */
    @SafeHtml
    @JsonProperty("institutionId")
    private String institutionId = null;

    /**
     * List of documents associated with the owner
     */
    @JsonProperty("documents")
    @Valid
    private List<Document> documents = null;

    /**
     * Relationship details of the owner
     */
    @JsonProperty("relationship")
    private Relationship relationship = null;

    @JsonProperty("active")
    private Boolean active;

    @Size(max = 64)
    @SafeHtml
    @JsonProperty("userName")
    private String userName;

    @Size(max = 50)
    @SafeHtml
    @JsonProperty("type")
    private String type;

    @JsonProperty("roles")
    @Valid
    private List<Role> roles;

    private String dob;

    /**
     * Additional details about the owner
     */
    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    /**
     * Created by user ID
     */
    @Size(max = 64)
    @JsonProperty("createdBy")
    private String createdBy;

    /**
     * Creation timestamp
     */
    @JsonProperty("createdDate")
    private Long createdDate;

    /**
     * Last modified by user ID
     */
    @Size(max = 64)
    @JsonProperty("lastModifiedBy")
    private String lastModifiedBy;

    /**
     * Last modification timestamp
     */
    @JsonProperty("lastModifiedDate")
    private Long lastModifiedDate;

    /**
     * Audit details of the owner
     */
    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;


    public boolean compareWithExistingUser(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OwnerInfo ownerInfo = (OwnerInfo) o;

        return Objects.equals(name, ownerInfo.getName()) &&
                Objects.equals(mobileNumber, ownerInfo.getMobileNumber()) &&
                Objects.equals(gender, ownerInfo.getGender()) &&
                Objects.equals(emailId, ownerInfo.getEmailId()) &&
                Objects.equals(altContactNumber, ownerInfo.getAltContactNumber()) &&
                Objects.equals(panNumber, ownerInfo.getPan()) &&
                Objects.equals(aadhaarNumber, ownerInfo.getAadhaarNumber()) &&
                //TODO: uncomment once done from UI Epoch format not converting properly from UI
             //   Objects.equals(dob, ownerInfo.getDob()) &&
                Objects.equals(fatherOrHusbandName, ownerInfo.getFatherOrHusbandName());
    }

    /*
     * Populates Owner fields from the given User object
     *
     * @param user
     *            User object obtained from user service
     */
    public void addUserWithoutAuditDetail(OwnerInfo user) {
        this.setUuid(user.getUuid());
        this.setId(user.getId());
        this.setUserName(user.getUserName());
        //	this.setPassword(user.getPassword());
        //	this.setSalutation(user.getSalutation());
        this.setName(user.getName());
        this.setGender(user.getGender());
        this.setMobileNumber(user.getMobileNumber());
        this.setEmailId(user.getEmailId());
        this.setAltContactNumber(user.getAltContactNumber());
        this.setPanNumber(user.getPan());
        this.setAadhaarNumber(user.getAadhaarNumber());
        this.setRelationship(user.getRelationship());
        //this.setPermanentAddress(user.getPermanentAddress());
        //	this.setPermanentCity(user.getPermanentCity());
        //	this.setPermanentPincode(user.getPermanentPincode());
        //	this.setCorrespondenceAddress(user.getCorrespondenceAddress());
        //	this.setCorrespondenceCity(user.getCorrespondenceCity());
        //	this.setCorrespondencePincode(user.getCorrespondencePincode());
        this.setActive(user.getActive());
        //this.setDob(user.getDob());
        //	this.setPwdExpiryDate(user.getPwdExpiryDate());
        //	this.setLocale(user.getLocale());
        this.setType(user.getType());
        //this.setAccountLocked(user.getAccountLocked());
        //	this.setRoles(user.getRoles());
        this.setFatherOrHusbandName(user.getFatherOrHusbandName());
        //	this.setBloodGroup(user.getBloodGroup());
        //	this.setIdentificationMark(user.getIdentificationMark());
        //	this.setPhoto(user.getPhoto());
        this.setTenantId(user.getTenantId());
    }
}