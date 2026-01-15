import React, { Fragment } from "react";
import { TextInput, SubmitBar, DatePicker, SearchField, Dropdown, CardLabelError, MobileNumber } from "@upyog/digit-ui-react-components";
import { useWatch } from "react-hook-form";
import { useTranslation } from "react-i18next";
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
    // control.setValue("status", "");
  sessionStorage.setItem("search_application", JSON.stringify(applicationType));
  const { applicationTypes, ServiceTypes } = Digit.Hooks.obps.useServiceTypeFromApplicationType({
    Applicationtype: applicationType?.code || (userInformation?.roles?.filter((ob) => ob.code.includes("BPAREG_") ).length>0 &&  userInformation?.roles?.filter((ob) => ob.code.includes("BPA_") || ob.code.includes("CITIZEN") ).length<=0 ?"BPA_STAKEHOLDER_REGISTRATION" :"BUILDING_PLAN_SCRUTINY"),
    tenantId: stateTenantId,
  });

    const applicationStatuses = [
      { i18nKey: "Pending RTP Approval", code: "PENDING_RTP_APPROVAL", value: t("BPA_PENDING_RTP_APPROVAL") },
      { i18nKey: "Edit Application", code: "EDIT_APPLICATION", value: t("BPA_EDIT_APPLICATION") },
      { i18nKey: "GIS Validation", code: "GIS_VALIDATION", value: t("BPA_GIS_VALIDATION") },
      { i18nKey: "Pending For Scrutiny", code: "PENDING_FOR_SCRUTINY", value: t("BPA_PENDING_FOR_SCRUTINY") },
      { i18nKey: "Send To Citizen", code: "SEND_TO_CITIZEN", value: t("BPA_SEND_TO_CITIZEN") },
      { i18nKey: "Citizen Approval", code: "CITIZEN_APPROVAL", value: t("BPA_CITIZEN_APPROVAL") },
      { i18nKey: "Pending DA Engineer", code: "PENDING_DA_ENGINEER", value: t("BPA_PENDING_DA_ENGINEER") },
      { i18nKey: "Pending DD AD Development Authority", code: "PENDING_DD_AD_DEVELOPMENT_AUTHORITY", value: t("BPA_PENDING_DD_AD_DEVELOPMENT_AUTHORITY") },
      { i18nKey: "Pending Chairman DA", code: "PENDING_CHAIRMAN_DA", value: t("BPA_PENDING_CHAIRMAN_DA") },
      { i18nKey: "Payment Pending", code: "PAYMENT_PENDING", value: t("BPA_PAYMENT_PENDING") },
      { i18nKey: "Forwarded To Technical Engineer", code: "FORWARDED_TO_TECHNICAL_ENGINEER", value: t("BPA_FORWARDED_TO_TECHNICAL_ENGINEER") },
      { i18nKey: "Forwarded To DD AD TCP", code: "FORWARDED_TO_DD_AD_TCP", value: t("BPA_FORWARDED_TO_DD_AD_TCP") },
      { i18nKey: "Pending Chairman President", code: "PENDING_CHAIRMAN_PRESIDENT", value: t("BPA_PENDING_CHAIRMAN_PRESIDENT") },
      { i18nKey: "Citizen Final Payment", code: "CITIZEN_FINAL_PAYMENT", value: t("BPA_CITIZEN_FINAL_PAYMENT") },
      { i18nKey: "Application Completed", code: "APPLICATION_COMPLETED", value: t("BPA_APPLICATION_COMPLETED") },
      { i18nKey: "Rejected", code: "REJECTED", value: t("BPA_REJECTED") }
    ].sort((a, b) => a.code.localeCompare(b.code));
  return (
    <>
      <SearchField>
        <label>{t("BPA_APPLICATION_NUMBER_LABEL")}</label>
        <TextInput name="applicationNo" inputRef={register({})} />
      </SearchField>
      
      <SearchField>
        <label>{t("CORE_COMMON_MOBILE_NUMBER")}</label>
        <MobileNumber
          name="mobileNumber"
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
          type="number"
        />
        <CardLabelError>{formState?.errors?.["mobileNumber"]?.message}</CardLabelError>
      </SearchField>
      
      <SearchField>
        <label>{t("APPLICANT_NAME")}</label>
        <TextInput name="name" inputRef={register({})} />
      </SearchField>
      
      <SearchField>
        <label>{t("BPA_STATUS_LABEL")}</label>
        <Controller
          control={control}
          name="status"
          render={(props) => (
            <Dropdown 
              selected={props.value} 
              select={props.onChange} 
              onBlur={props.onBlur} 
              option={applicationStatuses} 
              optionKey="i18nKey" 
              t={t}
              disable={false}
            />
          )}
        />
      </SearchField>
      
      <SearchField>
        <label>{t("BPA_FROM_DATE_LABEL")}</label>
        <Controller 
          render={(props) => <DatePicker date={props.value} disabled={false} onChange={props.onChange} />} 
          name="fromDate" 
          control={control} 
        />
      </SearchField>
      
      <SearchField>
        <label>{t("BPA_TO_DATE_LABEL")}</label>
        <Controller 
          render={(props) => <DatePicker date={props.value} disabled={false} onChange={props.onChange} />} 
          name="toDate" 
          control={control} 
        />
      </SearchField>
      <SearchField>
      </SearchField>
      <SearchField className="submit">
        <SubmitBar label={t("ES_COMMON_SEARCH")} submit />
        <p
          style={{ marginTop: "10px" }}
          onClick={() => {
            reset({
              applicationNo: "",
              mobileNumber: "",
              name: "",
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
          }}
        >
          {t(`ES_COMMON_CLEAR_ALL`)}
        </p>
      </SearchField>
     
    </>
  );
};

export default SearchFormFieldsComponent;