import React, { useState, useEffect } from "react";
import {
  Card,
  Modal,
  TextArea,
  UploadFile,
  Heading,
  CloseBtn,
  CardLabel,
  CardLabelDesc,
  SubmitBar,
  Toast,
} from "@upyog/digit-ui-react-components";
import { OBPSV2Services } from "../../../../libraries/src/services/elements/OBPSV2";
import { useTranslation } from "react-i18next";

const Action = ({ selectedAction, applicationNo, closeModal, setSelectedAction, setToastMessage, setShowToast: parentSetShowToast }) => {
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

  const [assignResponse, setAssignResponse] = useState(null);
  const tenantId =  Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
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
      if (file) {
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
  }, [file]);

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
          setPopup(true);
          break;
        case "RECOMMEND_TO_CEO":
          setPopup(true);
          break;
        case "SEND_BACK_TO_GMDA":
          setPopup(true);
          break;
        case "VALIDATE_GIS":
            setPopup(true);
            break;
        case "EDIT":
          const redirectingUrl = `${window.location.origin}/upyog-ui/citizen/obpsv2/editApplication/${applicationNo}`;
          redirectToPage(redirectingUrl);
          break;
        case "PAY":
          let redirectURL = `${window.location.origin}/upyog-ui/citizen/payment/my-bills/BPA.PLANNING_PERMIT_FEE/${applicationNo}`;
          redirectToPage(redirectURL);
          break;
          case "APPLY_FOR_SCRUTINY":
            let scrutinyurl=window.location.href;
            let scrutinyRedirectingUrl= scrutinyurl.split("/inbox")[0] + `/apply/home?applicationNo=${applicationNo}`;
            redirectToPage(scrutinyRedirectingUrl);
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

  const LoadingSpinner = () => (
    <div className="loading-spinner" />
  );
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
            config: { staleTime: Infinity, cacheTime: Infinity }
        });
        if(bpaDetails?.bpa?.[0]){
            bpaDetails.bpa[0].workflow = {
            ...(bpaDetails.bpa[0].workflow || {}),
            action: selectedAction ,
            assignes: null,
            comments: comments,
            varificationDocuments: uploadedFile ? [
              {
                documentType: file.type,
                fileName: file?.name,
                fileStoreId: uploadedFile,
              },
            ] : null
            };
            try {
                const response = await OBPSV2Services.update({BPA : bpaDetails?.bpa[0]}, tenantId);
                setAssignResponse(response);
                
                // Close popup first
                setPopup(false);
                
                // Then show toast after a small delay
                setTimeout(() => {
                    if (setToastMessage && typeof setToastMessage === 'function') {
                        setToastMessage(t(`CS_ACTION_UPDATE_${selectedAction}_TEXT`));
                    }
                    if (typeof parentSetShowToast === 'function') {
                        parentSetShowToast(true);
                    }
                    // Reset selected action to hide modal
                    if (setSelectedAction && typeof setSelectedAction === 'function') {
                        setSelectedAction(null);
                    }
                }, 100);
                
                // Refresh data if refetch function is available
                if (typeof refetch === 'function') {
                    await refetch();
                }
                
                return response;
            }
            catch(error){
                setError(error?.response?.data?.Errors?.[0]?.message || 'An error occurred');
                setPopup(false); // Close popup on error
                console.error('Update error:', error);
            }
        }
      

      if (closeModal && typeof closeModal === 'function') {
        closeModal();
      }
  }

  return (
    <React.Fragment>
      {selectedAction && popup && (
        <Modal
            headerBarMain={<Heading label={t(`CS_ACTION_${selectedAction}`)} />}
            headerBarEnd={<CloseBtn onClick={() => {
            setPopup(false);
            if (setSelectedAction && typeof setSelectedAction === 'function') {
              setSelectedAction(null);
            }
          }} />}
          actionCancelLabel={t("CS_COMMON_CANCEL")}
          actionCancelOnSubmit={() => {
            setPopup(false);
            if (setSelectedAction && typeof setSelectedAction === 'function') {
              setSelectedAction(null);
            }
          }}
          actionSaveLabel={t("CS_COMMON_SUBMIT")}
          popupStyles={{ zIndex: 1001 }}
          actionSaveOnSubmit={() => {
        if(selectedAction==="APPROVE"||selectedAction==="ACCEPT"||selectedAction==="SEND"||selectedAction==="REJECT"||selectedAction==="SEND_BACK_TO_RTP"||selectedAction==="VALIDATE_GIS" || selectedAction==="SUBMIT_REPORT" ||  selectedAction==="RECOMMEND_TO_CEO" || selectedAction==="SEND_BACK_TO_GMDA")
           onAssign(selectedAction, comments);
      if(selectedAction==="NEWRTP"&&!oldRTPName)
        setActionError(t("CS_OLD_RTP_NAME_MANDATORY"))
      if(selectedAction==="NEWRTP" &&!newRTPName)
        setActionError(t("CS_NEW_RTP_NAME_MANDATORY"))
      }}
          error={error}
          
        >
          <Card>
            <React.Fragment>
              {(selectedAction === "APPROVE" || selectedAction === "ACCEPT" || selectedAction === "SEND" || selectedAction === "REJECT" || selectedAction==="SEND_BACK_TO_RTP" || selectedAction==="SUBMIT_REPORT" ||  selectedAction==="RECOMMEND_TO_CEO" || selectedAction==="SEND_BACK_TO_GMDA") && (
                <div>
                  <CardLabel>{t("COMMENTS")}</CardLabel>
                  <TextArea
                    name="reason"
                    onChange={addComment}
                    value={comments}
                    maxLength={500}
                  />
                  <div style={{ textAlign: "right", fontSize: "12px", color: "#666" }}>
                    {comments.length}/500
                  </div>
                  <CardLabel>{t("CS_ACTION_SUPPORTING_DOCUMENTS")}</CardLabel>
                  <CardLabelDesc>{t("CS_UPLOAD_RESTRICTIONS")}</CardLabelDesc>
                  <UploadFile
                    id="approve-doc"
                    accept=".jpg"
                    onUpload={selectFile}
                    onDelete={() => setUploadedFile(null)}
                    message={isUploading ? (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <LoadingSpinner />
                        <span>Uploading...</span>
                      </div>
                      ) : uploadedFile
                        ? `1 ${t("CS_ACTION_FILEUPLOADED")}`
                        : t("CS_ACTION_NO_FILEUPLOADED")
                    }
                  />
                </div>
              )}
              {selectedAction === "VALIDATE_GIS" && (
        <div>
         <CardLabel>{t("CS_ACTION_UPLOAD_LOCATION_FILE")}</CardLabel>
          <UploadFile
            id="pgr-doc"
            accept=".jpg"
            onUpload={selectFile}
            onDelete={() => setUploadedFile(null)}
            message={isUploading ? (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <LoadingSpinner />
                        <span>Uploading...</span>
                      </div>
                      ) : 
              uploadedFile
                ? `1 ${t("CS_ACTION_FILEUPLOADED")}`
                : t("CS_ACTION_NO_FILEUPLOADED")
            }
          />
        </div>
      )}

              {selectedAction === "REJECT" && (
                <div>
                  <CardLabel>{t("COMMENTS")}</CardLabel>
                  <TextArea
                    name="reason"
                    onChange={addComment}
                    value={comments}
                    maxLength={500}
                  />
                  <div style={{ textAlign: "right", fontSize: "12px", color: "#666" }}>
                    {comments.length}/500
                  </div>
                </div>
              )}
            </React.Fragment>
          </Card>
        </Modal>
      )}

      {(toast||error) && (
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
};

export default Action;
