import React, { Fragment, useMemo } from "react";
import { FilterFormField, Dropdown, RemoveableTag, CheckBox, Loader, MultiSelectDropdown
 } from "@upyog/digit-ui-react-components";
import { useTranslation } from "react-i18next";
import { Controller, useWatch } from "react-hook-form";
const FilterFormFieldsComponent = ({
  controlFilterForm,
  localitiesForEmployeesCurrentTenant,
  loadingLocalitiesForEmployeesCurrentTenant,
}) => {
  const selectrole = (listOfSelections, props) => {
    const res = listOfSelections.map( (propsData) => {
      const data = propsData[1]
        return data
     })
    return props.onChange(res);
  };

  const { t } = useTranslation();
  const stateId = Digit.ULBService.getStateId();
  

  const { data: areaMappingData } = Digit.Hooks.useEnabledMDMS(stateId, "egov-location", [{ name: "egov-location" }], {
    select: (data) => {
      const formattedData = data?.["egov-location"]?.["egov-location"]?.[0];
      return formattedData;
    },
  });
  const districtOptions = areaMappingData?.districts.map((dist) => ({
        code: dist.districtCode,
        name: dist.districtName,
        i18nKey: dist.districtCode,
      })).sort((a, b) => a.code.localeCompare(b.code)) || [];

  if(localitiesForEmployeesCurrentTenant?.length===0){
    localitiesForEmployeesCurrentTenant=districtOptions
  }

  return (
    <Fragment>
      <FilterFormField>
        
      </FilterFormField>

      <FilterFormField>
         <Controller
          name="district"
          control={controlFilterForm}
          render={(props) => {
            const renderRemovableTokens = useMemo(()=>props?.value?.map((locality, index) => {
              return (
                <RemoveableTag
                key={index}
                text={locality.i18nKey}
                onClick={() => {
                  props.onChange(props?.value?.filter((loc) => loc.code !== locality.code))
                }}
                />
                );
              }),[props?.value])
            return loadingLocalitiesForEmployeesCurrentTenant ? <Loader/> : <>
              <div className="filter-label sub-filter-label" style={{fontSize: "18px", fontWeight: "600"}}>{t("ES_INBOX_LOCALITY")}</div>
              <MultiSelectDropdown
              options={localitiesForEmployeesCurrentTenant ? localitiesForEmployeesCurrentTenant : []}
              optionsKey="i18nKey"
              props={props}
              isPropsNeeded={true}
              onSelect={selectrole}
              selected={props?.value}
              defaultLabel={t("ES_BPA_ALL_SELECTED")}
              defaultUnit={t("BPA_SELECTED_TEXT")}
              />
              <div className="tag-container">
                {renderRemovableTokens}
              </div>
            </>
          }
        }
        />
      </FilterFormField>

      
    </Fragment>
  );
};

export default FilterFormFieldsComponent;