import { BackButton, CardHeader, CardLabelError, SearchOnRadioButtons, Loader } from "@upyog/digit-ui-react-components";
import React, { useMemo, useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import PageBasedInput from "../../../../../../react-components/src/molecules/PageBasedInput";

const LocationSelection = () => {
  const { t } = useTranslation();
  const history = useHistory();
  const location = useLocation();
  const { data: cities, isLoading } = Digit.Hooks.useTenants();

  const [selectedCity, setSelectedCity] = useState(null);
  const [showError, setShowError] = useState(false);

  // Auto-select State Municipal Board for RTP and redirect
  useEffect(() => {
    if (cities && cities.length > 0) {
      const stateTenant = cities.find(city => city.code === 'as');
      
      if (stateTenant) {
        setSelectedCity(stateTenant);
        Digit.SessionStorage.set("CITIZEN.COMMON.HOME.CITY", stateTenant);
        
        const redirectBackTo = location.state?.redirectBackTo;
        if (redirectBackTo) {
          history.replace(redirectBackTo);
        } else {
          history.push("/upyog-ui/citizen/rtp-login");
        }
      } else {
        setSelectedCity(cities[0]);
      }
    }
  }, [cities, history, location.state]);

  const texts = useMemo(
    () => ({
      header: t("CS_COMMON_CHOOSE_LOCATION"),
      submitBarLabel: t("CORE_COMMON_CONTINUE"),
    }),
    [t]
  );

  function selectCity(city) {
    setSelectedCity(city);
    setShowError(false);
  }

  const RadioButtonProps = useMemo(() => {
    return {
      options: cities,
      optionsKey: "i18nKey",
      additionalWrapperClass: "reverse-radio-selection-wrapper",
      onSelect: selectCity,
      selectedOption: selectedCity,
    };
  }, [cities, t, selectedCity]);

  function onSubmit() {
    if (selectedCity) {
      Digit.SessionStorage.set("CITIZEN.COMMON.HOME.CITY", selectedCity);
      const redirectBackTo = location.state?.redirectBackTo;
      if (redirectBackTo) {
        history.replace(redirectBackTo);
      } else history.push("/upyog-ui/citizen/rtp-login");
    } else {
      setShowError(true);
    }
  }

  return isLoading ? (
    <Loader />
  ) : null;
};

export default LocationSelection;
