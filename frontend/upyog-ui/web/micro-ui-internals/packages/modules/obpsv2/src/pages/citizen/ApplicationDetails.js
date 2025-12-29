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
    LinkButton,
    TextInput,
    TextArea,
    CardLabelDesc,
    UploadFile,
    Dropdown
  } from "@upyog/digit-ui-react-components";
  import React, { useEffect, useState , Fragment} from "react";
  import { useTranslation } from "react-i18next";
  import { useParams } from "react-router-dom";
  import get from "lodash/get";
  import { isError, useQueryClient } from "react-query";
  import WFApplicationTimeline from "../../pageComponents/WFApplicationTimeline";
  import DocumentsPreview from "../../../../templates/ApplicationDetails/components/DocumentsPreview";
  import useScrutinyFormDetails from "../../../../../libraries/src/hooks/obpsv2/useScrutinyFormDetails";
  import FormAcknowledgement from "./Create/FormAcknowledgement";
  import Accordion from "../../../../../react-components/src/atoms/Accordion";
  import GisDetails from "../../components/GisDetails";
  import { convertDateToEpoch, getEstimatePayload,formatEpochDateDMY} from "../../utils";
  import { OBPSV2Services } from "../../../../../libraries/src/services/elements/OBPSV2";
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
      config: { staleTime: Infinity, cacheTime: Infinity }
    });
    const nocFilters = {
      sourceRefId: acknowledgementIds,
    };

    // const nocData = Digit.Hooks.noc.useNOCSearchApplication(tenantId,nocFilters);
    const nocData = Digit.Hooks.noc.useNOCSearchApplication(tenantId, nocFilters);

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


    const [hasAccess, setHasAccess] = useState(false);
    const { roles } = Digit.UserService.getUser().info;
    const [workflowDetails, setWorkflowDetails] = useState(null);
    const [collectionBillArray, setCollectionBillArray] = useState([]);
    const [collectionBillDetails, setCollectionBillDetails] = useState([]);
    const [totalAmount, setTotalAmount] = useState(0);
    const [viewTimeline, setViewTimeline]=useState(false);
     // New state for GIS response
    const [gisResponse, setGisResponse] = useState(null);
    const [showGisResponse, setShowGisResponse] = useState(false);
    const [gisValidationSuccess, setGisValidationSuccess] = useState(false);
    const [gisData, setGisData] = useState(null);
    const [nocInput, setNocInput] = useState("");
    const [nocValidationResult, setNocValidationResult] = useState(null);
    const [nocSearchResult, setNocSearchResult] = useState(null);
   
  const handleViewTimeline=()=>{
    setViewTimeline(true);
      const timelineSection=document.getElementById('timeline');
      if(timelineSection){
        timelineSection.scrollIntoView({behavior: 'smooth'});
      } 
  };
    useEffect(() => {
      const fetchWorkflow = async () => {
        const details = await Digit.WorkflowService.getByBusinessId(tenantId, acknowledgementIds);
        setWorkflowDetails(details);
      };

      const handleNocSearch = async () => {
          let filters = {sourceRefId:acknowledgementIds};
          const response = await Digit.NOCSearch.all(tenantId, filters)
          setNocSearchResult(response);
      };

      fetchWorkflow();
      handleNocSearch();
    }, [acknowledgementIds, tenantId]);

    useEffect(() => {
      const nextActionRoles = workflowDetails?.ProcessInstances?.[0]?.nextActions[0]?.roles || [];
      const access = roles?.some(role => nextActionRoles.includes(role?.code));
      setHasAccess(access);
    }, [roles])
    const [expanded, setExpanded] = useState({
      form22: false,
      form23A: false,
      form23B: false,
      submitReport : false,
      Gis: false
    });
    const toggleExpanded = (key) => {
      setExpanded((prev) => ({ ...prev, [key]: !prev[key] }));
    };

   const client = useQueryClient();
    const [actioneError, setActionError] = useState(null);
    const [popup, setPopup] = useState(false);
    const [selectedAction, setSelectedAction] = useState(null);
    const [assignResponse, setAssignResponse] = useState(null);
    const [toast, setToast] = useState(false);
    const [ newRTPName, setNewRTPName ] = useState();
    const bpaApplicationDetail = get(data, "bpa", []);
    const [comments, setComments] = useState("");
    const [uploadedFile, setUploadedFile] = useState(null);
    const bpaId = get(data, "bpa[0].applicationNo", []);
    const [loader, setLoader] = useState(false);
    let bpa_details = (bpaApplicationDetail && bpaApplicationDetail.length > 0 && bpaApplicationDetail[0]) || {};
    const [edcrNumber, setEdcrNumber] = useState(bpaApplicationDetail?.[0]?.edcrNumber);
    const stateId = Digit.ULBService.getStateId();
    const [rtpPopUp, setrtpPopUp] = useState(false);
    const [oldRTPName, setOldRTPName] = useState(bpaApplicationDetail?.[0]?.rtpDetails?.rtpName);
    const [rtpOptions, setRtpOptions] = useState([])
    const { form22, form23A, form23B, loading } = useScrutinyFormDetails(edcrNumber, "assam", {
      enabled: !!edcrNumber,
    });
    useEffect(() => {
      const fetchUsers = async () => {
        try {
          const tenantId = Digit.ULBService.getCurrentTenantId();
          const response = await Digit.OBPSV2Services.rtpsearch({
            tenantId, roleCodes: ["BPA_ARCHITECT"] 
          });
          if (response?.user?.length > 0) {
            const formattedRtpOptions = response.user.map((user) => ({
              code:`${user.name}, +91 ${user.mobileNumber}, ${user.emailId}`,
              uuid: user.uuid,
              name: `${user.name}, +91 ${user.mobileNumber}, ${user.emailId}`,
              i18nKey:`${user.name}, +91 ${user.mobileNumber}, ${user.emailId}`,
            }));
            setRtpOptions(formattedRtpOptions);
          }
        } catch (error) {
          throw error;
        }
      };
      fetchUsers();
    }, [tenantId]);

    useEffect(() => {
      if (bpaApplicationDetail?.[0]?.rtpDetails?.rtpName) {
        setOldRTPName(bpaApplicationDetail[0].rtpDetails.rtpName);
      }
    }, [bpaApplicationDetail]);

    useEffect(() => {
      if (bpaApplicationDetail?.[0]?.edcrNumber !== edcrNumber) {
        setEdcrNumber(bpaApplicationDetail?.[0]?.edcrNumber);
      }
    }, [bpaApplicationDetail]);
    const application = bpa_details;
    const [isUploading, setIsUploading] = useState(false);
    const [file, setFile] = useState(null);
    sessionStorage.setItem("bpa", JSON.stringify(application));
    //const SiteReport = Digit.ComponentRegistryService.getComponent("siteReport")
    const mutation = Digit.Hooks.obpsv2.useBPACreateUpdateApi(tenantId, "update");
  
    const getBusinessService = () => {
      if (bpa_details?.status === "CITIZEN_FINAL_PAYMENT") {
        return "BPA.BUILDING_PERMIT_FEE";
      }
      return "BPA.PLANNING_PERMIT_FEE";
    };
    
    useEffect(() => {
      (async () => {
        setActionError(null);
        if (file && selectedAction !== "VALIDATE_GIS") {
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
  if (selectedAction !== "VALIDATE_GIS") {
    setPopup(false);
  }

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
          assignes: ["ACCEPT", "VALIDATE_GIS", "EDIT"].includes(selectedAction) ? [applicationDetails?.rtpDetails?.rtpUUID] : null,
            varificationDocuments: uploadedFile ? [
              {
                documentType: file.type,
                fileName: file?.name,
                fileStoreId: selectedAction === "VALIDATE_GIS" ? "" : uploadedFile,
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
    //window.location.reload();
    setTimeout(() => setToast(false), 1000);
  } catch (err) {
    console.error("Error while assigning:", err);
    setActionError(t("CS_ACTION_ASSIGN_RTPS_FAILED"));
    setTimeout(() => setActionError(null), 3000);  }
  }
  async function onrtpChange(comments) {
  if ( !oldRTPName) setActionError(t("CS_OLD_RTP_NAME_MANDATORY"));
  else if ( !newRTPName) setActionError(t("CS_NEW_RTP_NAME_MANDATORY"));
  else if(!comments) setActionError(t("CS_REASON_FOR_CHANGING_RTP_MANDATORY"))
  else {
  try {
        const applicationDetails = {
          ...data?.bpa?.[0],
          rtpDetails: {
            ...data?.bpa?.[0]?.rtpDetails,
            rtpName: newRTPName?.code,
            rtpUUID:newRTPName?.uuid,   
          },
        };
    const response = await mutation.mutateAsync({
      BPA: 
      {
        ...applicationDetails,
        workflow: {
          ...applicationDetails.workflow, 
          action: "RTP_IS_CHANGED",
          comments: comments,
            assignes: [newRTPName?.uuid],
            varificationDocuments: uploadedFile ? [
              {
                documentType: file.type,
                fileName: file?.name,
                fileStoreId:  uploadedFile,
              },
            ] : null
        },
      },
    

    });

    setAssignResponse(response);
    setToast(true);
    setLoader(true);
    setrtpPopUp(false);
    setNewRTPName("");
    setComments("");
    setUploadedFile(null);
    setFile(null);
    // Refresh data to show updated state
    await refetch();
    const updatedWorkflowDetails = await Digit.WorkflowService.getByBusinessId(tenantId, acknowledgementIds);
    setWorkflowDetails(updatedWorkflowDetails);
    //window.location.reload();
    setTimeout(() => setToast(false), 1000);
  } catch (err) {
    console.error("Error while assigning:", err);
    setActionError(t("CS_ACTION_ASSIGN_RTPS_FAILED"));
    setTimeout(() => setActionError(null), 3000);  }
  }
}
    
   async function onValidateGIS() {
       const occupancyType = data?.bpa?.[0]?.landInfo?.units?.[0]?.occupancyType;
      
   
       if (!file) {
         setActionError(t("CS_FILE_REQUIRED"));
         return;
       }
   
       try {
         setIsUploading(true);
         // Create multipart form data
         const formData = new FormData();
         formData.append("file", file);
         // Construct GIS request wrapper
         const gisRequestWrapper = {
           RequestInfo: {
             apiId: "gis-api",
             userInfo: {
               uuid: Digit.UserService.getUser()?.info?.uuid,
             },
           },
           gisRequest: {
             tenantId: data?.bpa?.[0]?.tenantId,
             planningAreaCode:data?.bpa?.[0]?.additionalDetails?.gisCode,
             applicationNo: data?.bpa?.[0]?.applicationNo,
             rtpiId: data?.bpa?.[0]?.rtpDetails?.rtpUUID,
             occupancyType: occupancyType,
           },
         };
   
         // explicitly mark JSON part as application/json because the controller expects application/json
         const blob = new Blob([JSON.stringify(gisRequestWrapper)], { type: "application/json" });
         formData.append("gisRequestWrapper", blob);
   
         const response = await Digit.OBPSV2Services.gisService({ data: formData });
         
        const wfsResponse = response?.data?.wfsResponse;
        const landuse = wfsResponse?.landuse;
        const validationStatus = response?.data?.validationStatus;


      // Store GIS response for display
      setGisResponse({
        district: wfsResponse?.district || "N/A",
        latitude: response?.data?.latitude || "N/A",
        longitude: response?.data?.longitude || "N/A",
        landuse: landuse || "N/A",
        village: wfsResponse?.village || "N/A",
        areaHectare: wfsResponse?.area_ha || "N/A",
        wardNo: wfsResponse?.ward_no || "N/A",
        zone: wfsResponse?.zone || "N/A",
        validationStatus: validationStatus || "N/A"
      });

         // if Validation Status is Rejected 
          if (validationStatus === "REJECTED") {
            setShowGisResponse(true);
            setGisValidationSuccess(false);
            setError(t("NOT_PERMISSIBLE_FOR_CONSTRUCTION"));
            return false;
          }

          // if Validation Status is Accepted
          if (validationStatus === "ACCEPTED") {
            setShowGisResponse(true);
            setGisValidationSuccess(true);
            // Call update API here as normal
            await onAssign(selectedAction, comments);
            return true;
          }
      
             // Fallback if no matching zone found
            setShowGisResponse(true);
            setGisValidationSuccess(false);
            setError(t("NO_MATCHING_PERMISSIBLE_ZONE_FOUND" || validationStatus));
            return false;
          } catch (err) {
            console.error("GIS Validation Error:", err);
            setActionError(err?.response?.data?.error || t("CS_GIS_VALIDATION_FAILED"));
          } finally {
            setIsUploading(false);
          }
        }

    const handleNocValidate = async () => {
      if (!nocInput.trim()) {
        setShowToast({ key: "error", label: t("NOC_INPUT_REQUIRED") });
        // Automatically close the toast after 2 seconds
        setTimeout(() => {
          setShowToast(null);
        }, 2000);
        return;
      }
      
      try {
        const response = await Digit.OBPSV2Services.nocValidate({
          Noc: {
          nocNo: nocInput.trim(),
          tenantId: tenantId,
          sourceRefId: acknowledgementIds
          }
        });
        setNocValidationResult(response);
      } catch (error) {
        setShowToast({ key: "error", label: t("NOC_VALIDATION_FAILED") });
        // Automatically close the toast after 2 seconds
        setTimeout(() => {
          setShowToast(null);
        }, 2000);
      }
    };


     
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

    
    const getPermitOccupancyOrderSearch = async (order, mode = "download") => {
      let applicationNo =  data?.bpa?.[0]?.applicationNo ;
      let bpaResponse = await Digit.OBPSV2Services.search({tenantId,
        filters: { applicationNo}});
       const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.bpa?.[0]?.edcrNumber });
      let bpaData = bpaResponse?.bpa?.[0];
        let edcrData = edcrResponse?.edcrDetail?.[0];
    
      let reqData = { ...bpaData, edcrDetail: [{ ...edcrData }] };
      let response = await Digit.PaymentService.generatePdf(tenantId, { Bpa: [reqData] }, "bpaPlanningPermit");
      const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: response?.filestoreIds?.[0] ||response });
      window.open(fileStore[response?.filestoreIds?.[0]] || fileStore[response], "_blank");

    };
    const getBuildingPermitOrder = async (order, mode = "download") => {
      let applicationNo =  data?.bpa?.[0]?.applicationNo ;
      let bpaResponse = await Digit.OBPSV2Services.search({tenantId,
        filters: { applicationNo}});
       const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.bpa?.[0]?.edcrNumber });
      let bpaData = bpaResponse?.bpa?.[0];
        let edcrData = edcrResponse?.edcrDetail?.[0];
    
      let reqData = { ...bpaData, edcrDetail: [{ ...edcrData }] };
      let response = await Digit.PaymentService.generatePdf(tenantId, { Bpa: [reqData] }, "bpaBuildingPermit");
      const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: response?.filestoreIds?.[0] ||response });
      window.open(fileStore[response?.filestoreIds?.[0]] || fileStore[response], "_blank");

    };
    const handleBuildingPermitOrder = async () => {
      const application = data?.bpa?.[0];
      let fileStoreId = application?.bpFileStoreId;
      const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.bpa?.[0]?.edcrNumber });
        let edcrDetail = edcrResponse?.edcrDetail?.[0];
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
          {
            Bpa: [{...application, edcrDetail: [{ ...edcrDetail }], gisResponse}] 
          },
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
  
        await refetch();
      }
  
      const fileStore = await Digit.PaymentService.printReciept(
        tenantId,
        { fileStoreIds: fileStoreId }
      );
  
      window.open(fileStore[fileStoreId], "_blank");
    };

    const handlePlanningPermitOrder = async () => {
      const application = data?.bpa?.[0];
      let fileStoreId = application?.ppFileStoreId;
      const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.bpa?.[0]?.edcrNumber });
        let edcrDetail = edcrResponse?.edcrDetail?.[0];
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
          {Bpa : [{...application, edcrDetail: [{ ...edcrDetail }], gisResponse}]},
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
  
        await refetch();
      }
  
      const fileStore = await Digit.PaymentService.printReciept(
        tenantId,
        { fileStoreIds: fileStoreId }
      );
  
      window.open(fileStore[fileStoreId], "_blank");
    };
    // Occupancy Certificate Download
    const getBuildingOccupancy = async (mode="download") => {
      const application = data?.bpa?.[0];
      let fileStoreId = application?.ocFileStoreId;
      if (!fileStoreId) {
        let currentDate = new Date();
        let applicationNo = data?.bpa?.[0]?.applicationNo;
        let bpaResponse = await Digit.OBPSV2Services.search({
          tenantId,
          filters: { applicationNo }
       });  
        let bpaData = bpaResponse?.bpa?.[0];
        bpaData.additionalDetails.runDate = convertDateToEpoch(
          `${currentDate.getFullYear()}-${currentDate.getMonth() + 1}-${currentDate.getDate()}`
        ); 
        const edcrResponse = await Digit.OBPSService.scrutinyDetails("assam", { edcrNumber: data?.bpa?.[0]?.edcrNumber });
        let edcrData = edcrResponse?.edcrDetail?.[0];
        let requestData = { ...bpaData, edcrDetail: [{ ...edcrData }] };
        const response = await Digit.PaymentService.generatePdf(tenantId,{ Bpa: [requestData] }, "bpa-occupancy-certificate");
        fileStoreId = response?.filestoreIds?.[0];
        const updatedApplication = {
          ...application,
          ocFileStoreId: fileStoreId,
          additionalDetails: {
            ...application.additionalDetails,
            UPDATE_FILESTORE_ID: true // workflow flag to update file store id
          }
        };
         await Digit.OBPSV2Services.update({
          BPA: updatedApplication
        });  await refetch();
      }
      const fileStore = await Digit.PaymentService.printReciept(tenantId, {
        fileStoreIds: fileStoreId
      });
      window.open(fileStore[fileStoreId], "_blank");
    };
    useEffect(() => {
      const collectionDetails = async () => {
        try {
          let appBusinessService = ["BPA.PLANNING_PERMIT_FEE", "BPA.BUILDING_PERMIT_FEE"];
          let collectionBillRes = [];
          let collectionArray = [];
          let collectionDetailsArray = [];
          let total = 0;
          if (appBusinessService?.[1]) {
            const fetchBillRes = await Digit.PaymentService.fetchBill(data?.bpa?.[0]?.tenantId, {
              consumerCode: data?.bpa?.[0]?.applicationNo,
              businessService: appBusinessService[1],
            });
          }
  
          for (let i = 0; i < appBusinessService?.length; i++) {
            const collectionres = await Digit.PaymentService.recieptSearch(
              data?.bpa?.[0]?.tenantId,
              appBusinessService[i],
              { consumerCodes: data?.bpa?.[0]?.applicationNo, isEmployee: true }
            );
  
            if (collectionres?.Payments?.length > 0) {
              collectionres?.Payments?.forEach(res => {
                res?.paymentDetails?.forEach(resData => {
                  if (resData?.businessService === appBusinessService[i]) {
                    collectionBillRes.push(res);
                  }
                });
              });
              collectionDetailsArray.push(...collectionres?.Payments);
            }
          }
  
          if (collectionBillRes?.length > 0) {
            collectionBillRes?.forEach(ob => {
              ob?.paymentDetails?.[0]?.bill?.billDetails?.[0]?.billAccountDetails.forEach(bill => {
                collectionArray.push(
                  { title: `${bill?.taxHeadCode}_DETAILS`, value: "", isSubTitle: true },
                  { title: bill?.taxHeadCode, value: `₹${bill?.amount}` },
                  { title: "BPA_STATUS_LABEL", value: "Paid" }
                );
                total += parseInt(bill?.amount || 0);
              });
            });
          }
          setCollectionBillArray(collectionArray);
          setCollectionBillDetails(collectionDetailsArray);
          setTotalAmount(total);
  
        } catch (err) {
          console.error("Error fetching collection details:", err);
        }
      };
      if (data?.bpa?.[0]?.applicationNo && data?.bpa?.[0]?.tenantId) {
        collectionDetails();
      }
    }, [data]);
   
    let dowloadOptions = [];
    
    if (collectionBillDetails?.length>0) {
      dowloadOptions.push({
        label: t("BPA_FEE_RECEIPT"),
        onClick: async () => {
          let response = null
          const payload = getEstimatePayload({
            tenantId : collectionBillDetails?.[0].tenantId,
            applicationNo: acknowledgementIds,
            edcrNumber: data?.bpa?.[0]?.edcrNumber,
            feeType:"PLANNING_PERMIT_FEE"
          });
          let estimateResponse = await OBPSV2Services.estimate(payload, true, null);
          const updatedPayments = [...collectionBillDetails];
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
          if(collectionBillDetails?.[0]?.fileStoreId){
            response = collectionBillDetails?.[0]?.fileStoreId          
          }
          else{
             response = await Digit.PaymentService.generatePdf(tenantId, { Payments:[updatedPayments[0]]}, "bpa-receipt");
          }
          const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: response?.filestoreIds?.[0] ||response });
          window.open(fileStore[response?.filestoreIds?.[0]] || fileStore[response], "_blank");
        },
      });
      dowloadOptions.push({
        order: 3,
        label: t("BPA_PLANNING_PERMIT_ORDER"),
        onClick: handlePlanningPermitOrder,
      });
      
    }
    if (collectionBillDetails?.length>1) {
      dowloadOptions.push({
        label: t("BPA_BUILDING_FEE_RECEIPT"),
        onClick: async () => {
          let response = null
          const payload = getEstimatePayload({
            tenantId : collectionBillDetails?.[1].tenantId,
            applicationNo: acknowledgementIds,
            edcrNumber: data?.bpa?.[0]?.edcrNumber,
            feeType:"BUILDING_PERMIT_FEE"
          });
          
          let estimateResponse = await OBPSV2Services.estimate(payload, true, null);
          
          
          const updatedPayments = [...collectionBillDetails];
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
          if(collectionBillDetails?.[1]?.fileStoreId){
            response = collectionBillDetails?.[1]?.fileStoreId          
          }
          else{
             response = await Digit.PaymentService.generatePdf(tenantId, { Payments: [updatedPayments[1]]}, "bpa-receipt");
          }

          const fileStore = await Digit.PaymentService.printReciept(tenantId, { fileStoreIds: response?.filestoreIds?.[0] ||response });
          window.open(fileStore[response?.filestoreIds?.[0]] || fileStore[response], "_blank");
        },
      });
      dowloadOptions.push({
        order: 3,
        label: t("BPA_BUILDING_PERMIT_ORDER"),
        onClick: handleBuildingPermitOrder,
      });
      dowloadOptions.push({
        order: 3,
        label: t("BPA_OCCUPANCY_CERTIFICATE"),
        onClick: () => getBuildingOccupancy({tenantId: data?.applicationData?.tenantId},"bpa-occupancy-certificate"),
      });
    }
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
    if (selectedAction === "VALIDATE_GIS") {
      setUploadedFile(e.target.files[0]);
    }
    setFile(e.target.files[0]);
  }
  function redirectToPage(redirectingUrl){
      window.location.href=redirectingUrl;
    }
    function rtpChange(){
      setrtpPopUp(true);
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
        let applicationNo = url.split("/application/")[1].split("/")[0];
        let redirectingUrl= url.split("/application/")[0] + "/editApplication/" + applicationNo + "/" + tenantId;

        redirectToPage(redirectingUrl);    
        break;
      case "APPLY_FOR_SCRUTINY":
        let scrutinyurl=window.location.href;
        let scrutinyRedirectingUrl= scrutinyurl.split("/application/")[0] +  `/rtp/apply/home?applicationNo=${bpaId}&tenantId=${tenantId}`;
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
        setShowGisResponse(false);
        setGisResponse(null);
        setGisValidationSuccess(false);
        break;
      case "PAY":
        const businessService = getBusinessService();
        let redirectURL = `${window.location.origin}/upyog-ui/citizen/payment/my-bills/${businessService}/${bpaId}`;
        redirectToPage(redirectURL);
        break;
      default:
        setDisplayMenu(false);
    }
  }
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
    const submitReport = additionalDetails?.submitReportinspection_pending?.length > 0 ? additionalDetails?.submitReportinspection_pending?.[0] : "";
    switch (formType) {
      case "FORM_22":
        formData = form22;
        break;
      case "FORM_23A":
        formData = form23A;
        break;
      case "FORM_23B":
        formData = form23B;
        break;
        case "SUBMIT_REPORT":
          formData = submitReport;
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
      t,
      acknowledgementIds
    );

    Digit.Utils.pdf.generate(acknowledgementData);
  } catch (err) {
    console.error("PDF download failed for", formType, err);
  }
  };

    if (isLoading) {
      return <Loader />;
    }
  
    // if (reciept_data && reciept_data?.Payments.length > 0 && recieptDataLoading == false) {
    //   dowloadOptions.push({
    //     label: t("BPA_FEE_RECEIPT"),
    //     onClick: () => getRecieptSearch({ tenantId: reciept_data?.Payments[0]?.tenantId, payments: reciept_data?.Payments[0] }),
    //   });
    //     dowloadOptions.push({
    //       order: 3,
    //       label: t("BPA_PERMIT_ORDER"),
    //       onClick: () => getPermitOccupancyOrderSearch({tenantId: data?.applicationData?.tenantId},"buildingpermit"),
    //     });
      
    // }
  
    // Extract data from response structure
    const landInfo = bpa_details?.landInfo || {};
    const owners = landInfo?.owners || [];
    const areaMapping= bpa_details?.areaMapping || {};
    const propertyDetails = bpa_details?.additionalDetails?.propertyDetails
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
            <div style={{zIndex: "10",display:"flex",flexDirection:"row-reverse",alignItems:"center",marginTop:"-25px"}}>      
               <div style={{zIndex: "10",  position: "relative"}}>
                {dowloadOptions.length > 0 && (
                  <MultiLink
                    className="multilinkWrapper"
                    onHeadClick={() => setShowOptions(!showOptions)}
                    displayOptions={showOptions}
                    options={dowloadOptions}
                    downloadBtnClassName={"employee-download-btn-className"}
                    //optionsClassName={"employee-options-btn-className"}
                  />
                )}
                </div>        
      <LinkButton label={t("VIEW_TIMELINE")} style={{ color:"#A52A2A"}} onClick={handleViewTimeline}></LinkButton>
        </div>
            
          </div>
          <Card>
          {/* {window.location.href.includes("/employee/") && bpa_details?.status==="PENDING_GMDA_ENGINEER" && (
            <SiteReport submitReport={submitReport} onChange={setSubmitReport}/>
          )} */}
            <StatusTable>
              <Row label={t("BPA_APPLICATION_NO")} text={bpa_details?.applicationNo || t("CS_NA")} />
              {bpa_details?.planningPermitNo && (
                <Row label={t("BPA_PLANNING_PERMIT_NO")} text={bpa_details.planningPermitNo || t("CS_NA")} />
              )}
              {bpa_details?.buildingPermitNo && (
                <Row label={t("BPA_BUILDING_PERMIT_NO")} text={bpa_details.buildingPermitNo} />
              )}
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
                label={t(areaMapping?.buildingPermitAuthority + "_NAME")}
                text={t(areaMapping?.concernedAuthority) || t("CS_NA")}
              />
              {areaMapping?.buildingPermitAuthority === "MUNICIPAL_BOARD" && (
                <>
                  <Row
                    label={t("WARD")}
                    text={t(areaMapping?.ward) || t("CS_NA")}
                  />
                  <Row
                    label={t("REVENUE_VILLAGE")}
                    text={t(areaMapping?.revenueVillage) || t("CS_NA")}
                  />
                </>
              )}
              {areaMapping?.buildingPermitAuthority === "GRAM_PANCHAYAT" && (
                <Row
                  label={t("VILLAGE_NAME")}
                  text={t(areaMapping?.villageName) || t("CS_NA")}
                />
              )}
              <Row
                label={t("MOUZA")}
                text={areaMapping?.mouza || t("CS_NA")}
              />
            </StatusTable>
            <CardSubHeader style={{ fontSize: "24px" }}>{t("BPA_PROPERTY_DETAILS")}</CardSubHeader>
            <StatusTable>
          <Row
            label={t("BPA_PROPERTY_ID")}
            text={bpa_details?.additionalDetails?.propertyID||"NA"}
          />
          {propertyDetails && Object.entries(propertyDetails.details || {}).map(([key, value]) => (
            <Row
              key={key}
              label={t(`BPA_${key.toUpperCase()}`)}
              text={value || "NA"}
            />
          ))}
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
                label={t("BPA_DISTRICT")}
                text={t(permanentAddress?.district) || t("CS_NA")}
              />
              <Row
                label={t("BPA_STATE")}
                text={permanentAddress?.state || t("CS_NA")}
              />
               <Row
                label={t("BPA_CITY_VILLAGE")}
                text={t(permanentAddress?.localityCode) || t("CS_NA")}
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
                <Row
                  label={t("BPA_STATE")}
                  text={primaryOwner?.correspondenceAddress?.state || t("CS_NA")}
                />
                 <Row
                  label={t("BPA_CITY_VILLAGE")}
                  text={t(primaryOwner?.correspondenceAddress?.localityCode) || t("CS_NA")}
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
                label={t("BPA_CONSTRUCTION_TYPE")}
                text={t(additionalDetails?.constructionType) || t("CS_NA")}
              />
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
              <div style={{ display: "flex", justifyContent: "space-between", width: "100%" }}>
              <div style={{ flex: 1 }}>
                <Row
                  label={t("BPA_REGISTERED_TECHNICAL_PERSON")}
                  text={t(bpa_details?.rtpDetails?.rtpName) || t("CS_NA")}
                />
              </div>

              <div style={{ marginLeft: "20px" }}>
              <LinkButton label={t("CHANGE_RTP")} onClick={rtpChange} />
              </div>
              </div>
              <Row
                label={t("BPA_OCCUPANCY_TYPE")}
                text={t(landInfo?.units?.[0]?.occupancyType) || t("CS_NA")}
              />

            <br />
            {mappedNocData.length > 0 && (
              <Accordion
                title={t("BPA_NOC_DETAILS")}
                t={t}
                isFlag={false}
              >
                {mappedNocData.map((noc, index) => (
                  <div key={noc.id || index} style={{ marginBottom: "2rem" }}>
                    <CardSubHeader style={{ fontSize: "20px", marginBottom: "0.5rem" }}>
                      {`${t("BPA_NOC")} ${index + 1} - ${t(noc.nocType)}`}
                    </CardSubHeader>

                    <StatusTable>
                      <Row
                        label={t("BPA_NOC_APPLICATION_NO")}
                        text={noc.applicationNo || t("CS_NA")}
                      />
                      <Row
                        label={t("BPA_SUBMISSION_DATE")}
                        text={formatEpochDateDMY(noc.submittedOn) || t("CS_NA")}
                      />
                      <Row
                        label={t("BPA_APPLICATION_STATUS")}
                        text={noc.applicationStatus || t("CS_NA")}
                      />
                      {noc.applicationStatus === 'APPROVED' && (
                        <Row
                          label={t("BPA_NOC_APPROVAL_DATES")}
                          text={formatEpochDateDMY(noc.approvalDate) || t("CS_NA")}
                        />
                      )}

                      {noc.applicationStatus === 'REJECTED' && (
                        <Row
                          label={t("BPA_NOC_REJECTION_DATES")}
                          text={formatEpochDateDMY(noc.rejectionDate) || t("CS_NA")}
                        />
                      )}
                    </StatusTable>
                  </div>
                ))}
                </Accordion>
            )}


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
            {bpa_details?.documents && bpa_details.documents.length > 0 && (
              <>
                <CardSubHeader style={{ fontSize: "24px" }}>{t("BPA_DOCUMENT_DETAILS_LABEL")}</CardSubHeader>
              <div style={{ marginTop: "16px" }}>
                <DocumentsPreview
                  documents={[{
                    values: bpa_details.documents.map(doc => ({
                      title: doc.documentType,
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
              </>
            )}
            {nocSearchResult?.Noc?.some(noc => noc.applicationStatus === "INPROGRESS") && (
             <StatusTable style={{ marginTop: "16px" }}>
              <Accordion
                title={t("NOC_VALIDATION")}
                t={t}
                isFlag={false}
              >
                {/* Show "Click Here to Apply" only if nocInput is empty and no validation results exist */}
                {!nocInput.trim() && !(nocValidationResult?.Noc?.length > 0) && (
                  <div style={{ marginBottom: "16px" }}>
                    <p>
                      {t("DON'T_HAVE_ARN_NUMBER")}{" "}
                      <LinkButton
                        label={t("CLICK_HERE_TO_APPLY")}
                        onClick={() => window.open("https://eodb.assam.gov.in/site/login", "_blank")}
                      />
                    </p>
                  </div>
                )}
                {/* Input for NOC ARN Number */}
                <div style={{ display: "flex", justifyContent: "space-between", width: "100%", marginBottom: "16px" }}>
                  <div style={{ flex: 1 }}>
                    <Row
                      label={t("NOC_ARN_NUMBER")}
                      text={
                        <TextInput
                          value={nocInput}
                          onChange={(e) => setNocInput(e.target.value)}
                          placeholder={t("Enter Noc Number")}
                        />
                      }
                    />
                  </div>
                  <div style={{ marginLeft: "20px" }}>
                    <LinkButton label={t("VALIDATE_NOC")} onClick={handleNocValidate} />
                  </div>
                </div>
                {nocValidationResult && nocValidationResult.Noc && nocValidationResult.Noc.length > 0 && (
                  <div>
                    {nocValidationResult.Noc.map((noc, index) => {
                      const details = noc.additionalDetails || {};
                      return (
                        <StatusTable key={index}>
                          <Row
                            label={t("APPLICATION_TYPE")}
                            text={details.application_type || t("CS_NA")}
                          />
                          <Row
                            label={t("ISSUED_DATE")}
                            text={details.issued_date || t("CS_NA")}
                          />
                          <Row
                            label={t("STATUS")}
                            text={details.status || t("CS_NA")}
                          />
                          <Row
                            label={t("ISSUED_BY")}
                            text={details.issued_by || t("CS_NA")}
                          />
                          <Row
                            label={t("COMPLIANCE_LETTER_NO")}
                            text={details.compliance_letter_no || t("CS_NA")}
                          />
                          <Row
                            label={t("LETTER_DATE")}
                            text={details.letter_date || t("CS_NA")}
                          />
                          <Row
                            label={t("DISTRICT_FIRE_OFFICE")}
                            text={details.district_fire_office || t("CS_NA")}
                          />
                          <Row
                            label={t("VALIDATION_MESSAGE")}
                            text={details.validation_message || t("CS_NA")}
                          />
                        </StatusTable>
                      );
                    })}
                  </div>
                )}
              </Accordion>
            </StatusTable>
            )}
            {additionalDetails?.submitReportinspection_pending?.length > 0 ? (
              <div>
              <StatusTable>
              <Accordion
                title={t("SUBMIT_REPORT_DETAILS")}
                t={t}
                onDownload={() => handleDownloadPdf("SUBMIT_REPORT")}
              >
                {getDetailsRow(additionalDetails?.submitReportinspection_pending?.[0])}
              </Accordion>
            </StatusTable>
              </div>
            ):null}
          {(form22 && form23A) ? (
            <div>
            <StatusTable>
              <Accordion
                title={t("FORM_22_DETAILS")}
                t={t}
                onDownload={() => handleDownloadPdf("FORM_22")}
              >
                {getDetailsRow(form22)}
              </Accordion>
            </StatusTable>

            <StatusTable>
              <Accordion
                title={t("FORM_23A_DETAILS")}
                t={t}
                onDownload={() => handleDownloadPdf("FORM_23A")}
              >
                {getDetailsRow(form23A)}
              </Accordion>
            </StatusTable>
            <StatusTable>
              <Accordion
                title={t("FORM_23B_DETAILS")}
                t={t}
                onDownload={() => handleDownloadPdf("FORM_23B")}
              >
                {getDetailsRow(form23B)}
              </Accordion>
            </StatusTable>
            <StatusTable>
              <Accordion
                title={t("GIS_DETAILS")}
                t={t}
                isFlag={false}
              >
                <GisDetails acknowledgementIds={acknowledgementIds} tenantId={tenantId} t={t} />
              </Accordion>
            </StatusTable>
          </div>
          ):null}
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
      headerBarEnd={<CloseBtn
             onClick={async () => {
              setPopup(false);
              if (setSelectedAction) setSelectedAction(null);

              // Only refetch if GIS succeeded 
              if (selectedAction === "VALIDATE_GIS" && gisValidationSuccess) {
                if (typeof refetch === "function") await refetch();
              }
            }}
            />}
      actionCancelLabel={selectedAction === "VALIDATE_GIS" && showGisResponse ? null : t("CS_COMMON_CANCEL")}
      actionCancelOnSubmit={async () => {
        setPopup(false);
        // Only refetch if GIS succeeded
        if (selectedAction === "VALIDATE_GIS" && gisValidationSuccess) {
          if (typeof refetch === "function") await refetch();
        }
      }}
      actionSaveLabel={
        t("CS_COMMON_SUBMIT")
      }
      popupStyles={{...(selectedAction === "VALIDATE_GIS" && showGisResponse ? {width: "800px", maxWidth: "90vw"}: {}) }}

      actionSaveOnSubmit={async (e) => {
            try {
              if (selectedAction === "VALIDATE_GIS") {
                 await onValidateGIS();
                // Don't close modal, just show response
              } else if (selectedAction==="APPROVE"||selectedAction==="ACCEPT"||selectedAction==="REJECT"||selectedAction==="SEND" ||selectedAction==="SEND_BACK_TO_RTP" || selectedAction==="SUBMIT_REPORT" || selectedAction==="RECOMMEND_TO_CEO" || selectedAction==="SEND_BACK_TO_GMDA") {
                await onAssign(selectedAction, comments);
              }

            } catch (err) {
              console.error(err);
            }
          }}
      error={actioneError}
      setError={setActionError}
      hideSubmit={selectedAction === "VALIDATE_GIS" && showGisResponse ? true : false}

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
          accept=".pdf, .jpeg, .jpg, .png"
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
            {selectedAction === "VALIDATE_GIS" && !showGisResponse && (
                  <div>
                    <CardLabel>{t("CS_ACTION_UPLOAD_LOCATION_FILE")}</CardLabel>
                    <UploadFile
                      id="pgr-doc"
                      accept=".kml"
                      onUpload={selectfile}
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
                      <div>
                        <strong>{t("VALIDATION_STATUS")}:</strong> {gisResponse.validationStatus}
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

    
  </React.Fragment>
</Card>

    </Modal>
            ):null}
            {rtpPopUp ? (
               <Modal
      headerBarMain={
        <Heading
          label={
             t(`CHANGE_RTP`)
          }
        />
      }
      headerBarEnd={<CloseBtn onClick={() => setrtpPopUp(false)} />}
      actionCancelLabel={t("CS_COMMON_CANCEL")}
      actionCancelOnSubmit={() => setrtpPopUp(false)}
      actionSaveLabel={
        t("CS_COMMON_SUBMIT")
      }
      popupStyles={{}}

      actionSaveOnSubmit={async (e) => {
            try {
              
                await onrtpChange(comments);
              

              
            } catch (err) {
              console.error(err);
            }
          }}
      error={actioneError}
      setError={setActionError}
      hideSubmit={selectedAction === "VALIDATE_GIS" && showGisResponse ? true : false}

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
          accept=".pdf, .jpeg, .jpg, .png"
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
    {rtpPopUp && (
      <div>
      <CardLabel>{t("OLD_RTP_NAME")}</CardLabel>
      <TextInput
            type="text"
            t={t}
            isMandatory={false}
            optionKey="i18nKey"
            name="oldRTPName"
            value={oldRTPName}
            onChange={(e) => setOldRTPName(e.target.value)}
            disabled={true}
        />
        <CardLabel>{t("NEW_RTP_NAME")}</CardLabel>
        <Dropdown
            t={t}
            option={rtpOptions}
            selected={newRTPName}
            optionKey="i18nKey"
            select={setNewRTPName}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={t("BPA_SELECT_REGISTERED_TECHNICAL_PERSON")}
          />
        <CardLabel>{t("REASON_FOR_CHANGING_PREVIOUS_RTP")}</CardLabel>
        
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
          accept=".pdf, .jpeg, .jpg, .png"
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
            {selectedAction === "VALIDATE_GIS" && !showGisResponse && (
                  <div>
                    <CardLabel>{t("CS_ACTION_UPLOAD_LOCATION_FILE")}</CardLabel>
                    <UploadFile
                      id="pgr-doc"
                      accept=".kml"
                      onUpload={selectfile}
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
             {toast && assignResponse && <Toast label={t(`CS_ACTION_UPDATE_${selectedAction ? selectedAction : "RTP_CHANGED"}_TEXT`)} onClose={closeToast} />}
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