import React, { useEffect, useState } from "react";
import { Row } from "@upyog/digit-ui-react-components";

// GIS Details Component to fetch and display GIS data 
const GisDetails = ({ acknowledgementIds, tenantId, t }) => {
  const [gisData, setGisData] = useState(null);

  useEffect(() => {
    const fetchGisData = async () => {
      try {
        const tenantId = Digit.ULBService.getCitizenCurrentTenant(true) || Digit.ULBService.getCurrentTenantId();
        const response = await Digit.OBPSV2Services.gisSearch({
          GisSearchCriteria: {
            applicationNo: acknowledgementIds,
            tenantId: tenantId,
            status: "SUCCESS"
          }
        });
        if (response?.Gis) {
          setGisData(response.Gis);
        }
      } catch (error) {
        console.error('GIS Search Error:', error);
      }
    };
    if (acknowledgementIds && tenantId) {
      fetchGisData();
    }
  }, [tenantId, acknowledgementIds]);

  return gisData && gisData.length > 0 ? (
    <div>
      <Row label={t("LATITUDE")} text={gisData[0].latitude?.toString() || "-"} />
      <Row label={t("LONGITUDE")} text={gisData[0].longitude?.toString() || "-"} />
      <Row label={t("DISTRICT")} text={gisData[0].details?.district || "-"} />
      <Row label={t("LANDUSE")} text={gisData[0].details?.landuse || "-"} />
      <Row label={t("VILLAGE")} text={gisData[0].details?.village || "-"} />
      <Row label={t("AREA_HECTARE")} text={gisData[0].details?.areaHectare || "-"} />
      <Row label={t("WARD_NO")} text={gisData[0].details?.ward || "-"} />
    </div>
  ) : null;
};

export default GisDetails;