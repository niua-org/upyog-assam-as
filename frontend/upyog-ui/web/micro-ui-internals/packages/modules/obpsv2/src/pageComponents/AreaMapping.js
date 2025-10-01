import React, { useEffect, useState } from "react";
import { FormStep, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";

const AreaMapping = ({ t, config, onSelect, formData, searchResult }) => {

  // State for dropdown options
  const [districts, setDistricts] = useState([]);
  const [planningAreas, setPlanningAreas] = useState([]);
  const [ppAuthorities, setPpAuthorities] = useState([]);
  const [bpAuthorities, setBpAuthorities] = useState([]);
  const [revenueVillages, setRevenueVillages] = useState([]);
  const [mouzas, setMouzas] = useState([]);
  
  // State for all dropdown values
  const [district, setDistrict] = useState(formData?.areaMapping?.district || "");
  const [planningArea, setPlanningArea] = useState(formData?.areaMapping?.planningArea || "");
  const [ppAuthority, setPpAuthority] = useState(formData?.areaMapping?.ppAuthority || "");
  const [bpAuthority, setBpAuthority] = useState(formData?.areaMapping?.bpAuthority || "");
  const [revenueVillage, setRevenueVillage] = useState(formData?.areaMapping?.revenueVillage || "");
  const [mouza, setMouza] = useState(formData?.areaMapping?.mouza || "");
  const [ward, setWard] = useState(searchResult?.areaMapping?.ward || formData?.areaMapping?.ward || "");

  // Fetch data from MDMS
  const { data: areaMappingData, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
      { name: "planningAreas" }, 
      { name: "ppAuthorities" }, 
      { name: "bpAuthorities" }, 
      { name: "revenueVillages" }, 
      { name: "mouzas" }
    ],
    {
      select: (data) => {
        const formattedData = data?.BPA || {};
        return formattedData;
      },
    }
  );

  // Initialize districts from MDMS data
  useEffect(() => {
    if (areaMappingData?.districts) {
      const formattedDistricts = areaMappingData.districts.map((district) => ({
        code: district.districtCode,
        name: district.districtName,
        i18nKey: district.districtCode,
      }));
      setDistricts(formattedDistricts);
    }
  }, [areaMappingData]);

  // Update planning areas when district changes
  useEffect(() => {
    if (district && areaMappingData?.planningAreas) {
      const filteredPlanningAreas = areaMappingData.planningAreas
        .filter(area => area.districtCode === district?.code)
        .map(area => ({
          code: area.planningAreaCode,
          name: area.planningAreaName,
          i18nKey: area.planningAreaCode,
        }));
      setPlanningAreas(filteredPlanningAreas);
    } else {
      setPlanningAreas([]);
    }
  }, [district, areaMappingData]);

  // Update PP authorities when planning area changes
  useEffect(() => {
    if (planningArea && areaMappingData?.ppAuthorities) {
      const filteredPpAuthorities = areaMappingData.ppAuthorities
        .filter(authority => authority.planningAreaCode === planningArea?.code)
        .map(authority => ({
          code: authority.ppAuthorityCode,
          name: authority.ppAuthorityName,
          i18nKey: authority.ppAuthorityCode,
        }));
      setPpAuthorities(filteredPpAuthorities);
    } else {
      setPpAuthorities([]);
    }
  }, [planningArea, areaMappingData]);

  // Update BP authorities when PP authority changes
  useEffect(() => {
    if (ppAuthority && areaMappingData?.bpAuthorities) {
      const filteredBpAuthorities = areaMappingData.bpAuthorities
        .filter(authority => authority.ppAuthorityCode === ppAuthority?.code)
        .map(authority => ({
          code: authority.bpAuthorityCode,
          name: authority.bpAuthorityName,
          i18nKey: authority.bpAuthorityCode,
        }));
      setBpAuthorities(filteredBpAuthorities);
    } else {
      setBpAuthorities([]);
    }
  }, [ppAuthority, areaMappingData]);

  // Update revenue villages when BP authority changes
  useEffect(() => {
    if (bpAuthority && areaMappingData?.revenueVillages) {
      const filteredRevenueVillages = areaMappingData.revenueVillages
        .filter(village => village.bpAuthorityCode === bpAuthority?.code)
        .map(village => ({
          code: village.revenueVillageCode,
          name: village.revenueVillageName,
          i18nKey: village.revenueVillageCode,
        }));
      setRevenueVillages(filteredRevenueVillages);
    } else {
      setRevenueVillages([]);
    }
  }, [bpAuthority, areaMappingData]);

  // Update mouzas when revenue village changes
  useEffect(() => {
    if (revenueVillage && areaMappingData?.mouzas) {
      const filteredMouzas = areaMappingData.mouzas
        .filter(mouza => mouza.revenueVillageCode === revenueVillage?.code)
        .map(mouza => ({
          code: mouza.mouzaCode,
          name: mouza.mouzaName,
          i18nKey: mouza.mouzaCode,
        }));
      setMouzas(filteredMouzas);
    } else {
      setMouzas([]);
    }
  }, [revenueVillage, areaMappingData]);

  // Custom handlers for dropdown changes
  const handleDistrictChange = (selectedDistrict) => {
    setDistrict(selectedDistrict);
    setPlanningArea("");
    setPpAuthority("");
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
  };

  const handlePlanningAreaChange = (selectedPlanningArea) => {
    setPlanningArea(selectedPlanningArea);
    setPpAuthority("");
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
  };

  const handlePpAuthorityChange = (selectedPpAuthority) => {
    setPpAuthority(selectedPpAuthority);
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
  };

  const handleBpAuthorityChange = (selectedBpAuthority) => {
    setBpAuthority(selectedBpAuthority);
    setRevenueVillage("");
    setMouza("");
  };

  const handleRevenueVillageChange = (selectedRevenueVillage) => {
    setRevenueVillage(selectedRevenueVillage);
    setMouza("");
  };

  // Go next
  const goNext = () => {
    let areaMappingStep = {
      district,
      planningArea,
      ppAuthority,
      bpAuthority,
      revenueVillage,
      mouza,
      ward
    };

    onSelect(config.key, { ...formData[config.key], ...areaMappingStep });
  };

  const onSkip = () => onSelect();

  return (
    <React.Fragment>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={
          !district ||
          !planningArea ||
          !ppAuthority ||
          !bpAuthority ||
          !revenueVillage ||
          !mouza ||
          !ward
        }
      >
        <div>
          {/* District */}
          <CardLabel>{`${t("DISTRICT")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={districts}
            optionKey="i18nKey"
            id="district"
            selected={district}
            select={handleDistrictChange}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={isLoading ? t("LOADING_DISTRICTS") : t("SELECT_DISTRICT")}
          />

          {/* Planning Area */}
          <CardLabel>{`${t("PLANNING_AREA")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={planningAreas}
            optionKey="i18nKey" 
            selected={planningArea}
            select={handlePlanningAreaChange} 
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!district ? t("SELECT_DISTRICT_FIRST") : t("SELECT_PLANNING_AREA")} 
          />

          {/* PP Authority */}
          <CardLabel>{`${t("PP_AUTHORITY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={ppAuthorities}
            optionKey="i18nKey"
            selected={ppAuthority}
            select={handlePpAuthorityChange} 
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!planningArea ? t("SELECT_PLANNING_AREA_FIRST") : t("SELECT_PP_AUTHORITY")}
          />

          {/* BP Authority */}
          <CardLabel>{`${t("BP_AUTHORITY")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={bpAuthorities}
            optionKey="i18nKey"
            selected={bpAuthority}
            select={handleBpAuthorityChange}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!ppAuthority ? t("SELECT_PP_AUTHORITY_FIRST") : t("SELECT_BP_AUTHORITY")}
          />

          {/* Revenue Village */}
          <CardLabel>{`${t("REVENUE_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={revenueVillages}
            optionKey="i18nKey"
            id="revenueVillage"
            selected={revenueVillage}
            select={handleRevenueVillageChange}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!bpAuthority ? t("SELECT_BP_AUTHORITY_FIRST") : t("SELECT_REVENUE_VILLAGE")}
          />

          {/* Mouza */}
          <CardLabel>{`${t("MOUZA")}`} <span className="check-page-link-button">*</span></CardLabel>
          {mouzas.length > 0 ? (
            <Dropdown
              t={t}
              option={mouzas}
              optionKey="i18nKey"
              selected={mouza}
              optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
              select={setMouza}
              placeholder={!revenueVillage ? t("SELECT_REVENUE_VILLAGE_FIRST") : t("SELECT_MOUZA")} 
            />
          ) : (
            <TextInput
              t={t}
              name="mouza"
              value={mouza}
              onChange={(e) => setMouza(e.target.value)}
              placeholder={`${t("ENTER_MOUZA_NAME")}`}
            />
          )}

          {/* Ward */}
          <CardLabel>{`${t("WARD")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            name="ward"
            value={ward}
            maxLength={3}
            minLength={1}
            onChange={(e) => setWard(e.target.value.replace(/[^0-9]/g, ""))}
            placeholder={`${t("ENTER_WARD_NUMBER")}`}
          />
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default AreaMapping;