import React, { Fragment } from "react";
import { FilterFormField, Dropdown } from "@upyog/digit-ui-react-components";
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

  const { data: areaMappingData, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
    ],
    {
      select: (data) => {
        const formattedData = data?.BPA || {};
        return formattedData;
      },
    }
  );

  const statusOptions = [
    { i18nKey: "Pending RTP Approval", code: "PENDING_RTP_APPROVAL", value: t("BPA_PENDING_RTP_APPROVAL") },
    { i18nKey: "Edit Application", code: "EDIT_APPLICATION", value: t("BPA_EDIT_APPLICATION") },
    { i18nKey: "GIS Validation", code: "GIS_VALIDATION", value: t("BPA_GIS_VALIDATION") },
    { i18nKey: "Pending For Scrutiny", code: "PENDING_FOR_SCRUTINY", value: t("BPA_PENDING_FOR_SCRUTINY") },
    { i18nKey: "Send To Citizen", code: "SEND_TO_CITIZEN", value: t("BPA_SEND_TO_CITIZEN") },
    { i18nKey: "Citizen Approval", code: "CITIZEN_APPROVAL", value: t("BPA_CITIZEN_APPROVAL") },
    { i18nKey: "Pending GMDA Engineer", code: "PENDING_GMDA_ENGINEER", value: t("BPA_PENDING_GMDA_ENGINEER") },
    { i18nKey: "Pending Town Planner", code: "PENDING_TOWNPLANNER", value: t("BPA_PENDING_TOWNPLANNER") },
    { i18nKey: "Pending CEO", code: "PENDING_CEO", value: t("BPA_PENDING_CEO") },
    { i18nKey: "Payment Pending", code: "PAYMENT_PENDING", value: t("BPA_PAYMENT_PENDING") },
    { i18nKey: "Forwarded To Zonal Officer", code: "FORWARDED_TO_ZONAL_OFFICER", value: t("BPA_FORWARDED_TO_ZONAL_OFFICER") },
    { i18nKey: "Forwarded To Associate Planner", code: "FORWARDED_TO_ASSOCIATE_PLANNER", value: t("BPA_FORWARDED_TO_ASSOCIATE_PLANNER") },
    { i18nKey: "Pending Commissioner", code: "PENDING_COMMISSIONER", value: t("BPA_PENDING_COMMISSIONER") },
    { i18nKey: "Citizen Final Payment", code: "CITIZEN_FINAL_PAYMENT", value: t("BPA_CITIZEN_FINAL_PAYMENT") },
    { i18nKey: "Application Completed", code: "APPLICATION_COMPLETED", value: t("BPA_APPLICATION_COMPLETED") },
    { i18nKey: "Rejected", code: "REJECTED", value: t("BPA_REJECTED") }
  ];

  const districtOptions = areaMappingData?.districts?.map((district) => ({
    code: district.districtCode,
    name: district.districtCode,
    i18nKey: district.districtCode,
  })) || [];

  return (
    <div style={{ minHeight: "auto", height: "auto", maxHeight: "400px", overflowY: "auto", position: "relative" }} className="rtp-filter-container">
      <FilterFormField>
        <Dropdown
          option={statusOptions}
          selected={getFilterFormValue("applicationStatus")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("applicationStatus", e)}
          disable={isInboxLoading}
          optionCardStyles={{ maxHeight: "200px", zIndex: 9999 }}
          placeholder={t("ES_INBOX_APPLICATION_STATUS")}
          t={t}
        />
      </FilterFormField>

      <FilterFormField>
        <Dropdown
          option={districtOptions}
          selected={getFilterFormValue("district")}
          optionKey="i18nKey"
          onAssignmentChange={(e) => setFilterFormValue("district", e)}
          disable={isLoading}
          optionCardStyles={{ maxHeight: "200px", zIndex: 9999 }}
          placeholder={t("DISTRICT")}
          t={t}
        />
      </FilterFormField>
      <FilterFormField>
      </FilterFormField>
    </div>
  );
};

export default FilterFormFieldsComponent;