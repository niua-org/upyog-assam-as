import { BackButton, CardHeader, CardLabelError, CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";
import React, { useMemo, useState, useEffect, Fragment } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import PageBasedInput from "../../../../../../react-components/src/molecules/PageBasedInput";

const AreaMapping = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();
  
  // Fetch area mapping data from MDMS
  const { data: areaMappingData, isLoading: isAreaDataLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
      { name: "planningAreas" }, 
      { name: "ppAuthorities" }, 
      { name: "concernedAuthorities" }
    ],
    {
      select: (data) => data?.BPA || {},
    }
  );

  // Fetch tenants data for additional fields
  const { data: tenantsData, isLoading: isTenantsLoading } = Digit.Hooks.useTenants();

  console.log("tenantsData", tenantsData);

  const [showError, setShowError] = useState(false);
  
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

  // Initialize districts
  useEffect(() => {
    if (areaMappingData?.districts) {
      const formattedDistricts = areaMappingData.districts.map((district) => ({
        code: district.districtCode,
        name: district.districtName,
        i18nKey: district.districtCode,
      })).sort((a, b) => a.code.localeCompare(b.code));
      setDistricts(formattedDistricts);
    }
  }, [areaMappingData]);

  // Update concerned authorities based on BP authority and planning area from tenants
  useEffect(() => {
    if (bpAuthority && planningArea && tenantsData) {
      const filteredConcernedAuthorities = tenantsData
        .filter(tenant => 
          tenant.city?.planningAreaCode === planningArea?.code && 
          tenant.city?.ulbGrade === bpAuthority?.code
        )
        .map(tenant => ({
          code: tenant.code,
          name: tenant.name,
          i18nKey: tenant.i18nKey,
        }))
        .sort((a, b) => a.name.localeCompare(b.name));
      setConcernedAuthorities(filteredConcernedAuthorities);
    } else {
      setConcernedAuthorities([]);
    }
  }, [bpAuthority, planningArea, tenantsData]);

  useEffect(() => {
    if (areaMappingData?.concernedAuthorities) {
      const formattedBpAuthorities = areaMappingData.concernedAuthorities.map((concernedAuthority) => ({
        code: concernedAuthority.authorityType,
        name: concernedAuthority.authorityType,
        i18nKey: concernedAuthority.authorityType,
      })).sort((a, b) => a.code.localeCompare(b.code));
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
        }))
        .sort((a, b) => a.code.localeCompare(b.code));
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
        }))
        .sort((a, b) => a.code.localeCompare(b.code));
      setPpAuthorities(filteredPpAuthorities);
    } else {
      setPpAuthorities([]);
    }
  }, [planningArea, areaMappingData]);

  // Update wards when concerned authority changes (only for MUNICIPAL_BOARD)
  useEffect(() => {
    if (concernedAuthority && bpAuthority?.code === "MUNICIPAL_BOARD" && tenantsData?.ulbWardDetails) {
      const filteredWards = tenantsData.ulbWardDetails
        .filter(ward => ward.ulbCode === concernedAuthority?.code)
        .map(ward => ({
          code: ward.wardCode,
          name: ward.wardName,
          i18nKey: ward.wardCode,
        }))
        .sort((a, b) => {
          const aNum = parseInt(a.name.match(/\d+/)?.[0] || '0');
          const bNum = parseInt(b.name.match(/\d+/)?.[0] || '0');
          return aNum - bNum;
        });
      setWards(filteredWards);
    } else {
      setWards([]);
    }
  }, [concernedAuthority, bpAuthority, tenantsData]);

  // Update revenue villages when ward changes (only for MUNICIPAL_BOARD)
  useEffect(() => {
    if (ward && bpAuthority?.code === "MUNICIPAL_BOARD" && tenantsData?.revenueVillages) {
      const filteredRevenueVillages = tenantsData.revenueVillages
        .filter(village => village.wardCode === ward?.code)
        .map(village => ({
          code: village.revenueVillageCode,
          name: village.revenueVillageName,
          i18nKey: village.revenueVillageCode,
        }))
        .sort((a, b) => a.code.localeCompare(b.code));
      setRevenueVillages(filteredRevenueVillages);
    } else {
      setRevenueVillages([]);
    }
  }, [ward, bpAuthority, tenantsData]);

  // Update villages when concerned authority changes (only for GRAM_PANCHAYAT)
  useEffect(() => {
    if (concernedAuthority && bpAuthority?.code === "GRAM_PANCHAYAT" && tenantsData?.villages) {
      const filteredVillages = tenantsData.villages
        .filter(village => village.gramPanchayatCode === concernedAuthority?.code)
        .map(village => ({
          code: village.villageCode,
          name: village.villageName,
          i18nKey: village.villageCode,
        }))
        .sort((a, b) => a.code.localeCompare(b.code));
      setVillages(filteredVillages);
    } else {
      setVillages([]);
    }
  }, [concernedAuthority, bpAuthority, tenantsData]);

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
    const baseValidation = !district;
    
    // if (bpAuthority?.code === "MUNICIPAL_BOARD") {
    //   return baseValidation || !ward || !revenueVillage;
    // } else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
    //   return baseValidation || !villageName;
    // }
    
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

  return (isAreaDataLoading || isTenantsLoading) ? (
    <loader />
  ) : (
    <div className="selection-card-wrapper">
      <BackButton />
      <PageBasedInput texts={texts} onSubmit={onSubmit}>
        <CardHeader>{t("BPA_AREA_MAPPING")}</CardHeader>
        
        <div>
          {/* District */}
          <CardLabel>{t("DISTRICT")} <span style={{ color: "red" }}>*</span></CardLabel>
          <Dropdown
            t={t}
            option={districts}
            optionKey="i18nKey"
            selected={district}
            select={handleDistrictChange}
            placeholder={t("SELECT_DISTRICT")}
          />

          {/* Planning Area */}
          <CardLabel>{t("PLANNING_AREA")} <span style={{ color: "red" }}>*</span></CardLabel>
          <Dropdown
            t={t}
            option={planningAreas}
            optionKey="i18nKey"
            selected={planningArea}
            select={handlePlanningAreaChange}
            placeholder={!district ? t("SELECT_DISTRICT_FIRST") : t("SELECT_PLANNING_AREA")}
          />

          {/* PP Authority */}
          <CardLabel>{t("PP_AUTHORITY")} <span style={{ color: "red" }}>*</span></CardLabel>
          <Dropdown
            t={t}
            option={ppAuthorities}
            optionKey="i18nKey"
            selected={ppAuthority}
            select={handlePpAuthorityChange}
            placeholder={!planningArea ? t("SELECT_PLANNING_AREA_FIRST") : t("SELECT_PP_AUTHORITY")}
          />

          {/* BP Authority */}
          <CardLabel>{t("BP_AUTHORITY")} <span style={{ color: "red" }}>*</span></CardLabel>
          <Dropdown
            t={t}
            option={bpAuthorities}
            optionKey="i18nKey"
            selected={bpAuthority}
            select={handleBpAuthorityChange}
            placeholder={t("SELECT_BP_AUTHORITY")}
          />

          {/* Concerned Authority - Dynamic Label */}
          {bpAuthority && (
            <>
              <CardLabel>{t(bpAuthority.code + "_NAME")} <span style={{ color: "red" }}>*</span></CardLabel>
              <Dropdown
                t={t}
                option={concernedAuthorities}
                optionKey="i18nKey"
                selected={concernedAuthority}
                select={handleConcernedAuthorityChange}
                placeholder={t("SELECT_CONCERNED_AUTHORITY")}
              />
            </>
          )}

          {/* Conditional fields based on BP authority */}
          {bpAuthority?.code === "MUNICIPAL_BOARD" && (
            <>
              {/* Ward */}
              <CardLabel>{t("WARD")} <span style={{ color: "red" }}>*</span></CardLabel>
              <Dropdown
                t={t}
                option={wards}
                optionKey="i18nKey"
                selected={ward}
                select={handleWardChange}
                placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_WARD")}
              />

              {/* Revenue Village */}
              <CardLabel>{t("REVENUE_VILLAGE")} <span style={{ color: "red" }}>*</span></CardLabel>
              <Dropdown
                t={t}
                option={revenueVillages}
                optionKey="i18nKey"
                selected={revenueVillage}
                select={setRevenueVillage}
                placeholder={!ward ? t("SELECT_WARD_FIRST") : t("SELECT_REVENUE_VILLAGE")}
              />
            </>
          )}

          {bpAuthority?.code === "GRAM_PANCHAYAT" && (
            <>
              {/* Village Name */}
              <CardLabel>{t("VILLAGE_NAME")} <span style={{ color: "red" }}>*</span></CardLabel>
              <Dropdown
                t={t}
                option={villages}
                optionKey="i18nKey"
                selected={villageName}
                select={setVillageName}
                placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_VILLAGE")}
              />
            </>
          )}

          {/* Mouza - Always text input */}
          <CardLabel>{t("MOUZA")}</CardLabel>
          <TextInput
            t={t}
            name="mouza"
            value={mouza}
            placeholder={t("ENTER_MOUZA_NAME")}
            onChange={(e) =>
              setMouza(
                e.target.value.replace(/[^a-zA-Z0-9\s]/g, "")
              )
            }
            ValidationRequired={true}
            pattern="^[A-Za-z0-9 ]+$"
            title={t("BPA_NAME_ERROR_MESSAGE")}
          />
        </div>
        
        {showError ? <CardLabelError>{t("COMPLETE_AREA_MAPPING")}</CardLabelError> : null}
      </PageBasedInput>
    </div>
  );
};

export default AreaMapping;