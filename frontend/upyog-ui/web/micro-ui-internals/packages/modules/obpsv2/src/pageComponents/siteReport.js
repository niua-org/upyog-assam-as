import React, { useState, useEffect } from "react";
import {
  TextInput,
  CardLabel,
  CardSectionHeader,
  DatePicker,
  TextArea,
  CheckBox,
  RadioButtons
} from "@upyog/digit-ui-react-components";
import MultiUploadWrapper from "../../../../react-components/src/molecules/MultiUploadWrapper";
import { useTranslation } from "react-i18next";
import DocumentsPreview from "../../../templates/ApplicationDetails/components/DocumentsPreview";

const siteReport = ({submitReport, onChange, data}) => {
  // Extract BPA data from FormComposer
  const stateId = Digit.ULBService.getStateId();
  const bpaData = data?.bpaData;
  const { t } = useTranslation();
  const [buildingPermitAuthority, setBuildingPermitAuthority] = useState("");
  const [siteQuestions, setSiteQuestions] = useState([]);
  
  const { data: siteQuestionLists } = Digit.Hooks.useEnabledMDMS(
    stateId, 
    "BPA", 
    [
      { name: "CheckList" },
    ],
    {
      select: (data) => {
        return data?.BPA?.CheckList|| [];
      },
    }
  );

  useEffect(() => {
    if (siteQuestionLists && siteQuestionLists.length > 0) {
      siteQuestionLists.map((item)=>{
        setSiteQuestions(item.questions)
      })
    }
  }, [siteQuestionLists]);

  

  const { data: nocLists, isLoading } = Digit.Hooks.useEnabledMDMS(
    stateId, 
    "NOC", 
    [
      { name: "NocTypeMapping" },
      { name: "DocumentTypeMapping"}
      
    ],
    {
      select: (data) => {
        return data?.NOC|| {};
      },
    }
  );
  //const { isLoading: nocDocsLoading, data: nocDocs } = useMDMS("as", "NOC", ["OldNocType"]);
  const nocChecklist = nocLists?.NocTypeMapping?.[0]?.nocs?.filter(item => item.isActive === true) || [];
  const nocDocuments = nocLists?.DocumentTypeMapping
  const civilAviationDocList =
  nocDocuments?.filter(doc => doc.nocType === "CIVIL_AVIATION")
               ?.flatMap(d => d.docTypes) || [];

  const [civilAviationDocs, setCivilAviationDocs] = useState([]);
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
  const path = window.location.pathname.split("/");
  const applicationNumber = path[path.indexOf("application") + 1];
  const [gisData, setGisData] = useState();
  const [plotSizeType, setPlotSizeType] = useState("");
  const [nocList, setNocList] = useState([]);
  const [nocDetails, setNocDetails] = useState({});
  const plotSizeOptions = [
    { code: "ABOVE_300", name: "Plot size above 300 meters" },
    { code: "BELOW_300", name: "Plot size below 300 meters" }
  ];

  const [form, setForm] = useState({
    proposalNo: "",
    submittedOn: "",
    applicantName: "",
    applicantAddress: "",
    architectName: "",
    inspectorName: "",
    revenueVillage: "",
    pattaNo: "",
    dagNo: "",
    plotArea: "",
    proposedUse: "",
    masterPlanZone: "",
    inspectionDate: "",
    mouza: "",
  });

  useEffect(() => {
  if (siteQuestions && siteQuestions.length > 0) {
    const dynamicFields = {};
    const directionFields = ["north", "south", "east", "west"];

    siteQuestions.forEach(question => {
      if (!question.active) return;
      const key = question.fieldKey;
      const isDirection = directionFields.includes(key);

      // Do not touch direction fields at all
      if (!isDirection) {
        dynamicFields[key] = "";
        dynamicFields[`${key}Remarks`] = "";
      }
    });

    setForm(prev => ({ ...prev, ...dynamicFields }));
  }
}, [siteQuestions]);

  // Autofill form with BPA data
  useEffect(() => {
    if (bpaData?.applicationData) {
      const appData = bpaData.applicationData;
      const landInfo = appData?.landInfo || {};
      const owners = landInfo?.owners || [];
      const primaryOwner = owners[0] || {};
      const address = landInfo?.address || {};
      const areaMapping = appData?.areaMapping || {};
      const adjoiningOwners = appData?.additionalDetails?.adjoiningOwners || {};
      const rtpDetails = appData?.rtpDetails || {};
      const architectName = rtpDetails?.rtpName ? rtpDetails.rtpName.split(',')[0] : '';
      setBuildingPermitAuthority(areaMapping?.buildingPermitAuthority || "");
      
      setForm(prev => ({
        ...prev,
        proposalNo: t(appData?.applicationNo) || t(prev.proposalNo),
        applicantName: t(primaryOwner?.name) || t(prev.applicantName),
        applicantAddress: `${address?.houseNo || ''} ${address?.addressLine1 || ''} ${address?.addressLine2 || ''}`.trim() || prev.applicantAddress,
        architectName: t(architectName) || t(prev.architectName),
        masterPlanZone: t(areaMapping?.planningArea) || t(prev.masterPlanZone),
        revenueVillage: t(areaMapping?.revenueVillage) || t(areaMapping?.villageName) || t(prev.revenueVillage),
        pattaNo: t(landInfo?.newPattaNumber) || t(landInfo?.oldPattaNumber) || t(prev.pattaNo),
        dagNo: t(landInfo?.newDagNumber) || t(landInfo?.oldDagNumber) || t(prev.dagNo),
        plotArea: t(landInfo?.totalPlotArea) || t(prev.plotArea),
        proposedUse: t(landInfo?.units?.[0]?.occupancyType) || t(prev.proposedUse),
        mouza: t(areaMapping?.mouza) || t(prev.mouza),
        north: t(adjoiningOwners?.north) || t(prev.north),
        south: t(adjoiningOwners?.south) || t(prev.south),
        east: t(adjoiningOwners?.east) || t(prev.east),
        west: t(adjoiningOwners?.west) || t(prev.west)
      }));
    }
  }, [bpaData]);
  
  const saveSession = (updatedNocDetails, updatedList) => {

    sessionStorage.setItem(
      "SUBMIT_REPORT_DATA",
      JSON.stringify({
        submitReport: form,
        nocList: updatedList || nocList,
        nocDetails: updatedNocDetails
      })
    );
  };

  const getAAIObject = () => {
    const list = nocDetails?.AAI_NOC_DETAILS;
    return list || {
          nocType: "CIVIL_AVIATION",
          siteElevation: "",
          documents: {}
        };
  };
  
  useEffect(() => {
    const prevAAI = getAAIObject();

    let updatedAAI = {
      ...prevAAI,
      nocType: "CIVIL_AVIATION",
      documents: Array.isArray(prevAAI.documents) ? prevAAI.documents : []
    };
  
    if (plotSizeType === "BELOW_300") {
      updatedAAI.CENTER = prevAAI.CENTER || {};
      delete updatedAAI.EAST;
      delete updatedAAI.WEST;
      delete updatedAAI.NORTH;
      delete updatedAAI.SOUTH;
    }
  
    if (plotSizeType === "ABOVE_300") {
      updatedAAI.EAST = prevAAI.EAST || {};
      updatedAAI.WEST = prevAAI.WEST || {};
      updatedAAI.NORTH = prevAAI.NORTH || {};
      updatedAAI.SOUTH = prevAAI.SOUTH || {};
  
      
      delete updatedAAI.CENTER;
    }
  
    const updated = {
      ...nocDetails,
      AAI_NOC_DETAILS: updatedAAI
    };
  
    setNocDetails(updated);
    saveSession(updated);
  }, [plotSizeType]);
  
  useEffect(() => {
    const fetchGIS = async () => {
      try {
        const res = await Digit.OBPSV2Services.gisSearch({
          GisSearchCriteria: {
            applicationNo: applicationNumber,
            tenantId: tenantId,
            status: "SUCCESS"
          }
        });
        if (res?.Gis?.length > 0) {
          setGisData(res);
        }
      } catch (err) {
        console.error("GIS API Error:", err);
      }
    };
  
    fetchGIS();
  }, [applicationNumber,tenantId]);

  useEffect(() => {
    if (plotSizeType === "BELOW_300" && gisData) {
      const prevAAI = getAAIObject();
  
      const updatedAAI = {
        ...prevAAI,
        CENTER: {
          latitude: gisData?.Gis?.[0]?.latitude || "",
          longitude: gisData?.Gis?.[0]?.longitude || ""
        }
      };
  
      const updated = {
        ...nocDetails,
        AAI_NOC_DETAILS: updatedAAI
      };
      setNocDetails(updated);
      saveSession(updated);
    }
  }, [plotSizeType, gisData]);
    
  
  const handleNocFieldChange = (nocType, key, value, parent = null) => {
    const prevAAI = getAAIObject();
    let updatedAAI = { ...prevAAI };
  
    if (!parent && key === "siteElevation") {
      updatedAAI.siteElevation = value;
    }
    else if (plotSizeType === "ABOVE_300" && parent) {
      updatedAAI[parent] = {
        ...updatedAAI[parent],
        [key]: value
      };
    }
    else if (plotSizeType === "BELOW_300") {
      updatedAAI.CENTER = {
        ...updatedAAI.CENTER,
        [key]: value
      };
    }
  
    const updated = {
      ...nocDetails,
      AAI_NOC_DETAILS: updatedAAI
    };
  
    setNocDetails(updated);
    saveSession(updated); 
  };

  const handleDocumentsUpload = (docsArray, docType) => {
    const formattedDocs = docsArray.map(([fileName, fileObj]) => ({
      documentType: docType,
      fileStoreId: fileObj?.fileStoreId?.fileStoreId,
      fileName,
      documentUid: fileObj?.fileStoreId?.fileStoreId,
      additionalDetails:{
        nocType:"CIVIL_AVIATION"
      }
    }));

    setNocDetails(prev => {
      const prevAAI = prev?.AAI_NOC_DETAILS || { nocType: "CIVIL_AVIATION", documents: [] };
    
      const prevDocs = Array.isArray(prevAAI.documents) ? prevAAI.documents : [];
      const filtered = prevDocs.filter(d => d.documentType !== docType);
    
      const merged = [...filtered, ...formattedDocs];
    
      const updatedAAI = {
        ...prevAAI,
        documents: merged
      };
    
      const updated = {
        ...prev,
        AAI_NOC_DETAILS: updatedAAI
      };
    
      setCivilAviationDocs(merged);
      saveSession(updated);
    
      return updated;
    });
    
  };
  
  
  const handleNocChange = (nocType, isChecked) => {
    const updatedList = isChecked
      ? [...nocList, nocType]
      : nocList.filter(item => item !== nocType);
  
    setNocList(updatedList); 
    saveSession(nocDetails, updatedList);
  };

  const handleChange = (key, value) => {
    const updatedForm = { ...form, [key]: value };
    setForm(updatedForm);
  
    saveSession(nocDetails); 
  };

   // Validation function to check if all mandatory fields are filled
  const validateForm = () => {
    // Check mandatory static fields
    if (!form.inspectorName || !form.inspectionDate) {
      return false;
    }

    // Check mandatory dynamic fields from siteQuestions
    if (siteQuestions && siteQuestions.length > 0) {
      const mandatoryQuestions = siteQuestions.filter(q => q.active && q.mandatory);
      for (const question of mandatoryQuestions) {
        if (!form[question.fieldKey]) {
          return false;
        }
      }
    }

    // Check Civil Aviation NOC if selected
    if (nocList.includes("CIVIL_AVIATION")) {
      const aaiDetails = nocDetails?.AAI_NOC_DETAILS;
      
      // Check site elevation
      if (!aaiDetails?.siteElevation) {
        return false;
      }

      // Check plot size type is selected
      if (!plotSizeType) {
        return false;
      }

      // Check coordinates based on plot size
      if (plotSizeType === "ABOVE_300") {
        const directions = ["EAST", "WEST", "NORTH", "SOUTH"];
        for (const dir of directions) {
          if (!aaiDetails?.[dir]?.latitude || !aaiDetails?.[dir]?.longitude) {
            return false;
          }
        }
      } else if (plotSizeType === "BELOW_300") {
        if (!aaiDetails?.CENTER?.latitude || !aaiDetails?.CENTER?.longitude) {
          return false;
        }
      }

      // Check mandatory documents
      const mandatoryDocs = civilAviationDocList.filter(doc => doc.required);
      const uploadedDocs = aaiDetails?.documents || [];
      
      for (const doc of mandatoryDocs) {
        const hasDoc = uploadedDocs.some(d => d.documentType === doc.documentType);
        if (!hasDoc) {
          return false;
        }
      }
    }

    return true;
  };


  const isFormValid = validateForm();

  useEffect(() => {
  sessionStorage.setItem(
    "SITE_REPORT_VALID",
    JSON.stringify(isFormValid)
  );
}, [isFormValid]);
  

  const fieldRowStyle = {
    display: "flex",
    alignItems: "flex-start",
    marginBottom: "12px",
    gap: "150px",
  };


  const inputStyle = {
    flex: "1",
  };

  const labelStyle = {
    flex: "0 0 250px", 
    fontWeight: 500,
    textAlign: "left",
    whiteSpace: "normal", 
    wordBreak: "break-word",
    lineHeight: "1.4", 
  };
  
  // Updated renderFieldWithRemarks to accept question object
  const renderFieldWithRemarks = (question) => {
    const { i18nKey, fieldKey, mandatory,dataType } = question;
    // const isAutoFilled = ['north', 'south', 'east', 'west'].includes(fieldKey) && !!form[fieldKey];
    const isDirectionField = ["north", "south", "east", "west"].includes(fieldKey);
    const isAutoFilled = isDirectionField && !!form[fieldKey];
    
    return (
      <div key={fieldKey} style={{ display: "grid", gridTemplateColumns: "250px 1fr", rowGap: "8px", columnGap: "150px", marginBottom: "16px" }}>
        <CardLabel style={{
          fontWeight: 500,
          textAlign: "left",
          whiteSpace: "nowrap",
          lineHeight: "1.4",
          alignSelf: "start"
        }}>
          {t(i18nKey)}
          {mandatory && <span style={{ color: "red" }}> *</span>}
        </CardLabel>
    
       <React.Fragment>
        {dataType === "textInput" ? (
          <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
            <TextInput
              style={{ width: "100%" }}
              value={form[fieldKey] || ""}
              onChange={(e) => handleChange(fieldKey, e.target.value)}
              disabled={isDirectionField?false:isAutoFilled}
            />
          </div>
        ) : dataType === "textArea" ? (
          <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
            <TextArea
              style={{ width: "100%" }}
              value={form[fieldKey] || ""}
              onChange={(e) => handleChange(fieldKey, e.target.value)}
              maxLength={500}
              placeholder={t("REMARKS")}
              rows={3}
            />
          </div>
        ) : null}
      </React.Fragment>
      </div>
    );
  };
  
  return (
    <React.Fragment>
      <div style={{ marginBottom: "16px", maxWidth: "950px" }}>
        <div className="fieldInspectionWrapper">
          <CardSectionHeader>{t("BPA_SITE_INSPECTION_REPORT")}</CardSectionHeader>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PROPOSAL_APPLICATION_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.proposalNo}
              onChange={(e) => handleChange("proposalNo", e.target.value)}
              disable={!!form.proposalNo}
            />
          </div>

          {/* <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_SUBMITTED_ON")}</CardLabel>
            <DatePicker
              style={inputStyle}
              date={form.submittedOn}
              onChange={(d) => handleChange("submittedOn", d)}
            />
          </div> */}

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_APPLICANT_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.applicantName}
              onChange={(e) => handleChange("applicantName", e.target.value)}
              disable={!!form.applicantName}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_APPLICANT_ADDRESS")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.applicantAddress}
              onChange={(e) => handleChange("applicantAddress", e.target.value)}
              disable={!!form.applicantAddress}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_ARCHITECT_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.architectName}
              onChange={(e) => handleChange("architectName", e.target.value)}
              disable={!!form.architectName}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_INSPECTOR_NAME")}<span style={{ color: "red" }}> *</span></CardLabel>
            <TextInput
              style={inputStyle}
              value={form.inspectorName}
              onChange={(e) => handleChange("inspectorName", e.target.value)}
            />
          </div>

          <CardSectionHeader style={{ marginTop: "20px" }}>
            {t("BPA_LOCATION_OF_LAND")}
          </CardSectionHeader>

           {/* Conditionally show village or revenueVillage field */}
           {buildingPermitAuthority === "GRAM_PANCHAYAT" ? (
            <div style={fieldRowStyle}>
              <CardLabel style={labelStyle}>{t("VILLAGE_NAME")}</CardLabel>
              <TextInput
                style={inputStyle}
                value={form.revenueVillage} // Still using revenueVillage field for village data
                onChange={(e) => handleChange("revenueVillage", e.target.value)}
                disable={!!form.revenueVillage}
              />
            </div>
          ) : (
            <div style={fieldRowStyle}>
              <CardLabel style={labelStyle}>{t("BPA_REVENUE_VILLAGE")}</CardLabel>
              <TextInput
                style={inputStyle}
                value={form.revenueVillage}
                onChange={(e) => handleChange("revenueVillage", e.target.value)}
                disable={!!form.revenueVillage}
              />
            </div>
          )}
          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PATTA_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.pattaNo}
              onChange={(e) => handleChange("pattaNo", e.target.value)}
              disable={!!form.pattaNo}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_DAG_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.dagNo}
              onChange={(e) => handleChange("dagNo", e.target.value)}
              disable={!!form.dagNo}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PLOT_AREA")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.plotArea}
              onChange={(e) => handleChange("plotArea", e.target.value)}
              disable={!!form.plotArea}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PROPOSED_USE_OF_BUILDING")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.proposedUse}
              onChange={(e) => handleChange("proposedUse", e.target.value)}
              disable={!!form.proposedUse}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_MASTER_PLAN_ZONE")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.masterPlanZone}
              onChange={(e) => handleChange("masterPlanZone", e.target.value)}
              disable={!!form.masterPlanZone}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_SITE_INSPECTION_DATE")}<span style={{ color: "red" }}> *</span></CardLabel>
            <DatePicker
              style={inputStyle}
              date={form.inspectionDate}
              onChange={(d) => handleChange("inspectionDate", d)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_MOUZA")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.mouza}
              onChange={(e) => handleChange("mouza", e.target.value)}
              disable={!!form.mouza}
            />
          </div>

         
          <CardSectionHeader style={{ marginTop: "20px" }}>
            {t("BPA_SITE_CHECKLIST")}
          </CardSectionHeader>

          {/* Dynamic rendering based on siteQuestions */}
          {siteQuestions && siteQuestions.length > 0 ? (
            siteQuestions
              .filter(question => question.active)
              .map((question) => renderFieldWithRemarks(question))
          ) : (
            <div>{t("MDMS_DATA_LOADING_ERROR")}</div>
          )}

          <CardSectionHeader style={{ marginTop: "20px" }}>
            {t("BPA_NOC_CHECKLIST")}
          </CardSectionHeader>

        {nocChecklist?.filter(noc => noc.source === "SITE_ENGINEER")?.length > 0 ? (
          nocChecklist
            .filter(noc => noc.source === "SITE_ENGINEER")
            .map((noc, index) => {
              const isChecked = nocList.includes(noc.code);

              return (
                <div key={index} style={{ marginBottom: "16px" }}>
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      borderBottom: "1px solid #ddd",
                      paddingBottom: "8px"
                    }}
                  >
                    <div style={{ maxWidth: "85%" }}>
                      <span style={{ fontWeight: 500 }}>
                        {index + 1}. {noc.name}
                      </span>
                    </div>

                    <CheckBox
                      onChange={(e) => handleNocChange(noc.code, e.target.checked)}
                      styles={{ height: "auto" }}
                      checked={isChecked || false}
                    />
                  </div>

                  {noc.code === "CIVIL_AVIATION" && isChecked && (
                    <div
                      style={{
                        marginTop: "12px",
                        padding: "16px",
                        border: "1px solid #b5b5b5",
                        borderRadius: "8px",
                        background: "#fafafa"
                      }}
                    >
                    <div style={fieldRowStyle}>
                    <CardLabel style={labelStyle}>Site Elevation (in sq meters)</CardLabel>
                    <TextInput
                      placeholder="Enter Site Elevation"
                      value={nocDetails?.AAI_NOC_DETAILS?.siteElevation || ""}
                      onChange={(e) =>
                        handleNocFieldChange("AAI_NOC_DETAILS", "siteElevation", e.target.value)
                      }
                    />
                  </div>

                      <RadioButtons
                        onSelect={(d) => setPlotSizeType(d.code)}
                        selectedOption={plotSizeOptions.find(
                          (option) => option.code === plotSizeType
                        )}
                        optionsKey="name"
                        options={plotSizeOptions}
                      />
                      {plotSizeType === "ABOVE_300" && (
                        <div style={{ marginTop: "16px" }}>
                          <strong>Coordinates</strong>

                          {["EAST", "WEST", "NORTH", "SOUTH"].map((dir) => (
                            <div
                              key={dir}
                              style={{
                                display: "flex",
                                gap: "12px",
                                alignItems: "center",
                                marginTop: "8px"
                              }}
                            >
                              <span style={{ width: "60px" }}>{dir}</span>

                              <TextInput
                                placeholder="Enter Latitude"
                                value={nocDetails?.AAI_NOC_DETAILS?.[dir]?.latitude || ""}
                                onChange={(e) =>
                                  handleNocFieldChange(
                                    "AAI_NOC_DETAILS",
                                    "latitude",
                                    e.target.value,
                                    dir
                                  )
                                }
                              />

                              <TextInput
                                placeholder="Enter Longitude"
                                value={nocDetails?.AAI_NOC_DETAILS?.[dir]?.longitude || ""}
                                onChange={(e) =>
                                  handleNocFieldChange(
                                    "AAI_NOC_DETAILS",
                                    "longitude",
                                    e.target.value,
                                    dir
                                  )
                                }
                              />
                            </div>
                          ))}
                        </div>
                      )}
                      {plotSizeType === "BELOW_300" && (
                        <div style={{ marginTop: "16px" }}>
                          <strong>Coordinates</strong>

                          <div
                            style={{
                              display: "flex",
                              gap: "12px",
                              marginTop: "8px",
                              alignItems: "center"
                            }}
                          >
                            <span style={{ width: "60px" }}>Latitude</span>
                            <TextInput
                              value={
                                nocDetails?.AAI_NOC_DETAILS?.CENTER?.latitude || "" 
                              }
                              onChange={(e) =>
                                handleNocFieldChange(
                                  "AAI_NOC_DETAILS",
                                  "latitude",
                                  e.target.value
                                )
                              }
                            />

                            <span style={{ width: "70px" }}>Longitude</span>
                            <TextInput
                              value={
                                nocDetails?.AAI_NOC_DETAILS?.CENTER?.longitude || ""
                              }
                              onChange={(e) =>
                                handleNocFieldChange(
                                  "AAI_NOC_DETAILS",
                                  "longitude",
                                  e.target.value
                                )
                              }
                            />
                          </div>
                        </div>
                      )}
                      
                      {civilAviationDocList.map((doc, idx) => (
                      <div
                        key={idx}
                        style={{
                          background: "#FAFAFA",
                          padding: "12px",
                          borderRadius: "6px",
                          border: "1px solid #ddd",
                          maxWidth: "600px",
                          marginTop: "16px"
                        }}
                      >
                        <CardLabel>
                          {t(doc.documentType)}{" "}
                          {doc.required ? <span style={{ color: "red" }}>*</span> : null}
                        </CardLabel>

                        <MultiUploadWrapper
                          module="BPA"
                          tenantId={tenantId}
                          t={t}
                          acceptFiles="image/*,.pdf,.jpg,.jpeg,.png"
                          allowedMaxSizeInMB={5}
                          extraStyleName={"OBPS"}
                          allowedFileTypesRegex={/(.*?)(jpg|jpeg|png|image|pdf)$/i}
                          getFormState={(files) => handleDocumentsUpload(files, doc.documentType)}
                        />

                        <div style={{ fontSize: "12px", marginTop: "5px" }}>
                          {t("CS_FILE_SIZE_RESTRICTIONS")}
                        </div>

                        {(nocDetails?.AAI_NOC_DETAILS?.documents || []).filter(d => d.documentType === doc.documentType).length > 0 && (
                          <DocumentsPreview
                            documents={(nocDetails?.AAI_NOC_DETAILS?.documents || []).filter(d => d.documentType === doc.documentType).map((d) => ({
                              title: doc.documentType,
                              fileStoreId: d.fileStoreId,

                            }))}
                          />
                        )}

                      </div>
                    ))}


                    </div>
                  )}
                 
                </div>
              );
            })
            
        ) : (
          <div style={{ marginBottom: "16px" }}>
            {t("NO_SITE_ENGINEER_NOC_AVAILABLE")}
          </div>
        )}

        </div>
      </div>
    </React.Fragment>
  );
}

export default siteReport;