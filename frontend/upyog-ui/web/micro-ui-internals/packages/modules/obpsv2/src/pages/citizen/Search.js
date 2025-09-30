import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import useOBPSV2Search from "../../../../../libraries/src/hooks/obpsv2/useOBPSV2Search";
const Search = ({ path }) => {
  const userInfos = sessionStorage.getItem("Digit.citizen.userRequestObject");
  const userInfo = userInfos ? JSON.parse(userInfos) : {};
  const userInformation = userInfo?.value?.info;

  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
  const location = useLocation();
  const details = () => {
    return "NEW_CONSTRUCTION"
  }
  const [selectedType, setSelectedType] = useState(details());
  const [payload, setPayload] = useState({});
  const [searchData, setSearchData] = useState({});

  useEffect(()=>{
    if (location.pathname === "/upyog-ui/citizen/obpsv2/rtp/search/application" || location.pathname === "/upyog-ui/employee/obps/search/application") {
      Digit.SessionStorage.del("OBPSV2.INBOX")
    }
  },[location.pathname])

  const Search = Digit.ComponentRegistryService.getComponent("RTASearchApplication");

  const checkData = (data) => {
    if (data?.applicationNo === "" && data?.fromDate === "" && data?.mobileNumber === "" && data?.serviceType === "" && data?.status === "" && data?.toDate === "") return false

    return true

  }

  const [paramerror,setparamerror] = useState("")

  function onSubmit(_data) {
    setSearchData(_data);
    var fromDate = new Date(_data?.fromDate);
    fromDate?.setSeconds(fromDate?.getSeconds() - 19800);
    var toDate = new Date(_data?.toDate);
    setSelectedType(_data?.applicationType?.code ? _data?.applicationType?.code : selectedType);
    toDate?.setSeconds(toDate?.getSeconds() + 86399 - 19800);
    const data = {
      ..._data,
      ...(_data.toDate ? { toDate: toDate?.getTime() } : {}),
      ...(_data.fromDate ? { fromDate: fromDate?.getTime() } : {}),
    };

    setPayload(
      Object.keys(data)
        .filter((k) => data[k])
        .reduce((acc, key) => ({ ...acc, [key]: typeof data[key] === "object" ? data[key].code : data[key] }), {})
    );
  }

  let params = {};
  let filters = {};
    if (Object.keys(payload).length === 0) {
      let payload1 = {
        applicationType: "NEW_CONSTRUCTION",
        // serviceType: "NEW_CONSTRUCTION",
        ...(window.location.href.includes("/search/obps-application") && {
          mobileNumber: Digit.UserService.getUser()?.info?.mobileNumber,
        }),
      };

      setPayload({ ...payload, ...payload1 });
    }
  filters = payload;
  const { data: bpaData = [], isLoading: isBpaSearchLoading, isSuccess: isBpaSuccess, error: bpaerror } = useOBPSV2Search(
    selectedType,
    payload,
    tenantId,
    filters,
    params,
    {enabled:paramerror===""}
  );
  return (
    <Search
      t={t}
      tenantId={tenantId}
      onSubmit={onSubmit}
      searchData={searchData}
      isLoading={isBpaSearchLoading}
      Count={bpaData?.[0]?.Count}
      error={paramerror}
      data={!isBpaSearchLoading && isBpaSuccess && bpaData?.length > 0 ? bpaData : [{ display: "ES_COMMON_NO_DATA" }]}
      setparamerror={setparamerror}
    />
  );
};

export default Search;