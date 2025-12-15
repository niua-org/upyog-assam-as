import React, { useEffect, useState, Fragment } from "react";
import { CardLabel, Dropdown, TextInput } from "@upyog/digit-ui-react-components";

const AreaMapping = ({
  t,
  district, setDistrict,
  planningArea, setPlanningArea,
  ppAuthority, setPpAuthority,
  concernedAuthority, setConcernedAuthority,
  bpAuthority, setBpAuthority,
  revenueVillage, setRevenueVillage,
  mouza, setMouza,
  ward, setWard,
  villageName, setVillageName,
  isDisabled = false,
  isLoading
}) => {

  // State for dropdown options
  const [districts, setDistricts] = useState([]);
  const [planningAreas, setPlanningAreas] = useState([]);
  const [ppAuthorities, setPpAuthorities] = useState([]);
  const [concernedAuthorities, setConcernedAuthorities] = useState([]);
  const [bpAuthorities, setBpAuthorities] = useState([]);
  const [revenueVillages, setRevenueVillages] = useState([]);
  const [wards, setWards] = useState([]);
  const [villages, setVillages] = useState([]);
  const [mouzaOptions, setMouzaOptions] = useState([]);

  const stateId = Digit.ULBService.getStateId();

  const { data: areaMappingData } = Digit.Hooks.useEnabledMDMS(stateId, "egov-location", [{ name: "egov-location" }], {
    select: (data) => {
      const formattedData = data?.["egov-location"]?.["egov-location"]?.[0];
      return formattedData;
    },
  });

  const tenantMasterDetails =
    bpAuthority?.code && planningArea?.code
      ? [{ name: "tenants", ulbGrade: bpAuthority.code, planningArea: planningArea.code }]
      : [{ name: "tenants" }];

  const { data: tenantData } = Digit.Hooks.useEnabledMDMS(
    stateId,
    "tenant",
    tenantMasterDetails,
    {
      select: (data) => {
        const formattedData = data?.["tenant"]?.["tenants"];
        return formattedData?.filter((tenant) => (bpAuthority?.code === tenant?.city?.ulbGrade && planningArea?.code === tenant?.city?.planningAreaCode));
      },
      enabled: !!bpAuthority?.code,
    }
  );

  const getBoundaryType = () => {
    if (bpAuthority?.code === "GRAM_PANCHAYAT") return "village";
    if (bpAuthority?.code === "MUNICIPAL_CORPORATION") return "mouza";
    return "revenuevillage";
  };

  const { data: fetchedLocalities } = Digit.Hooks.useBoundaryLocalities(
    concernedAuthority?.code,
    getBoundaryType(),
    {
      enabled: !!concernedAuthority && !!bpAuthority,
    },
    t
  );

  // Initialize Districts
  useEffect(() => {
    if (areaMappingData?.districts) {
      const formattedDistricts = areaMappingData.districts.map((dist) => ({
        code: dist.districtCode,
        name: dist.districtName,
        i18nKey: dist.districtCode,
      })).sort((a, b) => a.code.localeCompare(b.code));
      setDistricts(formattedDistricts);
    }
  }, [areaMappingData]);

  // Planning Areas
  useEffect(() => {
    if (district && areaMappingData?.districts) {
      const selectedDistrict = areaMappingData.districts.find(
        (d) => d.districtCode === district.code
      );
      const formattedPlanningAreas =
        selectedDistrict?.planningAreas?.map((area) => ({
          code: area.planningAreaCode,
          name: area.planningAreaName,
          i18nKey: area.planningAreaCode,
          gisCode: area.planningAreaGisCode
        })) || [];
      setPlanningAreas(formattedPlanningAreas);
    } else {
      setPlanningAreas([]);
    }
  }, [district, areaMappingData]);

  // PP Authority
  useEffect(() => {
    if (district && planningArea && areaMappingData?.districts) {
      const selectedDistrict = areaMappingData.districts.find(
        (d) => d.districtCode === district.code
      );
      const selectedArea = selectedDistrict?.planningAreas?.find(
        (a) => a.planningAreaCode === planningArea.code
      );

      if (selectedArea?.ppAuthority) {
        setPpAuthorities([
          {
            code: selectedArea.ppAuthority.ppAuthorityCode,
            name: selectedArea.ppAuthority.ppAuthorityName,
            i18nKey: selectedArea.ppAuthority.ppAuthorityCode,
          },
        ]);
      } else {
        setPpAuthorities([]);
      }
    } else {
      setPpAuthorities([]);
    }
  }, [district, planningArea, areaMappingData]);

  // BP Authorities
  useEffect(() => {
    if (district && planningArea && areaMappingData?.districts) {
      const selectedDistrict = areaMappingData.districts.find(
        (d) => d.districtCode === district.code
      );
      const selectedArea = selectedDistrict?.planningAreas?.find(
        (a) => a.planningAreaCode === planningArea.code
      );

      const formattedBpAuthorities =
        selectedArea?.bpAuthorities?.map((auth) => ({
          code: auth.code,
          name: auth.name,
          i18nKey: auth.code,
        })) || [];
      setBpAuthorities(formattedBpAuthorities);
    } else {
      setBpAuthorities([]);
    }
  }, [district, planningArea, areaMappingData]);

  // Concerned Authority
  useEffect(() => {
    if (tenantData && tenantData.length > 0) {
      const formattedConcernedAuthorities = tenantData.map((tenant) => ({
        code: tenant.code,
        name: tenant.name,
        i18nKey: tenant.name,
        ulbGrade: tenant.city?.ulbGrade,
        planningAreaCode: tenant.city?.planningAreaCode,
      }));
      setConcernedAuthorities(formattedConcernedAuthorities);
    } else {
      setConcernedAuthorities([]);
    }
  }, [tenantData]);

  // Wards / Revenue Villages / Villages
  useEffect(() => {
    if (Array.isArray(fetchedLocalities) && fetchedLocalities.length > 0) {
      if (bpAuthority?.code === "MUNICIPAL_BOARD") {
        const formattedWards = fetchedLocalities.map((ward) => ({
          code: ward.code,
          name: ward.name,
          i18nKey: ward.name,
          children: ward.children || [],
        }));

        const formattedRevenueVillages = fetchedLocalities.flatMap((ward) =>
          (ward.children || []).map((child) => ({
            code: child.code,
            name: child.name,
            i18nKey: child.name,
            parentWardCode: ward.code,
          }))
        );

        setWards(formattedWards);
        setRevenueVillages(formattedRevenueVillages);
        setVillages([]);

      } else if (bpAuthority?.code === "MUNICIPAL_CORPORATION") {
        const formattedMouzaOptions = fetchedLocalities.map((mouza) => ({
          code: mouza.code,
          name: mouza.name,
          i18nKey: mouza.name,
          children: mouza.children || [],
        }));

        const formattedRevenueVillages = fetchedLocalities.flatMap((mouza) =>
          (mouza.children || []).map((child) => ({
            code: child.code,
            name: child.name,
            i18nKey: child.name,
            parentMouzaCode: mouza.code,
          }))
        );

        setMouzaOptions(formattedMouzaOptions);
        setRevenueVillages(formattedRevenueVillages);
        setWards([]);
        setVillages([]);

      } else if (bpAuthority?.code === "GRAM_PANCHAYAT") {
        const formattedVillages = fetchedLocalities.map((loc) => ({
          code: loc.code,
          name: loc.name,
          i18nKey: loc.name,
        }));
        setVillages(formattedVillages);
        setWards([]);
        setRevenueVillages([]);
      }

    } else {
      setVillages([]);
      setWards([]);
      setRevenueVillages([]);
      setMouzaOptions([]);
    }
  }, [fetchedLocalities, bpAuthority]);

  // Handlers
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

  const handleMouzaChange = (selectedMouza) => {
    setMouza(selectedMouza);
    setRevenueVillage("");
  };

  const handleWardChange = (selectedWard) => {
    setWard(selectedWard);
    setRevenueVillage("");
  };

  return (
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
        disable={isDisabled}
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
        disable={isDisabled}
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
        disable={isDisabled}
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
        disable={isDisabled}
        optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
        placeholder={t("SELECT_BP_AUTHORITY")}
      />

      {/* Concerned Authority */}
      {bpAuthority && (
        <>
          <CardLabel>{`${t(bpAuthority.code + "_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={concernedAuthorities}
            optionKey="i18nKey"
            selected={concernedAuthority}
            select={handleConcernedAuthorityChange}
            disable={isDisabled}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={t("SELECT_CONCERNED_AUTHORITY")}
          />
        </>
      )}

      {/* Municipal Board Fields */}
      {bpAuthority?.code === "MUNICIPAL_BOARD" && (
        <>
          <CardLabel>{`${t("WARD")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={wards}
            optionKey="i18nKey"
            selected={ward}
            select={handleWardChange}
            disable={isDisabled}
            optionCardStyles={{ maxHeight: "250px", overflowY: "auto" }}
            placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_WARD")}
          />

          <CardLabel>{`${t("REVENUE_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={revenueVillages?.filter(rv => rv.parentWardCode === ward?.code) || []}
            optionKey="i18nKey"
            id="revenueVillage"
            selected={revenueVillage}
            select={setRevenueVillage}
            disable={isDisabled}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!ward ? t("SELECT_WARD_FIRST") : t("SELECT_REVENUE_VILLAGE")}
          />
        </>
      )}

      {/* Municipal Corporation Fields */}
      {bpAuthority?.code === "MUNICIPAL_CORPORATION" && (
        <>
          <CardLabel>{`${t("MOUZA")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={mouzaOptions}
            optionKey="i18nKey"
            selected={mouza}
            select={handleMouzaChange}
            disable={isDisabled}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_MOUZA")}
          />

          <CardLabel>{`${t("REVENUE_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={revenueVillages?.filter(rv => rv.parentMouzaCode === mouza?.code) || []}
            optionKey="i18nKey"
            selected={revenueVillage}
            select={setRevenueVillage}
            disable={isDisabled}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!mouza ? t("SELECT_MOUZA_FIRST") : t("SELECT_REVENUE_VILLAGE")}
          />
        </>
      )}

      {/* Gram Panchayat Fields */}
      {bpAuthority?.code === "GRAM_PANCHAYAT" && (
        <>
          <CardLabel>{`${t("VILLAGE_NAME")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={villages}
            optionKey="i18nKey"
            selected={villageName}
            select={setVillageName}
            disable={isDisabled}
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            placeholder={!concernedAuthority ? t("SELECT_CONCERNED_AUTHORITY_FIRST") : t("SELECT_VILLAGE")}
          />
        </>
      )}

      {/* Mouza Text Input for other cases */}
      {bpAuthority?.code !== "MUNICIPAL_CORPORATION" && (
        <>
          <CardLabel>{`${t("MOUZA")}`}</CardLabel>
          <TextInput
            t={t}
            name="mouza"
            value={mouza}
            placeholder={`${t("ENTER_MOUZA_NAME")}`}
            disabled={isDisabled}
            onChange={(e) =>
              setMouza(
                e.target.value.replace(/[^a-zA-Z0-9\s]/g, "")
              )
            }
            ValidationRequired={true}
            pattern="^[A-Za-z0-9 ]+$"
            title={t("BPA_NAME_ERROR_MESSAGE")}
          />
        </>
      )}
    </div>
  );
};

export default AreaMapping;