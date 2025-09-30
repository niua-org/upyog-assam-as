import React, { Fragment } from "react";
import { TextInput, SubmitBar, DatePicker, SearchField, Dropdown, CardLabelError, MobileNumber } from "@upyog/digit-ui-react-components";
import { useWatch } from "react-hook-form";
import { useTranslation } from "react-i18next";
import useBusinessServiceData from "../../../../../libraries/src/hooks/obpsv2/useBusinessServiceData";
const SearchFormFieldsComponent = ({ formState, Controller, register, control, t, reset, previousPage }) => {
  const stateTenantId = Digit.ULBService.getStateId();
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();;
  // const userInformation = Digit.UserService.getUser()?.info;
  const userInfos = sessionStorage.getItem("Digit.citizen.userRequestObject");
  const userInfo = userInfos ? JSON.parse(userInfos) : {};
  const userInformation = userInfo?.value?.info;
  const currentUserPhoneNumber = userInformation?.mobileNumber;
  const applicationType = useWatch({ control, name: "applicationType" });
  // 
    control.setValue("status", "");
  sessionStorage.setItem("search_application", JSON.stringify(applicationType));
  const { applicationTypes, ServiceTypes } = Digit.Hooks.obps.useServiceTypeFromApplicationType({
    Applicationtype: applicationType?.code || (userInformation?.roles?.filter((ob) => ob.code.includes("BPAREG_") ).length>0 &&  userInformation?.roles?.filter((ob) => ob.code.includes("BPA_") || ob.code.includes("CITIZEN") ).length<=0 ?"BPA_STAKEHOLDER_REGISTRATION" :"BUILDING_PLAN_SCRUTINY"),
    tenantId: stateTenantId,
  });
  const businessServices = "BPA_GMDA_GMC";
  const { isLoading, data: businessServiceData } = useBusinessServiceData(tenantId, businessServices, {});
  let bpaStatus = [],
    applicationStatuses = [];
  businessServiceData?.BusinessServices?.map((data) => {
    data.states.map((state) => {
      if (state.state && state.applicationStatus) {
        if (data.business == "bpa-services") {
          bpaStatus.push({
            code: state.applicationStatus,
            i18nKey: `WF_BPA_${state.state}`,
            module: data.business,
          });
        } 
            
          }
        })
      })
  applicationStatuses = bpaStatus;

  return (
    <>
      <SearchField>
        <label>{t("OBPAS_SEARCH_APPLICATION_NO_LABEL")}</label>
        <TextInput name="applicationNo" inputRef={register({})} />
      </SearchField>
      {
        window.location.href.includes("citizen/obpsv2/rtp/search/application") &&
        <SearchField>
           
          <CardLabelError>{formState?.errors?.["mobileNumber"]?.message}</CardLabelError>
          <label>{t("CORE_COMMON_MOBILE_NUMBER")}</label>
          <MobileNumber name="mobileNumber"
           type="number"
           inputRef={register({
            minLength: {
              value: 10,
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
            maxLength: {
              value: 10,
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
            pattern: {
              value: /[6789][0-9]{9}/,
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
          })}


        />
        </SearchField>
      }
      <SearchField>
        <label>{t("OBPAS_SEARCH_WARD_LABEL")}</label>
        <TextInput name="wardNo" inputRef={register({})} />
      </SearchField>
      <SearchField>
        <label>{t("OBPAS_APP_FROM_DATE_SEARCH_PARAM")}</label>
        <Controller render={(props) => <DatePicker date={props.value} onChange={props.onChange} />} name="fromDate" control={control} />
      </SearchField>
      <SearchField>
          <label>{t("OBPAS_APP_TO_DATE_SEARCH_PARAM")}</label>
          <Controller render={(props) => <DatePicker date={props.value} onChange={props.onChange} />} name="toDate" control={control} />
      </SearchField>
      <SearchField>
        <label>{t("OBPAS_SEARCH_APPLICATION_STATUS_LABEL")}</label>
        <Controller
          control={control}
          name="status"
          render={(props) => (
            <Dropdown selected={props.value} select={props.onChange} onBlur={props.onBlur} option={applicationStatuses} optionKey="i18nKey" t={t} />
          )}
        />
      </SearchField>
      {window.location.href.includes("citizen/obpsv2/search/application") && <SearchField></SearchField>}
      <SearchField className="submit">
              <SubmitBar label={t("ES_COMMON_SEARCH")} submit />
              <p
                style={{ marginTop: "24px" }}
                onClick={() => {
                  reset({
                    applicationNo: "",
                    mobileNumber: window.location.href.includes("/search/obps-application") ? currentUserPhoneNumber : "",
                    // mobileNumber: "",
                    fromDate: "",
                    toDate: "",
                    status: "",
                    offset: 0,
                    limit: 10,
                    sortBy: "commencementDate",
                    sortOrder: "DESC",
                    applicationType: "",
                    "isSubmitSuccessful":false,
                  });
                  previousPage();
                  // closeMobilePopupModal()
                }}
              >
                {t(`ES_COMMON_CLEAR_ALL`)}
              </p>
            </SearchField>
     
    </>
  );
};

export default SearchFormFieldsComponent;