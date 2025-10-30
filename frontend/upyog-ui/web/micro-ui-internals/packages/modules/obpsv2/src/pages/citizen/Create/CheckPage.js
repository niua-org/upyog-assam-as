import React, { useState, Fragment } from "react";
import {
  Card,
  CardHeader,
  CardSubHeader,
  StatusTable,
  Row,
  CheckBox,
  SubmitBar,
  LinkButton,
  EditIcon,
  CardLabel,
  Header,
  Table,
} from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { checkForNA, getOrderDocuments } from "../../../utils";
import DocumentsPreview from "../../../../../templates/ApplicationDetails/components/DocumentsPreview";
import Timeline from "../../../components/Timeline";
import FormAcknowledgement from "./FormAcknowledgement";
import useScrutinyFormDetails from "../../../../../../libraries/src/hooks/obpsv2/useScrutinyFormDetails";
const ActionButton = ({ jumpTo }) => {
  const history = useHistory();

  function routeTo() {
    history.push(jumpTo);
  }

  return (
    <LinkButton
      label={
        <EditIcon
          style={{ marginTop: "-5px", float: "right", position: "relative" }}
        />
      }
      className="check-page-link-button"
      onClick={routeTo}
    />
  );
};

const CheckPage = ({ onSubmit, value = {} }) => {
  const { t } = useTranslation();
  const [agree, setAgree] = useState(false);
  const [expanded, setExpanded] = useState({
    form22: false,
    form23A: false,
    form23B: false,
  });
  const toggleExpanded = (key) => {
    setExpanded((prev) => ({ ...prev, [key]: !prev[key] }));
  };
  // let form22 = null, form23A = null, form23B = null, loading = false;

  //   const edcrNumber = response?.bpa?.[0]?.edcrNumber;
  //   const stateId = Digit.ULBService.getStateId();

  //   if (edcrNumber) {
  //     ({ form22, form23A, form23B, loading } = useScrutinyFormDetails("DCR102025WDMEL", "assam"));
  //   }
  const { form22, form23A, form23B, loading } = useScrutinyFormDetails("DCR102025WDMEL", "assam");
  const {areaMapping={}, applicant = {}, address = {}, land = {}, documents = {} } = value;
  const flow = window.location.href.includes("editApplication") ? "editApplication" : "buildingPermit"
  const setDeclarationHandler = () => {
    setAgree(!agree);
  };
  const { data: storeData } = Digit.Hooks.useStore.getInitData();
  const { tenants } = storeData || {};
  const cellStyle = {
    border: "1px solid #ccc",
    padding: "8px",
    textAlign: "left",
    fontSize: "14px",
  };
const handleDownloadPdf = async (formType) => {
  try {
    let formData = null;
    let tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
    const tenantInfo  = tenants.find((tenant) => tenant.code === tenantId);
    switch (formType) {
      case "FORM_22":
        formData = value?.form||form22;
        break;
      case "FORM_23A":
        formData = value?.form23A || form23A;
        break;
      case "FORM_23B":
        formData = value?.form23B||form23B;
        break;
      default:
        formData = null;
    }

    if (!formData) {
      console.error("No data found for", formType);
      return;
    }

    const acknowledgementData = await FormAcknowledgement(
      { formType, formData },
      tenantInfo,
      t
    );

    Digit.Utils.pdf.generate(acknowledgementData);
  } catch (err) {
    console.error("PDF download failed for", formType, err);
  }
};


  
  let routeLink = window.location.href.includes("editApplication") ? `/upyog-ui/citizen/obpsv2/editApplication`:"";
  
  function routeTo(jumpTo) {
    location.href=jumpTo;
  }
  let improvedDoc = [];
  
  documents?.documents?.map((appDoc) => {
    improvedDoc.push({ ...appDoc, module: "BPA" });
  });
  const {
    data: pdfDetails,
    isLoading: pdfLoading,
    error,
  } = Digit.Hooks.useDocumentSearch(improvedDoc, {
    enabled: improvedDoc?.length > 0 ? true : false,
  });

  let applicationDocs = [];
  if (pdfDetails?.pdfFiles?.length > 0) {
    pdfDetails?.pdfFiles?.map((pdfAppDoc) => {
      if (pdfAppDoc?.module == "BPA") applicationDocs.push(pdfAppDoc);
    });
  }

  const getDetailsRow = (formDetails) => {
    if (!formDetails) return null;
  
    const renderValue = (val) => {
      if (val === null || val === undefined) return "NA";
      if (typeof val === "string" && val.trim() === "") return "NA";
      return val?.toString().trim() || "NA";
    };
  
    const renderTable = (data, key) => {
      if (!Array.isArray(data) || data.length === 0) return null;
  
      const headers = Object.keys(data[0] || {});
  
      return (
        <div key={key} style={{ marginTop: "20px" }}>
          <h4>{t(key.toUpperCase())}</h4>
          <table
            style={{
              borderCollapse: "collapse",
              border: "1px solid #ccc",
              tableLayout: "auto",
              width: "100%",
              fontSize: "12px",
              lineHeight: "1.5",
            }}
          >
            <thead>
              <tr style={{ backgroundColor: "#f0f0f0" }}>
                <th style={cellStyle}>Sl. No.</th>
                {headers.map((header) => (
                  <th key={header} style={cellStyle}>
                    {t(header.toUpperCase())}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.map((row, idx) => (
                <tr key={idx}>
                  <td style={cellStyle}>{idx + 1}</td>
                  {headers.map((field) => (
                    <td key={field} style={cellStyle}>
                      {renderValue(row[field])}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    };
  
    return (
      <div>
        <StatusTable>
          {Object.entries(formDetails)
            .filter(([key]) => key !== "scrutinyDetails")
            .map(([key, value], index) => {
              if (
                typeof value === "string" ||
                typeof value === "number" ||
                typeof value === "boolean"
              ) {
                return (
                  <Row
                    key={index}
                    label={t(key.toUpperCase())}
                    text={renderValue(value)}
                  />
                );
              }
  
              if (Array.isArray(value)) {
                return renderTable(value, key);
              }
  
              return null;
            })}
        </StatusTable>
      </div>
    );
  };
  

  return (
    <React.Fragment>
       <Timeline currentStep={flow === "editApplication" ? 8 : 4} flow={flow}/>
      <Card>
        <CardHeader>{t("BPA_SUMMARY_PAGE")}</CardHeader>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <CardSubHeader style={{ fontSize: "24px", marginTop: "24px" }}>
            {t("BPA_AREA_MAPPING")}
          </CardSubHeader>
          <ActionButton
            jumpTo={`/upyog-ui/citizen/obpsv2/building-permit/area-mapping`}
          />
        </div>

        <StatusTable>
          <Row
            label={t("DISTRICT")}
            text={checkForNA(t(areaMapping?.district?.code))}
          />
          <Row
            label={t("PLANNING_AREA")}
            text={checkForNA(t(areaMapping?.planningArea?.code))}
          />
          <Row
            label={t("PP_AUTHORITY")}
            text={checkForNA(t(areaMapping?.ppAuthority?.code))}
          />
          <Row
            label={t("BP_AUTHORITY")}
            text={checkForNA(t(areaMapping?.bpAuthority?.code))}
          />
          <Row
            label={t(areaMapping?.bpAuthority?.code + " NAME")}
            text={checkForNA(t(areaMapping?.concernedAuthority?.code))}
          />
          {areaMapping?.bpAuthority?.code === "MUNICIPAL_BOARD" && (
            <>
              <Row
                label={t("WARD")}
                text={checkForNA(t(areaMapping?.ward?.code))}
              />
              <Row
                label={t("REVENUE_VILLAGE")}
                text={checkForNA(t(areaMapping?.revenueVillage?.code))}
              />
            </>
          )}
          {areaMapping?.bpAuthority?.code === "GRAM_PANCHAYAT" && (
            <Row
              label={t("VILLAGE_NAME")}
              text={checkForNA(t(areaMapping?.villageName?.code))}
            />
          )}
          <Row
            label={t("MOUZA")}
            text={checkForNA(areaMapping?.mouza)}
          />
        </StatusTable>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <CardSubHeader style={{ fontSize: "24px", marginTop: "24px" }}>
            {t("BPA_APPLICANT_DETAILS")}
          </CardSubHeader>
          <ActionButton
            jumpTo={`/upyog-ui/citizen/obpsv2/building-permit/applicant-details`}
          />
        </div>

        <StatusTable>
          <Row
            label={t("BPA_APPLICANT_NAME")}
            text={checkForNA(applicant?.applicantName)}
          />
          <Row
            label={t("BPA_MOBILE_NO")}
            text={checkForNA(applicant?.mobileNumber)}
          />
          <Row
            label={t("BPA_ALT_MOBILE_NO")}
            text={checkForNA(applicant?.alternateNumber)}
          />
          <Row
            label={t("BPA_EMAIL_ID")}
            text={checkForNA(applicant?.emailId)}
          />
          <Row
            label={t("BPA_GENDER")}
            text={checkForNA(t(applicant?.gender?.code))}
          />
          <Row
            label={t("BPA_GUARDIAN")}
            text={checkForNA(applicant?.fatherName)}
          />
           <Row
            label={t("BPA_RELATIONSHIP")}
            text={checkForNA(t(applicant?.relationship?.code))}
          />
          <Row
            label={t("BPA_MOTHER_NAME")}
            text={checkForNA(applicant?.motherName)}
          />
          <Row
            label={t("BPA_PAN_CARD")}
            text={checkForNA(applicant?.panCardNumber)}
          />
          <Row
            label={t("BPA_AADHAAR_CARD")}
            text={checkForNA(applicant?.aadhaarNumber)}
          />
        </StatusTable>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "24px",
          }}
        >
          <CardSubHeader style={{ fontSize: "24px" }}>
            {t("BPA_ADDRESS_DETAILS")}
          </CardSubHeader>
          <ActionButton
            jumpTo={`/upyog-ui/citizen/obpsv2/building-permit/address-details`}
          />
        </div>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "16px",
          }}
        >
          <CardSubHeader style={{ fontSize: "20px" }}>
            {t("BPA_SITE_ADDRESS")}
          </CardSubHeader>
        </div>

        <StatusTable>
          <Row
            label={t("BPA_HOUSE_NO")}
            text={checkForNA(address?.permanent?.houseNo)}
          />
          <Row
            label={t("BPA_ADDRESS_LINE_1")}
            text={checkForNA(address?.permanent?.addressLine1)}
          />
          <Row
            label={t("BPA_ADDRESS_LINE_2")}
            text={checkForNA(address?.permanent?.addressLine2)}
          />
          <Row
            label={t("BPA_DISTRICT")}
            text={checkForNA(t(address?.permanent?.district?.code))}
          />
          <Row
            label={t("BPA_CITY_VILLAGE")}
            text={checkForNA(t(address?.permanent?.city?.code))}
          />
          <Row
            label={t("BPA_STATE")}
            text={checkForNA(t(address?.permanent?.state?.code))}
          />
          <Row
            label={t("BPA_PIN_CODE")}
            text={checkForNA(address?.permanent?.pincode)}
          />
        </StatusTable>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "16px",
          }}
        >
          <CardSubHeader style={{ fontSize: "20px" }}>
            {t("BPA_CORRESPONDENCE_ADDRESS")}
          </CardSubHeader>
        </div>

        {address?.sameAsPermanent ? (
          <div style={{ marginTop: "16px" }}>
            <CheckBox
              label={t("BPA_SAME_AS_SITE_ADDRESS")}
              checked={true}
              disabled={true}
            />
          </div>
        ) : (
          <StatusTable style={{ marginTop: "16px" }}>
            <Row
              label={t("BPA_HOUSE_NO")}
              text={checkForNA(address?.correspondence?.houseNo)}
            />
            <Row
              label={t("BPA_ADDRESS_LINE_1")}
              text={checkForNA(address?.correspondence?.addressLine1)}
            />
            <Row
              label={t("BPA_ADDRESS_LINE_2")}
              text={checkForNA(address?.correspondence?.addressLine2)}
            />
            <Row
              label={t("BPA_DISTRICT")}
              text={checkForNA(t(address?.correspondence?.district?.code))}
            />
            <Row
              label={t("BPA_CITY_VILLAGE")}
              text={checkForNA(t(address?.correspondence?.city?.code))}
            />
            <Row
              label={t("BPA_STATE")}
              text={checkForNA(t(address?.correspondence?.state?.code))}
            />
            <Row
              label={t("BPA_PIN_CODE")}
              text={checkForNA(address?.correspondence?.pincode)}
            />
          </StatusTable>
        )}

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "24px",
          }}
        >
          <CardSubHeader style={{ fontSize: "24px" }}>
            {t("BPA_LAND_DETAILS")}
          </CardSubHeader>
          <ActionButton
            jumpTo={`/upyog-ui/citizen/obpsv2/building-permit/land-details`}
          />
        </div>

        <StatusTable>
          <Row
            label={t("BPA_CONSTRUCTION_TYPE")}
            text={checkForNA(t(land?.constructionType?.code))}
          />
          <Row
            label={t("BPA_OLD_DAG_NUMBER")}
            text={checkForNA(land?.oldDagNumber)}
          />
          <Row
            label={t("BPA_NEW_DAG_NUMBER")}
            text={checkForNA(land?.newDagNumber)}
          />
          <Row
            label={t("BPA_OLD_PATTA_NUMBER")}
            text={checkForNA(land?.oldPattaNumber)}
          />
          <Row
            label={t("BPA_NEW_PATTA_NUMBER")}
            text={checkForNA(land?.newPattaNumber)}
          />
          <Row
            label={t("BPA_TOTAL_PLOT_AREA")}
            text={land?.totalPlotArea ? `${land.totalPlotArea} sq. m.` : ""}
          />
        </StatusTable>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "16px",
          }}
        >
          <CardSubHeader style={{ fontSize: "20px" }}>
            {t("BPA_ADJOINING_LAND_OWNERS")}
          </CardSubHeader>
        </div>

        <StatusTable>
          <Row
            label={t("BPA_NORTH")}
            text={checkForNA(land?.adjoiningOwners?.north)}
          />
          <Row
            label={t("BPA_SOUTH")}
            text={checkForNA(land?.adjoiningOwners?.south)}
          />
          <Row
            label={t("BPA_EAST")}
            text={checkForNA(land?.adjoiningOwners?.east)}
          />
          <Row
            label={t("BPA_WEST")}
            text={checkForNA(land?.adjoiningOwners?.west)}
          />
        </StatusTable>

        <div
          style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginTop: "16px",
          }}
        >
          <CardSubHeader style={{ fontSize: "20px" }}>
            {t("BPA_FUTURE_PROVISIONS")}
          </CardSubHeader>
        </div>

        <StatusTable>
          <Row
            label={t("BPA_VERTICAL_EXTENSION")}
            text={checkForNA(t(land?.futureProvisions?.verticalExtension?.code))}
          />
          {land?.futureProvisions?.verticalExtension?.code === "YES" && (
            <Row
              label={t("BPA_VERTICAL_EXTENSION_AREA")}
              text={`${land?.futureProvisions?.verticalExtensionArea} floors`}
            />
          )}
          <Row
            label={t("BPA_HORIZONTAL_EXTENSION")}
            text={checkForNA(t(land?.futureProvisions?.horizontalExtension?.code))}
          />
          {land?.futureProvisions?.horizontalExtension?.code === "YES" && (
            <Row
              label={t("BPA_HORIZONTAL_EXTENSION_AREA")}
              text={`${land?.futureProvisions?.horizontalExtensionArea} sq m`}
            />
          )}
        </StatusTable>

        <StatusTable style={{ marginTop: "16px" }}>
          <Row
            label={t("BPA_RTP_CATEGORY")}
            text={checkForNA(t(land?.rtpCategory?.code))}
          />
          <Row
            label={t("BPA_REGISTERED_TECHNICAL_PERSON")}
            text={checkForNA(t(land?.registeredTechnicalPerson?.code))}
          />
          <Row
            label={t("BPA_OCCUPANCY_TYPE")}
            text={checkForNA(t(land?.occupancyType?.code))}
          />
          <Row
            label={t("BPA_TOD_BENEFITS")}
            text={checkForNA(t(land?.todBenefits?.code))}
          />
          {land?.todBenefits?.code === "YES" && (
            <>
              <Row
                label={t("BPA_TOD_WITH_TDR")}
                text={checkForNA(t(land?.todWithTdr?.code))}
              />
              <Row
                label={t("BPA_TOD_ZONE")}
                text={checkForNA(t(land?.todZone?.code))}
              />
            </>
          )}
          {land?.documents && land.documents.length > 0 && (
            <div style={{ marginTop: "16px" }}>
              <DocumentsPreview
                documents={[{
                  values: land.documents.map(doc => ({
                    title: doc.documentType === "FORM_36" ? "Form 36" : "Form 39",
                    url: `/filestore/v1/files/id?tenantId=${Digit.ULBService.getCurrentTenantId()}&fileStoreId=${doc.fileStoreId}`,
                    documentType: doc.documentType
                  }))
                }]}
                svgStyles={{}}
                isSendBackFlow={false}
                isHrLine={true}
                titleStyles={{
                  fontSize: "16px",
                  lineHeight: "20px",
                  fontWeight: 600,
                  marginBottom: "8px",
                }}
              />
            </div>
          )}
        </StatusTable>
        {window.location.href.includes("editApplication") ? (
          <React.Fragment>
            <StatusTable>
              <CardLabel style={{fontSize: "18px", marginTop: "24px", fontWeight: "bold"}}>{t("BPA_DOCUMENT_DETAILS_LABEL")}</CardLabel>
              <LinkButton
                label={
                  <EditIcon
                    style={{
                      marginTop: "-10px",
                      float: "right",
                      position: "relative",
                      bottom: "32px",
                    }}
                  />
                }
                style={{ width: "100px", display: "inline" }}
                onClick={() => routeTo(`${routeLink}/document-details`)}
              />
              {
              <DocumentsPreview
              documents={getOrderDocuments(applicationDocs)}
              svgStyles={{}}
              isSendBackFlow={false}
              isHrLine={true}
              hideTitle={true}
              titleStyles={{
                fontSize: "18px",
                lineHeight: "24px",
                fontWeight: 700,
                marginBottom: "10px",
              }}
            />
              }
            </StatusTable>
            
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <CardLabel style={{ fontSize: "20px", marginTop: "24px", fontWeight: "bold" }}>{t("FORM_22_DETAILS")}</CardLabel>
                {!expanded.form22 && (
                  <LinkButton
                    label={t("VIEW_DETAILS")}
                    onClick={() => toggleExpanded("form22")}
                    style={{ marginRight: "1rem" }}
                  />
                )}
                <LinkButton
                  label={
                    <div className="response-download-button">
                      <span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="#a82227">
                          <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" />
                        </svg>
                      </span>
                      <span className="download-button">{t("CS_COMMON_DOWNLOAD")}</span>
                    </div>
                  }
                  onClick={() => handleDownloadPdf("FORM_22")}
                  className="w-full"
                />

              </div>

              {expanded.form22 && (
                <React.Fragment>
                  <StatusTable>
                  {getDetailsRow(value?.form||form22)}

                  <div style={{ marginTop: "1rem" }}>
                    <LinkButton
                      label={t("COLLAPSE")}
                      onClick={() => toggleExpanded("form22")}
                    />
                  </div>
                  </StatusTable>
                </React.Fragment>
              )}
           

            <StatusTable>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <CardLabel style={{ fontSize: "20px", marginTop: "24px", fontWeight: "bold" }}>{t("FORM_23A_DETAILS")}</CardLabel>
                {!expanded.form23A && (
                  <LinkButton
                    label={t("VIEW_DETAILS")}
                    onClick={() => toggleExpanded("form23A")}
                    style={{ marginRight: "1rem" }}
                  />
                )}
                <LinkButton
                  label={
                    <div className="response-download-button">
                      <span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="#a82227">
                          <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" />
                        </svg>
                      </span>
                      <span className="download-button">{t("CS_COMMON_DOWNLOAD")}</span>
                    </div>
                  }
                  onClick={() => handleDownloadPdf("FORM_23A")}
                  className="w-full"
                />

              </div>

              {expanded.form23A && (
                <React.Fragment>
                  {getDetailsRow(value?.form23A||form23A)}

                  <div style={{ marginTop: "1rem" }}>
                    <LinkButton
                      label={t("COLLAPSE")}
                      onClick={() => toggleExpanded("form23A")}
                    />
                  </div>
                </React.Fragment>
              )}
            </StatusTable>
            <StatusTable>
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <CardLabel style={{ fontSize: "20px", marginTop: "24px", fontWeight: "bold" }}>{t("FORM_23B_DETAILS")}</CardLabel>
                {!expanded.form23B && (
                  <LinkButton
                    label={t("VIEW_DETAILS")}
                    onClick={() => toggleExpanded("form23B")}
                    style={{ marginRight: "1rem" }}
                  />
                )}
                <LinkButton
                  label={
                    <div className="response-download-button">
                      <span>
                        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="#a82227">
                          <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" />
                        </svg>
                      </span>
                      <span className="download-button">{t("CS_COMMON_DOWNLOAD")}</span>
                    </div>
                  }
                  onClick={() => handleDownloadPdf("FORM_23B")}
                  className="w-full"
                />

              </div>

              {expanded.form23B && (
                <React.Fragment>
                  {getDetailsRow(value?.form23B||form23B)}

                  <div style={{ marginTop: "1rem" }}>
                    <LinkButton
                      label={t("COLLAPSE")}
                      onClick={() => toggleExpanded("form23B")}
                    />
                  </div>
                </React.Fragment>
              )}
            </StatusTable>
          </React.Fragment>
        ) : null}

        <div
          style={{
            marginTop: "24px",
            padding: "16px",
            border: "1px solid #ccc",
            borderRadius: "4px",
          }}
        >
          <CheckBox
            label={t("BPA_DECLARATION_MESSAGE").replace(
              "{applicantName}",
              applicant?.applicantName || t("CS_APPLICANT")
            )}
            onChange={setDeclarationHandler}
            checked={agree}
          />
        </div>

        <SubmitBar
          label={t("CS_COMMON_SUBMIT")}
          onSubmit={onSubmit}
          disabled={!agree}
          style={{ marginTop: "24px" }}
        />
      </Card>
    </React.Fragment>
  );
};

export default CheckPage;