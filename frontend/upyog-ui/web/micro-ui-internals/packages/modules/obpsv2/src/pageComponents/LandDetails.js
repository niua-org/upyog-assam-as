import React, { useState, useEffect, Fragment } from "react";
import { FormStep, TextInput, CardLabel, RadioButtons, Dropdown, CheckBox, CardHeader, UploadFile, Toast } from "@upyog/digit-ui-react-components";
import Timeline from "../components/Timeline";

const LandDetails = ({ t, config, onSelect, formData, searchResult }) => {

  // Future Provision Options
  const futureProvisionOptions = [
    { code: "YES", name: "Yes", i18nKey: "BPA_YES" },
    { code: "NO", name: "No", i18nKey: "BPA_NO" }
  ];
  const flow = window.location.href.includes("editApplication") ? "editApplication" : "buildingPermit"

  // TOD Zone Options
  const todZoneOptions = [
    { code: "INTENSE", name: "Intense", i18nKey: "BPA_INTENSE" },
    { code: "TRANSITION_ZONE", name: "Transition Zone", i18nKey: "BPA_TRANSITION_ZONE" }
  ];

  // Registered Technical Person Options
  const rtpOptions = [
    { code: "RTP001", name: "Ranjit +91 9988888890, ranjit@gmail.com", i18nKey: "BPA_RTP001" },
    { code: "RTP002", name: "Ajeet +91 7855552377, ajeet@gmail.com" , i18nKey: "BPA_RTP002"},
    { code: "RTP003", name: "Ani +91 9845454245, ani@gmail.com", i18nKey: "BPA_RTP003" },
    { code: "RTP004", name: "Khalid +91 9845858533, khalid@gmail.com", i18nKey: "BPA_RTP004" },
    { code: "RTP005", name: "Kunal +91 8755258542, kunal@gmail.com", i18nKey: "BPA_RTP005" },
    { code: "RTP006", name: "Anil +91 9907926555, anil@gmail.com", i18nKey: "BPA_RTP006" }
  ];

   // Fetch data from MDMS
   const { data: mdmsData } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "constructionTypes" }, 
      { name: "rtpCategories" },
      { name:"PermissibleZone"}
    ],
    {
      select: (data) => {
        return data?.BPA || {};
      },
    }
  );

  // State for dropdown options
  const [constructionTypeOptions, setConstructionTypeOptions] = useState([]);
  const [rtpCategoryOptions, setRtpCategoryOptions] = useState([]);
  const [occupancyTypeOptions, setOccupancyTypeOptions] = useState([]);
  
    // Initialize districts from MDMS data
    useEffect(() => {
      if (mdmsData?.constructionTypes) {
        const formattedConstructionTypes = mdmsData.constructionTypes.map((constructionTypes) => ({
          code: constructionTypes.code,
          name: constructionTypes.name,
          i18nKey: constructionTypes.code,
        }));
        setConstructionTypeOptions(formattedConstructionTypes);
        
      }
      if(mdmsData?.rtpCategories){
        const formattedRtpCategories = mdmsData.rtpCategories.map((rtpCategories) => ({
          code: rtpCategories.code,
          name: rtpCategories.name,
          i18nKey: rtpCategories.code,
        }));
        setRtpCategoryOptions(formattedRtpCategories);
      }
      if(mdmsData?.PermissibleZone){
        const formattedOccupancyTypes = mdmsData.PermissibleZone.map((occupancyTypes) => ({
          code: occupancyTypes.code,
          name: occupancyTypes.name,
          i18nKey: occupancyTypes.name,
        }));
        setOccupancyTypeOptions(formattedOccupancyTypes);
      }
    }, [mdmsData]);

  // State initialization from formData
  const landData = formData?.land || {};

  // Construction Type
  const [constructionType, setConstructionType] = useState(
    landData?.constructionType || 
    (searchResult?.additionalDetails?.constructionType ? {
      "code": searchResult?.additionalDetails?.constructionType,
      "i18nKey": searchResult?.additionalDetails?.constructionType
    } : "") || 
    ""
  );  

  // Land Record Numbers
  const [oldDagNumber, setOldDagNumber] = useState(landData?.oldDagNumber || searchResult?.landInfo?.oldDagNumber ||"");
  const [newDagNumber, setNewDagNumber] = useState(landData?.newDagNumber || searchResult?.landInfo?.newDagNumber ||"");
  const [oldPattaNumber, setOldPattaNumber] = useState(landData?.oldPattaNumber || searchResult?.landInfo?.oldPattaNumber|| "");
  const [newPattaNumber, setNewPattaNumber] = useState(landData?.newPattaNumber || searchResult?.landInfo?.newPattaNumber || "");
  const [totalPlotArea, setTotalPlotArea] = useState(landData?.totalPlotArea || searchResult?.landInfo?.totalPlotArea ||"");

  // Adjoining Land Owners
  const [northOwner, setNorthOwner] = useState(landData?.adjoiningOwners?.north || searchResult?.additionalDetails?.adjoiningOwners?.north || "");
  const [southOwner, setSouthOwner] = useState(landData?.adjoiningOwners?.south || searchResult?.additionalDetails?.adjoiningOwners?.south|| "");
  const [eastOwner, setEastOwner] = useState(landData?.adjoiningOwners?.east || searchResult?.additionalDetails?.adjoiningOwners?.east || "");
  const [westOwner, setWestOwner] = useState(landData?.adjoiningOwners?.west || searchResult?.additionalDetails?.adjoiningOwners?.west || "");

  // Future Provisions
  
  const [verticalExtension, setVerticalExtension] = useState(landData?.futureProvisions?.verticalExtension || searchResult?.additionalDetails?.futureProvisions?.verticalExtension || "NO");
  const [verticalExtensionArea, setVerticalExtensionArea] = useState(landData?.futureProvisions?.verticalExtensionArea || searchResult?.additionalDetails?.futureProvisions?.verticalExtensionArea || "");
  const [horizontalExtension, setHorizontalExtension] = useState(landData?.futureProvisions?.horizontalExtension || searchResult?.additionalDetails?.futureProvisions?.horizontalExtension || "NO");
  const [horizontalExtensionArea, setHorizontalExtensionArea] = useState(landData?.futureProvisions?.horizontalExtensionArea || searchResult?.additionalDetails?.futureProvisions?.horizontalExtensionArea || "");

  // RTP and Occupancy
  const [rtpCategory, setRtpCategory] = useState(landData?.rtpCategory || (searchResult?.rtpDetails?.rtpCategory ? {"code": searchResult?.rtpDetails?.rtpCategory, "i18nKey": searchResult?.rtpDetails?.rtpCategory} : "") || "");
  const [registeredTechnicalPerson, setRegisteredTechnicalPerson] = useState(landData?.registeredTechnicalPerson || rtpOptions.find(opt => opt.name === searchResult?.rtpDetails?.rtpName) ||"");
  const [occupancyType, setOccupancyType] = useState(landData?.occupancyType || (searchResult?.landInfo?.units?.[0]?.occupancyType ? {"code": searchResult?.landInfo?.units[0].occupancyType, "i18nKey": searchResult?.landInfo?.units[0].occupancyType} : "") || "");

  // TOD Benefits
  const [todBenefits, setTodBenefits] = useState(landData?.todBenefits || futureProvisionOptions.find(opt => opt.code === searchResult?.additionalDetails?.todBenefits)  ||"NO");
  const [todWithTdr, setTodWithTdr] = useState(landData?.todWithTdr || searchResult?.additionalDetails?.todWithTdr  ||  false);
  const [todZone, setTodZone] = useState(landData?.todZone || searchResult?.additionalDetails?.todZone || "");
  const [tdrUsed, setTdrUsed] = useState(landData?.tdrUsed || futureProvisionOptions.find(opt => opt.code === searchResult?.additionalDetails?.tdrUsed)||"NO");
  const [todAcknowledgement, setTodAcknowledgement] = useState(landData?.todAcknowledgement || searchResult?.additionalDetails?.todAcknowledgement||false);

  // File upload states
  const [isUploadingForm36, setIsUploadingForm36] = useState(false);
  const [isUploadingForm39, setIsUploadingForm39] = useState(false);
  const [uploadedForm36Id, setUploadedForm36Id] = useState(landData?.documents?.find(doc => doc.documentType === 'FORM_36') || searchResult?.documents?.find(doc => doc.documentType === 'FORM_36')|| null);
  const [uploadedForm39Id, setUploadedForm39Id] = useState(landData?.documents?.find(doc => doc.documentType === 'FORM_39') || searchResult?.documents?.find(doc => doc.documentType === 'FORM_39')|| null);
  const [form36File, setForm36File] = useState(null);
  const [form39File, setForm39File] = useState(null);
  const [showToast, setShowToast] = useState(null);

  // File selector functions
  const selectForm36File = (e) => {
    setForm36File(e.target.files[0]);
  };

  const selectForm39File = (e) => {
    setForm39File(e.target.files[0]);
  };

  // Handle file delete
  const handleDeleteForm36 = () => {
    setUploadedForm36Id(null);
    setForm36File(null);
  };

  const handleDeleteForm39 = () => {
    setUploadedForm39Id(null);
    setForm39File(null);
  };

  // Form 36 upload effect
  useEffect(() => {
    (async () => {
      if (form36File) {
        if (form36File.size >= 5242880) {
          setShowToast({ error: true, label: "CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED" });
        } else {
          try {
            setUploadedForm36Id(null);
            setIsUploadingForm36(true);
            const response = await Digit.UploadServices.Filestorage("OBPSV2", form36File, Digit.ULBService.getStateId());
            if (response?.data?.files?.length > 0) {
              setUploadedForm36Id({ documentType: "FORM_36", documentUid: response?.data?.files[0]?.fileStoreId,  fileStoreId: response?.data?.files[0]?.fileStoreId});
            } else {
              setShowToast({ error: true, label: "CS_FILE_UPLOAD_ERROR" });
            }
          } catch (err) {
            setShowToast({ error: true, label: "CS_FILE_UPLOAD_ERROR" });
          } finally {
            setIsUploadingForm36(false);
          }
        }
      }
    })();
  }, [form36File]);

  // Form 39 upload effect
  useEffect(() => {
    (async () => {
      if (form39File) {
        if (form39File.size >= 5242880) {
          setShowToast({ error: true, label: "CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED" });
        } else {
          try {
            setUploadedForm39Id(null);
            setIsUploadingForm39(true);
            const response = await Digit.UploadServices.Filestorage("OBPSV2", form39File, Digit.ULBService.getStateId());
            if (response?.data?.files?.length > 0) {
              setUploadedForm39Id({ documentType: "FORM_39", documentUid: response?.data?.files[0]?.fileStoreId,  fileStoreId: response?.data?.files[0]?.fileStoreId});
            } else {
              setShowToast({ error: true, label: "CS_FILE_UPLOAD_ERROR" });
            }
          } catch (err) {
            setShowToast({ error: true, label: "CS_FILE_UPLOAD_ERROR" });
          } finally {
            setIsUploadingForm39(false);
          }
        }
      }
    })();
  }, [form39File]);

  // Go next function
  const goNext = () => {
    const documents = [uploadedForm36Id, uploadedForm39Id].filter(Boolean);

    let landStep = {
      constructionType,
      oldDagNumber,
      newDagNumber,
      oldPattaNumber,
      newPattaNumber,
      totalPlotArea,
      adjoiningOwners: {
        north: northOwner,
        south: southOwner,
        east: eastOwner,
        west: westOwner
      },
      futureProvisions: {
        verticalExtension,
        verticalExtensionArea: verticalExtension?.code === "YES" ? verticalExtensionArea : "",
        horizontalExtension,
        horizontalExtensionArea: horizontalExtension?.code === "YES" ? horizontalExtensionArea : ""
      },
      rtpCategory,
      registeredTechnicalPerson,
      occupancyType,
      todBenefits,
      todWithTdr: todBenefits?.code === "YES" ? todWithTdr : false,
      todZone: todBenefits?.code === "YES" ? todZone : "",
      tdrUsed,
      documents,
      todAcknowledgement: todBenefits?.code === "YES" ? todAcknowledgement : false
    };

    onSelect(config.key, { ...formData[config.key], ...landStep });
  };

  const onSkip = () => onSelect();

  // Toast timeout effect
  useEffect(() => {
    if (showToast) {
      const timer = setTimeout(() => {
        setShowToast(null);
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [showToast]);

  // Loading spinner component
  const LoadingSpinner = () => (
    <div className="loading-spinner" />
  );

  return (
    <React.Fragment>
      <Timeline currentStep={flow==="editApplication"? 3 : 3} flow={flow} />
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={
          !constructionType || 
          !newDagNumber ||
          !newPattaNumber ||
          !totalPlotArea ||
          !northOwner ||
          !rtpCategory || 
          !registeredTechnicalPerson || 
          !occupancyType || 
          (todBenefits?.code === "YES" && !todAcknowledgement) // Form 39 is required
        }
      >
        <div>
          {/* Type of Construction */}
          <CardLabel>{`${t("BPA_CONSTRUCTION_TYPE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={constructionTypeOptions}
            selected={constructionType}
            optionKey="i18nKey"
            select={setConstructionType}
            placeholder={t("BPA_SELECT_CONSTRUCTION_TYPE")}
          />

          {/* Old Dag Number */}
          <CardLabel>{`${t("BPA_OLD_DAG_NUMBER")}`}</CardLabel>
          <TextInput
            t={t}
            type="text"
            name="oldDagNumber"
            placeholder={t("BPA_ENTER_OLD_DAG_NUMBER")}
            value={oldDagNumber}
            onChange={(e) => setOldDagNumber(e.target.value)}
          />

          {/* New Dag Number */}
          <CardLabel>{`${t("BPA_NEW_DAG_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="newDagNumber"
            placeholder={t("BPA_ENTER_NEW_DAG_NUMBER")}
            value={newDagNumber}
            onChange={(e) => setNewDagNumber(e.target.value)}
            ValidationRequired={true}
          />

          {/* Old Patta Number */}
          <CardLabel>{`${t("BPA_OLD_PATTA_NUMBER")}`}</CardLabel>
          <TextInput
            t={t}
            type="text"
            name="oldPattaNumber"
            placeholder={t("BPA_ENTER_OLD_PATTA_NUMBER")}
            value={oldPattaNumber}
            onChange={(e) => setOldPattaNumber(e.target.value)}
          />

          {/* New Patta Number */}
          <CardLabel>{`${t("BPA_NEW_PATTA_NUMBER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="newPattaNumber"
            placeholder={t("BPA_ENTER_NEW_PATTA_NUMBER")}
            value={newPattaNumber}
            onChange={(e) => setNewPattaNumber(e.target.value)}
            ValidationRequired={true}
          />

          {/* Total Plot Area */}
          <CardLabel>
            {`${t("BPA_TOTAL_PLOT_AREA")}`} <span className="check-page-link-button">*</span>
            {totalPlotArea && <span style={{ color: "#666", fontSize: "12px", marginLeft: "8px" }}>({totalPlotArea} sq m)</span>}
            </CardLabel>          
          <TextInput
            t={t}
            type="number"
            name="totalPlotArea"
            placeholder={t("BPA_ENTER_TOTAL_PLOT_AREA")}
            value={totalPlotArea}
            onChange={(e) => setTotalPlotArea(e.target.value.replace(/[^0-9]/g, ""))}
            ValidationRequired={true}
          />

          {/* Name of owners of adjoining land */}
          <CardLabel>{`${t("BPA_ADJOINING_LAND_OWNERS")}`} <span className="check-page-link-button">*</span></CardLabel>
          
          <CardLabel>{`${t("BPA_NORTH_OWNER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="northOwner"
            placeholder={t("BPA_ENTER_NORTH_OWNER")}
            value={northOwner}
            onChange={(e) => setNorthOwner(e.target.value)}
            ValidationRequired={true}
          />

          <CardLabel>{`${t("BPA_SOUTH_OWNER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="southOwner"
            placeholder={t("BPA_ENTER_SOUTH_OWNER")}
            value={southOwner}
            onChange={(e) => setSouthOwner(e.target.value)}
          />

          <CardLabel>{`${t("BPA_EAST_OWNER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="eastOwner"
            placeholder={t("BPA_ENTER_EAST_OWNER")}
            value={eastOwner}
            onChange={(e) => setEastOwner(e.target.value)}
          />

          <CardLabel>{`${t("BPA_WEST_OWNER")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="westOwner"
            placeholder={t("BPA_ENTER_WEST_OWNER")}
            value={westOwner}
            onChange={(e) => setWestOwner(e.target.value)}
          />

          {/* Future Provisions */}
          <CardLabel>{`${t("BPA_FUTURE_PROVISIONS")}`} <span className="check-page-link-button">*</span></CardLabel>
          
          <CardLabel>{`${t("BPA_VERTICAL_EXTENSION")}`}</CardLabel>
          <RadioButtons
            t={t}
            options={futureProvisionOptions}
            optionsKey="i18nKey"
            name="verticalExtension"
            value={verticalExtension}
            selectedOption={verticalExtension}
            onSelect={setVerticalExtension}
            style={{ display: "flex", flexWrap: "wrap", maxHeight: "30px" }}
            innerStyles={{ minWidth: "15%" }}
          />
          
          {verticalExtension?.code === "YES" && (
              <TextInput
                t={t}
                type="text"
                name="verticalExtensionArea"
                placeholder={t("BPA_ENTER_NO_OF_FLOORS")}
                value={verticalExtensionArea}
                onChange={(e) => setVerticalExtensionArea(e.target.value)}
                ValidationRequired={true}
                 {...{ pattern: "^[0-9]+$", title: t("BPA_NUMERIC_ERROR_MESSAGE") }}
              />
          )}

          <CardLabel>{`${t("BPA_HORIZONTAL_EXTENSION")}`}</CardLabel>
          <RadioButtons
            t={t}
            options={futureProvisionOptions}
            optionsKey="i18nKey"
            name="horizontalExtension"
            value={horizontalExtension}
            selectedOption={horizontalExtension}
            onSelect={setHorizontalExtension}
            style={{ display: "flex", flexWrap: "wrap", maxHeight: "30px" }}
            innerStyles={{ minWidth: "15%" }}
          />
          
          {horizontalExtension?.code === "YES" && (
              <TextInput
                t={t}
                type="text"
                name="horizontalExtensionArea"
                placeholder={t("BPA_ENTER_AREA_SQ_M")}
                value={horizontalExtensionArea}
                onChange={(e) => setHorizontalExtensionArea(e.target.value)}
                ValidationRequired={true}
                 {...{ pattern: "^[0-9]+(\.[0-9]{1,2})?$", title: t("BPA_AREA_ERROR_MESSAGE") }}
              />
          )}

          {/* RTP Category */}
          <CardLabel>{`${t("BPA_RTP_CATEGORY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={rtpCategoryOptions}
            selected={rtpCategory}
            optionKey="i18nKey"
            select={setRtpCategory}
            placeholder={t("BPA_SELECT_RTP_CATEGORY")}
          />

          {/* Registered Technical Person */}
          <CardLabel>{`${t("BPA_REGISTERED_TECHNICAL_PERSON")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={rtpOptions}
            selected={registeredTechnicalPerson}
            optionKey="i18nKey"
            select={setRegisteredTechnicalPerson}
            placeholder={t("BPA_SELECT_REGISTERED_TECHNICAL_PERSON")}
          />

          {/* Occupancy Type */}
          <CardLabel>{`${t("BPA_OCCUPANCY_TYPE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={occupancyTypeOptions}
            selected={occupancyType}
            optionKey="i18nKey"
            select={setOccupancyType}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={t("BPA_SELECT_OCCUPANCY_TYPE")}
          />

          {/* TOD Benefits */}
          <CardLabel>{`${t("BPA_TOD_BENEFITS")}`} <span className="check-page-link-button">*</span></CardLabel>
          <RadioButtons
            t={t}
            options={futureProvisionOptions}
            optionsKey="i18nKey"
            name="todBenefits"
            value={todBenefits}
            selectedOption={todBenefits}
            onSelect={(value) => {
              setTodBenefits(value);
              if (value?.code === "NO") {
                setTodWithTdr("");
                setTodZone("");
              }
            }}
            style={{ display: "flex", flexWrap: "wrap", maxHeight: "30px" }}
            innerStyles={{ minWidth: "15%" }}
          />
          
          {todBenefits?.code === "YES" && (
            <>
            <RadioButtons
                t={t}
                options={[
                { code: "WITH_TDR", name: "With TDR (if yes uploading TDR is mandatory)", i18nKey: "BPA_WITH_TDR" },
                { code: "WITHOUT_TDR", name: "Without TDR", i18nKey: "BPA_WITHOUT_TDR" }
                ]}
                optionsKey="i18nKey"
                name="todWithTdr"
                value={todWithTdr}
                selectedOption={todWithTdr}
                onSelect={setTodWithTdr}
            />
            <CardLabel>{`${t("BPA_TOD_ZONE")}`}</CardLabel>
              <RadioButtons
                t={t}
                options={todZoneOptions}
                optionsKey="i18nKey"
                name="todZone"
                value={todZone}
                selectedOption={todZone}
                onSelect={setTodZone}
              />
            </>
          )}

          {/* TDR Usage */}
          <CardLabel>{`${t("BPA_TDR_USED")}`} <span className="check-page-link-button">*</span></CardLabel>
          <RadioButtons
            t={t}
            options={futureProvisionOptions}
            optionsKey="i18nKey"
            name="tdrUsed"
            value={tdrUsed}
            selectedOption={tdrUsed}
            onSelect={setTdrUsed}
            style={{ display: "flex", flexWrap: "wrap", maxHeight: "30px" }}
            innerStyles={{ minWidth: "15%" }}
          />
          
          {/* File Uploads */}
          <CardLabel>{`${t("BPA_FORM_36")}`}</CardLabel>
          <div className="field" style={{ marginBottom: "16px" }}>
            <UploadFile
              onUpload={selectForm36File}
              onDelete={handleDeleteForm36}
              id="form36"
              message={isUploadingForm36 ? (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <LoadingSpinner />
                  <span>Uploading...</span>
                </div>
              ) : uploadedForm36Id ? "1 File Uploaded" : "No File Uploaded"}
              textStyles={{ width: "100%" }}
              inputStyles={{ width: "280px" }}
              accept=".pdf, .jpeg, .jpg, .png"
              buttonType="button"
              error={false}
            />
          </div>
          
          <CardLabel>{`${t("BPA_FORM_39")}`} <span className="check-page-link-button">*</span></CardLabel>
          <div className="field" style={{ marginBottom: "16px" }}>
            <UploadFile
              onUpload={selectForm39File}
              onDelete={handleDeleteForm39}
              id="form39"
              message={isUploadingForm39 ? (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <LoadingSpinner />
                  <span>Uploading...</span>
                </div>
              ) : uploadedForm39Id ? "1 File Uploaded" : "No File Uploaded"}
              textStyles={{ width: "100%" }}
              inputStyles={{ width: "280px" }}
              accept=".pdf, .jpeg, .jpg, .png"
              buttonType="button"
              error={!uploadedForm39Id}
            />
          </div>

          {/* TOD Acknowledgement */}
          {todBenefits?.code === "YES" && (
              <CheckBox
                label={t("BPA_TOD_ACKNOWLEDGEMENT")}
                checked={todAcknowledgement}
                onChange={(e) => setTodAcknowledgement(e.target.checked)}
              />
          )}
        </div>
      </FormStep>

      {/* Toast notification */}
      {showToast && (
        <Toast
          error={showToast.error}
          success={showToast.success}
          label={t(showToast.label)}
          onClose={() => setShowToast(null)}
        />
      )}
    </React.Fragment>
  );
};

export default LandDetails;