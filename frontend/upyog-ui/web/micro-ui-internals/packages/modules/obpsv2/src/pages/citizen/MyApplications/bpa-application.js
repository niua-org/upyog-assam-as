import { Card, KeyNote, SubmitBar, CardSubHeader } from "@upyog/digit-ui-react-components";
import React from "react";
import { useTranslation } from "react-i18next";
import { Link } from "react-router-dom";

const BPAApplication = ({ application, tenantId, buttonLabel }) => {
  const { t } = useTranslation();

  return (
    <Card> 
      <KeyNote keyValue={t("BPA_APPLICATION_NO")} note={application?.applicationNo} />
      <KeyNote keyValue={t("BPA_TYPE_OF_CONSTRUCTION")} note={t(application?.additionalDetails?.constructionType)} />
      <KeyNote keyValue={t("BPA_OCCUPANCY_TYPE")} note={t(application?.landInfo?.units[0]?.occupancyType)} />
      <KeyNote keyValue={t("PT_COMMON_TABLE_COL_STATUS_LABEL")} note={t(application?.status)} />
      
      {application?.slaDaysRemaining && (
        <KeyNote 
          keyValue={t("BPA_SLA_DAYS_REMAINING")} 
          note={`${application.slaDaysRemaining} ${t("BPA_DAYS")} ${t("BPA_SLA_DISCLAIMER")}`} 
        />
      )}
      
      <Link to={`/upyog-ui/citizen/obpsv2/application/${application?.applicationNo}/${application?.tenantId}`}>
        <SubmitBar label={buttonLabel} />
      </Link>
    </Card>
  );
};

export default BPAApplication;