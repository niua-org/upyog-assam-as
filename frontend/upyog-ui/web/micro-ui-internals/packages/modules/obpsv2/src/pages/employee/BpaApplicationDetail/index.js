import React, { useEffect, useState, Fragment } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { FormComposer, Header, Loader, MultiLink, Toast, ActionBar, Menu, SubmitBar, LinkButton } from "@upyog/digit-ui-react-components";
import ApplicationDetailsTemplate from "../../../../../templates/ApplicationDetails";
import useWorkflowDetails from "../../../../../../libraries/src/hooks/workflow";
import useBPAV2DetailsPage from "../../../../../../libraries/src/hooks/obpsv2/useBPAV2DetailsPage";
import { newConfig as newConfigSubmitReport } from "../../../config/submitReportConfig";
import useApplicationActions from "../../../../../../libraries/src/hooks/obpsv2/useApplicationActions";
import { convertDateToEpoch } from "../../../utils";
import { OBPSV2Services } from "../../../../../../libraries/src/services/elements/OBPSV2";
import { getEstimatePayload } from "../../../utils";
const BPAEmployeeDetails = () => {
  const { t } = useTranslation();
  const { acknowledgementIds, tenantId } = useParams();
  const [showOptions, setShowOptions] = useState(false);
  const [showToast, setShowToast] = useState(null);
  //const [workflowDetails, setWorkflowDetails] = useState(null);
  const [isSiteFormValid, setIsSiteFormValid] = useState(false);

  
  const [displayMenu, setDisplayMenu] = useState(false);
  const { roles } = Digit.UserService.getUser().info;
  const isMobile = window.Digit.Utils.browser.isMobile();
  const { data = {}, isLoading } = useBPAV2DetailsPage(tenantId, { applicationNo: acknowledgementIds });
  const nocFilters = {
      sourceRefId: acknowledgementIds,
    };
  const nocData = Digit.Hooks.noc.useNOCSearchApplication(tenantId, nocFilters);
  const [canSubmit, setSubmitValve] = useState(false);
  const [viewTimeline, setViewTimeline]=useState(false);
  const defaultValues = {};
  const userInfo = Digit.UserService.getUser();
  const application = data?.bpa?.[0] || {};
  let configs = newConfigSubmitReport;
  let workflowDetails = useWorkflowDetails({
    tenantId: tenantId,
    id: acknowledgementIds,
    moduleCode: "OBPSV2",
  });
  
      // Safely access and map the NOC array
  const mappedNocData = nocData?.data?.Noc?.map(item => ({
    id: item.id,
    tenantId: item.tenantId,
    applicationNo: item.applicationNo,
    applicationType: item.applicationType,
    nocType: item.nocType,
    source: item.source,
    sourceRefId: item.sourceRefId,
    applicationStatus: item.applicationStatus,
    applicantName: item.additionalDetails?.applicantName,
    workflowCode: item.additionalDetails?.workflowCode,
    submittedOn: item.additionalDetails?.SubmittedOn,
    approvalDate: item.additionalDetails?.approvalDate,
    rejectionDate: item.additionalDetails?.rejectionDate,
  })) || [];


  const handlePlanningPermitOrder = async () => {
    const application = data?.applicationData;
    let fileStoreId =application?.signedPpFileStoreId || application?.ppFileStoreId;
    const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.applicationData?.edcrNumber });
        let edcrDetail = edcrResponse?.edcrDetail;
        const gisResponse = await Digit.OBPSV2Services.gisSearch({
          GisSearchCriteria: {
            applicationNo: acknowledgementIds,
            tenantId: tenantId,
            status: "SUCCESS"
          }
        });

    if (!fileStoreId) {
      const response = await Digit.PaymentService.generatePdf(
        tenantId,
        { Bpa: [{...application, edcrDetail, gisResponse}] },
        "bpaPlanningPermit"
      );

      fileStoreId = response?.filestoreIds?.[0];

      const updatedApplication = {
        ...application,
        ppFileStoreId: fileStoreId,
        additionalDetails: {
          ...application.additionalDetails,
          UPDATE_FILESTORE_ID: true
        }
      };

      await Digit.OBPSV2Services.update({
        BPA: updatedApplication
      });
      
      // Update local data to reflect the new fileStoreId
      data.applicationData = updatedApplication;
    }

    const fileStore = await Digit.PaymentService.printReciept(
      tenantId,
      { fileStoreIds: fileStoreId }
    );

    window.open(fileStore[fileStoreId], "_blank");
  };

  const handleBuildingPermitOrder = async () => {
    const application = data?.applicationData;
    let fileStoreId = application?.signedBpFileStoreId || application?.bpFileStoreId;
    const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.applicationData?.edcrNumber });
        let edcrDetail = edcrResponse?.edcrDetail;
        const gisResponse = await Digit.OBPSV2Services.gisSearch({
          GisSearchCriteria: {
            applicationNo: acknowledgementIds,
            tenantId: tenantId,
            status: "SUCCESS"
          }
        });
    if (!fileStoreId) {
      const response = await Digit.PaymentService.generatePdf(
        tenantId,
        { Bpa: [{...application, edcrDetail, gisResponse}] },
        "bpaBuildingPermit"
      );

      fileStoreId = response?.filestoreIds?.[0];

      const updatedApplication = {
        ...application,
        bpFileStoreId: fileStoreId,
        additionalDetails: {
          ...application.additionalDetails,
          UPDATE_FILESTORE_ID: true
        }
      };

      await Digit.OBPSV2Services.update({
        BPA: updatedApplication
      });
      
      // Update local data to reflect the new fileStoreId
      data.applicationData = updatedApplication;
    }

    const fileStore = await Digit.PaymentService.printReciept(
      tenantId,
      { fileStoreIds: fileStoreId }
    );

    window.open(fileStore[fileStoreId], "_blank");
  };
  // Occupancy Certificate Download
  async function getBuildingOccupancy(mode="download") {
    const application = data?.applicationData;
    let fileStoreId =application?.signedOcFileStoreId || application?.ocFileStoreId;
      if (!fileStoreId) {
      let currentDate = new Date();
      let applicationNo = data?.bpa?.[0]?.applicationNo;
      let bpaResponse = await Digit.OBPSV2Services.search({tenantId,filters: { applicationNo }});
      let bpaData = bpaResponse?.bpa?.[0];  
      bpaData.additionalDetails.runDate = convertDateToEpoch(
        `${currentDate.getFullYear()}-${currentDate.getMonth() + 1}-${currentDate.getDate()}`
      ); 
      const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam",{ edcrNumber: data?.bpa?.[0]?.edcrNumber });
      let edcrData = edcrResponse?.edcrDetail?.[0];
      let requestData = { ...bpaData, edcrDetail: [{ ...edcrData }] };
      let response = await Digit.PaymentService.generatePdf(tenantId,{ Bpa: [requestData] },"bpa-occupancy-certificate");
      fileStoreId = response?.filestoreIds?.[0];
      const updatedApplication = {
        ...application,
        ocFileStoreId: fileStoreId,
        additionalDetails: {
          ...application.additionalDetails,
          UPDATE_FILESTORE_ID:true
        }
      };
      await Digit.OBPSV2Services.update({
        BPA: updatedApplication
      });
      data.applicationData = updatedApplication;
    }
    const fileStore = await Digit.PaymentService.printReciept(tenantId, {
      fileStoreIds: fileStoreId
    });
    window.open(fileStore[fileStoreId], "_blank");
  }

  useEffect(() => {
  const interval = setInterval(() => {
    const stored = sessionStorage.getItem("SITE_REPORT_VALID");
    if (stored !== null) {
      const value = JSON.parse(stored);
      setIsSiteFormValid(value);
    }
  }, 300); // 300ms

  return () => clearInterval(interval);
}, []);

  const applicationStatus = workflowDetails?.data?.actionState?.state || "";

  const isSubmitDisabled = applicationStatus === "PENDING_DA_ENGINEER" && !isSiteFormValid;


  let downloadOptions = [];
  if (data?.collectionBillDetails?.[0]) {
    downloadOptions.push({
      label: t("BPA_FEE_RECEIPT"),
      onClick: async () => {
        let response = null
        let paymentData = data?.collectionBillDetails
        const payload = getEstimatePayload({
          tenantId:data?.collectionBillDetails?.[0]?.tenantId,
          applicationNo: acknowledgementIds,
          edcrNumber: data?.applicationData?.edcrNumber,
          feeType:"PLANNING_PERMIT_FEE"
        });
        let estimateResponse = await OBPSV2Services.estimate(payload, true, null);
        
      const updatedPayments = [...data?.collectionBillDetails];
      updatedPayments[0] = {
        ...updatedPayments[0],
        paymentDetails: updatedPayments[0].paymentDetails.map(detail => ({
          ...detail,
          additionalDetails: {
            ...detail.additionalDetails,
            feebreakup: estimateResponse?.Calculations?.[0]?.taxHeadEstimates
          }
        }))
      };
        if(data?.collectionBillDetails?.[0]?.fileStoreId){
          response = data?.collectionBillDetails?.[0]?.fileStoreId          
        }
        else{
           response = await Digit.PaymentService.generatePdf(tenantId, { Payments: [updatedPayments[0]]}, "bpa-receipt");
        }
        const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: response?.filestoreIds?.[0] ||response });
        window.open(fileStore[response?.filestoreIds?.[0]] || fileStore[response], "_blank");
      },
    });
  }
  if (data?.collectionBillDetails?.[1]) {
    downloadOptions.push({
      label: t("BPA_BUILDING_FEE_RECEIPT"),
      onClick: async () => {
        let response = null
        const payload = getEstimatePayload({
          tenantId:data?.collectionBillDetails?.[1]?.tenantId,
          applicationNo: acknowledgementIds,
          edcrNumber: data?.applicationData?.edcrNumber,
          feeType:"BUILDING_PERMIT_FEE"
        });
       
        let estimateResponse = await OBPSV2Services.estimate(payload, true, null);
        
      const updatedPayments = [...data?.collectionBillDetails];
      updatedPayments[1] = {
        ...updatedPayments[1],
        paymentDetails: updatedPayments[1].paymentDetails.map(detail => ({
          ...detail,
          additionalDetails: {
            ...detail.additionalDetails,
            feebreakup: estimateResponse?.Calculations?.[0]?.taxHeadEstimates
          }
        }))
      };
        if(data?.collectionBillDetails?.[1]?.fileStoreId){
          response = data?.collectionBillDetails?.[1]?.fileStoreId          
        }
        else{
           response = await Digit.PaymentService.generatePdf(tenantId, { Payments: [updatedPayments[1]]}, "bpa-receipt");
        }
        const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: response?.filestoreIds?.[0] ||response });
        window.open(fileStore[response?.filestoreIds?.[0]] || fileStore[response], "_blank");
      },
    });
  }
  if(data?.collectionBillDetails?.[0]){
    downloadOptions.push({
      order: 3,
      label: t("BPA_PLANNING_PERMIT_ORDER"),
      onClick: handlePlanningPermitOrder,
    });
  }
  if(data?.collectionBillDetails?.length > 1){
    downloadOptions.push({
      order: 4,
      label: t("BPA_BUILDING_PERMIT_ORDER"),
      onClick: handleBuildingPermitOrder,
    });
  }
  if(data?.collectionBillDetails?.length > 1){
    downloadOptions.push({
      order: 3,
      label: t("BPA_OCCUPANCY_CERTIFICATE"),
      onClick: () => getBuildingOccupancy({tenantId: data?.applicationData?.tenantId},"bpa-occupancy-certificate"),
    });
  }
  function checkHead(head) {
    if (head === "ES_NEW_APPLICATION_LOCATION_DETAILS") {
      return "TL_CHECK_ADDRESS";
    } else if (head === "ES_NEW_APPLICATION_OWNERSHIP_DETAILS") {
      return "TL_OWNERSHIP_DETAILS_HEADER";
    } else {
      return head;
    }
  }
  const handleViewTimeline=()=>{
    setViewTimeline(true);
      const timelineSection=document.getElementById('timeline');
      if(timelineSection){
        timelineSection.scrollIntoView({behavior: 'smooth'});
      } 
  };
  const {
    isLoading: updatingApplication,
    isError: updateApplicationError,
    data: updateResponse,
    error: updateError,
    mutate,
  } = useApplicationActions(tenantId);
  const onFormValueChange = (setValue, formData, formState) => {
    setSubmitValve(!Object.keys(formState.errors).length);
  };

  if (isLoading) return <Loader />;

  return (
    <Fragment>
      <div className={"employee-main-application-details"}>
      <div className={"employee-application-detailsNew"} style={{marginBottom: "15px",height:"auto !important", maxHeight:"none !important"}}>
      <Header styles={{marginLeft:"0px", paddingTop: "10px", fontSize: "32px"}}>{t("CS_TITLE_APPLICATION_DETAILS")}</Header>
        
          <div style={{zIndex: "10",display:"flex",flexDirection:"row-reverse",alignItems:"center",marginTop:"-25px"}}>
               
               <div style={{zIndex: "10",  position: "relative"}}>
          {downloadOptions.length > 0 && (
            <MultiLink
              className="multilinkWrapper"
              onHeadClick={() => setShowOptions(!showOptions)}
              displayOptions={showOptions}
              options={downloadOptions}
              downloadBtnClassName={"employee-download-btn-className"}
              //optionsClassName={"employee-options-btn-className"}
            />
          )}
          </div>
          
      <LinkButton label={t("VIEW_TIMELINE")} style={{ color:"#A52A2A"}} onClick={handleViewTimeline}></LinkButton>
        </div>
        {(data?.applicationData?.status === "PENDING_DA_ENGINEER") && 
        (userInfo?.info?.roles.some(role => ["BPA_ENGINEER", "BPA_ENGINEER_DA"].includes(role.code)) && 
        <FormComposer
        heading={t("")}
        isDisabled={!canSubmit}
        config={configs.map((config) => {
          return {
            ...config,
            body: config.body.filter((a) => {
              return !a.hideInEmployee;
            }),
            head: checkHead(config.head),
          };
        })}
        fieldStyle={{ marginRight: 0 }}
        submitInForm={false}
        defaultValues={{
          ...defaultValues,
          bpaData: data,
        }}
        onFormValueChange={onFormValueChange}
        breaklineStyle={{ border: "0px" }}
        className={"employeeCard-override"}
        cardClassName={"employeeCard-override"}
      />
          )
        }
        <ApplicationDetailsTemplate
          applicationDetails={data}
          isLoading={isLoading}
          isDataLoading={isLoading}
          applicationData={data?.applicationData}
          mutate={mutate}
          workflowDetails={workflowDetails}
          businessService={workflowDetails?.data?.applicationBusinessService || application.businessService}
          moduleCode="OBPSV2"
          showToast={showToast}
          ActionBarStyle={isMobile?{}:{paddingRight:"50px"}}
          MenuStyle={isMobile?{}:{right:"50px"}}
          setShowToast={setShowToast}
          closeToast={() => setShowToast(null)}
          statusAttribute={"state"}
          timelineStatusPrefix={`WF_${workflowDetails?.data?.applicationBusinessService ? workflowDetails?.data?.applicationBusinessService : data?.applicationData?.businessService}_`}
          nocDetails={mappedNocData}
          isSubmitDisabled={isSubmitDisabled}
        />
      </div>
      </div>
    </Fragment>
  );
};

export default BPAEmployeeDetails;
