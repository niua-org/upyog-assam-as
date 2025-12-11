import React, { useState } from "react";
import { FormStep } from "@upyog/digit-ui-react-components";
import CommonAreaMapping from "../../../../react-components/src/atoms/AreaMapping.js";

const AreaMapping = ({ t, config, onSelect, formData, searchResult }) => {

  // Get data from session storage
  const sessionData = Digit.SessionStorage.get("CITIZEN.AREA.MAPPING");

  // State for all dropdown values
  const [district, setDistrict] = useState(formData?.areaMapping?.district || (searchResult?.areaMapping?.district ? { code: searchResult.areaMapping.district, name: searchResult.areaMapping.district, i18nKey: searchResult.areaMapping.district } : "") || sessionData?.district);

  const [planningArea, setPlanningArea] = useState(formData?.areaMapping?.planningArea || (searchResult?.areaMapping?.planningArea ? { code: searchResult.areaMapping.planningArea, name: searchResult.areaMapping.planningArea, i18nKey: searchResult.areaMapping.planningArea } : "") || sessionData?.planningArea);

  const [ppAuthority, setPpAuthority] = useState(formData?.areaMapping?.ppAuthority || (searchResult?.areaMapping?.planningPermitAuthority ? { code: searchResult.areaMapping.planningPermitAuthority, name: searchResult.areaMapping.planningPermitAuthority, i18nKey: searchResult.areaMapping.planningPermitAuthority } : "") || sessionData?.ppAuthority);

  const [concernedAuthority, setConcernedAuthority] = useState(formData?.areaMapping?.concernedAuthority || (searchResult?.areaMapping?.concernedAuthority ? { code: searchResult.areaMapping.concernedAuthority, name: searchResult.areaMapping.concernedAuthority, i18nKey: searchResult.areaMapping.concernedAuthority } : "") || sessionData?.concernedAuthority);

  const [bpAuthority, setBpAuthority] = useState(formData?.areaMapping?.bpAuthority || (searchResult?.areaMapping?.buildingPermitAuthority ? { code: searchResult.areaMapping.buildingPermitAuthority, name: searchResult.areaMapping.buildingPermitAuthority, i18nKey: searchResult.areaMapping.buildingPermitAuthority } : "") || sessionData?.bpAuthority);

  const [revenueVillage, setRevenueVillage] = useState(formData?.areaMapping?.revenueVillage || (searchResult?.areaMapping?.revenueVillage ? { code: searchResult.areaMapping.revenueVillage, name: searchResult.areaMapping.revenueVillage, i18nKey: searchResult.areaMapping.revenueVillage } : "") || sessionData?.revenueVillage);

  const [mouza, setMouza] = useState(formData?.areaMapping?.mouza || searchResult?.areaMapping?.mouza || sessionData?.mouza || "");

  const [ward, setWard] = useState(formData?.areaMapping?.ward || (searchResult?.areaMapping?.ward ? { code: searchResult.areaMapping.ward, name: searchResult.areaMapping.ward, i18nKey: searchResult.areaMapping.ward } : "") || sessionData?.ward);

  const [villageName, setVillageName] = useState(formData?.areaMapping?.villageName || (searchResult?.areaMapping?.villageName ? { code: searchResult.areaMapping.villageName, name: searchResult.areaMapping.villageName, i18nKey: searchResult.areaMapping.villageName } : "") || sessionData?.villageName);

  // Validation logic
  const getValidationLogic = () => {
    const baseValidation = !district || !planningArea || !ppAuthority || !bpAuthority || !concernedAuthority;
    
    if (bpAuthority?.code === "MUNICIPAL_BOARD") {
      return baseValidation || !ward || !revenueVillage;
    } else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
      return baseValidation || !villageName;
    }
    
    return baseValidation;
  };

  // Go next
  const goNext = () => {
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

    onSelect(config.key, { ...formData[config.key], ...areaMappingStep });
  };

  const onSkip = () => onSelect();

  return (
    <FormStep
      config={config}
      onSelect={goNext}
      onSkip={onSkip}
      t={t}
      isDisabled={getValidationLogic()}
    >
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
        isDisabled={true}
      />
    </FormStep>
  );
};

export default AreaMapping;