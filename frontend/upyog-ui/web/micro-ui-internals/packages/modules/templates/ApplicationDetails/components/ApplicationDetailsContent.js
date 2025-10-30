import {
  BreakLine,
  Card,
  CardSectionHeader,
  CardSubHeader,
  CheckPoint,
  ConnectingCheckPoints,
  Loader,
  Row,
  StatusTable,
  LinkButton,
  PDFSvg,
  EditIcon,
  ViewsIcon,
  DeleteIcon,
  CardLabel
} from "@upyog/digit-ui-react-components";
import { values } from "lodash";
import React, { Fragment,useState } from "react";
import { useTranslation } from "react-i18next";
import { Link, useParams } from "react-router-dom";
import BPADocuments from "./BPADocuments";
import OBPSV2Documents from "./OBPSV2Documents"
import InspectionReport from "./InspectionReport";
import NOCDocuments from "./NOCDocuments";
import PermissionCheck from "./PermissionCheck";
import PropertyDocuments from "./PropertyDocuments";
import PropertyEstimates from "./PropertyEstimates";
import PropertyFloors from "./PropertyFloors";
import PropertyOwners from "./PropertyOwners";
import ScruntinyDetails from "./ScruntinyDetails";
import SubOccupancyTable from "./SubOccupancyTable";
import TLCaption from "./TLCaption";
import TLTradeAccessories from "./TLTradeAccessories";
import TLTradeUnits from "./TLTradeUnits";
import WSAdditonalDetails from "./WSAdditonalDetails";
import WSFeeEstimation from "./WSFeeEstimation";
// import WSInfoLabel from "../../../ws/src/pageComponents/WSInfoLabel";
import DocumentsPreview from "./DocumentsPreview";
import InfoDetails from "./InfoDetails";
import ViewBreakup from"./ViewBreakup";
import ArrearSummary from "../../../common/src/payments/citizen/bills/routes/bill-details/arrear-summary"
import useScrutinyFormDetails from "../../../../libraries/src/hooks/obpsv2/useScrutinyFormDetails";
import FormAcknowledgement from "../../../obpsv2/src/pages/citizen/Create/FormAcknowledgement";
function ApplicationDetailsContent({
  applicationDetails,
  workflowDetails,
  isDataLoading,
  applicationData,
  businessService,
  timelineStatusPrefix,
  id,
  showTimeLine = true,
  statusAttribute = "status",
  paymentsList,
  oldValue,
  isInfoLabel = false,
}) {
  const { t } = useTranslation();
  let { id: applicationNo } = useParams(); // Extracts PG-1013-2025-I-001019
  const ownersSequences = applicationDetails?.applicationData?.owners;
  

  function OpenImage(imageSource, index, thumbnailsToShow) {
    window.open(thumbnailsToShow?.fullImage?.[0], "_blank");
  }
  const [expanded, setExpanded] = useState({
    form22: false,
    form23A: false,
    form23B: false,
  });
  const toggleExpanded = (key) => {
    setExpanded((prev) => ({ ...prev, [key]: !prev[key] }));
  };
  const [fetchBillData, updatefetchBillData] = useState({});

  const setBillData = async (tenantId, propertyIds, updatefetchBillData, updateCanFetchBillData) => {
    const assessmentData = await Digit.PTService.assessmentSearch({ tenantId, filters: { propertyIds } });
    let billData = {};
    if (assessmentData?.Assessments?.length > 0) {
      billData = await Digit.PaymentService.fetchBill(tenantId, {
        businessService: "PT",
        consumerCode: propertyIds,
      });
    }
    updatefetchBillData(billData);
    updateCanFetchBillData({
      loading: false,
      loaded: true,
      canLoad: true,
    });
  };
  const [billData, updateCanFetchBillData] = useState({
    loading: false,
    loaded: false,
    canLoad: false,
  });

  if (applicationData?.status == "ACTIVE" && !billData.loading && !billData.loaded && !billData.canLoad) {
    updateCanFetchBillData({
      loading: false,
      loaded: false,
      canLoad: true,
    });
  }
  if (billData?.canLoad && !billData.loading && !billData.loaded) {
    updateCanFetchBillData({
      loading: true,
      loaded: false,
      canLoad: true,
    });
    setBillData(applicationData?.tenantId || tenantId, applicationData?.propertyId, updatefetchBillData, updateCanFetchBillData);
  }
  const convertEpochToDateDMY = (dateEpoch) => {
    if (dateEpoch == null || dateEpoch == undefined || dateEpoch == "") {
      return "NA";
    }
    const dateFromApi = new Date(dateEpoch);
    let month = dateFromApi.getMonth() + 1;
    let day = dateFromApi.getDate();
    let year = dateFromApi.getFullYear();
    month = (month > 9 ? "" : "0") + month;
    day = (day > 9 ? "" : "0") + day;
    return `${day}/${month}/${year}`;
  };
  const getTimelineCaptions = (checkpoint, index = 0, timeline) => {
    if (checkpoint.state === "OPEN" || (checkpoint.status === "INITIATED" && !window.location.href.includes("/obps/"))) {
      const caption = {
        date: convertEpochToDateDMY(applicationData?.auditDetails?.createdTime),
        source: applicationData?.channel || "",
      };
      return <TLCaption data={caption} />;
    } else if (window.location.href.includes("/obps/") || window.location.href.includes("/noc/") || window.location.href.includes("/ws/")) {
      const privacy = {
        uuid: checkpoint?.assignes?.[0]?.uuid,
        fieldName: "mobileNumber",
        model: "User",
        showValue: false,
        loadData: {
          serviceName: "/egov-workflow-v2/egov-wf/process/_search",
          requestBody: {},
          requestParam: { tenantId: applicationDetails?.tenantId, businessIds: applicationDetails?.applicationNo, history: true },
          jsonPath: "ProcessInstances[0].assignes[0].mobileNumber",
          isArray: false,
          d: (res) => {
            let resultstring = "";
            resultstring = `+91 ${_.get(res, `ProcessInstances[${index}].assignes[0].mobileNumber`)}`;
            return resultstring;
          },
        },
      };
      const previousCheckpoint = timeline && timeline[index - 1] &&timeline?.[index - 1];
      const caption = {
        date: checkpoint?.auditDetails?.lastModified,
        name: checkpoint?.assignes?.[0]?.name,
        mobileNumber:
          applicationData?.processInstance?.assignes?.[0]?.uuid === checkpoint?.assignes?.[0]?.uuid &&
          applicationData?.processInstance?.assignes?.[0]?.mobileNumber
            ? applicationData?.processInstance?.assignes?.[0]?.mobileNumber
            : checkpoint?.assignes?.[0]?.mobileNumber,
          comment: t(checkpoint && checkpoint?.comment),
          wfComment: previousCheckpoint ? previousCheckpoint && previousCheckpoint.wfComment : [],
        thumbnailsToShow: checkpoint?.thumbnailsToShow,
      };

      return <TLCaption data={caption} OpenImage={OpenImage} privacy={privacy} />;
    } else {
      const caption = {
        date: convertEpochToDateDMY(applicationData?.auditDetails?.lastModifiedTime),
        name: checkpoint?.assignes?.[0]?.name,
        wfComment: checkpoint?.wfComment,
        mobileNumber: checkpoint?.assignes?.[0]?.mobileNumber,
        thumbnailsToShow: checkpoint?.thumbnailsToShow,
      };

      return <TLCaption data={caption} OpenImage={OpenImage} />;
    }
  };

  const getTranslatedValues = (dataValue, isNotTranslated) => {
    if (dataValue) {
      return !isNotTranslated ? t(dataValue) : dataValue;
    } else {
      return t("NA");
    }
  };

  const checkLocation =
  window.location.href.includes("employee/tl") || window.location.href.includes("employee/obps") || window.location.href.includes("employee/noc");
  const isNocLocation = window.location.href.includes("employee/noc");
  const isBPALocation = window.location.href.includes("employee/obps");
  const isWS = window.location.href.includes("employee/ws");

  const getRowStyles = () => {
    if (window.location.href.includes("employee/obps") || window.location.href.includes("employee/noc")) {
      return { justifyContent: "space-between", fontSize: "16px", lineHeight: "19px", color: "#0B0C0C" };
    } else if (checkLocation) {
      return { justifyContent: "space-between", fontSize: "16px", lineHeight: "19px", color: "#0B0C0C" };
    } else {
      return {};
    }
  };

  const getTableStyles = () => {
    if (window.location.href.includes("employee/obps") || window.location.href.includes("employee/noc")) {
      return { position: "relative", marginTop: "19px" };
    } else if (checkLocation) {
      return { position: "relative", marginTop: "19px" };
    } else {
      return {};
    }
  };

  const getMainDivStyles = () => {
    if (
      window.location.href.includes("employee/obps") ||
      window.location.href.includes("employee/noc") ||
      window.location.href.includes("employee/ws")
    ) {
      return { lineHeight: "19px", maxWidth: "950px", minWidth: "280px" };
    } else if (checkLocation) {
      return { lineHeight: "19px", maxWidth: "600px", minWidth: "280px" };
    } else {
      return {};
    }
  };

  const getTextValue = (value) => {
     // Handle time values specially
     if (value?.isTimeValue && value?.value) {
      // Format time from "HH:MM" to "H:MM AM/PM"
      const timeString = value.value;
      const [hours, minutes] = timeString.split(':').map(part => parseInt(part, 10));
      
      if (!isNaN(hours) && !isNaN(minutes)) {
        const period = hours >= 12 ? 'PM' : 'AM';
        const hour12 = hours % 12 || 12; // Convert 0 to 12 for 12 AM
        const paddedMinutes = String(minutes).padStart(2, '0'); // Ensures minutes are always two digits
        return `${hour12}:${paddedMinutes} ${period}`;
      }
      return value.value; // Return original if parsing failed
    }
    // Handle other values as before
    if (value?.skip) return value.value;
    else if (value?.isUnit) return value?.value ? `${getTranslatedValues(value?.value, value?.isNotTranslated)} ${t(value?.isUnit)}` : t("N/A");
    else return value?.value ? getTranslatedValues(value?.value, value?.isNotTranslated) : t("N/A");
  };

  const getClickInfoDetails = () => {
    if (window.location.href.includes("disconnection") || window.location.href.includes("application")) {
      return "WS_DISCONNECTION_CLICK_ON_INFO_LABEL";
    } else {
      return "WS_CLICK_ON_INFO_LABEL";
    }
  };
  const cellStyle = {
    border: "1px solid #ccc",
    padding: "8px",
    textAlign: "left",
    fontSize: "14px",
  };
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
  const handleDownloadPdf = async (formType) => {
    try {
      let formData = null;
      let tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
      const tenantInfo  = tenants.find((tenant) => tenant.code === tenantId);
      switch (formType) {
        case "FORM_22":
          formData = form22;
          break;
        case "FORM_23A":
          formData =form23A;
          break;
        case "FORM_23B":
          formData = form23B;
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

  const [showAllTimeline, setShowAllTimeline] = useState(false);
  const getClickInfoDetails1 = () => {
    if (window.location.href.includes("disconnection") || window.location.href.includes("application")) {
      return "WS_DISCONNECTION_CLICK_ON_INFO1_LABEL";
    } else {
      return "";
    }
  };
  const toggleTimeline = () => {
    setShowAllTimeline((prev) => !prev);
  };
  // this is file open service
  const openFilePDF = (fileId) => {
    Digit.UploadServices.Filefetch([fileId], Digit.ULBService.getStateId())
      .then((res) => {
        console.log("Response of file:", res);

        // Extract the concatenated URL string
        const concatenatedUrls = res?.data?.fileStoreIds?.[0]?.url;

        if (concatenatedUrls) {
          // Split the string by commas to get individual URLs
          const urlArray = concatenatedUrls.split(",");

          // Pick the first URL (or any other logic to decide which URL to open)
          const fileUrl = urlArray[0];

          if (fileUrl) {
            window.open(fileUrl, "_blank"); // Open the file in a new tab
          } else {
            console.error("No valid URL found to open!");
          }
        } else {
          console.error("URL is missing in the response!");
        }
      })
      .catch((error) => {
        console.error("Error fetching file:", error);
      });
  };


  return (
    <Card style={{ position: "relative" }} className={"employeeCard-override"}>
      {/* For UM-4418 changes */}
      {isInfoLabel ? (
        <InfoDetails
          t={t}
          userType={false}
          infoBannerLabel={"CS_FILE_APPLICATION_INFO_LABEL"}
          infoClickLable={"WS_CLICK_ON_LABEL"}
          infoClickInfoLabel={getClickInfoDetails()}
          infoClickInfoLabel1={getClickInfoDetails1()}
        />
      ) : null}

      {applicationDetails?.applicationDetails?.map((detail, index) => (
        <React.Fragment key={index}>
          <div style={getMainDivStyles()}>
            {index === 0 && !detail.asSectionHeader ? (
              <CardSubHeader style={{ marginBottom: "16px", fontSize: "24px" }}>{t(detail.title)}</CardSubHeader>
            ) : (
              <React.Fragment>
                <CardSectionHeader
                  style={
                    index == 0 && checkLocation
                      ? { marginBottom: "16px", fontSize: "24px" }
                      : { marginBottom: "16px", marginTop: "32px", fontSize: "24px" }
                  }
                >
                  {isNocLocation ? `${t(detail.title)}` : t(detail.title)}
                  {detail?.Component ? <detail.Component /> : null}
                </CardSectionHeader>
              </React.Fragment>
            )}
            {/* TODO, Later will move to classes */}
            {/* Here Render the table for adjustment amount details detail.isTable is true for that table*/}

            {detail?.isTable && (
              <table style={{ tableLayout: "fixed", width: "100%", borderCollapse: "collapse", borderCollapse: "collapse", border: "1px solid black"}}>
                <tr style={{ textAlign: "left" }}>
                  {detail?.headers.map((header) => (
                    <th style={{ padding: "10px", paddingLeft: "5px", border: "1px solid black"}}>{t(header)}</th>
                  ))}
                </tr>

                {detail?.tableRows.map((row, index) => {
                  // if (index === detail?.tableRows.length - 1) {
                  //   return <>
                  //     <hr style={{ width: "1200px", marginTop: "15px" }} className="underline" />
                  //     <tr>
                  //       {row.map(element => <td style={{ textAlign: "left" }}>{t(element)}</td>)}
                  //     </tr>
                  //   </>
                  // }
                  return (
                    <tr>
                      
                      {row.map((element, idx) =>
                        Array.isArray(element) && element.length > 1 && detail.isMaintenance === true ? (
                          <td style={{ paddingTop: "20px", textAlign: "left", border: "1px solid black", verticalAlign: "middle" }} key={idx}>
                            <div style={{ display: "flex", flexWrap: "nowrap", gap: "5px" }}>
                              {element.map((file, fileIndex) => (
                                <a
                                  key={fileIndex} // Ensure each <a> tag has a unique key
                                  onClick={() => openFilePDF(file.fileStoreId)}
                                  rel="noopener noreferrer"
                                  style={{ marginRight: "5px", display: "inline-block", cursor: "pointer" }}
                                >
                                  <PDFSvg style={{ width: "25px", height: "25px" }} />
                                </a>
                              ))}
                            </div>
                          </td>
                        ) : (
                          <td key={idx} style={{ paddingTop: "20px", textAlign: "left" , border: "1px solid black", verticalAlign: "middle"}}>
                            {console.log("Comming Maintainaince data row:- ", element.data)}
                            {element && element.editButton === true ? (
                              <span style={{ display: "inline-flex", gap: "10px", alignItems: "center" }}>
                                <Link
                                  to={{
                                    pathname: `/upyog-ui/employee/asset/assetservice/maintenance-edit/${applicationNo}`,
                                    state: { data: element.data },
                                  }}
                                >
                                  <button> <EditIcon /> </button>
                                </Link>
                              </span>
                            ) : (
                              t(element)
                            )}
                          </td>
                        )
                      )}
                    </tr>
                  );
                })}
              </table>
            )}
            <StatusTable style={getTableStyles()}>
              {detail?.title &&
                !detail?.title.includes("NOC") &&
                detail?.values?.map((value, index) => {
                  if (value.map === true && value.value !== "N/A") {
                    return (
                      <Row
                        labelStyle={{ wordBreak: "break-all" }}
                        textStyle={{ wordBreak: "break-all" }}
                        key={t(value.title)}
                        label={t(value.title)}
                        text={<img src={t(value.value)} alt="" privacy={value?.privacy} />}
                      />
                    );
                  }
                  if (value?.isLink == true) {
                    return (
                      <Row
                        key={t(value.title)}
                        label={
                          window.location.href.includes("tl") || window.location.href.includes("ws") ? (
                            <div style={{ width: "200%" }}>
                              <Link to={value?.to}>
                                <span className="link" style={{ color: "#a82227" }}>
                                  {t(value?.title)}
                                </span>
                              </Link>
                            </div>
                          ) : isNocLocation || isBPALocation ? (
                            `${t(value.title)}`
                          ) : (
                            t(value.title)
                          )
                        }
                        text={
                          <div>
                            <Link to={value?.to}>
                              <span className="link" style={{ color: "#a82227" }}>
                                {value?.value}
                              </span>
                            </Link>
                          </div>
                        }
                        last={index === detail?.values?.length - 1}
                        caption={value.caption}
                        className="border-none"
                        rowContainerStyle={getRowStyles()}
                        labelStyle={{ wordBreak: "break-all" }}
                        textStyle={{ wordBreak: "break-all" }}
                      />
                    );
                  }
                  return (
                    <div>
                      {window.location.href.includes("modify") ? (
                        <Row
                          className="border-none"
                          key={`${value.title}`}
                          label={`${t(`${value.title}`)}`}
                          privacy={value?.privacy}
                          text={value?.oldValue ? value?.oldValue : value?.value ? value?.value : ""}
                          labelStyle={{ wordBreak: "break-all" }}
                          textStyle={{ wordBreak: "break-all" }}
                        />
                      ) : (
                        <Row
                          key={t(value.title)}
                          label={t(value.title)}
                          text={getTextValue(value)}
                          last={index === detail?.values?.length - 1}
                          caption={value.caption}
                          className="border-none"
                          /* privacy object set to the Row Component */
                          privacy={value?.privacy}
                          // TODO, Later will move to classes
                          rowContainerStyle={getRowStyles()}
                          // labelStyle={{wordBreak: "break-all"}}
                          // textStyle={{wordBreak: "break-all"}}
                          labelStyle={{
                            wordBreak: "break-all",
                            fontWeight: value.isBold ? "bold" : "normal",
                            fontStyle: value.isBold ? "italic" : "normal",
                          }}
                          textStyle={{
                            wordBreak: "break-all",
                            fontWeight: value.isBold ? "bold" : "normal",
                            fontStyle: value.isBold ? "italic" : "normal",
                          }}
                        />
                      )}
                      {value.title === "PT_TOTAL_DUES" ? <ArrearSummary bill={fetchBillData.Bill?.[0]} /> : ""}
                    </div>
                  );
                })}
            </StatusTable>
          </div>
          {detail?.belowComponent && <detail.belowComponent />}
          {detail?.additionalDetails?.inspectionReport && (
            <ScruntinyDetails scrutinyDetails={detail?.additionalDetails} paymentsList={paymentsList} />
          )}
          {applicationDetails?.applicationData?.additionalDetails?.fieldinspection_pending?.length > 0 && detail?.additionalDetails?.fiReport && (
            <InspectionReport fiReport={applicationDetails?.applicationData?.additionalDetails?.fieldinspection_pending} />
          )}
          {/* {detail?.additionalDetails?.FIdocuments && detail?.additionalDetails?.values?.map((doc,index) => (
            <div key={index}>
            {doc.isNotDuplicate && <div> 
             <StatusTable>
             <Row label={t(doc?.documentType)}></Row>
             <OBPSDocument value={detail?.additionalDetails?.values} Code={doc?.documentType} index={index}/> 
             <hr style={{color:"#cccccc",backgroundColor:"#cccccc",height:"2px",marginTop:"20px",marginBottom:"20px"}}/>
             </StatusTable>
             </div>}
             </div>
          )) } */}
          {detail?.additionalDetails?.floors && <PropertyFloors floors={detail?.additionalDetails?.floors} />}
          {detail?.additionalDetails?.owners && <PropertyOwners owners={detail?.additionalDetails?.owners} />}
          {detail?.additionalDetails?.units && <TLTradeUnits units={detail?.additionalDetails?.units} />}
          {detail?.additionalDetails?.accessories && <TLTradeAccessories units={detail?.additionalDetails?.accessories} />}
          {detail?.additionalDetails?.permissions && workflowDetails?.data?.nextActions?.length > 0 && (
            <PermissionCheck applicationData={applicationDetails?.applicationData} t={t} permissions={detail?.additionalDetails?.permissions} />
          )}
          {detail?.additionalDetails?.obpsDocuments && (
            <BPADocuments
              t={t}
              applicationData={applicationDetails?.applicationData}
              docs={detail.additionalDetails.obpsDocuments}
              bpaActionsDetails={workflowDetails}
            />
          )}
          {detail?.additionalDetails?.noc && (
            <NOCDocuments
              t={t}
              isNoc={true}
              NOCdata={detail.values}
              applicationData={applicationDetails?.applicationData}
              docs={detail.additionalDetails.noc}
              noc={detail.additionalDetails?.data}
              bpaActionsDetails={workflowDetails}
            />
          )}
          {detail?.additionalDetails?.obpsDocuments && (
            <OBPSV2Documents
              t={t}
              applicationData={applicationDetails?.applicationData}
              docs={detail.additionalDetails.obpsDocuments}
              bpaActionsDetails={workflowDetails}
            />
          )}
          {detail?.additionalDetails?.form22Details && window.location.href.includes("obpsv2")  ? (
            <div>
            <StatusTable>
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
                  {getDetailsRow(form22)}

                  <div style={{ marginTop: "1rem" }}>
                    <LinkButton
                      label={t("COLLAPSE")}
                      onClick={() => toggleExpanded("form22")}
                    />
                  </div>
                  </StatusTable>
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
                  {getDetailsRow(form23A)}

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
                  {getDetailsRow(form23B)}

                  <div style={{ marginTop: "1rem" }}>
                    <LinkButton
                      label={t("COLLAPSE")}
                      onClick={() => toggleExpanded("form23B")}
                    />
                  </div>
                </React.Fragment>
              )}
            </StatusTable>
            </div>
          ):null}
          
          {detail?.additionalDetails?.scruntinyDetails && <ScruntinyDetails scrutinyDetails={detail?.additionalDetails} />}
          {detail?.additionalDetails?.buildingExtractionDetails && <ScruntinyDetails scrutinyDetails={detail?.additionalDetails} />}
          {detail?.additionalDetails?.subOccupancyTableDetails && (
            <SubOccupancyTable edcrDetails={detail?.additionalDetails} applicationData={applicationDetails?.applicationData} />
          )}
          {detail?.additionalDetails?.documentsWithUrl && <DocumentsPreview documents={detail?.additionalDetails?.documentsWithUrl} />}
          {detail?.additionalDetails?.documents && <PropertyDocuments documents={detail?.additionalDetails?.documents} />}
          {detail?.additionalDetails?.taxHeadEstimatesCalculation && (
            <PropertyEstimates taxHeadEstimatesCalculation={detail?.additionalDetails?.taxHeadEstimatesCalculation} />
          )}
          {detail?.isWaterConnectionDetails && <WSAdditonalDetails wsAdditionalDetails={detail} oldValue={oldValue} />}
          {/* {detail?.isLabelShow ? <WSInfoLabel t={t} /> : null} */}
          {detail?.additionalDetails?.redirectUrl && (
            <div style={{ fontSize: "16px", lineHeight: "24px", fontWeight: "400", padding: "10px 0px" }}>
              <Link to={detail?.additionalDetails?.redirectUrl?.url}>
                <span className="link" style={{ color: "#a82227" }}>
                  {detail?.additionalDetails?.redirectUrl?.title}
                </span>
              </Link>
            </div>
          )}
          {detail?.additionalDetails?.estimationDetails && <WSFeeEstimation wsAdditionalDetails={detail} workflowDetails={workflowDetails} />}
          {detail?.additionalDetails?.estimationDetails && <ViewBreakup wsAdditionalDetails={detail} workflowDetails={workflowDetails} />}
        </React.Fragment>
      ))}
      {showTimeLine && workflowDetails?.data?.timeline?.length > 0 && (
        <React.Fragment>
          <BreakLine />
          {(workflowDetails?.isLoading || isDataLoading) && <Loader />}
          {!workflowDetails?.isLoading && !isDataLoading && (
            <Fragment>
              <div id="timeline">
                <CardSectionHeader style={{ marginBottom: "16px", marginTop: "32px" }}>
                  {t("ES_APPLICATION_DETAILS_APPLICATION_TIMELINE")}
                </CardSectionHeader>
                {workflowDetails?.data?.timeline && workflowDetails?.data?.timeline?.length === 1 ? (
                  <CheckPoint
                    isCompleted={true}
                    label={t(`${timelineStatusPrefix}${workflowDetails?.data?.timeline[0]?.state}`)}
                    customChild={getTimelineCaptions(workflowDetails?.data?.timeline[0], workflowDetails?.data?.timeline)}
                  />
                ) : (
                  <ConnectingCheckPoints>
                    {workflowDetails?.data?.timeline &&
                      workflowDetails?.data?.timeline
                        .slice(0, showAllTimeline ? workflowDetails?.data.timeline.length : 2)
                        .map((checkpoint, index, arr) => {
                          let timelineStatusPostfix = "";
                          if (window.location.href.includes("/obps/")) {
                            if (
                              workflowDetails?.data?.timeline[index - 1]?.state?.includes("BACK_FROM") ||
                              workflowDetails?.data?.timeline[index - 1]?.state?.includes("SEND_TO_CITIZEN")
                            )
                              timelineStatusPostfix = `_NOT_DONE`;
                            else if (checkpoint?.performedAction === "SEND_TO_ARCHITECT") timelineStatusPostfix = `_BY_ARCHITECT_DONE`;
                            else timelineStatusPostfix = index == 0 ? "" : `_DONE`;
                          }

                          return (
                            <React.Fragment key={index}>
                              <CheckPoint
                                keyValue={index}
                                isCompleted={index === 0}
                                info={checkpoint.comment}
                                label={t(
                                  `${timelineStatusPrefix}${
                                    checkpoint?.performedAction === "REOPEN" ? checkpoint?.performedAction : checkpoint?.[statusAttribute]
                                  }${timelineStatusPostfix}`
                                )}
                                customChild={getTimelineCaptions(checkpoint, index, workflowDetails?.data?.timeline)}
                              />
                            </React.Fragment>
                          );
                        })}
                  </ConnectingCheckPoints>
                )}
                {workflowDetails?.data?.timeline?.length > 2 && (
                  <LinkButton label={showAllTimeline ? t("COLLAPSE") : t("VIEW_TIMELINE")} onClick={toggleTimeline}></LinkButton>
                )}
              </div>
            </Fragment>
          )}
        </React.Fragment>
      )}
    </Card>
  );
}

export default ApplicationDetailsContent;
