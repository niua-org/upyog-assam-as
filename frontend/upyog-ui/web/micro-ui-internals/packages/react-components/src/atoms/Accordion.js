import React, { useState } from "react";
import { CardLabel, LinkButton } from "@upyog/digit-ui-react-components";

const Accordion = ({ title, children, onDownload, t, isFlag=true }) => {
  const [open, setOpen] = useState(false);

  return (
    <div style={{ border: "1px solid #e0e0e0", borderRadius: "6px", marginBottom: "18px" }}>
      <div
        style={{
          background: "#f7f7f7",
          padding: "14px 18px",
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          cursor: "pointer",
          borderRadius: open ? "6px 6px 0 0" : "6px",
        }}
        onClick={() => setOpen(!open)}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <CardLabel style={{ fontSize: "18px", fontWeight: "600" }}>{title}</CardLabel>
          <span
            style={{
              fontSize: "20px",
              fontWeight: "bold",
              transition: "0.25s",
              marginTop: "-5px",
              transform: open ? "rotate(180deg)" : "rotate(0deg)"
            }}
          >
            ▼
          </span>
        </div>
        {isFlag && ( // conditionally render the download button
        <LinkButton
          label={
            <div className="response-download-button">
              <span>
                <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22"
                  viewBox="0 0 24 24" fill="#a82227">
                  <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z" />
                </svg>
              </span>
              <span className="download-button">{t("CS_COMMON_DOWNLOAD")}</span>
            </div>
          }
          onClick={(e) => {
            e.stopPropagation();
            onDownload();
          }}
        />
        )}
      </div>

      {open && (
        <div style={{ background: "white", padding: "18px", borderTop: "1px solid #e0e0e0" }}>
          {children}
        </div>
      )}
    </div>
  );
};

export default Accordion;
