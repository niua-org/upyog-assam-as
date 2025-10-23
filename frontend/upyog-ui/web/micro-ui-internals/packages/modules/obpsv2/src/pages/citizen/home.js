import {
  BPAHomeIcon,
  CitizenHomeCard,
  EmployeeModuleCard,
  Loader,
  Toast,
} from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import useBPAV2Inbox from "../../../../../libraries/src/hooks/obpsv2/useBPAV2Inbox";

const OBPASCitizenHomeScreen = ({ parentRoute }) => {
  const { t } = useTranslation();
  const location = useLocation();
  const userInfo = Digit.UserService.getUser();
  const userRoles = userInfo?.info?.roles?.map((roleData) => roleData.code);
  const stateCode = Digit.ULBService.getStateId();

  const [showToast, setShowToast] = useState(null);
  const [totalCount, setTotalCount] = useState("-");
  const [isArchitect, setIsArchitect] = useState(false);

  const closeToast = () => {
    window.location.replace("/upyog-ui/citizen/all-services");
    setShowToast(null);
  };

  const [searchParams] = useState({ applicationStatus: [] });
  const [pageOffset, setPageOffset] = useState(0);
  const [pageSize] = useState(window.Digit.Utils.browser.isMobile() ? 50 : 10);
  const [sortParams] = useState([{ id: "createdTime", sortOrder: "DESC" }]);
  const { isLoading: bpaLoading, data: bpaInboxData } = useBPAV2Inbox({
    tenantId: stateCode,
    moduleName: "bpa-services",
    businessService: ["BPA_GMDA_GMC"],
    filters: {
      searchForm: { ...searchParams },
      tableForm: {
        sortBy: sortParams?.[0]?.id,
        limit: pageSize,
        offset: pageOffset,
        sortOrder: sortParams?.[0]?.sortOrder,
      },
      filterForm: {
        moduleName: "bpa-services",
        businessService: [],
        applicationStatus: searchParams?.applicationStatus,
        locality: [],
        assignee: "ASSIGNED_TO_ALL",
      },
    },
    config: {},
    //withEDCRData: false,
  });
  useEffect(() => {
    if (location.pathname === "/upyog-ui/citizen/obpsv2/home") {
      Digit.SessionStorage.del("OBPSV2.INBOX");
    }
  }, [location.pathname]);
  useEffect(() => {
    if (userRoles?.includes("BPA_ARCHITECT")) {
      setIsArchitect(true);
    } else {
      setIsArchitect(false);
      setShowToast({ key: "true", message: "Please login as Architect or RTP" });
    }
  }, [userRoles]);

  useEffect(() => {
    if (!bpaLoading) {
      setTotalCount(bpaInboxData?.totalCount || 0);
    }
  }, [bpaInboxData]);

  if (showToast)
    return <Toast error={true} label={t(showToast?.message)} isDleteBtn={true} onClose={closeToast} />;

  if (bpaLoading || !isArchitect) {
    return <Loader />;
  }

  const homeDetails = [
    {
      Icon: <BPAHomeIcon />,
      moduleName: t("ACTION_TEST_OBPAS_RTP_INBOx"),
      name: "employeeCard",
      isCitizen: true,
      kpis: [
        {
          count: !bpaLoading ? totalCount : "-",
          label: t("OBPAS_PDF_TOTAL"),
          link: `/upyog-ui/citizen/obpsv2/rtp/inbox`,
        },
      ],
      links: [
        {
          count: !bpaLoading ? totalCount : "-",
          label: t("ES_COMMON_OBPS_RTP_INBOX_LABEL"),
          link: `/upyog-ui/citizen/obpsv2/rtp/inbox`,
        },
        {
          label: t("ES_COMMON_RTP_SEARCH_APPLICATION"),
          link: `/upyog-ui/citizen/obpsv2/rtp/search/application`
        },
      ],
      className: "CitizenHomeCard",
      styles: { padding: "0px", minWidth: "90%", minHeight: "90%" },
    },
    
  ];

  const homeScreen = (
    <div className="mainContent">
      {homeDetails.map((data, index) => (
        <div key={index}>
          {data.name === "employeeCard" ? (
            <EmployeeModuleCard {...data} />
          ) : (
            <CitizenHomeCard
              header={data.title}
              links={data.links}
              Icon={() => data.Icon}
              styles={data?.styles}
            />
          )}
        </div>
      ))}
    </div>
  );
  sessionStorage.setItem("isPermitApplication", true);
  sessionStorage.setItem("isEDCRDisable", JSON.stringify(false));
  return homeScreen;
};

export default OBPASCitizenHomeScreen;