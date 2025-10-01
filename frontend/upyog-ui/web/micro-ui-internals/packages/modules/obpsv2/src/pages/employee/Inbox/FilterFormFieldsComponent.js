import React, { Fragment } from "react";
import { FilterFormField, Dropdown, CheckBox } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";

const FilterFormFieldsComponent = ({
  statuses,
  isInboxLoading,
  registerRef,
  controlFilterForm,
  setFilterFormValue,
  filterFormState,
  getFilterFormValue,
  localitiesForEmployeesCurrentTenant,
  loadingLocalitiesForEmployeesCurrentTenant,
}) => {
  const { t } = useTranslation();

  const tenantId = Digit.ULBService.getCurrentTenantId();
  const { data: menu, isLoading } = Digit.Hooks.obps.SearchMdmsTypes.useApplicationTypes(tenantId, "BPA", ["ApplicationType"]);

  const applicationTypeOptions = menu?.BPA?.ApplicationType?.map((type) => ({
    code: type.code,
    i18nKey: `WF_BPA_${type.code}`,
  })) || [];

  const statusOptions = statuses?.map((status) => ({
    code: status,
    i18nKey: `WF_BPA_${status}`,
  })) || [];

  const assigneeOptions = [
    { code: "ASSIGNED_TO_ALL", i18nKey: "ES_INBOX_ASSIGNED_TO_ALL" },
    { code: "ASSIGNED_TO_ME", i18nKey: "ES_INBOX_ASSIGNED_TO_ME" },
  ];

  return (
    <Fragment>
      <FilterFormField>
        <Dropdown
          option={applicationTypeOptions}
          selected={getFilterFormValue("applicationType")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("applicationType", e)}
          disable={isInboxLoading}
          optionCardStyles={{ maxHeight: "200px" }}
          placeholder={t("BPA_SEARCH_APPLICATION_TYPE_LABEL")}
          t={t}
        />
      </FilterFormField>

      <FilterFormField>
        <Dropdown
          option={statusOptions}
          selected={getFilterFormValue("applicationStatus")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("applicationStatus", e)}
          disable={isInboxLoading}
          optionCardStyles={{ maxHeight: "200px" }}
          placeholder={t("ES_INBOX_APPLICATION_STATUS")}
          t={t}
        />
      </FilterFormField>

      <FilterFormField>
        <Dropdown
          option={localitiesForEmployeesCurrentTenant}
          selected={getFilterFormValue("locality")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("locality", e)}
          disable={loadingLocalitiesForEmployeesCurrentTenant}
          optionCardStyles={{ maxHeight: "200px" }}
          placeholder={t("ES_INBOX_LOCALITY")}
          t={t}
        />
      </FilterFormField>

      <FilterFormField>
        <Dropdown
          option={assigneeOptions}
          selected={getFilterFormValue("assignee")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("assignee", e)}
          disable={isInboxLoading}
          placeholder={t("ES_INBOX_ASSIGNEE")}
          t={t}
        />
      </FilterFormField>
    </Fragment>
  );
};

export default FilterFormFieldsComponent;