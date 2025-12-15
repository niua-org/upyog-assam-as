import { Loader, Modal} from "@upyog/digit-ui-react-components";
import React, { useState, useEffect } from "react";
import { useQueryClient } from "react-query";
import { configBPAApproverApplication } from "../config";
import * as predefinedConfig from "../config";
import  FormComposer  from "../../../../react-components/src/hoc/FormComposer";
import { useHistory } from "react-router-dom";

const Heading = (props) => {
  return <h1 style={{marginLeft:"22px"}} className="heading-m BPAheading-m">{props.label}</h1>;
};

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

const ActionModal = ({ t, action, tenantId, state, id, closeModal, submitAction, actionData, applicationDetails, applicationData, businessService, moduleCode,workflowDetails }) => {
  const mutation1 = Digit.Hooks.obps.useObpsAPI(
      applicationData?.landInfo?.address?.city ? applicationData?.landInfo?.address?.city : tenantId,
      false
    );
   const { data: approverData, isLoading: PTALoading } = Digit.Hooks.useEmployeeSearch(
    tenantId,
    {
      roles: workflowDetails?.data?.initialActionState?.nextActions?.filter(ele=>ele?.action==action?.action)?.[0]?.assigneeRoles?.map(role=>({code:role})),
      isActive: true,
    },
    { enabled: !action?.isTerminateState }
  );

  const queryClient = useQueryClient();
  const [config, setConfig] = useState({});
  const [defaultValues, setDefaultValues] = useState({});
  const [approvers, setApprovers] = useState([]);
  const [selectedApprover, setSelectedApprover] = useState({});
  const [file, setFile] = useState(null);
  const [uploadedFile, setUploadedFile] = useState(null);
  const [error, setError] = useState(null);
  const [selectedFinancialYear, setSelectedFinancialYear] = useState(null);
  const mobileView = Digit.Utils.browser.isMobile() ? true : false;
  const history = useHistory();

  useEffect(() => {
    setApprovers(approverData?.Employees?.map((employee) => ({ uuid: employee?.uuid, name: employee?.user?.name })));
  }, [approverData]);

  function selectFile(e) {
    setFile(e.target.files[0]);
  }

  useEffect(() => {
    (async () => {
      setError(null);
      if (file) {
        const allowedFileTypesRegex = /(.*?)(jpg|jpeg|png|image|pdf)$/i
        if (file.size >= 5242880) {
          setError(t("CS_MAXIMUM_UPLOAD_SIZE_EXCEEDED"));
        } else if (file?.type && !allowedFileTypesRegex.test(file?.type)) {
          setError(t(`NOT_SUPPORTED_FILE_TYPE`))
        } else {
          try {
            const response = await Digit.UploadServices.Filestorage("OBPS", file, Digit.ULBService.getStateId() || tenantId?.split(".")[0]);
            if (response?.data?.files?.length > 0) {
              setUploadedFile(response?.data?.files[0]?.fileStoreId);
            } else {
              setError(t("CS_FILE_UPLOAD_ERROR"));
            }
          } catch (err) {
            setError(t("CS_FILE_UPLOAD_ERROR"));
          }
        }
      }
    })();
  }, [file]);


  

  const getSubmitReport = (data) => {
    let inspectionOb = [];
      const formdata = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA"));
      if (formdata) {
        inspectionOb.push(formdata.submitReport);  
      }   
    return inspectionOb;
  };

  const getDocuments = (applicationData) => {
    let documentsformdata = JSON.parse(sessionStorage.getItem("OBPS_DOCUMENTS"));
    let documentList = [];
    documentsformdata && documentsformdata.map(doc => {
      if(doc?.uploadedDocuments?.[0]?.values?.length > 0) documentList = [...documentList, ...doc?.uploadedDocuments?.[0]?.values];
      if(doc?.newUploadedDocs?.length > 0) documentList = [...documentList, ...doc?.newUploadedDocs]
    });
    const submitReportFormdata = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA"));
    const nocDocuments = submitReportFormdata?.nocDetails?.AAI_NOC_DETAILS?.[0]?.documents;
    if (Array.isArray(nocDocuments) && nocDocuments.length > 0) {
      documentList = [...documentList, ...nocDocuments];
    }  
    return documentList;
  }

  const getPendingApprovals = () => {
    const approvals = Digit.SessionStorage.get("OBPS_APPROVAL_CHECKS");
    const newApprovals = Digit.SessionStorage.get("OBPS_NEW_APPROVALS");
    let result = approvals?.reduce((acc, approval) => approval?.checked ? acc.push(approval?.label) && acc : acc, []);
    result = result?.concat(newApprovals !== null?newApprovals.filter(ob => ob.label !== "").map(approval => approval?.label):[]);
    return result;
  }

  async function submit(data) {
    const storedData = JSON.parse(sessionStorage.getItem("SUBMIT_REPORT_DATA")) || {};
    const submitReport = getSubmitReport(applicationData);
    const nocList = storedData.nocList || [];
    const nocDetails = storedData.nocDetails || {};
    nocDetails.permitType = "Planning Permit";
    applicationData = {
      ...applicationData,
      nocList: nocList,
      documents: getDocuments(applicationData),
      additionalDetails: {
        ...applicationData?.additionalDetails,
        submitReportinspection_pending: submitReport,
        nocDetails:nocDetails,
        pendingapproval: getPendingApprovals()
      },
       workflow:{
        action: action?.action,
        comment: data?.comments?.length > 0 ? data?.comments : null,
        comments: data?.comments?.length > 0 ? data?.comments : null,
        assignee: !selectedApprover?.uuid ? null : [selectedApprover?.uuid],
        assignes: !selectedApprover?.uuid ? null : [selectedApprover?.uuid],
        varificationDocuments: uploadedFile
        ? [
          {
            documentType: action?.action + " DOC",
            fileName: file?.name,
            fileStoreId: uploadedFile,
          },
        ]
        : null,
      },
      action: action?.action,
      comment: data?.comments,
      assignee: !selectedApprover?.uuid ? null : [selectedApprover?.uuid],
      wfDocuments: uploadedFile
        ? [
          {
            documentType: action?.action + " DOC",
            fileName: file?.name,
            fileStoreId: uploadedFile,
          },
        ]
        : null,
    };
    submitAction({
      BPA:applicationData
    });
  }


  useEffect(() => {
    if (action) {
      if (action.action === "PAY") {
        let servicePath = "BPA.PLANNING_PERMIT_FEE";
        if (applicationData?.status === "CITIZEN_FINAL_PAYMENT") {
          servicePath = "BPA.BUILDING_PERMIT_FEE";
        }
        console.log(servicePath);
        return history.push(`/upyog-ui/employee/payment/collect/${servicePath}/${applicationData.applicationNo}`);
      }
      setConfig(
        configBPAApproverApplication({
          t,
          action,
          approvers,
          selectedApprover,
          setSelectedApprover,
          selectFile,
          uploadedFile,
          setUploadedFile,
          businessService,
          assigneeLabel: "WF_ASSIGNEE_NAME_LABEL",
          error
        })
      );
    }
  }, [action, approvers, selectedFinancialYear, uploadedFile, error]);

  return action && config.form ? (
    <Modal
      headerBarMain={<Heading label={t(config.label.heading)} />}
      headerBarEnd={<CloseBtn onClick={closeModal} />}
      actionCancelLabel={t(config.label.cancel)}
      actionCancelOnSubmit={closeModal}
      actionSaveLabel={t(config.label.submit)}
      actionSaveOnSubmit={() => { }}
      formId="modal-action"
      isOBPSFlow={true}
      popupStyles={mobileView?{width:"720px"}:{}}
      style={!mobileView?{minHeight: "45px", height: "auto", width:"107px",paddingLeft:"0px",paddingRight:"0px"}:{minHeight: "45px", height: "auto",width:"44%"}}
      popupModuleMianStyles={mobileView?{paddingLeft:"5px"}: {}}
    >
      {(
        <FormComposer
          config={config.form}
          cardStyle={{marginLeft:"0px",marginRight:"0px", marginTop:"-25px"}}
          className="BPAemployeeCard"
          noBoxShadow
          inline
          childrenAtTheBottom
          onSubmit={submit}
          defaultValues={defaultValues}
          formId="modal-action"
        />
      )}
    </Modal>
  ) : (
    <Loader />
  );
};

export default ActionModal;