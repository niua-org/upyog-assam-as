import { BackButton, CardHeader, CardLabelError, LinkButton } from "@upyog/digit-ui-react-components";
import CommonAreaMapping from "../../../../../../react-components/src/atoms/AreaMapping.js";
import React, { useMemo, useState, useEffect, Fragment } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation, Link } from "react-router-dom";
import PageBasedInput from "../../../../../../react-components/src/molecules/PageBasedInput";

const AreaMapping = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();
  
  const [showError, setShowError] = useState(false);

  // State for all dropdown values
  const [district, setDistrict] = useState("");
  const [planningArea, setPlanningArea] = useState("");
  const [ppAuthority, setPpAuthority] = useState("");
  const [concernedAuthority, setConcernedAuthority] = useState("");
  const [bpAuthority, setBpAuthority] = useState("");
  const [revenueVillage, setRevenueVillage] = useState("");
  const [mouza, setMouza] = useState("");
  const [ward, setWard] = useState("");
  const [villageName, setVillageName] = useState("");

  const texts = useMemo(
    () => ({
      header: t("BPA_AREA_MAPPING"),
      submitBarLabel: t("CORE_COMMON_CONTINUE"),
    }),
    [t]
  );

  // Validation logic
  const getValidationLogic = () => {
    const baseValidation = !district;
    
    if (bpAuthority?.code === "MUNICIPAL_BOARD") {
      return baseValidation || !ward || !revenueVillage;
    } else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
      return baseValidation || !villageName;
    }
    
    return baseValidation;
  };

  function onSubmit() {
    if (getValidationLogic()) {
      setShowError(true);
      return;
    }
    
    let areaMappingStep = {
      district,
      planningArea,
      ppAuthority,
      concernedAuthority,
      bpAuthority,
      ...(bpAuthority?.code === "MUNICIPAL_BOARD" && { ward, revenueVillage }),
      ...(bpAuthority?.code === "GRAM_PANCHAYAT" && { villageName }),
      mouza
    };
    
    Digit.SessionStorage.set("CITIZEN.AREA.MAPPING", areaMappingStep);
    
    // Set the selected tenant
    if (concernedAuthority?.code) {
      Digit.SessionStorage.set("CITIZEN.COMMON.HOME.CITY", concernedAuthority);
    }
    
    const redirectBackTo = location.state?.redirectBackTo;
    if (redirectBackTo) {
      history.replace(redirectBackTo);
    } else history.push("/upyog-ui/citizen");
  }

  return (
    <div className="selection-card-wrapper">
      <BackButton />
      <PageBasedInput texts={texts} onSubmit={onSubmit}>
        <div style={{ position: "relative" }}>
          <CardHeader>
            <span>{t("BPA_AREA_MAPPING")}</span>
          </CardHeader>
          
          <Link 
            to={`/upyog-ui/citizen/rtp/select-location`}
            style={{
              position: "absolute",
              top: "0",
              right: "0",
              zIndex: 10
            }}
          >
            <LinkButton label={t("BPA_RTP_LOGIN")} />
          </Link>
        </div>
        
        <CommonAreaMapping
          t={t}
          district={district}
          setDistrict={setDistrict}
          planningArea={planningArea}
          setPlanningArea={setPlanningArea}
          ppAuthority={ppAuthority}
          setPpAuthority={setPpAuthority}
          concernedAuthority={concernedAuthority}
          setConcernedAuthority={setConcernedAuthority}
          bpAuthority={bpAuthority}
          setBpAuthority={setBpAuthority}
          revenueVillage={revenueVillage}
          setRevenueVillage={setRevenueVillage}
          mouza={mouza}
          setMouza={setMouza}
          ward={ward}
          setWard={setWard}
          villageName={villageName}
          setVillageName={setVillageName}
          isDisable={false}
        />
        
        {showError ? <CardLabelError>{t("COMPLETE_AREA_MAPPING")}</CardLabelError> : null}
      </PageBasedInput>
    </div>
  );
};

export default AreaMapping;