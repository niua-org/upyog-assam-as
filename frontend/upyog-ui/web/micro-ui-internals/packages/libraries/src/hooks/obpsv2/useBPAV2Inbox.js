import useInbox from "../useInbox"

const useBPAV2Inbox = ({ tenantId, filters, config={} }) => {
    const { filterForm, searchForm , tableForm } = filters;
    const user = Digit.UserService.getUser();
    let { applicationStatus, district, assignee, applicationType } = filterForm;
    const { mobileNumber, applicationNo, name } = searchForm;
    console.log("first", name);
    const { sortBy, limit, offset, sortOrder } = tableForm;
    const stateId = Digit.ULBService.getStateId();

     const { data:tenantData } = Digit.Hooks.useEnabledMDMS(
          stateId,
          "tenant",
          [{ name: "tenants" }],
          {
            select: (data) => {
              const tenants = data?.tenant?.tenants || [];
              return tenants.filter(
                (tenant) => tenant?.code === tenantId
              );
            },
          }
        );
    let _filters = {
        tenantId,
        processSearchCriteria: {
          assignee : user.info.type=="EMPLOYEE"? "": user.info.uuid,
          moduleName: "bpa-services", 
          businessService: ["BPA_DA_MB","BPA_DA_GP","BPA_GMDA_GMC"],
        },
        moduleSearchCriteria: {
          ...(mobileNumber ? {mobileNumber}: {}),
          ...(name ? {name}: {}),
          ...(applicationNo ? {applicationNo} : {}),
          sortOrder: sortOrder || "DESC",
          sortBy: sortBy || "createdTime",
          ...(applicationType && applicationType?.length > 0 ? {applicationType} : {}),
          ...(district?.length > 0 ? { district: district.map((item) => item.code) } : {}),
          ...(applicationStatus?.length > 0 ? {status: applicationStatus.map((item) => item.code)} : {}),
        },
        limit
    }

    // Add planningAreaCode only if tenantId ends with "da"
    if (tenantId.endsWith("da") && user.info.roles.some((role) => role.code.endsWith("_DA")) && user.info.type=="EMPLOYEE") {
      _filters = {
        ..._filters,
        planningAreaCode: tenantData?.[0]?.city?.planningAreaCode,
      };
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
            name:
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
