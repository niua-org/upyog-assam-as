import React, { Fragment, useCallback, useMemo, useReducer } from "react";
import { CaseIcon, Header } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";
import useBPAV2Inbox from "../../../../../../libraries/src/hooks/obpsv2/useBPAV2Inbox";
import SearchFormFieldsComponents from "./SearchFormFieldsComponent";
import FilterFormFieldsComponent from "./FilterFormFieldsComponent";
import useInboxTableConfig from "./useInboxTableConfig";
import useInboxMobileCardsData from "./useInboxMobileCardsData";
import InboxComposer from "../../../../../../react-components/src/hoc/InboxComposer"
const Inbox = ({ parentRoute }) => {
  window.scroll(0, 0);
  const { t } = useTranslation();

  const tenantId = Digit.ULBService.getCurrentTenantId();

  const searchFormDefaultValues = {};

  const filterFormDefaultValues = {
    moduleName: "bpa-services",
    applicationStatus: [],
    locality: [],
    assignee: "ASSIGNED_TO_ALL",
    applicationType: [],
  };
  const tableOrderFormDefaultValues = {
    sortBy: "",
    limit: Digit.Utils.browser.isMobile() ? 50 : 10,
    offset: 0,
    sortOrder: "DESC",
  };

  function formReducer(state, payload) {
    switch (payload.action) {
      case "mutateSearchForm":
        Digit.SessionStorage.set("OBPSV2.INBOX", { ...state, searchForm: payload.data });
        return { ...state, searchForm: payload.data };
      case "mutateFilterForm":
        Digit.SessionStorage.set("OBPSV2.INBOX", { ...state, filterForm: payload.data });
        return { ...state, filterForm: payload.data };
      case "mutateTableForm":
        Digit.SessionStorage.set("OBPSV2.INBOX", { ...state, tableForm: payload.data });
        return { ...state, tableForm: payload.data };
      default:
        break;
    }
  }
  const InboxObjectInSessionStorage = Digit.SessionStorage.get("OBPSV2.INBOX");
  
  const onSearchFormReset = (setSearchFormValue) => {
    setSearchFormValue("mobileNumber", null);
    setSearchFormValue("applicationNo", null);
    setSearchFormValue("applicantName", null);
    dispatch({ action: "mutateSearchForm", data: searchFormDefaultValues });
  };

  const onFilterFormReset = (setFilterFormValue) => {
    setFilterFormValue("moduleName", "bpa-services");
    setFilterFormValue("applicationStatus", "");
    setFilterFormValue("locality", []);
    setFilterFormValue("assignee", "ASSIGNED_TO_ALL");
    setFilterFormValue("applicationType", []);
    dispatch({ action: "mutateFilterForm", data: filterFormDefaultValues });
  };

  const onSortFormReset = (setSortFormValue) => {
    setSortFormValue("sortOrder", "DESC");
    dispatch({ action: "mutateTableForm", data: tableOrderFormDefaultValues });
  };

  const formInitValue = useMemo(() => {
    return (
      InboxObjectInSessionStorage || {
        filterForm: filterFormDefaultValues,
        searchForm: searchFormDefaultValues,
        tableForm: tableOrderFormDefaultValues,
      }
    );
  }, [
    Object.values(InboxObjectInSessionStorage?.filterForm || {}),
    Object.values(InboxObjectInSessionStorage?.searchForm || {}),
    Object.values(InboxObjectInSessionStorage?.tableForm || {}),
  ]);

  const [formState, dispatch] = useReducer(formReducer, formInitValue);
  const onPageSizeChange = (e) => {
    dispatch({ action: "mutateTableForm", data: { ...formState.tableForm, limit: e.target.value } });
  };
  const onSortingByData = (e) => {
    if (e.length > 0) {
      const [{ id, desc }] = e;
      const sortOrder = desc ? "DESC" : "ASC";
      const sortBy = id;
      if (!(formState.tableForm.sortBy === sortBy && formState.tableForm.sortOrder === sortOrder)) {
        dispatch({ action: "mutateTableForm", data: { ...formState.tableForm, sortBy: id, sortOrder: desc ? "DESC" : "ASC" } });
      }
    }
  };

  const onMobileSortOrderData = (data) => {
    const { sortOrder } = data;
    dispatch({ action: "mutateTableForm", data: { ...formState.tableForm, sortOrder } });
  };

  const getRedirectionLink = (bService) => {
    let redirectBS = "";
    
      redirectBS = window.location.href.includes("/citizen") ? "bpa" : "search/application/bpa"
    
    return redirectBS;
  };
  const { data: localitiesForEmployeesCurrentTenant, isLoading: loadingLocalitiesForEmployeesCurrentTenant } = Digit.Hooks.useBoundaryLocalities(
    tenantId,
    "revenue",
    {},
    t
  );

  const { isLoading: isInboxLoading, data: { table, statuses, totalCount } = {}, refetch } = useBPAV2Inbox({
    tenantId,
    filters: { ...formState },
  });
  const PropsForInboxLinks = {
    logoIcon: <CaseIcon />,
    headerText: "CS_COMMON_OBPS",
    links: [
      {
        text: t("OBPS_SEARCH_APPLICATION"),
        link: "/upyog-ui/citizen/obpsv2/rtp/search/application",
        businessService: "bpa-services",
        roles: ["CITIZEN"],
      },
    ],
  };

  const SearchFormFields = useCallback(
    ({ registerRef, searchFormState, searchFieldComponents }) => (
      <SearchFormFieldsComponents {...{ registerRef, searchFormState, searchFieldComponents }} />
    ),
    []
  );

  const FilterFormFields = useCallback(
    ({ registerRef, controlFilterForm, setFilterFormValue, getFilterFormValue }) => (
      <FilterFormFieldsComponent
        {...{
          statuses,
          isInboxLoading,
          registerRef,
          controlFilterForm,
          setFilterFormValue,
          filterFormState: formState?.filterForm,
          getFilterFormValue,
          localitiesForEmployeesCurrentTenant,
          loadingLocalitiesForEmployeesCurrentTenant,
        }}
      />
    ),
    [statuses, isInboxLoading, localitiesForEmployeesCurrentTenant, loadingLocalitiesForEmployeesCurrentTenant]
  );

  const onSearchFormSubmit = (data) => {
    data.hasOwnProperty("") && delete data?.[""] ;
    dispatch({ action: "mutateTableForm", data: { ...tableOrderFormDefaultValues } });
    dispatch({ action: "mutateSearchForm", data });
  };

  const onFilterFormSubmit = (data) => {
    data.hasOwnProperty("") && delete data?.[""] ;
    dispatch({ action: "mutateTableForm", data: { ...tableOrderFormDefaultValues } });
    dispatch({ action: "mutateFilterForm", data });
  };

  const propsForSearchForm = {
    SearchFormFields,
    onSearchFormSubmit,
    searchFormDefaultValues: formState?.searchForm,
    resetSearchFormDefaultValues: searchFormDefaultValues,
    onSearchFormReset,
  };

  const propsForFilterForm = {
    FilterFormFields,
    onFilterFormSubmit,
    filterFormDefaultValues: formState?.filterForm,
    resetFilterFormDefaultValues: filterFormDefaultValues,
    onFilterFormReset,
  };

  const propsForInboxTable = useInboxTableConfig({ ...{ parentRoute, onPageSizeChange, formState, totalCount, table, dispatch, onSortingByData , refetch} });
  const propsForInboxMobileCards = useInboxMobileCardsData({ parentRoute, table, getRedirectionLink });

  const propsForMobileSortForm = { onMobileSortOrderData, sortFormDefaultValues: formState?.tableForm, onSortFormReset };

  return (
    <>
      <style>
        {`
          @media (min-width: 780px) {
            .filter-form-field-container { height: auto !important; min-height: auto !important; max-height: 300px !important; }
            .filter-form { height: auto !important; min-height: auto !important; max-height: 300px !important; }
            .inbox-search-wrapper .filter-form-field-container { height: auto !important; max-height: 300px !important; }
            .inbox-search-wrapper .filter-form { height: auto !important; max-height: 300px !important; }
            .rtp-filter-container { height: auto !important; min-height: auto !important; max-height: 300px !important; }
          }
        `}
      </style>
      <Header>
        {t("ES_COMMON_INBOX")}
        {totalCount ? <p className="inbox-count">{totalCount}</p> : null}
      </Header>
      {Digit.Utils.browser.isMobile() &&
        <div style={{marginLeft: "12px"}}>
         
          <Link to={"/upyog-ui/citizen/obpsv2/rtp/search/application"}>
          <span className="link">{"search"}</span>
          </Link>
        </div>
      }
      <InboxComposer
        {...{
          isInboxLoading,
          PropsForInboxLinks,
          ...propsForSearchForm,
          ...propsForFilterForm,
          ...propsForMobileSortForm,
          propsForInboxTable,
          propsForInboxMobileCards,
          formState,
        }}
      ></InboxComposer>
    </>
  );
};

export default Inbox;