import React, { Fragment, useMemo, useState, useEffect } from "react"
import { Link } from "react-router-dom";
import { format } from "date-fns";
import { useTranslation } from "react-i18next";
import { Dropdown, Toast, SubmitBar } from "@upyog/digit-ui-react-components";
import { OBPSV2Services } from "../../../../../../libraries/src/services/elements/OBPSV2";
import Action from "../../../components/Action";

const useInboxTableConfig = ({ parentRoute, onPageSizeChange, formState, totalCount, table = [], dispatch, onSortingByData, refetch}) => {
    const GetCell = (value) => <span className="cell-text styled-cell">{value}</span>;
    const GetStatusCell = (value) => value === "CS_NA" ? t(value) : value === "Active" || value>0 ? <span className="sla-cell-success">{value}</span> : <span className="sla-cell-error">{value}</span> 
    const { t } = useTranslation()
    const [error, setError] = useState(null);
    const [showToast, setShowToast] = useState(false);
    const [toastMessage, setToastMessage] = useState("");
    const [selectedAction, setSelectedAction] = useState(null);
    const [applicationNo, setApplicationNo] = useState();
    const [showActionMenu, setShowActionMenu] = useState(null);
    
    useEffect(() => {
        if (showToast || error) {
          const timer = setTimeout(() => {
            setShowToast(null);
            setError(null);
            setToastMessage("");
            setSelectedAction(null);
            setApplicationNo(null);
            //window.location.reload(); // Reload page after toast
          }, 1000);
          return () => clearTimeout(timer);
        }
    }, [showToast, error]);

    // Close dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = () => {
            setShowActionMenu(null);
        };
        document.addEventListener('click', handleClickOutside);
        return () => document.removeEventListener('click', handleClickOutside);
    }, []);
    
    const tableColumnConfig = useMemo(() => {
        return [
        {
            Header: t("BPA_APPLICATION_NUMBER_LABEL"),
            accessor: "applicationNo",
            disableSortBy: true,
            Cell: ({ row }) => {
            return (
                <div>
                <Link to={window.location.href.includes("/citizen") ? `${parentRoute}/application/${row?.original["applicationId"]}/${row?.original["tenantId"]}` : `${parentRoute}/inbox/bpa/${row.original["applicationId"]}`}>
                    <span className="link">{row?.original["applicationId"]||"NA"}</span>
                </Link>
                </div>
            );
            },
        },
        {
            Header: t("APPLICANT_NAME"),
            accessor: "applicantName",
            Cell: ({row}) => row?.original?.applicantName || "NA"
        },
        {
            Header: t("DISTRICT"),
            accessor: "district",
            Cell: ({row}) => t(row?.original?.areaMapping?.district) || "NA"
        },
        {
            Header: t("MOBILE_NUMBER"),
            accessor: "mobileNumber",
            Cell: ({row}) => row?.original?.mobileNumber || "NA"
        },
        {
            Header: t("STATUS"),
            accessor: "status",
            Cell: ({row}) => t(row?.original?.status) || t("CS_NA")
        },
        {
            Header: t("ACTION"),
            accessor: "action",
            disableSortBy: true,
            Cell: ({ row }) => {
                const processInstance = row?.original;
                const stateActions = processInstance?.nextActions?.nextActions || [];
                const filteredActions = stateActions;
                
                const options = Array.isArray(filteredActions)
                ? filteredActions.map((action) => ({
                    code: action?.action,
                    i18nKey: `CS_ACTION_${action?.action}`
                    }))
                : [];

                const handleSelect = (value) => {
                    setSelectedAction(value.code);
                    let applicationId = processInstance?.nextActions?.businessId || row.original["applicationId"];
                    setApplicationNo(applicationId);
                };

                const closeModal = () => {
                    setSelectedAction(null);
                    setApplicationNo(null);
                };

                return (
                    <React.Fragment>
                {options.length > 0 ? (
                    <div style={{ minWidth: "140px", position: "relative" }}>
                        <div
                            onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                // Preserve scroll position
                                const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
                                setShowActionMenu(showActionMenu === row.original.applicationId ? null : row.original.applicationId);
                                // Restore scroll position after state update
                                setTimeout(() => {
                                    window.scrollTo(0, scrollTop);
                                }, 0);
                            }}
                            onMouseDown={(e) => e.preventDefault()}
                            style={{
                                width: "100%",
                                padding: "8px 12px",
                                backgroundColor: "#a82227",
                                color: "white",
                                border: "none",
                                borderRadius: "4px",
                                cursor: "pointer",
                                fontSize: "14px",
                                fontWeight: "500",
                                outline: "none",
                                textAlign: "center",
                                userSelect: "none",
                                display: "inline-block"
                            }}
                        >
                            {t("Take Action")}
                        </div>
                        {showActionMenu === row.original.applicationId && (
                            <div 
                                onClick={(e) => e.stopPropagation()}
                                style={{
                                position: "absolute",
                                top: "100%",
                                left: 0,
                                right: 0,
                                backgroundColor: "white",
                                border: "1px solid #ccc",
                                borderRadius: "4px",
                                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                                zIndex: 1000,
                                marginTop: "2px"
                            }}>
                                {options.map(option => (
                                    <div
                                        key={option.code}
                                        style={{
                                            padding: "10px 12px",
                                            cursor: "pointer",
                                            borderBottom: options.length > 1 ? "1px solid #eee" : "none",
                                            fontSize: "14px"
                                        }}
                                        onClick={() => {
                                            handleSelect(option);
                                            setShowActionMenu(null);
                                        }}
                                        onMouseEnter={(e) => e.target.style.backgroundColor = "#f5f5f5"}
                                        onMouseLeave={(e) => e.target.style.backgroundColor = "white"}
                                    >
                                        {t(option.i18nKey)}
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                ) : (
                    <span style={{ fontSize: "12px", color: "#666" }}>{t("No Actions Available")}</span>
                )}
                {selectedAction && applicationNo === (processInstance?.nextActions?.businessId || row.original["applicationId"]) && (
                    <Action
                        row={row}
                        selectedAction={selectedAction}
                        applicationNo={applicationNo}
                        parentRoute={parentRoute}
                        setShowToast={setShowToast}
                        setError={setError}
                        setToastMessage={setToastMessage}
                        closeModal={closeModal}
                        setSelectedAction={setSelectedAction}
                        refetch={refetch}
                    />
                )}
                {(showToast||error) && !selectedAction && (
                 <Toast
                        error={
                            error 
                            ? error 
                            : (typeof showToast === 'object' && showToast?.error) 
                                ? true 
                                : null
                        }
                        warning={typeof showToast === 'object' && showToast?.warning ? true : null}
                        label={error ? error : (toastMessage || t(`CS_ACTION_${selectedAction}_SUCCESS`))}
                        onClose={() => {
                            setShowToast(null);
                            setError(null);
                            setToastMessage("");
                        }}
                        />
                    )}
                </React.Fragment>
                );
            },
        }
        ]
    })

    return {
        getCellProps: (cellInfo) => {
        return {
            style: {
            padding: "20px 18px",
            fontSize: "16px"
        }}},
        disableSort: false,
        autoSort:false,
        manualPagination:true,
        initSortId:"applicationDate",
        onPageSizeChange:onPageSizeChange,
        currentPage: formState.tableForm?.offset / formState.tableForm?.limit,
        onNextPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: (parseInt(formState.tableForm?.offset) + parseInt(formState.tableForm?.limit)) }}),
        onPrevPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: (parseInt(formState.tableForm?.offset) - parseInt(formState.tableForm?.limit)) }}),
        pageSizeLimit: formState.tableForm?.limit,
        onSort: onSortingByData,
        totalRecords: totalCount,
        onSearch: formState?.searchForm?.message,
        onLastPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: (Math.ceil(totalCount / 10) * 10 - parseInt(formState.tableForm?.limit)) }}),
        onFirstPage: () => dispatch({action: "mutateTableForm", data: {...formState.tableForm , offset: 0 }}),
        data: table || [],
        columns: tableColumnConfig
    }
}

export default useInboxTableConfig