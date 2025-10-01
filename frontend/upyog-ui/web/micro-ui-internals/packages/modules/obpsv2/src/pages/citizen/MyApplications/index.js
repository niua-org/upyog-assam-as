import React, { useState, useEffect } from "react";
import { Header, Loader, TextInput, Dropdown, SubmitBar, CardLabel, Card } from "@upyog/digit-ui-react-components";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import BPAApplication from "./bpa-application";

export const BPAMyApplications = () => {
  const { t } = useTranslation();
  const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
  const user = Digit.UserService.getUser().info;

  const [searchTerm, setSearchTerm] = useState("");
  const [status, setStatus] = useState(null);

  const filter = window.location.href.split("/").pop();
  const t1 = !isNaN(parseInt(filter)) ? parseInt(filter) + 50 : 4;
  const off = !isNaN(parseInt(filter)) ? filter : "0";

  const initialFilters = !isNaN(parseInt(filter))
    ? { limit: "50", sortOrder: "ASC", sortBy: "createdTime", offset: off, tenantId }
    : { limit: "4", sortOrder: "ASC", sortBy: "createdTime", offset: "0", tenantId, mobileNumber: user?.mobileNumber };

  const [filters, setFilters] = useState(initialFilters);

  // Use the search hook with dynamic filters
  const { isLoading, data } = Digit.Hooks.obpsv2.useBPASearchApi({ filters });

  const handleSearch = () => {
    const trimmedSearchTerm = searchTerm.trim();
    const searchFilters = {
      ...initialFilters,
      applicationNo: trimmedSearchTerm || undefined,
      status: status?.code || undefined,
    };

    setFilters(searchFilters);
  };

  const handleLoadMore = () => {
    const newFilters = {
      limit: "50",
      sortOrder: "ASC", 
      sortBy: "createdTime",
      offset: t1.toString(),
      tenantId,
      mobileNumber: user?.mobileNumber
    };
    setFilters(newFilters);
  };

  if (isLoading) {
    return <Loader />;
  }

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

  const filteredApplications = data?.bpa || [];

  return (
    <React.Fragment>
      <Header>{`${t("BPA_MY_APPLICATIONS_HEADER")} (${filteredApplications.length})`}</Header>
      <Card>
        <div style={{ marginLeft: "16px" }}>
          <div style={{ display: "flex", flexDirection: "row", alignItems: "center", gap: "16px" }}>
            <div style={{ flex: 1 }}>
              <div style={{ display: "flex", flexDirection: "column" }}>
                <CardLabel>{t("BPA_APPLICATION_NO")}</CardLabel>
                <TextInput
                  placeholder={t("BPA_ENTER_APPLICATION_NO")}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  style={{ width: "100%", padding: "8px", height: "150%" }}
                />
              </div>
            </div>
            <div style={{ flex: 1 }}>
              <div style={{ display: "flex", flexDirection: "column" }}>
                <CardLabel>{t("PT_COMMON_TABLE_COL_STATUS_LABEL")}</CardLabel>
                <Dropdown
                  className="form-field"
                  selected={status}
                  select={setStatus}
                  option={statusOptions}
                  placeholder={t("BPA_SELECT_STATUS")}
                  optionKey="value"
                  style={{ width: "100%" }}
                  t={t}
                />
              </div>
            </div>
            <div>
              <div style={{ marginTop: "17%" }}>
                <SubmitBar label={t("ES_COMMON_SEARCH")} onSubmit={handleSearch} />
                <p
                  className="link"
                  style={{ marginLeft: "30%", marginTop: "10px", display: "block" }}
                  onClick={() => {
                    setSearchTerm("");
                    setStatus(null);
                    setFilters(initialFilters);
                  }}
                >
                  {t(`ES_COMMON_CLEAR_ALL`)}
                </p>
              </div>
            </div>
          </div>
          <Link to="/upyog-ui/citizen/obpsv2/applicant-details">
            <SubmitBar style={{ borderRadius: "30px", width: "20%", marginTop: "16px" }} label={t("BPA_NEW_APPLICATION") + " +"} />
          </Link>
        </div>
      </Card>
      <div>
        {filteredApplications.length > 0 &&
          filteredApplications.map((application, index) => (
            <div key={index}>
              <BPAApplication application={application} tenantId={tenantId} buttonLabel={t("BPA_VIEW_DETAILS")} />
            </div>
          ))}
        {filteredApplications.length === 0 && !isLoading && (
          <p style={{ marginLeft: "16px", marginTop: "16px" }}>{t("BPA_NO_APPLICATION_FOUND_MSG")}</p>
        )}

        {filteredApplications.length !== 0 && data?.count > t1 && (
          <div>
            <p style={{ marginLeft: "16px", marginTop: "16px" }}>
              <span className="link" onClick={handleLoadMore}>
                {t("BPA_LOAD_MORE_MSG")}
              </span>
            </p>
          </div>
        )}
      </div>
    </React.Fragment>
  );
};