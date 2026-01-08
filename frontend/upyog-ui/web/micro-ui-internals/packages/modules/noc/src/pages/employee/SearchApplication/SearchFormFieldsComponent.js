import React, { Fragment } from "react";
import { TextInput, SubmitBar, DatePicker, SearchField, Dropdown, CardLabelError, MobileNumber, CardHeader } from "@upyog/digit-ui-react-components";
import { Controller, useFormContext } from "react-hook-form";
import { useTranslation } from "react-i18next";



const SearchFormFieldsComponent = (props) => {
  const { register, control, setValue, getValues, reset, formState, trigger  } = useFormContext()
  const { t } = useTranslation();
  const { businessServices } = Digit.Hooks.noc.useBusinessServiceList(true);

    // Prepare NOC types by removing '_SRV' suffix so that it can be used in API filters
  const nocTypeList = businessServices?.map((item) => ({
    ...item,
    code: item.replace(/_SRV$/, ""), // remove _SRV only at end
  }));
  let availableNocTypes = [];
  if (nocTypeList?.length == 1) availableNocTypes = [nocTypeList?.[0]?.code];
  if (nocTypeList?.length > 1) nocTypeList?.forEach(nocDta => {availableNocTypes.push(nocDta.code);})

  function previousPage() {
    setValue("offset", getValues("offset") - getValues("limit"));
    props?.onSubmit({
      offset: 0,
      limit: 10,
      sortBy: "commencementDate",
      sortOrder: "DESC",
      nocType: availableNocTypes[0]
    }, true);
    props?.isMobileView ? props.closeMobilePopupModal() : null;
  }
  
  
  return (
    <>
      <SearchField>
        <label>{t("NOC_APP_NO_LABEL")}</label>
        <TextInput name="applicationNo" inputRef={register({})} />
      </SearchField>
      <SearchField>
        <label>{t("NOC_SOURCE_MODULE_NUMBER")}</label>
        <TextInput name="sourceRefId" inputRef={register({})} />
      </SearchField>
      
      <SearchField>
        <label>{t("NOC_APP_MOBILE_NO_SEARCH_PARAM")}</label>
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
              //type: "tel",
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
          })}
          type="number"
          componentInFront={<div className="employee-card-input employee-card-input--front">+91</div>}
          //maxlength={10}
        />
        <CardLabelError>{formState?.errors?.["mobileNumber"]?.message}</CardLabelError>
      </SearchField>
      <SearchField>
        <label>{t("NOC_NUMBER_LABEL")}</label>
        <TextInput name="nocNo" inputRef={register({})} />
      </SearchField>
      {/* <SearchField></SearchField> */}
      <SearchField className="submit">
        <SubmitBar label={t("ES_COMMON_SEARCH")} submit />
        <p
          style={{ marginTop: "24px" }}
          onClick={() => {
            setValue("applicationNo", null);
            setValue("sourceRefId", null);
            setValue("mobileNumber", null);
            setValue("nocType", availableNocTypes[0]);
            setValue("offset", 0);
            setValue("limit", 10);
            setValue("sortBy","commencementDate");
            setValue("sortOrder","DESC");
            setValue("isSubmitSuccessful","false");
            reset({
              applicationNo: "",
              sourceRefId: "",
              nocNo: "",
              nocType: "",
              mobileNumber: "",
              offset: 0,
              limit: 10,
              sortBy: "commencementDate",
              sortOrder: "DESC",
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
