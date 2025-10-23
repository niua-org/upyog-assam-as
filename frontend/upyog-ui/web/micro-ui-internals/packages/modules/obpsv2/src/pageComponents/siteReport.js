import React, { useState } from "react";
import {
  TextInput,
  CardLabel,
  CardSectionHeader,
  DatePicker,
  TextArea
} from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import DocumentsPreview from "../../../templates/ApplicationDetails/components/DocumentsPreview";

const siteReport = ({submitReport, onChange}) => {
  const { t } = useTranslation();
  const [form, setForm] = useState({
    proposalNo: "",
    submittedOn: "",
    applicantName: "",
    applicantAddress: "",
    architectName: "",
    architectAddress: "",
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
  const handleChange = (key, value) => {
    const updated = { ...form, [key]: value };
    setForm(updated);
    const updatedSubmitReport = {
        ...submitReport,
        bpa_details: {
          ...(submitReport?.bpa_details || {}),
          additionalDetails: {
            ...(submitReport?.bpa_details?.additionalDetails || {}),
            ...updated,
          },
        },
      };
    if (onChange) onChange(updatedSubmitReport);
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
  
  const renderFieldWithRemarks = (labelKey, fieldKey) => (
    <div style={{ display: "grid", gridTemplateColumns: "250px 1fr", rowGap: "8px", columnGap: "150px", marginBottom: "16px" }}>
      <CardLabel style={{
        fontWeight: 500,
        textAlign: "left",
        whiteSpace: "normal",
        wordBreak: "break-word",
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
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_APPLICANT_ADDRESS")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.applicantAddress}
              onChange={(e) => handleChange("applicantAddress", e.target.value)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_ARCHITECT_NAME")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.architectName}
              onChange={(e) => handleChange("architectName", e.target.value)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_ARCHITECT_ADDRESS")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.architectAddress}
              onChange={(e) => handleChange("architectAddress", e.target.value)}
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
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PATTA_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.pattaNo}
              onChange={(e) => handleChange("pattaNo", e.target.value)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_DAG_NO")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.dagNo}
              onChange={(e) => handleChange("dagNo", e.target.value)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PLOT_AREA")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.plotArea}
              onChange={(e) => handleChange("plotArea", e.target.value)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_PROPOSED_USE_OF_BUILDING")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.proposedUse}
              onChange={(e) => handleChange("proposedUse", e.target.value)}
            />
          </div>

          <div style={fieldRowStyle}>
            <CardLabel style={labelStyle}>{t("BPA_MASTER_PLAN_ZONE")}</CardLabel>
            <TextInput
              style={inputStyle}
              value={form.masterPlanZone}
              onChange={(e) => handleChange("masterPlanZone", e.target.value)}
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

        </div>
      </div>
    </React.Fragment>
  );
}

export default siteReport;
