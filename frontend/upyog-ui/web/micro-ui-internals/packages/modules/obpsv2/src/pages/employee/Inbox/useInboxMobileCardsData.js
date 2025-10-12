import React from "react";
import { useTranslation } from "react-i18next";
import { format } from "date-fns";
import { SearchField, RadioButtons } from "@upyog/digit-ui-react-components";
import { Controller, useFormContext } from "react-hook-form";

const useInboxMobileCardsData = ({parentRoute, table, getRedirectionLink}) => {
    const { t } = useTranslation()

    const dataForMobileInboxCards = table?.map((row) => ({
            [t("BPA_APPLICATION_NUMBER_LABEL")]: row?.applicationId || "NA",
            [t("APPLICANT_NAME")]: row?.applicantName || "NA",
            [t("DISTRICT")]: t(row?.areaMapping?.district) || "NA",
            [t("MOBILE_NUMBER")]: row?.mobileNumber || "NA",
            [t("STATUS")]: t(row?.status) || t("CS_NA")
    }))

    const MobileSortFormValues = () => {
        const sortOrderOptions = []
        const { control: controlSortForm  } = useFormContext()
        return <SearchField>
            <Controller
                name="sortOrder"
                control={controlSortForm}
                render={({onChange, value}) => <RadioButtons
                    onSelect={(e) => {
                        onChange(e.code)
                    }}
                    selectedOption={sortOrderOptions.filter((option) => option.code === value)[0]}
                    optionsKey="i18nKey"
                    name="sortOrder"
                    options={sortOrderOptions}
                />}
            />
        </SearchField>
    }

    return ({ data:dataForMobileInboxCards, isTwoDynamicPrefix:true, linkPrefix: window.location.href.includes("/citizen") ?  `/upyog-ui/citizen/obpsv2/` : `/upyog-ui/employee/obpsv2/`, getRedirectionLink:getRedirectionLink, serviceRequestIdKey: "applicationNo", MobileSortFormValues})

}

export default useInboxMobileCardsData