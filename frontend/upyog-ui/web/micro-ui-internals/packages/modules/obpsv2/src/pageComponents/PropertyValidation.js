import React, { useState, useEffect } from "react";
import {
  SearchIcon,
  TextInput,
  FormStep,
  StatusTable,
  Row,
  Header,
  Toast,
  Loader,
  SubmitBar
} from "@upyog/digit-ui-react-components";
import Timeline from "../components/Timeline";
import { OBPSV2Services } from "../../../../libraries/src/services/elements/OBPSV2";

const PropertyValidation = ({ t, config, onSelect, formData, searchResult }) => {
  const [propertyID, setPropertyID] = useState(formData?.propertyValidation?.propertyID||searchResult?.additionalDetails?.propertyID);
  const [propertyDetails, setPropertyDetails] = useState(formData?.propertyValidation?.propertyDetails||searchResult?.additionalDetails?.propertyDetails);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true);
  useEffect(() => {
    if (error) {
      const timer = setTimeout(() => {
        setError(null);
      }, 2000);

      return () => clearTimeout(timer);
    }
  }, [error]);
  // Regex to match the TIN pattern: TIN followed by exactly 10 digits
  const isValidPropertyID = (propertyID) => {
    if (!propertyID || typeof propertyID !== 'string') {
      return false;
    }
    const regex = /^([A-Z]{3}\d{10}|\d{16}|\d+\/\d+|[a-zA-Z]+\d+)$/;
    return regex.test(propertyID.replace(/\s/g, ''));
  };

  const displayDetails = (propertyDetails) => {
    return (
      <div>
        <StatusTable>
          <Header style={{ marginTop: "18px", marginBottom: "10px" }}>
            {t("BPA_PROPERTY_DETAILS")}
          </Header>
          <Row label={t("BPA_PROPERTY_ID")} text={propertyDetails?.property}/>
          {Object.entries(propertyDetails.details || {}).map(([key, value]) => (
            <Row
              key={key}
              label={t(`BPA_${key.toUpperCase()}`)}
              text={value || "NA"}
            />
          ))}
          <Row label={t("BPA_PROPERTY_TAXES")} text={propertyDetails?.taxPaid ? "Completed" : "Pending for payment"} />

        </StatusTable>
      </div>
    );
  };

  // Function to handle property details fetching and validation
  const getPropertyDetails = async () => {
    if (!isValidPropertyID(propertyID)) {
      setPropertyDetails(null);
      setError("Invalid Property ID. Please enter a valid TIN number.");
      return;
    }

    setLoading(true); // Set loading state to true when starting the API call
    try {
      const property = await OBPSV2Services.propertyValidate({
        tenantId: tenantId,
        propertyNumber: propertyID,
      });
      if (property.valid) {
        setPropertyDetails(property);
        setError(null);
      } else {
        setPropertyDetails(null);
        setError(property.message);
      }
    } catch (error) {
      setError("Error fetching property details");
    } finally {
      setLoading(false); // Set loading state to false once the request is completed
    }
  };

  const goNext = async() => {
    if (!isValidPropertyID(propertyID)) {
      setPropertyDetails(null);
      setError("Invalid Property ID. Please enter a valid TIN number.");
      return;
    }
    if (propertyDetails && !propertyDetails.taxPaid) {
      setError("Please pay the taxes to proceed");
      return; // Don't proceed if taxes are not paid
    }

    onSelect(config.key, { ...formData[config.key], propertyID, propertyDetails });
  };

  const onSkip = () => onSelect();

  return (
    <React.Fragment>
      <Timeline currentStep={1} flow={"buildingPermit" || "editApplication"} />
      
      <FormStep config={propertyDetails ? config : ""} onSelect={getPropertyDetails} onSkip={onSkip} t={t}>                                                                                                                                                                                                                                                                                                                 
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center"}}>
        <Header>{t("BPA_PROPERTY_VALIDATION")}</Header>
        <div style={{ marginLeft: "auto" }}>
          <SubmitBar label={t("CS_SKIP_AND_CONTINUE")} onSubmit={onSkip} />
        </div>
      </div>
      <div>{t("Please Enter a valid property Number to search")}</div>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <TextInput
            type="text"
            t={t}
            isMandatory={false}
            optionKey="i18nKey"
            name="propertyID"
            value={propertyID}
            onChange={(e) => setPropertyID(e.target.value)}
        />

        <div style={{ marginBottom: "20px" }}>
            <SubmitBar label={t("SEARCH")} onSubmit={getPropertyDetails} />
        </div>
    </div>


        {!loading  && propertyDetails && displayDetails(propertyDetails)}

        {/* Show loader when API is fetching */}
        {loading && (
          <div style={{ position: "relative", minHeight: "80px" }}>
            <Loader />
          </div>
        )}
        {propertyDetails && !error && (
        <div>
          <SubmitBar label ={t("CS_COMMON_NEXT")} onSubmit={goNext}/>
        </div>
    )}
      </FormStep>

      {/* Show e.rror Toast */}
      {error && <Toast error={error} label={error} onClose={() => setError(null)} />}
    </React.Fragment>
  );
};

export default PropertyValidation;
