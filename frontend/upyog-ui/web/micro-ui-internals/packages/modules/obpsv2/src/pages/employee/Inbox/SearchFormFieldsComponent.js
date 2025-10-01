import React, { Fragment } from "react";
import { SearchField, TextInput } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const SearchFormFieldsComponents = ({ registerRef, searchFormState, searchFieldComponents }) => {
  const { t } = useTranslation();

  return (
    <Fragment>
      <SearchField>
        <label>{t("BPA_APPLICATION_NUMBER_LABEL")}</label>
        <TextInput
          name="applicationNo"
          inputRef={registerRef({
            maxLength: {
              value: 64,
              message: t("CORE_COMMON_APPLICANT_MAX_LENGTH_ERROR").replace("{{maxLength}}", 64),
            },
          })}
        />
      </SearchField>

      <SearchField>
        <label>{t("APPLICANT_NAME")}</label>
        <TextInput
          name="applicantName"
          inputRef={registerRef({})}
        />
      </SearchField>

      <SearchField>
        <label>{t("MOBILE_NUMBER")}</label>
        <TextInput
          name="mobileNumber"
          inputRef={registerRef({
            minLength: {
              value: 10,
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
            maxLength: {
              value: 10,
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
            pattern: {
              value: /^[0-9]+$/i,
              message: t("CORE_COMMON_MOBILE_ERROR"),
            },
          })}
        />
      </SearchField>

      {searchFieldComponents}
    </Fragment>
  );
};

export default SearchFormFieldsComponents;