import useInbox from "../useInbox"

const useBPAV2Inbox = ({ tenantId, filters, config={} }) => {
    const { filterForm, searchForm , tableForm } = filters;
    const user = Digit.UserService.getUser();
    let { moduleName, businessService, applicationStatus, locality, assignee, applicationType } = filterForm;
    const { mobileNumber, applicationNo, applicantName } = searchForm;
    const { sortBy, limit, offset, sortOrder } = tableForm;
    let applicationNumber = "";
    let _filters = {
        tenantId,
        processSearchCriteria: {
          assignee : assignee === "ASSIGNED_TO_ME"?user?.info?.uuid:"",
          moduleName: "bpa-services", 
          businessService: ["BPA_DA_MB","BPA_DA_GP"],
          ...(applicationStatus?.length > 0 ? {status: applicationStatus} : {}),
        },
        moduleSearchCriteria: {
          ...(mobileNumber ? {mobileNumber}: {}),
          ...(applicantName ? {applicantName}: {}),
          ...(!applicationNumber ? applicationNo ? {applicationNo} : {} : (applicationNumber ? {applicationNumber} : {})),
          ...(sortOrder ? {sortOrder} : {}),
          ...(sortBy ? {sortBy} : {}),
          ...(applicationType && applicationType?.length > 0 ? {applicationType} : {}),
          ...(locality?.length > 0 ? {locality: locality.map((item) => item.code.split("_").pop()).join(",")} : {}),
        },
        limit
    }

    if (!applicationNo) {
      _filters = { ..._filters, offset}
    }
  const queryResult = useInbox({
  tenantId,
  filters: _filters,
  config: {
    select: (data) => ({
        statuses: data.statusMap,
        table: Array.isArray(data?.items) ? 
          data.items.map((application) => ({
            applicationId:
              application?.businessObject?.applicationNo || application?.ProcessInstance?.businessId || "NA",
            applicantName:
              application?.businessObject?.landInfo?.owners?.[0]?.name || "NA",
            fatherOrHusbandName:
              application?.businessObject?.landInfo?.owners?.[0]
                ?.fatherOrHusbandName || "NA",
            mobileNumber:
              application?.businessObject?.landInfo?.owners?.[0]?.mobileNumber ||
              "NA",
            locality:
              application?.businessObject?.landInfo?.ownerAddresses?.[0]
                ?.locality || "NA",
            wardNo:
              application?.businessObject?.areaMapping?.ward || "NA",
            status: application?.businessObject?.status || application?.ProcessInstance?.state?.applicationStatus || "NA",
            nextActions: application?.ProcessInstance,
            sla: application?.ProcessInstance?.businesssServiceSla ? Math.round(
              application.ProcessInstance.businesssServiceSla /
                (24 * 60 * 60 * 1000)
            ) : 0,
            tenantId: application?.businessObject?.tenantId || application?.ProcessInstance?.tenantId,
            areaMapping: application?.businessObject?.areaMapping,
          })) : [],
        totalCount: data?.totalCount,
        
        nearingSlaCount: data?.nearingSlaCount,
      }),
    ...config,
  },
});

return queryResult;
};

export default useBPAV2Inbox
