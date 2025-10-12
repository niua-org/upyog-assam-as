import React, { useEffect, useState } from "react";
import { FormStep, TextInput, CardLabel, CheckBox, Dropdown, CardHeader, Loader } from "@upyog/digit-ui-react-components";
import Timeline from "../components/Timeline";

const AddressDetails = ({ t, config, onSelect, formData, searchResult}) => {
  // Fetch data from MDMS
  const { data: mdmsData, isLoading } = Digit.Hooks.useEnabledMDMS(
    "as", 
    "BPA", 
    [
      { name: "districts" }, 
      { name: "revenueVillages" },
      { name: "states" }
    ],
    {
      select: (data) => {
        return data?.BPA || {};
      },
    }
  );

  // State for dropdown options
  const [districtOptions, setDistrictOptions] = useState([]);
  const [permanentCityOptions, setPermanentCityOptions] = useState([]);
  const [correspondenceCityOptions, setCorrespondenceCityOptions] = useState([]);
  const [stateOptions, setStateOptions] = useState([]);
  const flow = window.location.href.includes("editApplication") ? "editApplication" : "buildingPermit"
  // Initialize districts from MDMS data
  useEffect(() => {
    if (mdmsData?.districts) {
      const formattedDistricts = mdmsData.districts.map((district) => ({
        code: district.districtCode,
        name: district.districtName,
        i18nKey: district.districtCode,
      }));
      setDistrictOptions(formattedDistricts);
    }
    if(mdmsData?.states){
      const formattedStates = mdmsData.states.map((state) => ({
        code: state.stateCode,
        name: state.stateName,
        i18nKey: state.stateCode,
      }));
      setStateOptions(formattedStates);
    }
  }, [mdmsData]);

  // Permanent Address Fields
  const [permanentHouseNo, setPermanentHouseNo] = useState(formData?.address?.permanent?.houseNo || searchResult?.landInfo?.address?.houseNo ||"");
  const searchResultDistrict = searchResult?.landInfo?.address?.district? {
    "code" : searchResult?.landInfo?.address?.district,
    "i18nKey" : searchResult?.landInfo?.address?.district
  } : "";
  const [permanentAddressLine1, setPermanentAddressLine1] = useState(formData?.address?.permanent?.addressLine1 || searchResult?.landInfo?.address?.addressLine1||"");
  const [permanentAddressLine2, setPermanentAddressLine2] = useState(formData?.address?.permanent?.addressLine2 || searchResult?.landInfo?.address?.addressLine2 || "");
  const [permanentDistrict, setPermanentDistrict] = useState(formData?.address?.permanent?.district || searchResultDistrict|| "");
  const searchResultCity = searchResult?.landInfo?.address?.locality?.code? {
    "code" : searchResult?.landInfo?.address?.locality?.code,
    "i18nKey" : searchResult?.landInfo?.address?.locality?.code
  } : "";
  const [permanentCity, setPermanentCity] = useState(formData?.address?.permanent?.city || searchResultCity || "");
  const searchResultState = searchResult?.landInfo?.address?.state ? {
    "code" : searchResult?.landInfo?.address?.state,
    "i18nKey" : searchResult?.landInfo?.address?.state
  } : "";
  const [permanentState, setPermanentState] = useState(formData?.address?.permanent?.state || searchResultState || "");
  const [permanentPincode, setPermanentPincode] = useState(formData?.address?.permanent?.pincode || searchResult?.landInfo?.address?.pincode || "");

  // Correspondence Address Fields
  const [sameAsPermanent, setSameAsPermanent] = useState(formData?.address?.sameAsPermanent || false);
  const [correspondenceHouseNo, setCorrespondenceHouseNo] = useState(formData?.address?.correspondence?.houseNo || searchResult?.landInfo?.owners[0]?.correspondenceAddress?.houseNo || "");
  const [correspondenceAddressLine1, setCorrespondenceAddressLine1] = useState(formData?.address?.correspondence?.addressLine1 || searchResult?.landInfo?.owners[0]?.correspondenceAddress?.addressLine1 || "");
  const [correspondenceAddressLine2, setCorrespondenceAddressLine2] = useState(formData?.address?.correspondence?.addressLine2 || searchResult?.landInfo?.owners[0]?.correspondenceAddress?.addressLine2 || "");
  const searchResultDistrictCorr = searchResult?.landInfo?.owners[0]?.correspondenceAddress?.district ? {
    "code" : searchResult?.landInfo?.owners[0]?.correspondenceAddress?.district,
    "i18nKey" : searchResult?.landInfo?.owners[0]?.correspondenceAddress?.district
  } : "";
  const searchResultStateCorr = searchResult?.landInfo?.owners[0]?.correspondenceAddress?.state ? {
    "code" : searchResult?.landInfo?.owners[0]?.correspondenceAddress?.state,
    "i18nKey" : searchResult?.landInfo?.owners[0]?.correspondenceAddress?.state
  } : "";
  const searchResultCityCorr = searchResult?.landInfo?.owners[0]?.correspondenceAddress?.localityCode ? {
    "code" : searchResult?.landInfo?.owners[0]?.correspondenceAddress?.localityCode,
    "i18nKey" : searchResult?.landInfo?.owners[0]?.correspondenceAddress?.localityCode
  } : "";
  const [correspondenceDistrict, setCorrespondenceDistrict] = useState(formData?.address?.correspondence?.district || searchResultDistrictCorr || "");
  const [correspondenceCity, setCorrespondenceCity] = useState(formData?.address?.correspondence?.city || searchResultCityCorr || "");
  const [correspondenceState, setCorrespondenceState] = useState(formData?.address?.correspondence?.state || searchResultStateCorr || "");
  const [correspondencePincode, setCorrespondencePincode] = useState(formData?.address?.correspondence?.pincode || searchResult?.landInfo?.owners[0]?.correspondenceAddress?.pincode || "");
    // Update permanent city options when permanent district changes
  useEffect(() => {
    if (permanentDistrict && mdmsData?.revenueVillages) {
      const formattedRevenueVillage = mdmsData.revenueVillages
        .filter(revenueVillage => revenueVillage.districtCode === permanentDistrict?.code)
        .map((revenueVillage) => ({
          code: revenueVillage.revenueVillageCode,
          name: revenueVillage.revenueVillageName,
          i18nKey: revenueVillage.revenueVillageCode,
        }));
      setPermanentCityOptions(formattedRevenueVillage);
      
      // Agar current city selected hai, check karo ki wo naye district ki hai ya nahi
      if (permanentCity) {
        const isCityInNewDistrict = formattedRevenueVillage.some(city => city.code === permanentCity.code);
        // Agar city naye district ki nahi hai to clear karo
        if (!isCityInNewDistrict) {
          setPermanentCity("");
        }
      }
    } else {
      setPermanentCityOptions([]);
    }
  }, [permanentDistrict, mdmsData]);

  // Update correspondence city options when correspondence district changes
  useEffect(() => {
    if (correspondenceDistrict && mdmsData?.revenueVillages) {
      const formattedRevenueVillage = mdmsData.revenueVillages
        .filter(revenueVillage => revenueVillage.districtCode === correspondenceDistrict?.code)
        .map((revenueVillage) => ({
          code: revenueVillage.revenueVillageCode,
          name: revenueVillage.revenueVillageName,
          i18nKey: revenueVillage.revenueVillageCode,
        }));
      setCorrespondenceCityOptions(formattedRevenueVillage);
      
      // Agar current city selected hai, check karo ki wo naye district ki hai ya nahi
      if (correspondenceCity) {
        const isCityInNewDistrict = formattedRevenueVillage.some(city => city.code === correspondenceCity.code);
        // Agar city naye district ki nahi hai to clear karo
        if (!isCityInNewDistrict) {
          setCorrespondenceCity("");
        }
      }
    } else {
      setCorrespondenceCityOptions([]);
    }
  }, [correspondenceDistrict, mdmsData]);

  // Update correspondence address when sameAsPermanent is checked
  useEffect(() => {
    if (sameAsPermanent) {
      setCorrespondenceHouseNo(permanentHouseNo);
      setCorrespondenceAddressLine1(permanentAddressLine1);
      setCorrespondenceAddressLine2(permanentAddressLine2);
      setCorrespondenceDistrict(permanentDistrict);
      setCorrespondenceCity(permanentCity);
      setCorrespondenceState(permanentState);
      setCorrespondencePincode(permanentPincode);
    }
  }, [sameAsPermanent, permanentHouseNo, permanentAddressLine1, permanentAddressLine2, permanentDistrict, permanentCity, permanentState, permanentPincode, correspondenceCity]);

  // Go next
  const goNext = () => {
    let addressStep = {
      permanent: {
        houseNo: permanentHouseNo,
        addressLine1: permanentAddressLine1,
        addressLine2: permanentAddressLine2,
        district: permanentDistrict,
        city: permanentCity,
        state: permanentState,
        pincode: permanentPincode
      },
      correspondence: sameAsPermanent ? null : {
        houseNo: correspondenceHouseNo,
        addressLine1: correspondenceAddressLine1,
        addressLine2: correspondenceAddressLine2,
        district: correspondenceDistrict,
        city: correspondenceCity,
        state: correspondenceState,
        pincode: correspondencePincode
      },
      sameAsPermanent
    };

    onSelect(config.key, { ...formData[config.key], ...addressStep });
  };

  const onSkip = () => onSelect();

  if (isLoading) {
    return <Loader />;
  }

  return (
    <React.Fragment>
      <Timeline currentStep={flow === "editApplication" ? 2 : 2} flow={flow }/>
      <FormStep
        config={config}
        onSelect={goNext}
        onSkip={onSkip}
        t={t}
        isDisabled={
          !permanentAddressLine1 ||
          !permanentDistrict ||
          !permanentCity ||
          !permanentState ||
          !permanentPincode ||
          (!sameAsPermanent && (
            !correspondenceAddressLine1 ||
            !correspondenceDistrict ||
            !correspondenceCity ||
            !correspondenceState ||
            !correspondencePincode
          ))
        }
      >
        <div>
          {/* Permanent Address Section */}
          <CardHeader>{t("BPA_SITE_ADDRESS")}</CardHeader>
          
          {/* House No */}
          <CardLabel>{`${t("BPA_HOUSE_NO")}`}</CardLabel>
          <TextInput
            t={t}
            type="text"
            name="permanentHouseNo"
            placeholder={t("BPA_ENTER_HOUSE_NO")}
            value={permanentHouseNo}
            onChange={(e) => setPermanentHouseNo(e.target.value)}
            {...{ pattern: "^[a-zA-Z0-9\\s\\-\\/#]+$", title: t("BPA_HOUSE_NO_ERROR_MESSAGE") }}
          />

          {/* Address Line 1 */}
          <CardLabel>{`${t("BPA_ADDRESS_LINE_1")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="permanentAddressLine1"
            placeholder={t("BPA_ENTER_ADDRESS")}
            value={permanentAddressLine1}
            onChange={(e) => setPermanentAddressLine1(e.target.value)}
            {...{ pattern: "^[a-zA-Z0-9\\s\\-\\/,.#]+$", title: t("BPA_ADDRESS_ERROR_MESSAGE") }}
          />

          {/* Address Line 2 */}
          <CardLabel>{`${t("BPA_ADDRESS_LINE_2")}`}</CardLabel>
          <TextInput
            t={t}
            type="text"
            name="permanentAddressLine2"
            placeholder={t("BPA_ENTER_ADDRESS")}
            value={permanentAddressLine2}
            onChange={(e) => setPermanentAddressLine2(e.target.value)}
            {...{ pattern: "^[a-zA-Z0-9\\s\\-\\/,.#]*$", title: t("BPA_ADDRESS_ERROR_MESSAGE") }}
          />
          
           {/* State */}
           <CardLabel>{`${t("BPA_STATE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={stateOptions}
            selected={permanentState}
            optionKey="i18nKey"
            select={(value) => setPermanentState(value)}
            placeholder={t("BPA_SELECT_STATE")}
          />

          {/* District */}
          <CardLabel>{`${t("BPA_DISTRICT")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={districtOptions}
            selected={permanentDistrict}
            optionKey="i18nKey"
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            select={(value) => setPermanentDistrict(value)}
            placeholder={t("BPA_SELECT_DISTRICT")}
          />

          {/* City/Village */}
          <CardLabel>{`${t("BPA_CITY_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <Dropdown
            t={t}
            option={permanentCityOptions}
            selected={permanentCity}
            optionKey="i18nKey"
            optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
            select={(value) => setPermanentCity(value)}
            placeholder={!permanentDistrict ? t("BPA_SELECT_DISTRICT_FIRST") : t("BPA_SELECT_CITY")}
            disable={!permanentDistrict}
          />

          {/* Pincode */}
          <CardLabel>{`${t("BPA_PINCODE")}`} <span className="check-page-link-button">*</span></CardLabel>
          <TextInput
            t={t}
            type="text"
            name="permanentPincode"
            placeholder={t("BPA_ENTER_PINCODE")}
            value={permanentPincode}
            onChange={(e) => setPermanentPincode(e.target.value.replace(/[^0-9]/g, "").slice(0, 6))}
            minLength={6}
            maxLength={6}
            {...{ pattern: "[0-9]{6}", title: t("BPA_PINCODE_ERROR_MESSAGE") }}
          />


          {/* Correspondence Address Section */}
          <CardHeader>{t("BPA_CORRESPONDENCE_ADDRESS")}</CardHeader>
          
          {/* Same as Permanent Address Checkbox */}
          <CheckBox
            label={t("BPA_SAME_AS_PERMANENT")}
            checked={sameAsPermanent}
            onChange={(e) => setSameAsPermanent(e.target.checked)}
          />

          {/* Correspondence Address Fields - ALWAYS VISIBLE */}
          <div>
            {/* House No */}
            <CardLabel>{`${t("BPA_HOUSE_NO")}`}</CardLabel>
            <TextInput
              t={t}
              type="text"
              name="correspondenceHouseNo"
              placeholder={t("BPA_ENTER_HOUSE_NO")}
              value={correspondenceHouseNo}
              onChange={(e) => setCorrespondenceHouseNo(e.target.value)}
              disabled={sameAsPermanent}
              {...{ pattern: "^[a-zA-Z0-9\\s\\-\\/#]+$", title: t("BPA_HOUSE_NO_ERROR_MESSAGE") }}
            />

            {/* Address Line 1 */}
            <CardLabel>{`${t("BPA_ADDRESS_LINE_1")}`} <span className="check-page-link-button">*</span></CardLabel>
            <TextInput
              t={t}
              type="text"
              name="correspondenceAddressLine1"
              placeholder={t("BPA_ENTER_ADDRESS")}
              value={correspondenceAddressLine1}
              onChange={(e) => setCorrespondenceAddressLine1(e.target.value)}
              disabled={sameAsPermanent}
              {...{ pattern: "^[a-zA-Z0-9\\s\\-\\/,.#]+$", title: t("BPA_ADDRESS_ERROR_MESSAGE") }}
            />

            {/* Address Line 2 */}
            <CardLabel>{`${t("BPA_ADDRESS_LINE_2")}`}</CardLabel>
            <TextInput
              t={t}
              type="text"
              name="correspondenceAddressLine2"
              placeholder={t("BPA_ENTER_ADDRESS")}
              value={correspondenceAddressLine2}
              onChange={(e) => setCorrespondenceAddressLine2(e.target.value)}
              disabled={sameAsPermanent}
              {...{ pattern: "^[a-zA-Z0-9\\s\\-\\/,.#]*$", title: t("BPA_ADDRESS_ERROR_MESSAGE") }}
            />

             {/* State */}
             <CardLabel>{`${t("BPA_STATE")}`} <span className="check-page-link-button">*</span></CardLabel>
            <Dropdown
              t={t}
              option={stateOptions}
              selected={correspondenceState}
              optionKey="i18nKey"
              select={(value) => setCorrespondenceState(value)}
              placeholder={t("BPA_SELECT_STATE")}
              disable={sameAsPermanent}
            />

            {/* District */}
            <CardLabel>{`${t("BPA_DISTRICT")}`} <span className="check-page-link-button">*</span></CardLabel>
            <Dropdown
              t={t}
              option={districtOptions}
              selected={correspondenceDistrict}
              optionKey="i18nKey"
              optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
              select={(value) => setCorrespondenceDistrict(value)}
              placeholder={t("BPA_SELECT_DISTRICT")}
              disable={sameAsPermanent}
            />

            {/* City/Village */}
            <CardLabel>{`${t("BPA_CITY_VILLAGE")}`} <span className="check-page-link-button">*</span></CardLabel>
            <Dropdown
              t={t}
              option={correspondenceCityOptions}
              selected={correspondenceCity}
              optionKey="i18nKey"
              optionCardStyles={{ maxHeight: "300px", overflowY: "auto" }}
              select={(value) => setCorrespondenceCity(value)}
              placeholder={!correspondenceDistrict ? t("BPA_SELECT_DISTRICT_FIRST") : t("BPA_SELECT_CITY")}
              disable={sameAsPermanent || !correspondenceDistrict}
            />

            {/* Pincode */}
            <CardLabel>{`${t("BPA_PINCODE")}`} <span className="check-page-link-button">*</span></CardLabel>
            <TextInput
              t={t}
              type="text"
              name="correspondencePincode"
              placeholder={t("BPA_ENTER_PINCODE")}
              value={correspondencePincode}
              onChange={(e) => setCorrespondencePincode(e.target.value.replace(/[^0-9]/g, "").slice(0, 6))}
              minLength={6}
              maxLength={6}
              disabled={sameAsPermanent}
              {...{ pattern: "[0-9]{6}", title: t("BPA_PINCODE_ERROR_MESSAGE") }}
            />
          </div>
        </div>
      </FormStep>
    </React.Fragment>
  );
};

export default AddressDetails;