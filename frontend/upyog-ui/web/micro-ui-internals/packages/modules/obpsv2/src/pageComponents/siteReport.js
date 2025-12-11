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
  const bpaData = data?.bpaData;
  const { t } = useTranslation();
  const { data: nocLists, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
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
  const nocChecklist = nocLists?.NocTypeMapping?.[0]?.nocs || [];
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
 
    topographyOfLand: "",
    topographyOfLandRemarks: "",
    earthFillingRequired: "",
    earthFillingRequiredRemarks: "",
    provisionOfExistingRoadSideDrain: "",
    provisionOfExistingRoadSideDrainRemarks: "",
    provisionOfParkingForHighRiseBuilding: "",
    provisionOfParkingForHighRiseBuildingRemarks: "",
    roadWidthInFrontPlot: "",
    roadWidthInFrontPlotRemarks: "",
    roadWidthNearestPlot: "",
    roadWidthNearestPlotRemarks: "",
    roadWidthNarrowestPlot: "",
    roadWidthNarrowestPlotRemarks: "",
    totalAverageRoadWidth: "",
    totalAverageRoadWidthRemarks: "",
    proposedRoadWidth: "",
    proposedRoadWidthRemarks: "",
    descriptionOfAnyOtherRoad: "",
    descriptionOfAnyOtherRoadRemarks: "",
    existingNatureOfApproachRoad: "",
    existingNatureOfApproachRoadRemarks: "",
    approximateLengthDeadEndRoad: "",
    approximateLengthDeadEndRoadRemarks: "",
    roadCondition: "",
    roadConditionRemarks: "",
    proposedUseConformityWithMasterPlan: "",
    proposedUseConformityWithMasterPlanRemarks: "",
    anyWaterBodyExistsInPlot: "",
    anyWaterBodyExistsInPlotRemarks: "",
    distanceOfPlotFromNearestWaterBody: "",
    distanceOfPlotFromNearestWaterBodyRemarks: "",
    areaOfPlotMeasured: "",
    areaOfPlotMeasuredRemarks: "",
    north: "",
    northRemarks: "",
    south: "",
    southRemarks: "",
    east: "",
    eastRemarks: "",
    west: "",
    westRemarks: "",
    commentsOnProposal: "",
    commentsOnProposalRemarks: ""
  });

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
      
      setForm(prev => ({
        ...prev,
        proposalNo: t(appData?.applicationNo) || t(prev.proposalNo),
        applicantName: t(primaryOwner?.name) || t(prev.applicantName),
        applicantAddress: `${address?.houseNo || ''} ${address?.addressLine1 || ''} ${address?.addressLine2 || ''}`.trim() || prev.applicantAddress,
        architectName: t(architectName) || t(prev.architectName),
        masterPlanZone: t(areaMapping?.planningArea) || t(prev.masterPlanZone),
        revenueVillage: t(areaMapping?.revenueVillage) || t(prev.revenueVillage),
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
  const saveSession = (updatedNocDetails) => {
    sessionStorage.setItem(
      "SUBMIT_REPORT_DATA",
      JSON.stringify({
        submitReport: form,
        nocList,
        nocDetails: updatedNocDetails
      })
    );
  };
  useEffect(() => {
    const prevAAI = nocDetails?.AAI_NOC_DETAILS || {};
    const siteElevation = prevAAI.siteElevation ?? "";
    const documents = Array.isArray(prevAAI.documents) ? prevAAI.documents : [];
  
    let updatedAAI = {
      siteElevation,
      documents
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
  
    const updated = { ...nocDetails, AAI_NOC_DETAILS: updatedAAI };
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
      const updated = {
        ...nocDetails,
        AAI_NOC_DETAILS: {
          ...nocDetails.AAI_NOC_DETAILS,
          CENTER: {
            latitude: gisData?.Gis?.[0]?.latitude || "",
            longitude: gisData?.Gis?.[0]?.longitude || ""
          }
        }
      };
      setNocDetails(updated);
  
      saveSession(updated);

    }
  }, [plotSizeType, gisData]);
    
  
  const handleNocFieldChange = (nocType, key, value, parent = null) => {
    let updated = { ...nocDetails };
  
    if (!parent && key === "siteElevation") {
      updated[nocType] = {
        ...updated[nocType],
        [key]: value
      };
    }
    else if (plotSizeType === "ABOVE_300" && parent) {
      updated[nocType] = {
        ...updated[nocType],
        [parent]: {
          ...updated[nocType]?.[parent],
          [key]: value
        }
      };
    }
    else if (plotSizeType === "BELOW_300") {
      updated[nocType] = {
        ...updated[nocType],
        CENTER: {
          ...updated[nocType]?.CENTER,
          [key]: value
        }
      };
    }
  
    setNocDetails(updated);
    saveSession(updated); 
  };
  const handleDocumentsUpload = (docsArray, docType) => {
    const formattedDocs = docsArray.map(([fileName, fileObj]) => ({
      documentType: docType,
      fileStoreId: fileObj?.fileStoreId?.fileStoreId,
      fileName,
      documentUid: fileObj?.fileStoreId?.fileStoreId
    }));
    setNocDetails(prev => {
      const prevAAI = prev?.AAI_NOC_DETAILS || {};
      const prevDocs = Array.isArray(prevAAI.documents) ? prevAAI.documents : [];
      const filtered = prevDocs.filter(d => d.documentType !== docType);
  
      const merged = [...filtered, ...formattedDocs];
  
      const updated = {
        ...prev,
        AAI_NOC_DETAILS: {
          ...prevAAI,
          documents: merged
        }
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
    saveSession(nocDetails);
  };
  const handleChange = (key, value) => {
    const updatedForm = { ...form, [key]: value };
    setForm(updatedForm);
  
    saveSession(nocDetails); 
  };
  

  const fieldRowStyle = {
    display: "flex",
    alignItems: "flex-start",
    marginBottom: "12px",
    gap: "150px",
  };

//   const labelStyle = {
//     flex: "0 0 250px", // fixed width for all labels
//     fontWeight: 500,
//     textAlign: "left",
//     marginTop: "8px",
//   };

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
  
  const renderFieldWithRemarks = (labelKey, fieldKey) => {
    const isAutoFilled = ['north', 'south', 'east', 'west'].includes(fieldKey) && !!form[fieldKey];
    
    return (
      <div style={{ display: "grid", gridTemplateColumns: "250px 1fr", rowGap: "8px", columnGap: "150px", marginBottom: "16px" }}>
        <CardLabel style={{
          fontWeight: 500,
          textAlign: "left",
          whiteSpace: "nowrap",
          lineHeight: "1.4",
          alignSelf: "start"
        }}>
          {t(labelKey)}
        </CardLabel>
    
        <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
          <TextInput
            style={{ width: "100%" }}
            value={form[fieldKey]}
            onChange={(e) => handleChange(fieldKey, e.target.value)}
            disable={isAutoFilled}
          />
          <TextArea
            style={{ width: "100%" }}
            value={form[`${fieldKey}Remarks`]}
            onChange={(e) => handleChange(`${fieldKey}Remarks`, e.target.value)}
            maxLength={500}
            placeholder={t("REMARKS")}
            rows={3}
          />
        </div>
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

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_SUBMITTED_ON")}</CardLabel>
            <DatePicker
              style={inputStyle}
              date={form.submittedOn}
              onChange={(d) => handleChange("submittedOn", d)}
            />
          </div>

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
            <CardLabel style={labelStyle}>{t("BPA_INSPECTOR_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.inspectorName}
              onChange={(e) => handleChange("inspectorName", e.target.value)}
            />
          </div>

          <CardSectionHeader style={{ marginTop: "20px" }}>
            {t("BPA_LOCATION_OF_LAND")}
          </CardSectionHeader>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_REVENUE_VILLAGE")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.revenueVillage}
              onChange={(e) => handleChange("revenueVillage", e.target.value)}
              disable={!!form.revenueVillage}
            />
          </div>

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
            <CardLabel style={labelStyle}>{t("BPA_SITE_INSPECTION_DATE")}</CardLabel>
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

          {renderFieldWithRemarks("TOPOGRAPHY_OF_LAND", "topographyOfLand")}
          {renderFieldWithRemarks("EARTH_FILLING_REQUIRED", "earthFillingRequired")}
          {renderFieldWithRemarks("BPA_PROVISION_OF_EXISTING_ROAD_SIDE_DRAIN", "provisionOfExistingRoadSideDrain")}
          {renderFieldWithRemarks("PROVISION_OF_PARKING_FOR_HIGH_RISE_BUILDING", "provisionOfParkingForHighRiseBuilding")}
          {renderFieldWithRemarks("ROAD_WIDTH_INFRONT_PLOT", "roadWidthInFrontPlot")}
          {renderFieldWithRemarks("ROAD_WIDTH_NEAREST_PLOT", "roadWidthNearestPlot")}
          {renderFieldWithRemarks("ROAD_WIDTH_NARROWEST_PLOT", "roadWidthNarrowestPlot")}
          {renderFieldWithRemarks("TOTAL_AVERAGE_ROAD_WIDTH", "totalAverageRoadWidth")}
          {renderFieldWithRemarks("PROPOSED_ROAD_WIDTH", "proposedRoadWidth")}
          {renderFieldWithRemarks("DESCRIPTION_OF_ANY_OTHER_ROAD", "descriptionOfAnyOtherRoad")}
          {renderFieldWithRemarks("EXISTING_NATURE_OF_APPROACH_ROAD", "existingNatureOfApproachRoad")}
          {renderFieldWithRemarks("APPROXIMATE_LENGTH_DEAD_END_ROAD", "approximateLengthDeadEndRoad")}
          {renderFieldWithRemarks("ROAD_CONDITION", "roadCondition")}
          {renderFieldWithRemarks("WHETHER_THE_PROPOSED_USE_IS_IN_CONFORMITY_WITH_MASTER_PLAN", "proposedUseConformityWithMasterPlan")}
          {renderFieldWithRemarks("WHETHER_ANY_WATER_BODY_EXISTS_IN_PLOT", "anyWaterBodyExistsInPlot")}
          {renderFieldWithRemarks("DISTANCE_OF_PLOT_FROM_NEAREST_WATER_BODY", "distanceOfPlotFromNearestWaterBody")}
          {renderFieldWithRemarks("AREA_OF_PLOT_MEASURED", "areaOfPlotMeasured")}
          {renderFieldWithRemarks("NORTH", "north")}
          {renderFieldWithRemarks("SOUTH", "south")}
          {renderFieldWithRemarks("EAST", "east")}
          {renderFieldWithRemarks("WEST", "west")}
          {renderFieldWithRemarks("COMMENTS_ON_PROPOSAL", "commentsOnProposal")}
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
