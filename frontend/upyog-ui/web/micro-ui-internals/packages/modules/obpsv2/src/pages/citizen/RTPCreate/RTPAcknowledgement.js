import { Banner, Card, CardText, LinkButton, SubmitBar, Toast } from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { Link, useHistory, useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

const RTPAcknowledgement = (props) => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();
  const [showToast, setShowToast] = useState(false);
  const [bpaUpdateResponse, setBpaUpdateResponse] = useState(null);
  
  // Extract applicationNo from URL
  const urlParams = new URLSearchParams(location.search);
  const applicationNo = urlParams.get('applicationNo');
  const tenantId = urlParams.get('tenantId');
  useEffect(() => { 
    if (props?.data?.type == "ERROR" && !showToast) setShowToast(true); 
  }, [props?.data?.data]);

  if (props?.data?.type == "ERROR") {
    return (
      <Card style={{ padding: "0px" }}>
        <Banner
          message={t("CS_BPA_APPLICATION_FAILED")}
          applicationNumber={""}
          info={""}
          successful={false}
          infoStyles={{ fontSize: "18px", lineHeight: "21px", fontWeight: "bold", textAlign: "center", padding: "0px 15px" }}
          applicationNumberStyles={{ fontSize: "24px", lineHeight: "28px", fontWeight: "bold", marginTop: "10px" }}
          style={{ width: "100%", padding: "10px" }}
        />
        <div style={{ padding: "10px", paddingBottom: "10px" }}>
          <Link to={`/upyog-ui/citizen`} >
            <SubmitBar label={t("CORE_COMMON_GO_TO_HOME")} />
          </Link>
        </div>
      </Card>
    )
  }
  
  sessionStorage.setItem("isPermitApplication", true);
  sessionStorage.setItem("isEDCRDisable", JSON.stringify(true));
  const edcrData = props?.data?.[0];
  const [bpaLinks, setBpaLinks] = useState({});
  const state = Digit.ULBService.getStateId();
  const isMobile = window.Digit.Utils.browser.isMobile();
  const { data:homePageUrlLinks , isLoading: homePageUrlLinksLoading } = Digit.Hooks.obps.useMDMS(state, "BPA", ["homePageUrlLinks"]);
  const { isMdmsLoading, data: mdmsData } = Digit.Hooks.obps.useMDMS(state, "BPA", ["RiskTypeComputation"]);
  //const { isMdmsLoading, data: mdmsData } = Digit.Hooks.obps.useMDMS(state, "BPA", ["GaushalaFees","MalbaCharges","LabourCess"]);


  useEffect(() => {
    if (!homePageUrlLinksLoading && homePageUrlLinks?.BPA?.homePageUrlLinks?.length > 0) {
        let uniqueLinks = [];
        homePageUrlLinks?.BPA?.homePageUrlLinks?.map(linkData => {
            if(linkData?.applicationType === edcrData?.appliactionType && linkData?.serviceType === edcrData?.applicationSubType){
              setBpaLinks({
                linkData: linkData,
                edcrNumber: edcrData?.edcrNumber
              });
            }
        });
    }
}, [!homePageUrlLinksLoading]);

  // Single useEffect to handle BPA process
  useEffect(() => {
    if (applicationNo && edcrData?.status === "Accepted") {
      handleBPAProcess();
    }
  }, [applicationNo, edcrData]);


  const printReciept = async () => {
    var win = window.open(edcrData.planReport, '_blank');
    if (win) {
      win.focus();
    }
  };

  const handleBPAProcess = async () => {
    if (!applicationNo) {
      console.log('No applicationNo found in URL');
      return;
    }
    
    try {
      // BPA Search call using applicationNumber from URL
      const bpaSearchResponse = await Digit.OBPSV2Services.search({
        tenantId: tenantId,
        filters: { applicationNo }
      });
      

      if (bpaSearchResponse?.bpa?.length > 0) {
        const bpaData = bpaSearchResponse.bpa[0];
        
        // BPA Update call with workflow action and edcrNumber
        const updateResponse = await Digit.OBPSV2Services.update({
          BPA: {
            ...bpaData,
            edcrNumber: edcrData?.edcrNumber,
            workflow: {
              action: "APPLY_FOR_SCRUTINY",
              assignes: [bpaData?.accountId]
            }
          }
        });
        
        setBpaUpdateResponse(updateResponse);
      } else {
        console.log('No BPA data found for applicationNo:', applicationNo);
      }
    } catch (error) {
      console.error("Error in BPA process:", error);
    }
  };

  const routeToBPAScreen = async () => {
    await handleBPAProcess();
    history.push(
      `/upyog-ui/citizen/obps/bpa/${edcrData?.appliactionType?.toLowerCase()}/${edcrData?.applicationSubType?.toLowerCase()}/docs-required`,
      { edcrNumber: edcrData?.edcrNumber }
    );
  }

  return (
    <div>
      {edcrData.status == "Accepted" ?
        <Card style={{ padding: "0px" }}>
          <Banner
            message={t("RTP_ACKNOWLEDGEMENT_SUCCESS_MESSAGE_LABEL")}
            applicationNumber={edcrData?.edcrNumber}
            info={t("RTP_SCRUTINY_NUMBER_LABEL")}
            successful={true}
            infoStyles = {{fontSize: "18px", lineHeight: "21px", fontWeight: "bold", textAlign: "center", padding: "0px 15px"}}
            applicationNumberStyles = {{fontSize: "24px", lineHeight: "28px", fontWeight: "bold", marginTop: "10px"}}
            style={{width: "100%", padding: "10px"}}
          />
          <CardText style={{ padding: "0px 8px", marginBottom: "10px" }}>{`${t("PDF_STATIC_LABEL_CONSOLIDATED_BILL_CONSUMER_ID_TL")} - ${edcrData?.applicationNumber}`}</CardText>
          {bpaUpdateResponse?.bpa?.[0] && (
            <CardText style={{ 
              whiteSpace: "pre", 
              width: "60%", 
              fontWeight: "bold",
              color: "#a82227",
              padding: "0px 8px",
              marginBottom: "10px"
            }}>
              {t(bpaUpdateResponse.bpa[0].status)}
            </CardText>
          )}
          <div className="primary-label-btn d-grid" style={{ marginLeft: "unset", marginBottom: "10px", padding: "0px 8px" }} onClick={printReciept}>
            <svg width="20" height="23" viewBox="0 0 20 23" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19.3334 8H14V0H6.00002V8H0.666687L10 17.3333L19.3334 8ZM0.666687 20V22.6667H19.3334V20H0.666687Z" fill="#a82227" />
            </svg>
            {t("RTP_DOWNLOAD_SCRUTINY_REPORT_LABEL")}
          </div>
          <div style={{padding: "0px 10px"}}>
            <div style={{marginTop: "12px", paddingBottom: "10px"}}>
              <Link to={`/upyog-ui/citizen`} >
                <LinkButton label={t("CORE_COMMON_GO_TO_HOME")} />
              </Link>
            </div>
          </div>    
        </Card> :
        <Card style={{ padding: "0px" }}>
          <Banner
            message={t("RTP_ACKNOWLEDGEMENT_REJECTED_MESSAGE_LABEL")}
            applicationNumber={""}
            info={""}
            successful={false}
            infoStyles = {{fontSize: "18px", lineHeight: "21px", fontWeight: "bold", textAlign: "center", padding: "0px 15px"}}
            applicationNumberStyles = {{fontSize: "24px", lineHeight: "28px", fontWeight: "bold", marginTop: "10px"}}
            style={{width: "100%", padding: "10px"}}
          />          
          <CardText style={{ padding: "0px 8px", marginBottom: "10px" }}>{t("EDCR_ACKNOWLEDGEMENT_REJECTED_MESSAGE_TEXT_LABEL")}</CardText>
          <CardText style={{ padding: "0px 8px", marginBottom: "10px" }}>{`${t("PDF_STATIC_LABEL_CONSOLIDATED_BILL_CONSUMER_ID_TL")} - ${edcrData?.applicationNumber}`}</CardText>
          <div className="primary-label-btn d-grid" style={{ marginLeft: "unset", marginBottom: "10px", padding: "0px 8px" }} onClick={printReciept}>
            <svg width="20" height="23" viewBox="0 0 20 23" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19.3334 8H14V0H6.00002V8H0.666687L10 17.3333L19.3334 8ZM0.666687 20V22.6667H19.3334V20H0.666687Z" fill="#a82227" />
            </svg>
            {t("EDCR_DOWNLOAD_SCRUTINY_REPORT_LABEL")}
          </div>
          <div style={{padding: "10px", paddingBottom: "10px"}}>
            <Link to={`/upyog-ui/citizen`} >
              <SubmitBar label={t("CORE_COMMON_GO_TO_HOME")} />
            </Link>
          </div>
        </Card>
      }

    </div>
  );
};
export default RTPAcknowledgement;