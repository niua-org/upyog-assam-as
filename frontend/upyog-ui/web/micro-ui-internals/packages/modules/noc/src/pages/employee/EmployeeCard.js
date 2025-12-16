import React, { useMemo, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { EmployeeModuleCard } from "@upyog/digit-ui-react-components";
import { useLocation } from "react-router-dom";

const NOCEmployeeHomeCard = () => {
    const { t } = useTranslation();
    const location = useLocation()
    const tenantId = Digit.ULBService.getCurrentTenantId();
    const { businessServices, isLoading: isBusinessServiceLoading } = Digit.Hooks.noc.useBusinessServiceList(true);

    const { data: nocTypeRoleMapping, isLoading: isMDMSLoading } = Digit.Hooks.useCustomMDMS(
        Digit.ULBService.getStateId(),
        "NOC",
        [{ name: "NOCBusinessServiceRoleMaping" }],
        {
            select: (data) => {
                const formattedData = data?.["NOC"]?.["NOCBusinessServiceRoleMaping"];
                return formattedData?.filter(item => item.active === true);
            },
        }
    );

    const nocRoles = useMemo(() => {
        return nocTypeRoleMapping?.map((items) => items.roles)?.flat() || [];
    }, [nocTypeRoleMapping]);

    const hasNOCAccess = useMemo(() => {
        return Digit.Utils.NOCAccess(nocRoles);
    }, [nocRoles]);

    const searchFormDefaultValues = {}
  
    const filterFormDefaultValues = {
        moduleName: "noc-services",
        applicationStatus: "",
        locality: [],
        assignee: "ASSIGNED_TO_ALL",
        businessServiceArray: businessServices || []
        // businessServiceList(true) || []
    }

    const tableOrderFormDefaultValues = {
        // sortBy: "",
        limit: 10,
        offset: 0,
        // sortOrder: "DESC"
    }
  
    const formInitValue = {
      filterForm: filterFormDefaultValues,
      searchForm: searchFormDefaultValues,
      tableForm: tableOrderFormDefaultValues
    }

    const { isLoading: isInboxLoading, data: {table, statuses, totalCount, nearingSlaCount} = {} } = Digit.Hooks.noc.useInbox({
        tenantId,
        filters: { ...formInitValue },
        config: { 
            enabled: !isMDMSLoading && hasNOCAccess && formInitValue?.filterForm?.businessServiceArray?.length > 0
        },
        workflowCode: nocTypeRoleMapping
    });

    const newName = formInitValue?.filterForm?.businessServiceArray.map(item=>item);


    const ComplaintIcon = () => <svg xmlns="http://www.w3.org/2000/svg" height="24" viewBox="0 0 24 24" width="24">
        <path d="M0 0h24v24H0z" fill="none"></path>
        <path d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 9h-2V5h2v6zm0 4h-2v-2h2v2z" fill="white"></path>
    </svg>

    useEffect(() => {
        if (location.pathname === "/upyog-ui/employee") {
            Digit.SessionStorage.del("NOC.INBOX");
        }
    }, [location.pathname]);

    const propsForModuleCard = useMemo(() => ({
        Icon: <ComplaintIcon />,
        moduleName: <div style={{ width: "200px", wordWrap: "break-word" }}>{t(newName + "_NOC")}</div>,
        kpis: [
            {
                count: !isInboxLoading ? totalCount : "",
                label: t("TOTAL_APPLICATIONS"),
                link: `/upyog-ui/employee/obps/inbox`
            }
            // {   
            //     count: !isInboxLoading ? nearingSlaCount : "-",
            //     label: t("TOTAL_NEARING_SLA"),
            //     link: `/upyog-ui/employee/obps/inbox`
            // }  
        ],
        links: [
            {
                count: totalCount,
                label: t("ES_COMMON_INBOX"),
                link: `/upyog-ui/employee/noc/inbox`
            },
            {
                label: t("ES_COMMON_APPLICATION_SEARCH"),
                link: `/upyog-ui/employee/noc/search`
            }
        ]
    }), [isInboxLoading, totalCount, nearingSlaCount, t, ComplaintIcon]);

    // Early return AFTER all hooks
    if (isMDMSLoading || !hasNOCAccess) return null;
  
    return <EmployeeModuleCard {...propsForModuleCard} />;
};

export default NOCEmployeeHomeCard;
