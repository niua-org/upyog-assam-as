import React from "react";
import { useTranslation } from "react-i18next";
import { useRouteMatch } from "react-router-dom";
import { Loader, CitizenHomeCard, OBPSIcon, CitizenInfoLabel } from "@upyog/digit-ui-react-components";
import CitizenApp from "./pages/citizen";
import Create from "./pages/citizen/Create";
import RTPCreate from "./pages/citizen/RTPCreate";
import Inbox from "./pages/citizen/RTPInbox";
import Edit from "./pages/citizen/EditApplication";
import ApplicantDetails  from "./pageComponents/ApplicantDetails";
import AddressDetails from "./pageComponents/AddressDetails";
import LandDetails from "./pageComponents/LandDetails";
import CheckPage from "./pages/citizen/Create/CheckPage";
import { BPAMyApplications } from "./pages/citizen/MyApplications";
import AreaMapping from "./pageComponents/AreaMapping";
import BPAAcknowledgement from "./pages/citizen/Create/BPAAcknowledgement";
import DocumentDetails from "./pageComponents/DocumentDetails";
import RTPAcknowledgement from "./pages/citizen/RTPCreate/RTPAcknowledgement";
import Form22A from "./pageComponents/Form22A";
import Form23A from "./pageComponents/Form23A";
import Form23B from "./pageComponents/Form23B";
import OBPASCitizenHomeScreen from "./pages/citizen/home";
import RTPForm from "./pageComponents/RTPForm";
import EmployeeApp from "./pages/employee";
import BPAApplicationDetails from "./pages/citizen/ApplicationDetails";
import BPAEmployeeDetails from "./pages/employee/BpaApplicationDetail";
import OBPSV2Inbox from "./pages/employee/Inbox";
import OBPSV2EmployeeCard from "./pages/employee/EmployeeCard";
import siteReport from "./pageComponents/siteReport"
import RTASearchApplication from "./components/SearchApplication";
const OBPSV2Module = ({ stateCode, userType, tenants }) => {
  const moduleCode = "OBPSV2";
  const { path, url } = useRouteMatch();
  const language = Digit.StoreData.getCurrentLanguage();
  const { isLoading, data: store } = Digit.Services.useStore({ stateCode, moduleCode, language });

  Digit.SessionStorage.set("OBPSV2_TENANTS", tenants);

  if (isLoading) {
    return <Loader />;
  }

  if (userType === "citizen") {
    return <CitizenApp path={path} stateCode={stateCode} />;
  }

  return <EmployeeApp path={path} stateCode={stateCode} />
}

const OBPSV2Links = ({ matchPath, userType }) => {
  const { t } = useTranslation();

  const links = [
    
    {
      link: `${matchPath}/building-permit`,
      i18nKey: t("BPA_CITIZEN_HOME_STAKEHOLDER_LOGIN_LABEL"),
    },
    {
      link: `${matchPath}/home`,
      i18nKey: t("BPA_CITIZEN_HOME_ARCHITECT_LOGIN_LABEL"),
    }
  ];

  return (
    <CitizenHomeCard header={t("ACTION_TEST_BUILDING_PLAN_APPROVAL")} links={links} Icon={() => <OBPSIcon />}
      Info={() => <CitizenInfoLabel style={{margin: "0px", padding: "10px"}} info={t("CS_FILE_APPLICATION_INFO_LABEL")} text={t(`BPA_CITIZEN_HOME_STAKEHOLDER_INCLUDES_INFO_LABEL`)} />} isInfo={true}
    />
  );
} 

const componentsToRegister = {
  OBPSV2Module,
  OBPSV2Links,
  ApplicantDetails,
  AddressDetails,
  DocumentDetails,
  Form22A,
  Form23A,
  Form23B,
  LandDetails,
  BPACreate: Create,
  RTPCreate,
  RTPInbox: Inbox,
  OBPSV2Inbox,
  OBPSV2Card:OBPSV2EmployeeCard,
  CheckPage,
  BPAMyApplications,
  AreaMapping,
  RTPForm,
  siteReport,
  BPAAcknowledgement,
  RTPAcknowledgement,
  BPAEdit : Edit,
  OBPASCitizenHomeScreen,
  RTASearchApplication,
  BPAApplicationDetails,
  BPAEmployeeDetails
}

export const initOBPSV2Components = () => {
  Object.entries(componentsToRegister).forEach(([key, value]) => {
    Digit.ComponentRegistryService.setComponent(key, value);
  });
};