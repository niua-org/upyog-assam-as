import React, { useEffect, useState, Fragment } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { FormComposer, Header, Loader, MultiLink, Toast, ActionBar, Menu, SubmitBar, LinkButton } from "@upyog/digit-ui-react-components";
import ApplicationDetailsTemplate from "../../../../../templates/ApplicationDetails";
import useWorkflowDetails from "../../../../../../libraries/src/hooks/workflow";
import useBPAV2DetailsPage from "../../../../../../libraries/src/hooks/obpsv2/useBPAV2DetailsPage";
import { newConfig as newConfigSubmitReport } from "../../../config/submitReportConfig";
import useApplicationActions from "../../../../../../libraries/src/hooks/obpsv2/useApplicationActions";
const BPAEmployeeDetails = () => {
  const { t } = useTranslation();
  const { acknowledgementIds, tenantId } = useParams();
  const [showOptions, setShowOptions] = useState(false);
  const [showToast, setShowToast] = useState(null);
  //const [workflowDetails, setWorkflowDetails] = useState(null);
  const [displayMenu, setDisplayMenu] = useState(false);
  const { roles } = Digit.UserService.getUser().info;
  const isMobile = window.Digit.Utils.browser.isMobile();
  const { data = {}, isLoading } = useBPAV2DetailsPage(tenantId, { applicationNo: acknowledgementIds });
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
  let downloadOptions = [];
  if (application?.paymentReceiptFilestoreId) {
    downloadOptions.push({
      label: t("BPA_FEE_RECEIPT"),
      onClick: async () => {
        const fileStore = await Digit.PaymentService.printReciept(tenantId, {
          fileStoreIds: application.paymentReceiptFilestoreId,
        });
        window.open(fileStore[application.paymentReceiptFilestoreId], "_blank");
      },
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
          {/* {downloadOptions.length > 0 && (
            <MultiLink
              className="multilinkWrapper"
              onHeadClick={() => setShowOptions(!showOptions)}
              displayOptions={showOptions}
              options={downloadOptions}
            />
          )} */}
          </div>
          
      <LinkButton label={t("VIEW_TIMELINE")} style={{ color:"#A52A2A"}} onClick={handleViewTimeline}></LinkButton>
        </div>
        {data?.applicationData?.status === "PENDING_GMDA_ENGINEER" && (userInfo?.info?.roles.filter(role => role.code === "BPA_ENGINEER")).length>0 && <FormComposer
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
        defaultValues={defaultValues}
        onFormValueChange={onFormValueChange}
        breaklineStyle={{ border: "0px" }}
        className={"employeeCard-override"}
        cardClassName={"employeeCard-override"}
      />}
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
        />
      </div>
      </div>
    </Fragment>
  );
};

export default BPAEmployeeDetails;
