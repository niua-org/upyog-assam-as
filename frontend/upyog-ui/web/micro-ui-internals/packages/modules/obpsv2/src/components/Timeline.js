import React from "react";
import { useTranslation } from "react-i18next";
import { TickMark } from "@upyog/digit-ui-react-components";

let actions = [];

const getAction = (flow) => {
  console.log("Flow in timeline:", flow);
  switch (flow) {
    case "buildingPermit":
      actions = [ "BPA_PROPERTY_VALIDATION", "BPA_APPLICANT_DETAILS", "BPA_ADDRESS_DETAILS", "BPA_LAND_DETAILS", "BPA_SUMMARY"];
      break;
    case "editApplication":
      actions = [ "BPA_PROPERTY_VALIDATION", "BPA_APPLICANT_DETAILS", "BPA_ADDRESS_DETAILS", "BPA_LAND_DETAILS", "BPA_DOCUMENTS", "BPA_SUMMARY"];
      break;
    default:
      actions = [
        "BPA_PROPERTY_VALIDATION",
        "BPA_APPLICANT_DETAILS",
        "BPA_ADDRESS_DETAILS",
        "BPA_LAND_DETAILS",
        "BPA_DOCUMENTS",
        "BPA_FORM_22A",
        "BPA_FORM_23A",
        "BPA_FORM_23B",
        "BPA_SUMMARY"
      ];
  }
};
const Timeline = ({ currentStep = 1, flow = "" }) => {
  const { t } = useTranslation();
  const isMobile = window.Digit.Utils.browser.isMobile();
  getAction(flow);
  return (
    <div className="timeline-container" style={isMobile ? {} : { margin: "0 8px 15px" }}>
      {actions.map((action, index, arr) => (
        <div className="timeline-checkpoint" key={index}>
          <div className="timeline-content">
            <span className={`circle ${index <= currentStep - 1 && "active"}`}>{index < currentStep - 1 ? <TickMark /> : index + 1}</span>
            <span className="secondary-color">{t(action)}</span>
          </div>
          {index < arr.length - 1 && <span className={`line ${index < currentStep - 1 && "active"}`}></span>}
        </div>
      ))}
    </div>
  );
};

export default Timeline;