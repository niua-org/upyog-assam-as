import {
    Card,
    CardSubHeader,
    Header,
    Loader,
    Row,
    StatusTable,
    MultiLink,
    Toast,
    CheckBox,
    PopUp,
    HeaderBar,
    ActionBar,
    Menu,
    Modal,
    SubmitBar,
    CardLabel,
    TextInput,
    TextArea,
    CardLabelDesc,
    UploadFile
  } from "@upyog/digit-ui-react-components";
  import React, { useEffect, useState } from "react";
  import { useTranslation } from "react-i18next";
  import { useParams } from "react-router-dom";
  import get from "lodash/get";
  import { isError, useQueryClient } from "react-query";
  import WFApplicationTimeline from "../../pageComponents/WFApplicationTimeline";
  import DocumentsPreview from "../../../../templates/ApplicationDetails/components/DocumentsPreview";

  // import getBPAAcknowledgementData from "../../utils/getBPAAcknowledgementData";
  
  /**
   * `BPAApplicationDetails` is a React component that fetches and displays detailed information for a specific Building Plan Approval (BPA) application.
   * It fetches data for the application using the `useBPASearchAPI` hook and displays the details in sections such as:
   * - Application Number
   * - Applicant Information (name, mobile number, email, father's name, mother's name, PAN, Aadhaar)
   * - Address Information (permanent and correspondence address)
   * - Land Details (construction type, plot details, adjoining owners, future provisions, technical person details)
   * 
   * The component also handles:
   * - Displaying a loading state (via a `Loader` component) while fetching data.
   * - A "toast" notification for any errors or status updates.
   * - Showing downloadable options via `MultiLink` if available.
   * 
   * @returns {JSX.Element} Displays detailed BPA application information with applicant details, address, and land details.
   */
  const BPAApplicationDetails =  () => {
    const { t } = useTranslation();
    const { acknowledgementIds, tenantId } = useParams();
    const [showOptions, setShowOptions] = useState(false);
    const [showToast, setShowToast] = useState(null);
    const { data: storeData } = Digit.Hooks.useStore.getInitData();
    const { tenants } = storeData || {};
    const [displayMenu, setDisplayMenu] = useState(false);
    const { isLoading, isError, error, data, refetch } =Digit.Hooks.obpsv2.useBPASearchApi({
      tenantId,
      filters: { applicationNo: acknowledgementIds },
    });
    const [hasAccess, setHasAccess] = useState(false);
    const { roles } = Digit.UserService.getUser().info;
    const [workflowDetails, setWorkflowDetails] = useState(null);

    useEffect(() => {
      const fetchWorkflow = async () => {
        const details = await Digit.WorkflowService.getByBusinessId(tenantId, acknowledgementIds);
        setWorkflowDetails(details);
      };

      fetchWorkflow();
    }, [acknowledgementIds, tenantId]);

    useEffect(() => {
      const nextActionRoles = workflowDetails?.ProcessInstances?.[0]?.nextActions[0]?.roles || [];
      const access = roles?.some(role => nextActionRoles.includes(role?.code));
      setHasAccess(access);
    }, [roles])

   const client = useQueryClient();
    const [actioneError, setActionError] = useState(null);
    const [popup, setPopup] = useState(false);
    const [selectedAction, setSelectedAction] = useState(null);
    const [assignResponse, setAssignResponse] = useState(null);
    const [toast, setToast] = useState(false);
    const [oldRTPName, setOldRTPName] = useState();
    const [ newRTPName, setNewRTPName ] = useState();
    const bpaApplicationDetail = get(data, "bpa", []);
    const [comments, setComments] = useState("");
    const [uploadedFile, setUploadedFile] = useState(null);
    const bpaId = get(data, "bpa[0].applicationNo", []);
    const [loader, setLoader] = useState(false);
    let bpa_details = (bpaApplicationDetail && bpaApplicationDetail.length > 0 && bpaApplicationDetail[0]) || {};
    // const [submitReport, setSubmitReport] = useState({
    //   bpa_details: bpa_details,
    // });
    const application = bpa_details;
    const [isUploading, setIsUploading] = useState(false);
    const [file, setFile] = useState(null);
    sessionStorage.setItem("bpa", JSON.stringify(application));
    //const SiteReport = Digit.ComponentRegistryService.getComponent("siteReport")
    const mutation = Digit.Hooks.obpsv2.useBPACreateUpdateApi(tenantId, "update");
  
    const { data: reciept_data, isLoading: recieptDataLoading } = Digit.Hooks.useRecieptSearch(
      {
        tenantId: tenantId,
        businessService: "BPA_GMDA_GMC",
        consumerCodes: acknowledgementIds,
        isEmployee: false,
      },
      { enabled: acknowledgementIds ? true : false }
    );
    // useEffect(() => {
    //   if (bpa_details && Object.keys(bpa_details).length > 0) {
    //     setSubmitReport({ bpa_details });
    //   }
    // }, [bpa_details]);
    useEffect(() => {
      (async () => {
        setActionError(null);
        if (file) {
          if (file.size >= 5242880) {
            setActionError(t("CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED"));
          } else {
            try {
              setUploadedFile(null);
              setIsUploading(true);
              // TODO: change module in file storage
              const response = await Digit.UploadServices.Filestorage("OBPSV2", file, Digit.ULBService.getStateId());
              if (response?.data?.files?.length > 0) {
                setUploadedFile(response?.data?.files[0]?.fileStoreId);
              } else {
                setActionError(t("CS_FILE_UPLOAD_ERROR"));
              }
            } catch (err) {
              setActionError(t("CS_FILE_UPLOAD_ERROR"));
            } finally {
              setIsUploading(false);
            }
          }
        }
      })();
    }, [file]);
    async function onAssign(selectedAction, comments, type) {
  setPopup(false);

  try {
    const applicationDetails = data?.bpa?.[0]
    const response = await mutation.mutateAsync({
      BPA: 
      {
        ...applicationDetails,
        workflow: {
          ...applicationDetails.workflow, 
          action: selectedAction,
          comments: comments,
            assignes: null,
            varificationDocuments: uploadedFile ? [
              {
                documentType: file.type,
                fileName: file?.name,
                fileStoreId: uploadedFile,
              },
            ] : null
        },
      },
    

    });

    setAssignResponse(response);
    setToast(true);
    setLoader(true);
    
    // Refresh data to show updated state
    await refetch();
    const updatedWorkflowDetails = await Digit.WorkflowService.getByBusinessId(tenantId, acknowledgementIds);
    setWorkflowDetails(updatedWorkflowDetails);
    window.location.reload();
    setTimeout(() => setToast(false), 10000);
  } catch (err) {
    console.error("Error while assigning:", err);
    setToast(true);
  }
}

     
  //  const refreshData = async () => {
  //      await client.refetchQueries(["fetchInboxData"]);
  //      await workflowDetails.revalidate();
  //      //await revalidateComplaintDetails();
  //    };
   
  //    useEffect(() => {
  //      (async () => {
  //        if (bpaApplicationDetail) {
  //          setLoader(true);
  //          await refreshData();
  //          setLoader(false);
  //        }
  //      })();
  //    }, []);
    /**
     * This function handles the receipt generation and updates the BPA application details
     * with the generated receipt's file store ID.
     */
    async function getRecieptSearch({ tenantId, payments, ...params }) {
      let application = bpaApplicationDetail[0] || {};
      let fileStoreId = application?.paymentReceiptFilestoreId
      if (!fileStoreId) {
        let response = { filestoreIds: [payments?.fileStoreId] };
        response = await Digit.PaymentService.generatePdf(tenantId, { Payments: [{ ...payments }] }, "bpa-services-receipt");
        const updatedApplication = {
          ...application,
          paymentReceiptFilestoreId: response?.filestoreIds[0]
        };
        await mutation.mutateAsync({
          BPA: [updatedApplication]
        });
        fileStoreId = response?.filestoreIds[0];
        refetch();
      }
      const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: fileStoreId });
      window.open(fileStore[fileStoreId], "_blank");
    }
  
    let dowloadOptions = [];
    dowloadOptions.push({
      label: t("BPA_DOWNLOAD_ACKNOWLEDGEMENT"),
      onClick: () => getAcknowledgementData(),
    });
   const Heading = (props) => {
     return <h1 className="heading-m">{props.label}</h1>;
   };
   const LoadingSpinner = () => (
    <div className="loading-spinner" />
  );
   function closeToast() {
    setToast(false);
  }
   
   const CloseBtn = (props) => {
     return (
       <div className="icon-bg-secondary" onClick={props.onClick}>
           <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
             <path d="M0 0h24v24H0V0z" fill="none" />
             <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
           </svg>
         
       </div>
     );
   };
    // const getAcknowledgementData = async () => {
    //   const applications = application || {};
    //   const tenantInfo = tenants.find((tenant) => tenant.code === applications.tenantId);
    //   const acknowldgementDataAPI = await getBPAAcknowledgementData({ ...applications }, tenantInfo, t);
    //   Digit.Utils.pdf.generate(acknowldgementDataAPI);
    // };
    function addComment(e) {
    setActionError(null);
    setComments(e.target.value);
  }
  function selectfile(e) {
    setFile(e.target.files[0]);
  }
  function redirectToPage(redirectingUrl){
      window.location.href=redirectingUrl;
    }
  
    function onActionSelect(action) {
    setSelectedAction(action);
    switch (action) {
      case "NEWRTP":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "REJECT":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "SEND":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "EDIT":
        let url=window.location.href;
        let redirectingUrl= url.split("/application/")[0] + "/editApplication/" + url.split("/application/")[1].split("/")[0];

        redirectToPage(redirectingUrl);    
        break;
      case "APPLY_FOR_SCRUTINY":
        let scrutinyurl=window.location.href;
        let scrutinyRedirectingUrl= scrutinyurl.split("/application/")[0] +  `/rtp/apply/home?applicationNo=${bpaId}`;
        redirectToPage(scrutinyRedirectingUrl);
        break;
      case "APPROVE":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "ACCEPT":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "SEND_BACK_TO_RTP":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "SUBMIT_REPORT":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "SEND_BACK_TO_GMDA":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "RECOMMEND_TO_CEO":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "VALIDATE_GIS":
        setPopup(true);
        setDisplayMenu(false);
        break;
      case "PAY":
        let redirectURL = `${window.location.origin}/upyog-ui/citizen/payment/my-bills/BPA.PLANNING_PERMIT_FEE/${bpaId}`;
        redirectToPage(redirectURL);
        break;
      default:
        setDisplayMenu(false);
    }
  }

    if (isLoading) {
      return <Loader />;
    }
  
    if (reciept_data && reciept_data?.Payments.length > 0 && recieptDataLoading == false) {
      dowloadOptions.push({
        label: t("BPA_FEE_RECEIPT"),
        onClick: () => getRecieptSearch({ tenantId: reciept_data?.Payments[0]?.tenantId, payments: reciept_data?.Payments[0] }),
      });
    }
  
    // Extract data from response structure
    const landInfo = bpa_details?.landInfo || {};
    const owners = landInfo?.owners || [];
    const areaMapping= bpa_details?.areaMapping || {};
    const primaryOwner = owners.length > 0 ? owners[0] : {};
    const address = landInfo?.address || {};
    const permanentAddress = primaryOwner?.permanentAddress || {};
    const additionalDetails = bpa_details?.additionalDetails || {};
    const adjoiningOwners = additionalDetails?.adjoiningOwners || {};
  
    return (
      <React.Fragment>
        <div>
          <div className="cardHeaderWithOptions" style={{ marginRight: "auto", maxWidth: "960px" }}>
            <Header styles={{ fontSize: "32px" }}>{t("BPA_APPLICATION_DETAILS")}</Header>
            {/* {dowloadOptions && dowloadOptions.length > 0 && (
              <MultiLink
                className="multilinkWrapper"
                onHeadClick={() => setShowOptions(!showOptions)}
                displayOptions={showOptions}
                options={dowloadOptions}
              />
            )} */}
          </div>
          <Card>
          {/* {window.location.href.includes("/employee/") && bpa_details?.status==="PENDING_GMDA_ENGINEER" && (
            <SiteReport submitReport={submitReport} onChange={setSubmitReport}/>
          )} */}
            <StatusTable>
              <Row className="border-none" label={t("BPA_APPLICATION_NO")} text={bpa_details?.applicationNo || t("CS_NA")} />
            </StatusTable>
                    
            <CardSubHeader style={{ fontSize: "24px" }}>{t("BPA_AREA_MAPPING")}</CardSubHeader>
            <StatusTable>
              <Row
                label={t("DISTRICT")}
                text={t(areaMapping?.district) || t("CS_NA")}
              />
              <Row
                label={t("PLANNING_AREA")}
                text={t(areaMapping?.planningArea) || t("CS_NA")}
              />
              <Row
                label={t("PP_AUTHORITY")}
                text={t(areaMapping?.planningPermitAuthority) || t("CS_NA")}
              />
              <Row
                label={t("BP_AUTHORITY")}
                text={t(areaMapping?.buildingPermitAuthority) || t("CS_NA")}
              />
              <Row
                label={t("REVENUE_VILLAGE")}
                text={t(areaMapping?.revenueVillage) || t("CS_NA")}
              />
              <Row
                label={t("MOUZA")}
                text={t(areaMapping?.mouza) || t("CS_NA")}
              />
              <Row
                label={t("WARD")}
                text={t(areaMapping?.ward) || t("CS_NA")}
              />
            </StatusTable>
  
            <CardSubHeader style={{ fontSize: "24px" }}>{t("BPA_APPLICANT_DETAILS")}</CardSubHeader>
            <StatusTable>
              <Row
                label={t("BPA_APPLICANT_NAME")}
                text={primaryOwner?.name || t("CS_NA")}
              />
              <Row
                label={t("BPA_MOBILE_NO")}
                text={primaryOwner?.mobileNumber || t("CS_NA")}
              />
              <Row
                label={t("BPA_ALT_MOBILE_NO")}
                text={primaryOwner?.altContactNumber || t("CS_NA")}
              />
              <Row
                label={t("BPA_EMAIL_ID")}
                text={primaryOwner?.emailId || t("CS_NA")}
              />
               <Row
                label={t("BPA_GENDER")}
                text={t(primaryOwner?.gender) || t("CS_NA")}
              />
              <Row
                label={t("BPA_GUARDIAN")}
                text={primaryOwner?.fatherOrHusbandName || t("CS_NA")}
              />
               <Row
                label={t("BPA_RELATIONSHIP")}
                text={t(primaryOwner?.relationship) || t("CS_NA")}
              />
              <Row
                label={t("BPA_MOTHER_NAME")}
                text={primaryOwner?.motherName || t("CS_NA")}
              />
              <Row
                label={t("BPA_PAN_CARD")}
                text={primaryOwner?.pan || t("CS_NA")}
              />
              <Row
                label={t("BPA_AADHAAR_CARD")}
                text={primaryOwner?.aadhaarNumber || t("CS_NA")}
              />
            </StatusTable>
  
            <CardSubHeader style={{ fontSize: "24px" }}>{t("BPA_ADDRESS_DETAILS")}</CardSubHeader>
            <CardSubHeader style={{ fontSize: "20px" }}>{t("BPA_SITE_ADDRESS")}</CardSubHeader>
            <StatusTable>
              <Row
                label={t("BPA_HOUSE_NO")}
                text={permanentAddress?.houseNo || t("CS_NA")}
              />
              <Row
                label={t("BPA_ADDRESS_LINE_1")}
                text={permanentAddress?.addressLine1 || t("CS_NA")}
              />
              <Row
                label={t("BPA_ADDRESS_LINE_2")}
                text={permanentAddress?.addressLine2 || t("CS_NA")}
              />
              <Row
                label={t("BPA_LANDMARK")}
                text={permanentAddress?.landmark || t("CS_NA")}
              />
              <Row
                label={t("BPA_DISTRICT")}
                text={t(permanentAddress?.district) || t("CS_NA")}
              />
              {/* <Row
                label={t("BPA_CITY")}
                text={permanentAddress?.locality?.name || t("CS_NA")}
              /> */}
              <Row
                label={t("BPA_STATE")}
                text={permanentAddress?.state || t("CS_NA")}
              />
              <Row
                label={t("BPA_PIN_CODE")}
                text={permanentAddress?.pincode || t("CS_NA")}
              />
            </StatusTable>
  
            <CardSubHeader style={{ fontSize: "20px" }}>{t("BPA_CORRESPONDENCE_ADDRESS")}</CardSubHeader>
            {primaryOwner?.correspondenceAddress ? (
              <StatusTable style={{ marginTop: "16px" }}>
                <Row
                  label={t("BPA_HOUSE_NO")}
                  text={primaryOwner?.correspondenceAddress?.houseNo || t("CS_NA")}
                />
                <Row
                  label={t("BPA_ADDRESS_LINE_1")}
                  text={primaryOwner?.correspondenceAddress?.addressLine1 || t("CS_NA")}
                />
                <Row
                  label={t("BPA_ADDRESS_LINE_2")}
                  text={primaryOwner?.correspondenceAddress?.addressLine2 || t("CS_NA")}
                />
                <Row
                  label={t("BPA_DISTRICT")}
                  text={t(primaryOwner?.correspondenceAddress?.district) || t("CS_NA")}
                />
                {/* <Row
                  label={t("BPA_CITY")}
                  text={primaryOwner?.correspondenceAddress?.locality?.name || t("CS_NA")}
                /> */}
                <Row
                  label={t("BPA_STATE")}
                  text={primaryOwner?.correspondenceAddress?.state || t("CS_NA")}
                />
                <Row
                  label={t("BPA_PIN_CODE")}
                  text={primaryOwner?.correspondenceAddress?.pincode || t("CS_NA")}
                />
              </StatusTable>
            ) : (
              <div style={{ marginTop: "16px" }}>
                <CheckBox
                  label={t("BPA_SAME_AS_SITE_ADDRESS")}
                  checked={true}
                  disabled={true}
                />
              </div>
            )}
  
            <CardSubHeader style={{ fontSize: "24px" }}>{t("BPA_LAND_DETAILS")}</CardSubHeader>
            <StatusTable>
              <Row
                label={t("BPA_OLD_DAG_NUMBER")}
                text={landInfo?.oldDagNumber || t("CS_NA")}
              />
              <Row
                label={t("BPA_NEW_DAG_NUMBER")}
                text={landInfo?.newDagNumber || t("CS_NA")}
              />
              <Row
                label={t("BPA_OLD_PATTA_NUMBER")}
                text={landInfo?.oldPattaNumber || t("CS_NA")}
              />
              <Row
                label={t("BPA_NEW_PATTA_NUMBER")}
                text={landInfo?.newPattaNumber || t("CS_NA")}
              />
              <Row
                label={t("BPA_TOTAL_PLOT_AREA")}
                text={landInfo?.totalPlotArea ? `${landInfo.totalPlotArea} sq. m.` : t("CS_NA")}
              />
            </StatusTable>
  
            <CardSubHeader style={{ fontSize: "20px" }}>{t("BPA_ADJOINING_LAND_OWNERS")}</CardSubHeader>
            <StatusTable>
              <Row
                label={t("BPA_NORTH")}
                text={adjoiningOwners?.north || t("CS_NA")}
              />
              <Row
                label={t("BPA_SOUTH")}
                text={adjoiningOwners?.south || t("CS_NA")}
              />
              <Row
                label={t("BPA_EAST")}
                text={adjoiningOwners?.east || t("CS_NA")}
              />
              <Row
                label={t("BPA_WEST")}
                text={adjoiningOwners?.west || t("CS_NA")}
              />
            </StatusTable>
  
            <CardSubHeader style={{ fontSize: "20px" }}>{t("BPA_FUTURE_PROVISIONS")}</CardSubHeader>
            <StatusTable>
              <Row
                label={t("BPA_VERTICAL_EXTENSION")}
                text={t(additionalDetails?.futureProvisions?.verticalExtension?.code) || t("CS_NA")}
              />
              
              {/* Vertical Extension Area - Only show if Vertical Extension is YES */}
              {(additionalDetails?.futureProvisions?.verticalExtension?.code === "YES") && (
                <Row
                  label={t("BPA_VERTICAL_EXTENSION_AREA")}
                  text={additionalDetails?.futureProvisions?.verticalExtensionArea ? `${additionalDetails.futureProvisions.verticalExtensionArea} floors` : t("CS_NA")}
                />
              )}
              
              {/* Horizontal Extension - Only show if YES */}
              <Row
                label={t("BPA_HORIZONTAL_EXTENSION")}
                text={t(additionalDetails?.futureProvisions?.horizontalExtension?.code) || t("CS_NA")}
              />
              
              {/* Horizontal Extension Area - Only show if Horizontal Extension is YES */}
              {(additionalDetails?.futureProvisions?.horizontalExtension?.code === "YES") && (
                <Row
                  label={t("BPA_HORIZONTAL_EXTENSION_AREA")}
                  text={additionalDetails?.futureProvisions?.horizontalExtensionArea ? `${additionalDetails.futureProvisions.horizontalExtensionArea} sq. m.` : t("CS_NA")}
                />
              )}
              
              {/* Always show these fields */}
              <Row
                label={t("BPA_TOD_BENEFITS")}
                text={t(additionalDetails?.todBenefits) || t("CS_NA")}
              />
              <Row
                label={t("BPA_TDR_USED")}
                text={t(additionalDetails?.tdrUsed) || t("CS_NA")}
              />
              
              {/* TOD Zone - Only show if TOD Benefits is YES */}
              {(additionalDetails?.todBenefits === "YES" || additionalDetails?.todBenefits?.code === "YES") && additionalDetails?.todZone && (
                <Row
                  label={t("BPA_TOD_ZONE")}
                  text={t(additionalDetails?.todZone?.code) || t("CS_NA")}
                />
              )}
            </StatusTable>

            <StatusTable style={{ marginTop: "16px" }}>
              <Row
                label={t("BPA_RTP_CATEGORY")}
                text={t(bpa_details?.rtpDetails?.rtpCategory) || t("CS_NA")}
              />
              <Row
                label={t("BPA_REGISTERED_TECHNICAL_PERSON")}
                text={t(bpa_details?.rtpDetails?.rtpName) || t("CS_NA")}
              />
              <Row
                label={t("BPA_OCCUPANCY_TYPE")}
                text={t(landInfo?.units?.[0]?.occupancyType) || t("CS_NA")}
              />
              {landInfo?.documents && landInfo.documents.length > 0 && (
              <div style={{ marginTop: "16px" }}>
                <DocumentsPreview
                  documents={[{
                    values: landInfo.documents.map(doc => ({
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
            {popup ? (
               <Modal
      headerBarMain={
        <Heading
          label={
             t(`${selectedAction}`)
          }
        />
      }
      headerBarEnd={<CloseBtn onClick={() => setPopup(false)} />}
      actionCancelLabel={t("CS_COMMON_CANCEL")}
      actionCancelOnSubmit={() => setPopup(false)}
      actionSaveLabel={
        t("CS_COMMON_SUBMIT")
      }
      actionSaveOnSubmit={() => {
        if(selectedAction==="APPROVE"||selectedAction==="ACCEPT"||selectedAction==="REJECT"||selectedAction==="SEND"||selectedAction==="VALIDATE_GIS" ||selectedAction==="SEND_BACK_TO_RTP" || selectedAction==="SUBMIT_REPORT" || selectedAction==="RECOMMEND_TO_CEO" || selectedAction==="SEND_BACK_TO_GMDA")
          onAssign(selectedAction, comments, "Edit");
      if(selectedAction==="NEWRTP" &&!oldRTPName)
        setActionError(t("CS_OLD_RTP_NAME_MANDATORY"))
      if(selectedAction==="NEWRTP" && !newRTPName)
        setActionError(t("CS_NEW_RTP_NAME_MANDATORY"))

        
       
      }}
      error={actioneError}
      setError={setActionError}
    >
      <Card>
  <React.Fragment>
    {(selectedAction === "APPROVE" || selectedAction === "ACCEPT" || selectedAction === "SEND" || selectedAction === "REJECT"|| selectedAction==="SEND_BACK_TO_RTP" || selectedAction==="SUBMIT_REPORT" || selectedAction==="RECOMMEND_TO_CEO" || selectedAction==="SEND_BACK_TO_GMDA") && (
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
          id="pgr-doc"
          accept=".jpg"
          onUpload={selectfile}
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
  {selectedAction === "VALIDATE_GIS" && (
        <div>
         <CardLabel>{t("CS_ACTION_UPLOAD_LOCATION_FILE")}</CardLabel>
          <UploadFile
            id="pgr-doc"
            accept=".jpg"
            onUpload={selectfile}
            onDelete={() => setUploadedFile(null)}
            message={
              uploadedFile
                ? `1 ${t("CS_ACTION_FILEUPLOADED")}`
                : t("CS_ACTION_NO_FILEUPLOADED")
            }
          />
        </div>
      )}

    {selectedAction === "NEW_RTP" && (
      <div>
        <CardLabel></CardLabel>
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
          id="pgr-doc"
          accept=".jpg"
          onUpload={selectfile}
          onDelete={() => setUploadedFile(null)}
          message={
            uploadedFile
              ? `1 ${t("CS_ACTION_FILEUPLOADED")}`
              : t("CS_ACTION_NO_FILEUPLOADED")
          }
        />
      </div>
    )}
  </React.Fragment>
</Card>

    </Modal>
            ):null}
  
            <WFApplicationTimeline application={application} id={application?.applicationNo} userType={"citizen"} />
            {showToast && (
              <Toast
                error={showToast.key}
                label={t(showToast.label)}
                style={{ bottom: "0px" }}
                onClose={() => {
                  setShowToast(null);
                }}
              />
            )}
            {toast && <Toast label={t(assignResponse ? `CS_ACTION_UPDATE_${selectedAction}_TEXT` : "CS_ACTION_ASSIGN_FAILED")} onClose={closeToast} />}
             {hasAccess && <ActionBar>
              {displayMenu && workflowDetails?.ProcessInstances?.[0]?.nextActions ? (
                  <Menu options={workflowDetails?.ProcessInstances?.[0]?.nextActions.map((action) => action.action)} t={t} onSelect={onActionSelect} />
              ) : null}
              <SubmitBar label={t("WF_TAKE_ACTION")} onSubmit={() => setDisplayMenu(!displayMenu)} />
            </ActionBar>
             }
          </Card>
        </div>
      </React.Fragment>
    );
  };

  export default BPAApplicationDetails;