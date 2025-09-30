import React, { useMemo } from "react";
import { Link } from "react-router-dom";
import { convertEpochToDateDMY } from "../../utils";

const useSearchApplicationTableConfig = ({t}) => {

    const getRedirectionLink = (bService) => {
        let redirectBS = bService === "BPAREG"?"search/application/stakeholder":"search/application/bpa";
        if (window.location.href.includes("/citizen")) {
          redirectBS = bService === "BPAREG"?"stakeholder":"bpa";
        }
        return redirectBS;
    }
    const GetCell = (value) => <span className="cell-text">{value}</span>;
    const GetStatusCell = (value) => value === "CS_NA" ? t(value) : value === "Active" || value>0 ? <span className="sla-cell-success">{value}</span> : <span className="sla-cell-error">{value}</span> 
    return useMemo( () => ([


        {
          Header: t("BPA_APPLICATION_NUMBER_LABEL"),
          accessor: "applicationNo",
          disableSortBy: true,
          Cell: ({ row }) => {
            return (
              <div>
                <span className="link">
                <Link to={window.location.href.includes("/citizen") ? `/upyog-ui/citizen/obpsv2/application/${row?.original["applicationNo"]}/${row?.original["tenantId"]}` : `${parentRoute}/inbox/bpa/${row.original["applicationNo"]}`}>
                    {row.original["applicationNo"] || row.original["applicationNumber"]}
                  </Link>
                </span>
              </div>
            );
          },
        },
        {
          Header: t("APPLICANT_NAME"),
          accessor: "applicantName",
          Cell: ({row}) => row?.original?.landInfo?.owners?.[0]?.name || "NA",
          },
          {
          Header: t("FATHERS_NAME"),
          accessor: "fatherOrHusbandName",
          Cell: ({row}) => row?.original?.landInfo?.owners?.[0]?.fatherOrHusbandName || "NA"
          },
          {
          Header: t("MOBILE_NUMBER"),
          accessor: "mobileNumber",
          Cell: ({row}) => row?.original?.landInfo?.owners?.[0]?.mobileNumber ||"NA",
          },
      
      {
          Header: t("WARD_NUMBER"),
          accessor: "wardNo",
          Cell: ({row}) => row?.original?.areaMapping?.ward || "NA",
          },
          {
            Header: t("STATUS"),
            accessor: "status",
            Cell: ({row}) => row?.original?.status || "NA",
            },
      
      {
          Header: t("SLA"),
          accessor: row => GetStatusCell(row?.original?.sla),
          Cell: ({row}) => row?.original?.sla
      },
      ]), [] )
}

export default useSearchApplicationTableConfig