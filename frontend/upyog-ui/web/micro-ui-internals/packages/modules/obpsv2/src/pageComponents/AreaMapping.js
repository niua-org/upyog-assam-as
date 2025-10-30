import React, { useEffect, useState, Fragment } from "react";
import { FormStep, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";

const AreaMapping = ({ t, config, onSelect, formData, searchResult }) => {

  // State for dropdown options
  const [districts, setDistricts] = useState([]);
  const [planningAreas, setPlanningAreas] = useState([]);
  const [ppAuthorities, setPpAuthorities] = useState([]);
  const [concernedAuthorities, setConcernedAuthorities] = useState([]);
  const [bpAuthorities, setBpAuthorities] = useState([]);
  const [revenueVillages, setRevenueVillages] = useState([]);
  const [wards, setWards] = useState([]);
  const [villages, setVillages] = useState([]);
  
 // State for all dropdown values
const [district, setDistrict] = useState(formData?.areaMapping?.district || (searchResult?.areaMapping?.district ? { code: searchResult.areaMapping.district, name: searchResult.areaMapping.district, i18nKey: searchResult.areaMapping.district } : ""));

const [planningArea, setPlanningArea] = useState(formData?.areaMapping?.planningArea || (searchResult?.areaMapping?.planningArea ? { code: searchResult.areaMapping.planningArea, name: searchResult.areaMapping.planningArea, i18nKey: searchResult.areaMapping.planningArea } : ""));

const [ppAuthority, setPpAuthority] = useState(formData?.areaMapping?.ppAuthority || (searchResult?.areaMapping?.planningPermitAuthority ? { code: searchResult.areaMapping.planningPermitAuthority, name: searchResult.areaMapping.planningPermitAuthority, i18nKey: searchResult.areaMapping.planningPermitAuthority } : ""));

const [concernedAuthority, setConcernedAuthority] = useState(formData?.areaMapping?.concernedAuthority || (searchResult?.areaMapping?.concernedAuthority ? { code: searchResult.areaMapping.concernedAuthority, name: searchResult.areaMapping.concernedAuthority, i18nKey: searchResult.areaMapping.concernedAuthority } : ""));

const [bpAuthority, setBpAuthority] = useState(formData?.areaMapping?.bpAuthority || (searchResult?.areaMapping?.buildingPermitAuthority ? { code: searchResult.areaMapping.buildingPermitAuthority, name: searchResult.areaMapping.buildingPermitAuthority, i18nKey: searchResult.areaMapping.buildingPermitAuthority } : ""));

const [revenueVillage, setRevenueVillage] = useState(formData?.areaMapping?.revenueVillage || (searchResult?.areaMapping?.revenueVillage ? { code: searchResult.areaMapping.revenueVillage, name: searchResult.areaMapping.revenueVillage, i18nKey: searchResult.areaMapping.revenueVillage } : ""));

const [mouza, setMouza] = useState(formData?.areaMapping?.mouza || searchResult?.areaMapping?.mouza || "");

const [ward, setWard] = useState(formData?.areaMapping?.ward || (searchResult?.areaMapping?.ward ? { code: searchResult.areaMapping.ward, name: searchResult.areaMapping.ward, i18nKey: searchResult.areaMapping.ward } : ""));

const [villageName, setVillageName] = useState(formData?.areaMapping?.villageName || (searchResult?.areaMapping?.villageName ? { code: searchResult.areaMapping.villageName, name: searchResult.areaMapping.villageName, i18nKey: searchResult.areaMapping.villageName } : ""));

  // Fetch data from MDMS
  const { data: areaMappingData, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
      { name: "planningAreas" }, 
      { name: "ppAuthorities" }, 
      { name: "concernedAuthorities" },
      { name: "bpAuthorities" }, 
      { name: "revenueVillages" }, 
      { name: "villages" },
      { name: "ulbWardDetails" }
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

  // Update concerned authorities based on BP authority and PP authority
  useEffect(() => {
    if (bpAuthority && planningArea && areaMappingData?.bpAuthorities) {
      const filteredConcernedAuthorities = areaMappingData.bpAuthorities
        .filter(authority => authority.planningAreaCode === planningArea?.code && authority.authorityType === bpAuthority?.code)
        .map(authority => ({
          code: authority.bpAuthorityCode,
          name: authority.bpAuthorityName,
          i18nKey: authority.bpAuthorityCode,
        }));
      setConcernedAuthorities(filteredConcernedAuthorities);
    } else {
      setConcernedAuthorities([]);
    }
  }, [bpAuthority, planningArea, areaMappingData]);

  useEffect(() => {
    if (areaMappingData?.concernedAuthorities) {
      const formattedBpAuthorities = areaMappingData.concernedAuthorities.map((concernedAuthority) => ({
        code: concernedAuthority.authorityType,
        name: concernedAuthority.authorityType,
        i18nKey: concernedAuthority.authorityType,
      }));
      setBpAuthorities(formattedBpAuthorities);
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



  // Update wards when concerned authority changes (only for MUNICIPAL_BOARD)
  useEffect(() => {
    if (concernedAuthority && bpAuthority?.code === "MUNICIPAL_BOARD" && areaMappingData?.ulbWardDetails) {
      const filteredWards = areaMappingData.ulbWardDetails
        .filter(ward => ward.ulbCode === concernedAuthority?.code)
        .map(ward => ({
          code: ward.wardCode,
          name: ward.wardName,
          i18nKey: ward.wardCode,
        }));
      setWards(filteredWards);
    } else {
      setWards([]);
    }
  }, [concernedAuthority, bpAuthority, areaMappingData]);

  // Update revenue villages when ward changes (only for MUNICIPAL_BOARD)
  useEffect(() => {
    if (ward && bpAuthority?.code === "MUNICIPAL_BOARD" && areaMappingData?.revenueVillages) {
      const filteredRevenueVillages = areaMappingData.revenueVillages
        .filter(village => village.wardCode === ward?.code)
        .map(village => ({
          code: village.revenueVillageCode,
          name: village.revenueVillageName,
          i18nKey: village.revenueVillageCode,
        }));
      setRevenueVillages(filteredRevenueVillages);
    } else {
      setRevenueVillages([]);
    }
  }, [ward, bpAuthority, areaMappingData]);

  // Update villages when concerned authority changes (only for GRAM_PANCHAYAT)
  useEffect(() => {
    if (concernedAuthority && bpAuthority?.code === "GRAM_PANCHAYAT" && areaMappingData?.villages) {
      const filteredVillages = areaMappingData.villages
        .filter(village => village.gramPanchayatCode === concernedAuthority?.code)
        .map(village => ({
          code: village.villageCode,
          name: village.villageName,
          i18nKey: village.villageCode,
        }));
      setVillages(filteredVillages);
    } else {
      setVillages([]);
    }
  }, [concernedAuthority, bpAuthority, areaMappingData]);

  // Custom handlers for dropdown changes
  const handleDistrictChange = (selectedDistrict) => {
    setDistrict(selectedDistrict);
    setPlanningArea("");
    setPpAuthority("");
    setConcernedAuthority("");
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
    setWard("");
    setVillageName("");
  };

  const handlePlanningAreaChange = (selectedPlanningArea) => {
    setPlanningArea(selectedPlanningArea);
    setPpAuthority("");
    setConcernedAuthority("");
    setBpAuthority("");
    setRevenueVillage("");
    setMouza("");
    setWard("");
    setVillageName("");
  };

  const handlePpAuthorityChange = (selectedPpAuthority) => {
    setPpAuthority(selectedPpAuthority);
    setConcernedAuthority("");
    setRevenueVillage("");
    setWard("");
    setVillageName("");
  };

  const handleBpAuthorityChange = (selectedBpAuthority) => {
    setBpAuthority(selectedBpAuthority);
    setConcernedAuthority("");
    setRevenueVillage("");
    setWard("");
    setVillageName("");
  };

  const handleConcernedAuthorityChange = (selectedConcernedAuthority) => {
    setConcernedAuthority(selectedConcernedAuthority);
    setRevenueVillage("");
    setWard("");
    setVillageName("");
  };

  const handleWardChange = (selectedWard) => {
    setWard(selectedWard);
    setRevenueVillage("");
  };

  // Validation logic based on BP authority
  const getValidationLogic = () => {
    const baseValidation = !district || !planningArea || !ppAuthority || !bpAuthority || !concernedAuthority;
    
    if (bpAuthority?.code === "MUNICIPAL_BOARD") {
      return baseValidation || !ward || !revenueVillage || !mouza;
    } else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
      return baseValidation || !villageName || !mouza;
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
    <React.Fragment>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={getValidationLogic()}
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
            placeholder={t("SELECT_BP_AUTHORITY")}
          />

          {/* Concerned Authority - Dynamic Label */}
          {bpAuthority && (
            <>
              <CardLabel>{`${t(bpAuthority.code + " NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={concernedAuthorities}
                optionKey="i18nKey"
                selected={concernedAuthority}
                select={handleConcernedAuthorityChange} 
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={t("SELECT_CONCERNED_AUTHORITY")}
              />
            </>
          )}

          {/* Conditional fields based on BP authority */}
          {bpAuthority?.code === "MUNICIPAL_BOARD" && (
            <>
              {/* Ward */}
              <CardLabel>{`${t("WARD")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={wards}
                optionKey="i18nKey"
                selected={ward}
                select={handleWardChange}
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_WARD")}
              />

              {/* Revenue Village */}
              <CardLabel>{`${t("REVENUE_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={revenueVillages}
                optionKey="i18nKey"
                id="revenueVillage"
                selected={revenueVillage}
                select={setRevenueVillage}
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={!ward ? t("SELECT_WARD_FIRST") : t("SELECT_REVENUE_VILLAGE")}
              />
            </>
          )}

          {bpAuthority?.code === "GRAM_PANCHAYAT" && (
            <>
              {/* Village Name */}
              <CardLabel>{`${t("VILLAGE_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
              <Dropdown
                t={t}
                option={villages}
                optionKey="i18nKey"
                selected={villageName}
                select={setVillageName}
                optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
                placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_VILLAGE")}
              />
            </>
          )}

          {/* Mouza - Always text input */}
          <CardLabel>{`${t("MOUZA")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            name="mouza"
            value={mouza}
            onChange={(e) => setMouza(e.target.value)}
            placeholder={`${t("ENTER_MOUZA_NAME")}`}
          />
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default AreaMapping;