import React, { useEffect, useState } from "react";
import { FormStep, TextInput, CardLabel, MobileNumber, RadioButtons } from "@upyog/digit-ui-react-components";
import Timeline from "../components/Timeline";

const ApplicantDetails = ({ t, config, onSelect, formData, searchResult }) => {
  const user = Digit.UserService.getUser().info;
  const flow = window.location.href.includes("editApplication") ? "editApplication" : "buildingPermit"

  // Applicant Fields
  const [applicantName, setApplicantName] = useState(formData?.applicant?.applicantName || searchResult?.landInfo?.owners?.[0]?.name || "");
  const [emailId, setEmail] = useState(formData?.applicant?.emailId || searchResult?.landInfo?.owners?.[0]?.emailId || "");
  const [mobileNumber, setMobileNumber] = useState(formData?.applicant?.mobileNumber ||  searchResult?.landInfo?.owners?.[0]?.mobileNumber || user?.mobileNumber);
  const [alternateNumber, setAltMobileNumber] = useState(formData?.applicant?.alternateNumber || searchResult?.landInfo?.owners?.[0]?.altContactNumber || "");
  const [fatherName, setFatherName] = useState(formData?.applicant?.fatherName || searchResult?.landInfo?.owners?.[0]?.fatherOrHusbandName || "");
  const [motherName, setMotherName] = useState(formData?.applicant?.motherName || searchResult?.landInfo?.owners?.[0]?.motherName ||"");
  const [panCardNumber, setPanCardNumber] = useState(formData?.applicant?.panCardNumber || searchResult?.landInfo?.owners?.[0]?.pan || "");
  const [aadhaarNumber, setAadhaarNumber] = useState(formData?.applicant?.aadhaarNumber || searchResult?.landInfo?.owners?.[0]?.aadhaarNumber ||"");
  const [gender, setGender] = useState(formData?.applicant?.gender || (searchResult?.landInfo?.owners?.[0]?.gender ? { code: searchResult.landInfo.owners[0].gender, name: searchResult.landInfo.owners[0].gender, i18nKey: searchResult.landInfo.owners[0].gender } : ""));
  const [relationship, setRelationship] = useState(formData?.applicant?.relationship || (searchResult?.landInfo?.owners?.[0]?.relationship ? { code: searchResult.landInfo.owners[0].relationship, name: searchResult.landInfo.owners[0].relationship, i18nKey: searchResult.landInfo.owners[0].relationship } : ""));  
  // Options for radio buttons
  const genderOptions = [
    { code: "MALE", i18nKey: "MALE" , name:"MALE"},
    { code: "FEMALE", i18nKey: "FEMALE", name:"FEMALE" },
    { code: "TRANSGENDER", i18nKey: "TRANSGENDER", name:"TRANSGENDER" }
  ];

  const relationshipOptions = [
    { code: "HUSBAND", i18nKey: "HUSBAND", name:"HUSBAND" },
    { code: "FATHER", i18nKey: "FATHER", name:"FATHER" },
  ];

  // Go next
  const goNext = () => {
    let applicantStep = {
      applicantName,
      mobileNumber,
      alternateNumber,
      emailId,
      fatherName,
      motherName,
      panCardNumber,
      aadhaarNumber,
      gender,
      relationship,
    };

    onSelect(config.key, { ...formData[config.key], ...applicantStep });
    
  };

  const onSkip = () => onSelect();

  return (
    <React.Fragment>
      <Timeline currentStep={flow==="editApplication" ? 1 : 1} flow={flow}/>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={
          !applicantName ||
          !mobileNumber ||
          !emailId ||
          !fatherName ||
          !panCardNumber ||
          !aadhaarNumber ||
          !gender ||
          !relationship
        }
      >
        <div>
          {/* Applicant Name */}
          <CardLabel>{`${t("BPA_APPLICANT_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="applicantName"
            placeholder="Enter Applicant Name"
            value={applicantName}
            onChange={(e) => setApplicantName(e.target.value.replace(/[^a-zA-Z\s]/g, ""))}
            ValidationRequired={true}
            {...{ pattern: "^[a-zA-Z ]+$", title: t("BPA_NAME_ERROR_MESSAGE") }}
          />

          {/* Mobile Number */}
          <CardLabel>{`${t("BPA_MOBILE_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <MobileNumber
            value={mobileNumber}
            name="mobileNumber"
            placeholder="Enter Mobile Number"
            onChange={(value) => setMobileNumber(value)}
            {...{ pattern: "[6-9]{1}[0-9]{9}", title: t("CORE_COMMON_APPLICANT_MOBILE_NUMBER_INVALID") }}
          />

          {/* Alternate Mobile Number */}
          <CardLabel>{`${t("BPA_ALT_MOBILE_NUMBER")}`}</CardLabel>
          <MobileNumber
            value={alternateNumber}
            name="alternateNumber"
            placeholder="Enter Alternate Mobile Number"
            onChange={(value) => setAltMobileNumber(value)}
            {...{ required: false, pattern: "[6-9]{1}[0-9]{9}", title: t("CORE_COMMON_APPLICANT_MOBILE_NUMBER_INVALID") }}
          />

          {/* Email */}
          <CardLabel>{`${t("BPA_EMAIL_ID")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="email"
            name="emailId"
            value={emailId}
            placeholder="Enter Email Id"
            onChange={(e) => setEmail(e.target.value)}
            ValidationRequired={true}
            {...{ pattern: "[A-Za-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}$", title: t("BPA_EMAIL_ERROR_MESSAGE") }}
          />
          {/* Gender */}
          <CardLabel>{`${t("BPA_GENDER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <RadioButtons
            t={t}
            options={genderOptions}
            style={{ display: "flex", flexWrap: "wrap", maxHeight: "20px" }}
            innerStyles={{ minWidth: "20%" }}
            optionsKey="i18nKey"
            name="gender"
            value={gender}
            selectedOption={gender}
            onSelect={setGender}
            labelKey="i18nKey"
          />
          {/* Guardian */}
          <CardLabel>{`${t("BPA_GUARDIAN")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="fatherName"
            value={fatherName}
            placeholder="Enter Guardian Name"
            onChange={(e) => setFatherName(e.target.value.replace(/[^a-zA-Z\s]/g, ""))}
            ValidationRequired={true}
          />

          {/* Relationship */}
          <CardLabel>{`${t("BPA_RELATIONSHIP")}`} <span className="check-page-link-button">*</span></CardLabel>
          <RadioButtons
            t={t}
            options={relationshipOptions}
            style={{ display: "flex", flexWrap: "wrap", maxHeight: "20px" }}
            innerStyles={{ minWidth: "20%" }}
            optionsKey="i18nKey"
            name="relationship"
            value={relationship}
            selectedOption={relationship}
            onSelect={setRelationship}
            labelKey="i18nKey"
          />

          {/* Mother's Name */}
          <CardLabel>{`${t("BPA_MOTHER_NAME")}`}</CardLabel>
          <TextInput
            t={t}
            type="text"
            name="motherName"
            value={motherName}
            placeholder="Enter Mother's Name"
            onChange={(e) => setMotherName(e.target.value.replace(/[^a-zA-Z\s]/g, ""))}
          />

          {/* PAN Card Number */}
          <CardLabel>{`${t("BPA_PAN_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="panCardNumber"
            value={panCardNumber}
            placeholder="Enter PAN Card Number"
            onChange={(e) => {
              const input = e.target.value.toUpperCase();
              if (input.length <= 10) {
                setPanCardNumber(input);
              }
            }}
            ValidationRequired={true}
            minLength={10}
            maxLength={10}
            {...{ pattern: "[A-Z]{5}[0-9]{4}[A-Z]{1}", title: t("BPA_PAN_ERROR_MESSAGE") }}
          />

          {/* Aadhaar Number */}
          <CardLabel>{`${t("BPA_AADHAAR_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="aadhaarNumber"
            value={aadhaarNumber}
            placeholder="Enter Aadhaar Card Number"
            onChange={(e) => {
              const input = e.target.value.replace(/[^0-9]/g, "");
              if (input.length <= 12) {
                setAadhaarNumber(input);
              }
            }}
            ValidationRequired={true}
            minLength={12}
            maxLength={12}
            {...{ pattern: "[0-9]{12}", title: t("BPA_AADHAAR_ERROR_MESSAGE") }}
          />
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default ApplicantDetails;