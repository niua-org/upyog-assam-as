import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useQueryClient } from "react-query";
import { Redirect, Route, Switch, useHistory, useLocation, useRouteMatch } from "react-router-dom";
import { newConfig } from "../../../config/rtpConfig";
import { uuidv4 } from "../../../utils";

const RTPCreate = ({ parentRoute }) => {
  const queryClient = useQueryClient();
  const match = useRouteMatch();
  const { t } = useTranslation();
  const location = useLocation();
  const { pathname } = location;
  const history = useHistory();
  let config = [];
  const [params, setParams, clearParams] = Digit.Hooks.useSessionStorage("EDCR_CREATE", {});
  const [isShowToast, setIsShowToast] = useState(null);
  const [isSubmitBtnDisable, setIsSubmitBtnDisable] = useState(false);
  Digit.SessionStorage.set("RTP_BACK", "IS_RTP_BACK");

  const stateId = Digit.ULBService.getStateId();
  // let { data: newConfig } = Digit.Hooks.obps.SearchMdmsTypes.getFormConfig(stateId, []);

  function handleSelect(key, data, skipStep, index) {
    setIsSubmitBtnDisable(true);
    const loggedInuserInfo = Digit.UserService.getUser();
    const userInfo = { uuid: loggedInuserInfo?.info?.uuid, tenantId: loggedInuserInfo?.info?.tenantId };
    let edcrRequest = {
      transactionNumber: "",
      edcrNumber: "",
      planFile: null,
      tenantId: "",
      RequestInfo: {
        apiId: "",
        ver: "",
        ts: "",
        action: "",
        did: "",
        authToken: "",
        key: "",
        msgId: "",
        correlationId: "",
        userInfo: userInfo
      }
    };

    const applicantName = data?.applicantName;
    const file = data?.file;
    const tenantId = "assam";
    const transactionNumber = uuidv4();
    const appliactionType = "BUILDING_PLAN_SCRUTINY";
    const applicationSubType = "NEW_CONSTRUCTION";

    edcrRequest = { ...edcrRequest, tenantId };
    edcrRequest = { ...edcrRequest, transactionNumber };
    edcrRequest = { ...edcrRequest, applicantName };
    edcrRequest = { ...edcrRequest, appliactionType };
    edcrRequest = { ...edcrRequest, applicationSubType };
    let bodyFormData = new FormData();
    bodyFormData.append("edcrRequest", JSON.stringify(edcrRequest));
    bodyFormData.append("planFile", file);

    Digit.OBPSV2Services.rtpcreate({ data: bodyFormData }, tenantId)
      .then((result, err) => {
        setIsSubmitBtnDisable(false);
        if (result?.data?.edcrDetail) {
          setParams(result?.data?.edcrDetail);
          const urlParams = new URLSearchParams(location.search);
          const applicationNo = urlParams.get('applicationNo');
          const tenantId = urlParams.get('tenantId');
          const redirectUrl = applicationNo
            ? `/upyog-ui/citizen/obpsv2/rtp/apply/acknowledgement?applicationNo=${applicationNo}&tenantId=${tenantId}`
            : `/upyog-ui/citizen/obpsv2/rtp/apply/acknowledgement`;
          history.replace(
            redirectUrl,
            { data: result?.data?.edcrDetail }
          );
        }

      })
      .catch((e) => {
        setParams({ data: e?.response?.data?.errorCode ? e?.response?.data?.errorCode : "BPA_INTERNAL_SERVER_ERROR", type: "ERROR" });
        setIsSubmitBtnDisable(false);

        if (e?.response?.status === 413) {
          history.push(`${match.path}/home`);
          setIsShowToast({ key: true, label: "BPA_FILE_SIZE_TOO_LARGE" })
        }
      });
  }

  useEffect(() => {
    if (isShowToast) {
      const timer = setTimeout(() => {
        setIsShowToast(null);
      }, 2000);

      return () => clearTimeout(timer);
    }
  }, [isShowToast]);

  const handleSkip = () => { };
  const handleMultiple = () => { };

  const onSuccess = () => {
    sessionStorage.removeItem("CurrentFinancialYear");
    queryClient.invalidateQueries("TL_CREATE_TRADE");
  };

  newConfig.forEach((obj) => {
    config = config.concat(obj.body.filter((a) => !a.hideInCitizen));
  });
  config.indexRoute = "home";

  const RTPAcknowledgement = Digit?.ComponentRegistryService?.getComponent('RTPAcknowledgement');

  return (
    <Switch>
      {config.map((routeObj, index) => {
        const { component, texts, inputs, key } = routeObj;
        const Component = typeof component === "string" ? Digit.ComponentRegistryService.getComponent(component) : component;
        return (
          <Route path={`${match.path}/${routeObj.route}`} key={index}>
            <Component config={{ texts, inputs, key }} onSelect={handleSelect} onSkip={handleSkip} t={t} formData={params} onAdd={handleMultiple} isShowToast={isShowToast} isSubmitBtnDisable={isSubmitBtnDisable} setIsShowToast={setIsShowToast} />
          </Route>
        );
      })}
      <Route path={`${match.path}/acknowledgement`}>
        <RTPAcknowledgement data={params} onSuccess={onSuccess} />
      </Route>
    </Switch>
  );
};

export default RTPCreate;