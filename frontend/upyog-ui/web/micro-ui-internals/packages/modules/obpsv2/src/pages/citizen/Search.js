import React, { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

const Search = ({ path }) => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
  const location = useLocation();
  const [selectedType, setSelectedType] = useState("");
  const [payload, setPayload] = useState({});
  const [searchData, setSearchData] = useState({});
  const [paramerror, setparamerror] = useState("");

  useEffect(() => {
    if (location.pathname === "/upyog-ui/citizen/obpsv2/rtp/search/application" || location.pathname === "/upyog-ui/employee/obpsv2/search/application") {
      Digit.SessionStorage.del("OBPSV2.INBOX");
    }
  }, [location.pathname]);

  useEffect(() => {
    if (Object.keys(payload).length === 0) {
      const initialPayload = {
        ...(window.location.href.includes("/search/obps-application") && {
          mobileNumber: Digit.UserService.getUser()?.info?.mobileNumber,
        }),
      };
      if (Object.keys(initialPayload).length > 0) {
        setPayload(initialPayload);
      }
    }
  }, []);

  const SearchComponent = Digit.ComponentRegistryService.getComponent("RTASearchApplication");

  function onSubmit(_data) {
    setSearchData(_data);
    const fromDate = new Date(_data?.fromDate);
    fromDate?.setSeconds(fromDate?.getSeconds() - 19800);
    const toDate = new Date(_data?.toDate);
    setSelectedType(_data?.applicationType?.code ? _data?.applicationType?.code : selectedType);
    toDate?.setSeconds(toDate?.getSeconds() + 86399 - 19800);
    
    const data = {
      ..._data,
      ...(_data.toDate ? { toDate: toDate?.getTime() } : {}),
      ...(_data.fromDate ? { fromDate: fromDate?.getTime() } : {}),
    };

    setPayload(
      Object.keys(data)
        .filter((k) => data[k] && k !== "businessServices")
        .reduce((acc, key) => ({ ...acc, [key]: typeof data[key] === "object" ? data[key].code : data[key] }), {})
    );
  }

  const { isLoading: isBpaSearchLoading, isError, error: bpaerror, data: bpaData } = Digit.Hooks.obpsv2.useBPASearchApi({
    tenantId,
    filters: payload,
    enabled: Object.keys(payload).length > 0
  });

  const processedData = React.useMemo(() => {
    if (isBpaSearchLoading || isError || !bpaData?.bpa?.length) {
      return [];
    }
    return bpaData.bpa;
  }, [isBpaSearchLoading, isError, bpaData]);

  return (
    <SearchComponent
      t={t}
      tenantId={tenantId}
      onSubmit={onSubmit}
      searchData={searchData}
      isLoading={isBpaSearchLoading}
      Count={bpaData?.count || 0}
      error={paramerror}
      data={processedData}
      setparamerror={setparamerror}
    />
  );
};

export default Search;