import { Banner, Card, CardText, Loader, Row, StatusTable, SubmitBar, DownloadPrefixIcon } from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import { Link, useParams } from "react-router-dom";

export const SuccessfulPayment = (props)=>{
  if(localStorage.getItem("BillPaymentEnabled")!=="true"){
    window.history.forward();
   return null;
 }
 return <WrapPaymentComponent {...props}/>
}

export const convertEpochToDate = (dateEpoch) => {
  // Returning NA in else case because new Date(null) returns Current date from calender
  if (dateEpoch) {
    const dateFromApi = new Date(dateEpoch);
    let month = dateFromApi.getMonth() + 1;
    let day = dateFromApi.getDate();
    let year = dateFromApi.getFullYear();
    month = (month > 9 ? "" : "0") + month;
    day = (day > 9 ? "" : "0") + day;
    return `${day}/${month}/${year}`;
  } else {
    return "NA";
  }
};
 const WrapPaymentComponent = (props) => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const { eg_pg_txnid: egId, workflow: workflw, propertyId } = Digit.Hooks.useQueryParams();
  const [printing, setPrinting] = useState(false);
  const [allowFetchBill, setallowFetchBill] = useState(false);
  const { businessService: business_service, consumerCode, tenantId } = useParams();
  
  
  const { isLoading, data, isError } = Digit.Hooks.usePaymentUpdate({ egId }, business_service, {
    
    retry: false,
    staleTime: Infinity,
    refetchOnWindowFocus: false,
  });

  const { label } = Digit.Hooks.useApplicationsForBusinessServiceSearch({ businessService: business_service }, { enabled: false });


  const payments = data?.payments;

  useEffect(() => {
    return () => {
      localStorage.setItem("BillPaymentEnabled","false")
      queryClient.clear();
    };
  }, []);

  useEffect(() => {
    if (data && data.txnStatus && data.txnStatus !== "FAILURE") {
      setallowFetchBill(true);
    }
  }, [data]);

  if (isLoading) {
    return <Loader />;
  }

  const applicationNo = data?.applicationNo;

  const isMobile = window.Digit.Utils.browser.isMobile();


  if (isError || !payments || !payments.Payments || payments.Payments.length === 0 || data.txnStatus === "FAILURE") {
    return (
      <Card>
        <Banner
          message={t("CITIZEN_FAILURE_COMMON_PAYMENT_MESSAGE")}
          info={t("CS_PAYMENT_TRANSANCTION_ID")}
          applicationNumber={egId}
          successful={false}
        />
        <CardText>{t("CS_PAYMENT_FAILURE_MESSAGE")}</CardText>
        {!(business_service?.includes("PT")) && !(business_service?.includes("TL")) ? (
          <Link to={`/upyog-ui/citizen`}>
            <SubmitBar label={t("CORE_COMMON_GO_TO_HOME")} />
          </Link>
        ) : (
          <React.Fragment>
            <Link to={(applicationNo && `/upyog-ui/citizen/payment/my-bills/${business_service}/${applicationNo}`) || "/upyog-ui/citizen"}>
              <SubmitBar label={t("CS_PAYMENT_TRY_AGAIN")} />
            </Link>
           
            <div className="link" style={isMobile ? { marginTop: "8px", width: "100%", textAlign: "center" } : { marginTop: "8px" }}>
              <Link to={`/upyog-ui/citizen`}>{t("CORE_COMMON_GO_TO_HOME")}</Link>
            </div>
          </React.Fragment>
        )}
      </Card>
    );
  }

  const paymentData = data?.payments?.Payments[0];
  const transactionDate = paymentData?.transactionDate;


   const printReciept = async () => {
      if (printing) return;
      setPrinting(true);
      let paymentArray=[];
      const state = Digit.ULBService.getStateId();
      let response = { filestoreIds: [payments.Payments[0]?.fileStoreId] };
      if (!paymentData?.fileStoreId) {
          //   const filters = {
          //     "CalulationCriteria": [
          //     {
          //         "tenantId": "as",
          //         "applicationNo": payments.Payments[0]?.bill?.consumerCode,
          //         "feeType": "PLANNING_PERMIT_FEE",
          //         "applicationType": "RESIDENTIAL_RCC",
          //         "BPA": {
          //             "edcrNumber": data?.bpa?.[0]?.edcrNumber,
          //             "tenantId": "as",
          //         }
          //     }
          // ]}
            //let estimateResponse = await OBPSV2Services.estimate(filters, true, null);
            payments.Payments[0].additionalDetails=details;
            // const updatedpayments={
            //   ...payments,
             
            //       paymentDetails:[
            //         {
            //           ...paymentData.paymentDetails?.[0],
            //           additionalDetails:{
            //             ...paymentData.paymentDetails[0].additionalDetails,
            //             "feebreakup":estimateResponse?.Calculations?.[0]?.taxHeadEstimates
                        
            //           },
            //         },
            //       ],  
               
            // }
            //paymentArray[0]=updatedpayments.Payments[0]
              //response = await Digit.PaymentService.generatePdf(state, { Payments: [{...updatedpayments}] }, "bpa-receipt");
              paymentArray[0]=payments.Payments[0]
              response = await Digit.PaymentService.generatePdf(state, { Payments: [{...paymentData}] }, "bpa-receipt");
            
          } 
          
      const fileStore = await Digit.PaymentService.printReciept(state, { fileStoreIds: response.filestoreIds[0] });
      if (fileStore && fileStore[response.filestoreIds[0]]) {
        window.open(fileStore[response.filestoreIds[0]], "_blank");
      }
      setPrinting(false);
    };

  const convertDateToEpoch = (dateString, dayStartOrEnd = "dayend") => {
    //example input format : "2018-10-02"
    try {
      const parts = dateString.match(/(\d{4})-(\d{1,2})-(\d{1,2})/);
      const DateObj = new Date(Date.UTC(parts[1], parts[2] - 1, parts[3]));
      DateObj.setMinutes(DateObj.getMinutes() + DateObj.getTimezoneOffset());
      if (dayStartOrEnd === "dayend") {
        DateObj.setHours(DateObj.getHours() + 24);
        DateObj.setSeconds(DateObj.getSeconds() - 1);
      }
      return DateObj.getTime();
    } catch (e) {
      return dateString;
    }
  };
  const printPlanningPermitReceipt = async () => {
    try {
      const applicationNo =consumerCode;
      
      if (!applicationNo) return;
      
      const bpaResponse = await Digit.OBPSV2Services.search({
        tenantId: tenantId,
        filters: { applicationNo }
      });
      
      const application = bpaResponse?.bpa?.[0];
      if (!application) return;
      
      const filters = {
        CalulationCriteria: [
        {
            tenantId: tenantId,
            applicationNo: applicationNo,
            feeType: "PLANNING_PERMIT_FEE",
            applicationType: "RESIDENTIAL_RCC",
            BPA: {
                edcrNumber: application?.edcrNumber,
                tenantId: tenantId,
            }
        }
    ]}
      
      const estimateResponse = await Digit.OBPSV2Services.estimate(filters, true, null);
      const paymentData = payments?.Payments?.[0];
      
      if (!paymentData) return;
      
      const updatedPayment = {
        ...paymentData,
        paymentDetails: paymentData.paymentDetails?.map(detail => ({
          ...detail,
          additionalDetails: {
            ...detail.additionalDetails,
            feebreakup: estimateResponse?.Calculations?.[0]?.taxHeadEstimates || []
          }
        }))
      };
      
      let fileStoreId = application?.ppFeeReceiptFileStoreId;
      
      if (!fileStoreId) {
        const pdfResponse = await Digit.PaymentService.generatePdf(
          tenantId,
          { Payments: [updatedPayment] },
          "bpa-receipt"
        );
        fileStoreId = pdfResponse?.filestoreIds?.[0];
        
        // Update pp_fee_receipt_filestore_id in application
        const updatedApplication = {
          ...application,
          additionalDetails: {
            ...application.additionalDetails,
            ppFeeReceiptFileStoreId: fileStoreId,
            UPDATE_FILESTORE_ID: true,
          },
        };
        
        await Digit.OBPSV2Services.update({
          BPA: updatedApplication,
        });
      }
      
      const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: fileStoreId });
      
      if (fileStore && fileStore[fileStoreId]) {
        window.open(fileStore[fileStoreId], "_blank");
      }
    } catch (error) {}
  };
  
  const printBuildingPermitReceipt = async () => {
      try {
        const applicationNo =consumerCode;
        
        if (!applicationNo) return;
        
        const bpaResponse = await Digit.OBPSV2Services.search({
          tenantId: tenantId,
          filters: { applicationNo }
        });
        
        const application = bpaResponse?.bpa?.[0];
        if (!application) return;
        
        const filters = {
          CalulationCriteria: [
          {
              tenantId: tenantId,
              applicationNo: applicationNo,
              feeType: "BUILDING_PERMIT_FEE",
              applicationType: "RESIDENTIAL_RCC",
              BPA: {
                  edcrNumber: application?.edcrNumber,
                  tenantId: tenantId,
              }
          }
      ]}
        
        const estimateResponse = await Digit.OBPSV2Services.estimate(filters, true, null);
        const paymentData = payments?.Payments?.[0];
        
        if (!paymentData) return;
        
        const updatedPayment = {
          ...paymentData,
          paymentDetails: paymentData.paymentDetails?.map(detail => ({
            ...detail,
            additionalDetails: {
              ...detail.additionalDetails,
              feebreakup: estimateResponse?.Calculations?.[0]?.taxHeadEstimates || []
            }
          }))
        };
        
        let fileStoreId = application?.bpFeeReceiptFileStoreId;
        
        if (!fileStoreId) {
          const pdfResponse = await Digit.PaymentService.generatePdf(
            tenantId,
            { Payments: [updatedPayment] },
            "bpa-receipt"
          );
          fileStoreId = pdfResponse?.filestoreIds?.[0];
          
          // Update pp_fee_receipt_filestore_id in application
          const updatedApplication = {
            ...application,
            additionalDetails: {
              ...application.additionalDetails,
              bpFeeReceiptFileStoreId: fileStoreId,
              UPDATE_FILESTORE_ID: true,
            },
          };
          
          await Digit.OBPSV2Services.update({
            BPA: updatedApplication,
          });
        }
        
        const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: fileStoreId });
        
        if (fileStore && fileStore[fileStoreId]) {
          window.open(fileStore[fileStoreId], "_blank");
        }
      } catch (error) {}
    };  
  const getBuildingPermitOrder = async () => {
    let applicationNo =  data?.[0]?.applicationNo;
    let bpaResponse=await Digit.OBPSV2Services.search({tenantId,
      filters: { applicationNo }});
    const application = bpaResponse?.bpa?.[0];
    let fileStoreId = application?.bpFileStoreId;
    const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: application?.edcrNumber });
    let edcrDetail = edcrResponse?.edcrDetail?.[0];
    const gisResponse = await Digit.OBPSV2Services.gisSearch({
      GisSearchCriteria: {
        applicationNo: applicationNo,
        tenantId: tenantId,
        status: "SUCCESS",
      },
    });
    if (!fileStoreId) {
      const response = await Digit.PaymentService.generatePdf(
        tenantId,
        {
          Bpa: [{ ...application, edcrDetail: [{ ...edcrDetail }], gisResponse }],
        },
        "bpaBuildingPermit"
      );
      fileStoreId = response?.filestoreIds?.[0];
      const updatedApplication = {
        ...application,
        bpFileStoreId: fileStoreId,
        additionalDetails: {
          ...application.additionalDetails,
          UPDATE_FILESTORE_ID: true,
        },
      };
      await Digit.OBPSV2Services.update({
        BPA: updatedApplication,
      });
    }
    const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: fileStoreId });
    window.open(fileStore[fileStoreId], "_blank");
  };

  const getPlanningPermitOrder = async () => {
    let applicationNo =  data?.[0]?.applicationNo;
    let bpaResponse=await Digit.OBPSV2Services.search({tenantId,
      filters: { applicationNo }});
    const application = bpaResponse?.bpa?.[0];
    let fileStoreId = application?.ppFileStoreId;
    const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: application?.edcrNumber });
    let edcrDetail = edcrResponse?.edcrDetail?.[0];
    const gisResponse = await Digit.OBPSV2Services.gisSearch({
      GisSearchCriteria: {
        applicationNo: applicationNo,
        tenantId: tenantId,
        status: "SUCCESS",
      },
    });
    if (!fileStoreId) {
      const response = await Digit.PaymentService.generatePdf(tenantId, { Bpa: [{ ...application, edcrDetail: [{ ...edcrDetail }], gisResponse }] }, "bpaPlanningPermit");
      fileStoreId = response?.filestoreIds?.[0];
      const updatedApplication = {
        ...application,
        ppFileStoreId: fileStoreId,
        additionalDetails: {
          ...application.additionalDetails,
          UPDATE_FILESTORE_ID: true,
        },
      };
      await Digit.OBPSV2Services.update({
        BPA: updatedApplication,
      });
    }
    const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: fileStoreId });
    window.open(fileStore[fileStoreId], "_blank");
  };

  // Occupancy Certificate Download
  const getBuildingOccupancy = async () => {
    let applicationNo =  data?.[0]?.applicationNo;
    let bpaResponse=await Digit.OBPSV2Services.search({tenantId,
      filters: { applicationNo }});
    const application = bpaResponse?.bpa?.[0];
    let fileStoreId = application?.ocFileStoreId;
    if (!fileStoreId) {
      let currentDate = new Date();
      let bpaResponse = await Digit.OBPSV2Services.search({
        tenantId,
        filters: { applicationNo },
      });
      let bpaData = bpaResponse?.bpa?.[0];
      bpaData.additionalDetails.runDate = convertDateToEpoch(`${currentDate.getFullYear()}-${currentDate.getMonth() + 1}-${currentDate.getDate()}`);
      const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: application?.edcrNumber });
      let edcrData = edcrResponse?.edcrDetail?.[0];
      let requestData = { ...bpaData, edcrDetail: [{ ...edcrData }] };
      const response = await Digit.PaymentService.generatePdf(tenantId, { Bpa: [requestData] }, "bpa-occupancy-certificate");
      fileStoreId = response?.filestoreIds?.[0];
      const updatedApplication = {
        ...application,
        ocFileStoreId: fileStoreId,
        additionalDetails: {
          ...application.additionalDetails,
          UPDATE_FILESTORE_ID: true, // workflow flag to update file store id
        },
      };
      await Digit.OBPSV2Services.update({
        BPA: updatedApplication,
      });
    }
    const fileStore = await Digit.PaymentService.printReciept(tenantId, {
      fileStoreIds: fileStoreId,
    });
    window.open(fileStore[fileStoreId], "_blank");
  };

  let bannerText;
  if (workflw) {
    bannerText = `CITIZEN_SUCCESS_UC_PAYMENT_MESSAGE`;
  } else {
    if (paymentData?.paymentDetails?.[0]?.businessService && paymentData?.paymentDetails?.[0]?.businessService?.includes("BPA")) {
      let nameOfAchitect = sessionStorage.getItem("BPA_ARCHITECT_NAME");
      let parsedArchitectName = nameOfAchitect ? JSON.parse(nameOfAchitect) : "ARCHITECT";
      bannerText = `CITIZEN_SUCCESS_${paymentData?.paymentDetails[0]?.businessService.replace(/\./g, "_")}_${parsedArchitectName}_PAYMENT_MESSAGE`;
    } 
    else {
      bannerText = paymentData?.paymentDetails[0]?.businessService ? `CITIZEN_SUCCESS_${paymentData?.paymentDetails[0]?.businessService.replace(/\./g, "_")}_PAYMENT_MESSAGE` : t("CITIZEN_SUCCESS_UC_PAYMENT_MESSAGE");
    }
  }


  const rowContainerStyle = {
    padding: "4px 0px",
    justifyContent: "space-between",
  };


  return (
    <Card>
      <Banner
        svg={
          <svg className="payment-svg" xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 40 40" fill="none">
            <path
              d="M20 0C8.96 0 0 8.96 0 20C0 31.04 8.96 40 20 40C31.04 40 40 31.04 40 20C40 8.96 31.04 0 20 0ZM16 30L6 20L8.82 17.18L16 24.34L31.18 9.16L34 12L16 30Z"
              fill="white"
            />
          </svg>
        }
        message={t("CS_COMMON_PAYMENT_COMPLETE")}
        info={t("CS_COMMON_RECIEPT_NO")}
        applicationNumber={paymentData?.paymentDetails[0].receiptNumber}
        successful={true}
      />
      <CardText></CardText>
      <StatusTable>
        <Row rowContainerStyle={rowContainerStyle} last label={t(label)} text={applicationNo} />
        <Row rowContainerStyle={rowContainerStyle} last label={t("CS_PAYMENT_TRANSANCTION_ID")} text={egId} />
        <Row
          rowContainerStyle={rowContainerStyle}
          last
          label={t("CS_PAYMENT_AMOUNT_PAID")}
          text={"₹ " + paymentData?.totalAmountPaid}
        />
        {(business_service.includes("BPA")) && (
          <Row
            rowContainerStyle={rowContainerStyle}
            last
            label={t("CS_PAYMENT_TRANSANCTION_DATE")}
            text={transactionDate && new Date(transactionDate).toLocaleDateString("in")}
          />
        )}
      </StatusTable>
      <div style={{display:"flex"}}>
      {/* { <div className="primary-label-btn d-grid" style={{ marginLeft: "unset", marginRight: "20px" }} onClick={printReciept}>
              <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
                <path d="M0 0h24v24H0z" fill="none" />
                <path d="M19 8H5c-1.66 0-3 1.34-3 3v6h4v4h12v-4h4v-6c0-1.66-1.34-3-3-3zm-3 11H8v-5h8v5zm3-7c-.55 0-1-.45-1-1s.45-1 1-1 1 .45 1 1-.45 1-1 1zm-1-9H6v4h12V3z" />
              </svg>
              {t("CS_COMMON_PRINT_RECEIPT")}
        </div>} */}
        {business_service=== "BPA.PLANNING_PERMIT_FEE" ? (
        <div className="primary-label-btn d-grid" style={{ marginLeft: "unset" }} onClick={printPlanningPermitReceipt}>
          <DownloadPrefixIcon />
            {t("CS_COMMON_PRINT_RECEIPT")}
          </div>
      ) : null}
       {business_service=== "BPA.BUILDING_PERMIT_FEE" ? (
        <div className="primary-label-btn d-grid" style={{ marginLeft: "unset" }} onClick={printBuildingPermitReceipt}>
          <DownloadPrefixIcon />
            {t("CS_COMMON_PRINT_RECEIPT")}
          </div>
      ) : null}
      {business_service=== "BPA.PLANNING_PERMIT_FEE" ? (
        <div className="primary-label-btn d-grid" style={{ marginLeft: "unset" }} onClick={getPlanningPermitOrder}>
          <DownloadPrefixIcon />
            {t("BPA_PLANNING_PERMIT_ORDER")}
          </div>
      ) : null}
      {business_service === "BPA.BUILDING_PERMIT_FEE" ? (
        <div className="primary-label-btn d-grid" style={{ marginLeft: "unset" }} onClick={getBuildingPermitOrder}>
          <DownloadPrefixIcon />
            {t("BPA_BUILDING_PERMIT_ORDER")}
          </div>
      ) : null}
      {business_service === "BPA.BUILDING_PERMIT_FEE" ? (
        <div className="primary-label-btn d-grid" style={{ marginLeft: "unset" }} onClick={getBuildingOccupancy}>
          <DownloadPrefixIcon />
            {t("BPA_OCCUPANCY_CERTIFICATE")}
          </div>
      ) : null}

      </div>
     
    <div className="link" style={isMobile ? { marginTop: "8px", width: "100%", textAlign: "center" } : { marginTop: "8px" }}>
      <Link to={`/upyog-ui/citizen`}>{t("CORE_COMMON_GO_TO_HOME")}</Link>
    </div>

    
    </Card>
  );
};

export const FailedPayment = (props) => {
  const { addParams, clearParams } = props;
  const { t } = useTranslation();
  const { consumerCode } = useParams();

  const getMessage = () => "Failure !";
  return (
    <Card>
      <Banner message={getMessage()} complaintNumber={consumerCode} successful={false} />
      <CardText>{t("ES_COMMON_TRACK_COMPLAINT_TEXT")}</CardText>
    </Card>
  );
};


