import useInbox from "../useInbox"

const useNOCInbox = ({ tenantId, filters, config={}, workflowCode}) => {
    const { filterForm, searchForm , tableForm } = filters;
    let { moduleName, businessService, applicationStatus, locality, assignee, businessServiceArray } = filterForm;
    const { sourceRefId, applicationNo } = searchForm;
    const { sortBy, limit, offset, sortOrder } = tableForm;
    const user = Digit.UserService.getUser();
    const businessServiceList = () => {
    const availableBusinessServices = workflowCode || []; // fallback to empty if undefined
    const newAvailableBusinessServices = [];
    const loggedInUserRoles = user?.info?.roles || [];

    availableBusinessServices.forEach(({ roles, code }) => {
      roles.forEach((role) => {
        loggedInUserRoles.forEach((el) => {
          if (el.code === role) {
            newAvailableBusinessServices.push(code);
          }
        });
      });
    });

    return newAvailableBusinessServices;
  };

  if (!businessServiceArray?.length && !businessService) {
    businessServiceArray = businessServiceList();
  }


    const _filters = {
        tenantId,
        processSearchCriteria: {
          assignee : assignee === "ASSIGNED_TO_ME"?user?.info?.uuid:"",
          moduleName: "noc-services", 
          businessService:businessService?.code ? [businessService?.code] : businessServiceArray ,
          ...(applicationStatus?.length > 0 ? {status: applicationStatus} : {}),
        },
        moduleSearchCriteria: {
          ...(sourceRefId ? {sourceRefId}: {}),
          ...(applicationNo ? {applicationNo} : {}),
          ...(sortOrder ? {sortOrder} : {}),
          ...(sortBy ? {sortBy} : {}),
          ...(locality?.length > 0 ? {locality: locality.map((item) => item.code.split("_").pop()).join(",")} : {}),
        },
        // sortBy,
        limit,
        offset,
        // sortOrder
    }

    return useInbox({tenantId, filters: _filters, config:{
        select: (data) =>({
          statuses: data.statusMap,
          table: data?.items.map( application => ({
              applicationId: application.businessObject.applicationNo,
              date: parseInt(application.businessObject?.auditDetails?.createdTime),
              businessService: application?.ProcessInstance?.businessService,
              locality: `${application.businessObject?.tenantId?.toUpperCase()?.split(".")?.join("_")}`,
              status: `WF_${application.businessObject.additionalDetails.workflowCode}_${application.businessObject.applicationStatus}`,//application.businessObject.applicationStatus,
              owner: application?.ProcessInstance?.assignes?.[0]?.name || "-",
              source: application.businessObject.source,
              sla: application?.businessObject?.applicationStatus.match(/^(APPROVED)$/) ? "CS_NA" : Math.round(application.ProcessInstance?.businesssServiceSla / (24 * 60 * 60 * 1000))
          })),
          totalCount: data.totalCount,
          nearingSlaCount: data.nearingSlaCount
        }), 
        ...config 
      }
    })
}

export default useNOCInbox
