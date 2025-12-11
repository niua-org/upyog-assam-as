import React, { useState, useEffect } from "react";
import { Card, Modal, TextArea, UploadFile, Heading, CloseBtn, CardLabel, CardLabelDesc, SubmitBar, Toast } from "@upyog/digit-ui-react-components";
import { OBPSV2Services } from "../../../../libraries/src/services/elements/OBPSV2";
import { useTranslation } from "react-i18next";
///TODO: Remove unwanted multiple search calls, instead use once and cache the response in a state of useRef 
const Action = ({ selectedAction, applicationNo, closeModal, setSelectedAction, setToastMessage, setShowToast: parentSetShowToast, refetch,bpaStatus}) => {
  const { t } = useTranslation();
  const [comments, setComments] = useState("");
  const [uploadedFile, setUploadedFile] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [toast, setToast] = useState(false);
  const [oldRTPName, setOldRTPName] = useState();
  const [newRTPName, setNewRTPName] = useState();
  const [popup, setPopup] = useState(false);
  const [error, setError] = useState(null);
  const [file, setFile] = useState(null);
  const [isUploading, setIsUploading] = useState(false);

  // New state for GIS response
  const [gisResponse, setGisResponse] = useState(null);
  const [showGisResponse, setShowGisResponse] = useState(false);
  const [gisValidationSuccess, setGisValidationSuccess] = useState(false);

  const WORKFLOW_ACTIONS = [
    "APPROVE",
    "ACCEPT",
    "SEND",
    "REJECT",
    "SEND_BACK_TO_RTP",
    "RECOMMEND_TO_CEO",
    "SEND_BACK_TO_GMDA",
    "RECOMMEND_TO_CHAIRMAN_DA",
    "SEND_BACK_TO_DA",
    "FORWARD"
  ];

  const [assignResponse, setAssignResponse] = useState(null);
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
  const { data: mdmsData, isLoading } = Digit.Hooks.useEnabledMDMS("as", "BPA", [{ name: "PermissibleZone" }], {
    select: (data) => {
      return data?.BPA?.PermissibleZone || {};
    },
  });
  useEffect(() => {
    if (toast || error) {
      const timer = setTimeout(() => {
        setToast(false);
        setError(null);
      }, 2000);
      return () => clearTimeout(timer);
    }
  }, [toast, error]);
  useEffect(() => {
    (async () => {
      setError(null);
      if (file && selectedAction !== "VALIDATE_GIS") {
        if (file.size >= 5242880) {
          setError(t("CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED"));
        } else {
          try {
            setUploadedFile(null);
            setIsUploading(true);
            // TODO: change module in file storage
            const response = await Digit.UploadServices.Filestorage("OBPSV2", file, Digit.ULBService.getStateId());
            if (response?.data?.files?.length > 0) {
              setUploadedFile(response?.data?.files[0]?.fileStoreId);
            } else {
              setError(t("CS_FILE_UPLOAD_ERROR"));
            }
          } catch (err) {
            setError(t("CS_FILE_UPLOAD_ERROR"));
          } finally {
            setIsUploading(false);
          }
        }
      }
    })();
  }, [file, selectedAction]);

  // Cleanup effect when component unmounts
  useEffect(() => {
    return () => {
      setPopup(false);
      setError(null);
      setToast(false);
    };
  }, []);
  useEffect(() => {
    if (selectedAction) {
      switch (selectedAction) {
        case "NEWRTP":
          setPopup(true);
          break;
        case "REJECT":
          setPopup(true);
          break;
        case "APPROVE":
          setPopup(true);
          break;
        case "ACCEPT":
          setPopup(true);
          break;
        case "SEND":
          setPopup(true);
          break;
        case "SEND_BACK_TO_RTP":
          setPopup(true);
          break;
        case "SUBMIT_REPORT":
          const submitReportUrl = `${window.location.origin}/upyog-ui/employee/obpsv2/application/${applicationNo}/${tenantId}`;
          redirectToPage(submitReportUrl);
          break;
        case "RECOMMEND_TO_CEO":
          setPopup(true);
          break;
        case "SEND_BACK_TO_GMDA":
          setPopup(true);
          break;
        case "VALIDATE_GIS":
          setPopup(true);
          setShowGisResponse(false);
          setGisResponse(null);
          setGisValidationSuccess(false);
          break;
        case "EDIT":
          const redirectingUrl = `${window.location.origin}/upyog-ui/citizen/obpsv2/editApplication/${applicationNo}`;
          redirectToPage(redirectingUrl);
          break;
        case "PAY":
          const isEmployeeRoute = window.location.href.includes("/employee/");
          let businessService = "BPA.PLANNING_PERMIT_FEE";
          
          if (bpaStatus === "CITIZEN_FINAL_PAYMENT") {
            businessService = "BPA.BUILDING_PERMIT_FEE";
          }
          
          let redirectURL = isEmployeeRoute 
            ? `${window.location.origin}/upyog-ui/employee/payment/collect/${businessService}/${applicationNo}`
            : `${window.location.origin}/upyog-ui/citizen/payment/my-bills/${businessService}/${applicationNo}`;
          redirectToPage(redirectURL);
          break;
        case "APPLY_FOR_SCRUTINY":
          let scrutinyurl = window.location.href;
          let scrutinyRedirectingUrl = scrutinyurl.split("/inbox")[0] + `/apply/home?applicationNo=${applicationNo}`;
          redirectToPage(scrutinyRedirectingUrl);
          break;
        case "RECOMMEND_TO_CHAIRMAN_DA":
          setPopup(true);
          break;
        case "SEND_BACK_TO_DA":
          setPopup(true);
          break;
        case "FORWARD":
          setPopup(true);
          break;
        default:
          setPopup(false);
      }
    }
  }, [selectedAction]);

  function addComment(e) {
    setActionError(null);
    setComments(e.target.value);
  }
  function selectFile(e) {
    if (selectedAction === "VALIDATE_GIS") {
      setUploadedFile(e.target.files[0]);
    }
    setFile(e.target.files[0]);
  }
  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );

  const CloseBtn = (props) => {
    return (
      <div className="icon-bg-secondary" onClick={props.onClick}>
        <Close />
      </div>
    );
  };

  const LoadingSpinner = () => <div className="loading-spinner" />;
  const Heading = (props) => {
    return <h1 className="heading-m">{props.label}</h1>;
  };

  function closeToast() {
    setToast(false);
  }
  function close(state) {
    switch (state) {
      case popup:
        setPopup(!popup);
        break;
      default:
        break;
    }
  }
  function redirectToPage(redirectingUrl) {
    window.location.href = redirectingUrl;
  }

  async function onAssign(selectedAction, comments) {
    const bpaDetails = await OBPSV2Services.search({
      tenantId,
      filters: { applicationNo },
      config: { staleTime: Infinity, cacheTime: Infinity },
    });
    if (bpaDetails?.bpa?.[0]) {
      bpaDetails.bpa[0].workflow = {
        ...(bpaDetails.bpa[0].workflow || {}),
        action: selectedAction,
        assignes: ["ACCEPT", "VALIDATE_GIS", "EDIT"].includes(selectedAction) ? [bpaDetails?.bpa[0]?.rtpDetails?.rtpUUID] : null,
        comments: comments,
        varificationDocuments: uploadedFile
          ? [
              {
                documentType: file.type,
                fileName: file?.name,
                fileStoreId: selectedAction === "VALIDATE_GIS" ? "" : uploadedFile,
              },
            ]
          : null,
      };
      try {
        const response = await OBPSV2Services.update({ BPA: bpaDetails?.bpa[0] }, tenantId);
        setAssignResponse(response);


        // For VALIDATE_GIS, don't close popup automatically - it will be closed manually
        if (selectedAction !== "VALIDATE_GIS") {
          setPopup(false);

          setTimeout(() => {
            if (setToastMessage && typeof setToastMessage === "function") {
              setToastMessage(t(`CS_ACTION_UPDATE_${selectedAction}_TEXT`));
            }
            if (typeof parentSetShowToast === "function") {
              parentSetShowToast(true);
            }
            if (setSelectedAction && typeof setSelectedAction === "function") {
              setSelectedAction(null);
            }
          }, 100);

        // Refresh data if refetch function is available
        if (typeof refetch === "function") {
          await refetch();
        }
      }

        return response;
      } catch (error) {
        setError(error?.response?.data?.Errors?.[0]?.message || "An error occurred");
        setPopup(false); // Close popup on error
        console.error("Update error:", error);
      }
    }

    if (closeModal && typeof closeModal === "function") {
      closeModal();
    }
  }

  async function onValidateGIS() {
    const bpaDetails = await OBPSV2Services.search({
      tenantId,
      filters: { applicationNo },
      config: { staleTime: Infinity, cacheTime: Infinity },
    });
    const occupancyType = bpaDetails?.bpa?.[0]?.landInfo?.units?.[0]?.occupancyType;

    if (!file) {
      setError(t("CS_FILE_REQUIRED"));
      return;
    }

    try {
      setIsUploading(true);
      // Create multipart form data
      const formData = new FormData();
      formData.append("file", file);
      const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();

      // Construct GIS request wrapper
      const gisRequestWrapper = {
        RequestInfo: {
          apiId: "gis-api",
          userInfo: {
            uuid: Digit.UserService.getUser()?.info?.uuid,
          },
        },
        gisRequest: {
          tenantId: bpaDetails?.bpa?.[0]?.tenantId,
          planningAreaCode:bpaDetails?.bpa?.[0]?.additionalDetails?.gisCode,
          applicationNo,
          rtpiId: bpaDetails?.bpa?.[0]?.rtpDetails?.rtpUUID,
        },
      };

      // explicitly mark JSON part as application/json because the controller expects application/json
      const blob = new Blob([JSON.stringify(gisRequestWrapper)], { type: "application/json" });
      formData.append("gisRequestWrapper", blob);

      const response = await OBPSV2Services.gisService({ data: formData });
      const wfsResponse = response?.data?.wfsResponse;
      const landuse = wfsResponse?.landuse;

      // Store GIS response for display
      setGisResponse({
        district: wfsResponse?.district || "N/A",
        latitude: response?.data?.latitude || "N/A",
        longitude: response?.data?.longitude || "N/A",
        landuse: landuse || "N/A",
        village: wfsResponse?.village || "N/A",
        areaHectare: wfsResponse?.area_ha || "N/A",
        wardNo: wfsResponse?.ward_no || "N/A",
        zone: wfsResponse?.zone || "N/A"
      });
      // Filter MDMS Data using both occupancyType and landuse
      const permissibleZones = mdmsData || [];
      const filteredZones = permissibleZones.filter(
        (zone) => zone?.code === occupancyType && zone?.typeOfLand?.toLowerCase() === landuse?.toLowerCase()
      );

      // store the filtered data in a new variable (or state if needed)
      const matchedZone = filteredZones?.[0] || null;

      // Check if permissible is "No"
      if (matchedZone && matchedZone?.permissible === "No") {
        setShowGisResponse(true);
        setGisValidationSuccess(false);
        setError(t("NOT_PERMISSIBLE_FOR_CONSTRUCTION"));
        return false;
      }

      // If permissible is "Yes"
      if (matchedZone && matchedZone?.permissible === "Yes") {
        setShowGisResponse(true);
        setGisValidationSuccess(true);
        // Call update API here as normal
        await onAssign(selectedAction, comments);
        return true;
      }

       // Fallback if no matching zone found
      setShowGisResponse(true);
      setGisValidationSuccess(false);
      setError(t("NO_MATCHING_PERMISSIBLE_ZONE_FOUND"));
      return false;
    } catch (err) {
      console.error("GIS Validation Error:", err);
      setError(err?.response?.data?.error || t("CS_GIS_VALIDATION_FAILED"));
    } finally {
      setIsUploading(false);
    }
  }

  return (
    <React.Fragment>
      {selectedAction && popup && (
        <Modal
          headerBarMain={<Heading label={t(`CS_ACTION_${selectedAction}`)} />}
          headerBarEnd={
            <CloseBtn
             onClick={async () => {
              setPopup(false);
              if (setSelectedAction) setSelectedAction(null);

              // Only refetch if GIS succeeded
              if (selectedAction === "VALIDATE_GIS" && gisValidationSuccess) {
                if (typeof refetch === "function") await refetch();
              }
            }}
            />
          }
          actionCancelLabel={selectedAction === "VALIDATE_GIS" && showGisResponse ? null : t("CS_COMMON_CANCEL")}
          actionCancelOnSubmit={async () => {
            setPopup(false);
            if (setSelectedAction) setSelectedAction(null);

            // Only refetch if GIS succeeded
            if (selectedAction === "VALIDATE_GIS" && gisValidationSuccess) {
              if (typeof refetch === "function") await refetch();
            }
          }}
          actionSaveLabel={t("CS_COMMON_SUBMIT")}
          popupStyles={{ zIndex: 1001, ...(selectedAction === "VALIDATE_GIS" && showGisResponse ? {width: "800px", maxWidth: "90vw"}: {}) }}
          actionSaveOnSubmit={async (e) => {
            try {
              if (selectedAction === "VALIDATE_GIS") {
                 await onValidateGIS();
                // Don't close modal, just show response
              } else if (WORKFLOW_ACTIONS.includes(selectedAction)) {
                await onAssign(selectedAction, comments);
              }

              if (selectedAction === "NEWRTP" && !oldRTPName) setActionError(t("CS_OLD_RTP_NAME_MANDATORY"));
              if (selectedAction === "NEWRTP" && !newRTPName) setActionError(t("CS_NEW_RTP_NAME_MANDATORY"));
            } catch (err) {
              console.error(err);
            }
          }}
          error={error}
          hideSubmit={selectedAction === "VALIDATE_GIS" && showGisResponse ? true : false}
        >
          <Card>
            <React.Fragment>
              {WORKFLOW_ACTIONS.includes(selectedAction) && (
                <div>
                  <CardLabel>{t("COMMENTS")}</CardLabel>
                  <TextArea name="reason" onChange={addComment} value={comments} maxLength={500} />
                  <div style={{ textAlign: "right", fontSize: "12px", color: "#666" }}>{comments.length}/500</div>
                  <CardLabel>{t("CS_ACTION_SUPPORTING_DOCUMENTS")}</CardLabel>
                  <CardLabelDesc>{t("CS_UPLOAD_RESTRICTIONS")}</CardLabelDesc>
                  <UploadFile
                    id="approve-doc"
                    accept=".jpg"
                    onUpload={selectFile}
                    onDelete={() => setUploadedFile(null)}
                    message={
                      isUploading ? (
                        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                          <LoadingSpinner />
                          <span>Uploading...</span>
                        </div>
                      ) : uploadedFile ? (
                        `1 ${t("CS_ACTION_FILEUPLOADED")}`
                      ) : (
                        t("CS_ACTION_NO_FILEUPLOADED")
                      )
                    }
                  />
                </div>
              )}

              {selectedAction === "VALIDATE_GIS" && !showGisResponse && (
                <div>
                  <CardLabel>{t("CS_ACTION_UPLOAD_LOCATION_FILE")}</CardLabel>
                  <UploadFile
                    id="pgr-doc"
                    accept=".kml"
                    onUpload={selectFile}
                    onDelete={() => setUploadedFile(null)}
                    message={
                      isUploading ? (
                        <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                          <LoadingSpinner />
                          <span>Uploading...</span>
                        </div>
                      ) : uploadedFile ? (
                        `1 ${t("CS_ACTION_FILEUPLOADED")}`
                      ) : (
                        t("CS_ACTION_NO_FILEUPLOADED")
                      )
                    }
                  />
                </div>
              )}

              {selectedAction === "VALIDATE_GIS" && showGisResponse && gisResponse && (
                <div style={{padding: "16px", backgroundColor: gisValidationSuccess ? "#e8f5e9" : "#ffebee", borderRadius: "4px" }}>
                  <h3 style={{ marginBottom: "16px", color: gisValidationSuccess ? "#2e7d32" : "#c62828" }}>
                    {gisValidationSuccess ? t("GIS_VALIDATION_SUCCESS") : t("GIS_VALIDATION_FAILED")}
                  </h3>
                  <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "12px", whiteSpace:"nowrap", overflow:"hidden", textOverflow:"ellipsis" }}>
                    <div>
                      <strong>{t("LATITUDE")}:</strong> {gisResponse.latitude}
                    </div>
                    <div>
                      <strong>{t("LONGITUDE")}:</strong> {gisResponse.longitude}
                    </div>
                    <div>
                      <strong>{t("DISTRICT")}:</strong> {gisResponse.district}
                    </div>
                    <div>
                      <strong>{t("LANDUSE")}:</strong> {gisResponse.landuse}
                    </div>
                    <div>
                      <strong>{t("VILLAGE")}:</strong> {gisResponse.village}
                    </div>
                    <div>
                      <strong>{t("AREA_HECTARE")}:</strong> {gisResponse.areaHectare}
                    </div>
                    <div>
                      <strong>{t("WARD_NO")}:</strong> {gisResponse.wardNo}
                    </div>

                   <div style={{ gridColumn: "span 2", textAlign: "center", marginTop: "20px" }}>
                    <button
                      onClick={() => {
                        if (gisResponse.latitude && gisResponse.longitude) {
                          window.open(`https://www.google.com/maps?q=${gisResponse.latitude},${gisResponse.longitude}`, "_blank");
                        } else {
                          setToastMessage(t("CS_GIS_MAP_COORDINATES_MISSING"));
                        }
                      }}
                      style={{
                        padding: "10px 16px",
                        backgroundColor: "#a82227",
                        border: "none",
                        borderRadius: "6px",
                        color: "white",
                        cursor: "pointer",
                        fontWeight: "600",
                      }}
                    >
                      {t("VIEW_ON_MAP")}
                    </button>
                  </div>
                  </div>
                </div>
              )}


              {selectedAction === "REJECT" && (
                <div>
                  <CardLabel>{t("COMMENTS")}</CardLabel>
                  <TextArea name="reason" onChange={addComment} value={comments} maxLength={500} />
                  <div style={{ textAlign: "right", fontSize: "12px", color: "#666" }}>{comments.length}/500</div>
                </div>
              )}
            </React.Fragment>
          </Card>
        </Modal>
      )}

      {(toast || error) && (
        <Toast
          error={error ? error : null}
          label={error ? error : t(`CS_ACTION_UPDATE_${selectedAction}_TEXT`)}
          onClose={() => {
            setToast(false);
          }}
        />
      )}
    </React.Fragment>
  );
};;

export default Action;
