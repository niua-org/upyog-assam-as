import React from "react";

/* methid to get date from epoch */
export const convertEpochToDate = (dateEpoch) => {
    // Returning null in else case because new Date(null) returns initial date from calender
    if (dateEpoch) {
        const dateFromApi = new Date(dateEpoch);
        let month = dateFromApi.getMonth() + 1;
        let day = dateFromApi.getDate();
        let year = dateFromApi.getFullYear();
        month = (month > 9 ? "" : "0") + month;
        day = (day > 9 ? "" : "0") + day;
        return `${day}/${month}/${year}`;
    } else {
        return null;
    }
};

export const stringReplaceAll = (str = "", searcher = "", replaceWith = "") => {
    if (searcher == "") return str;
    while (str.includes(searcher)) {
      str = str.replace(searcher, replaceWith);
    }
    return str;
  };

export const businessServiceList = (isCode= false) => {
    let isSearchScreen = window.location.href.includes("/search");
    const availableBusinessServices = 
    [{
        code: isSearchScreen ? "FIRE_NOC" : "FIRE_NOC_SRV",
        active: true,
        roles: ["FIRE_NOC_APPROVER"],
        i18nKey: "WF_FIRE_NOC_FIRE_NOC_SRV",
    }, 
    {
        code: isSearchScreen ? "AIRPORT_AUTHORITY" : "AIRPORT_NOC_SRV",
        active: true,
        roles: ["AIRPORT_AUTHORITY_APPROVER"],
        i18nKey: "WF_FIRE_NOC_AIRPORT_NOC_SRV"
    },
    {
        code: "SOIL_TEST_SRV",
        active: true,
        roles: ["SOIL_TEST_APPROVER"],
        i18nKey: "WF_SOIL_TEST_SRV"
    },
    {
        code: "ARCHAEOLOGY_SRV",
        active: true,
        roles: [
            "ARCHAEOLOGY_APPROVER"
        ],
        i18nKey: "WF_ARCHAEOLOGY_SRV"
    },
    {
        code: "ARMY_DEFENCE_SRV",
        active: true,
        roles: [
            "ARMY_DEFENCE_APPROVER"
        ],
        i18nKey: "WF_ARMY_DEFENCE_SRV"
    },
    {
        code: "AZARA_CIRCLE_SRV",
        active: true,
        roles: [
            "AZARA_CIRCLE_APPROVER"
        ],
        i18nKey: "WF_AZARA_CIRCLE_SRV"
    },
    {
        code: "CIVIL_AVIATION_AZARA_SRV",
        active: true,
        roles: [
            "CIVIL_AVIATION_AZARA_APPROVER"
        ],
        i18nKey: "WF_CIVIL_AVIATION_AZARA_SRV"
    },
    {
        code: "CIVIL_AVIATION_SRV",
        active: true,
        roles: [
            "CIVIL_AVIATION_APPROVER"
        ],
        i18nKey: "WF_CIVIL_AVIATION_SRV"
    },
    {
        code: "ELECTRICAL_INSPECTOR_SRV",
        active: true,
        roles: [
            "ELECTRICAL_INSPECTOR_APPROVER"
        ],
        i18nKey: "WF_ELECTRICAL_INSPECTOR_SRV"
    },
    {
        code: "ELECTRICITY_DEPT_SRV",
        active: true,
        roles: [
            "ELECTRICITY_DEPT_APPROVER"
        ],
        i18nKey: "WF_ELECTRICITY_DEPT_SRV"
    },
    {
        code: "ENVIRONMENT_SRV",
        active: true,
        roles: [
            "ENVIRONMENT_APPROVER"
        ],
        i18nKey: "WF_ENVIRONMENT_SRV"
    },
    {
        code: "ESZ_CLEARANCE_SRV",
        active: true,
        roles: [
            "ESZ_CLEARANCE_APPROVER"
        ],
        i18nKey: "WF_ESZ_CLEARANCE_SRV"
    },
    {
        code: "FIRE_SAFETY_SRV",
        active: true,
        roles: [
            "FIRE_SAFETY_APPROVER"
        ],
        i18nKey: "WF_FIRE_SAFETY_SRV"
    },
    {
        code: "GROUND_WATER_SRV",
        active: true,
        roles: [
            "GROUND_WATER_APPROVER"
        ],
        i18nKey: "WF_GROUND_WATER_SRV"
    },
    {
        code: "HEALTH_AUTHORITY_SRV",
        active: true,
        roles: [
            "HEALTH_AUTHORITY_APPROVER"
        ],
        i18nKey: "WF_HEALTH_AUTHORITY_SRV"
    },
    {
        code: "HOME_SECURITY_SRV",
        active: true,
        roles: [
            "HOME_SECURITY_APPROVER"
        ],
        i18nKey: "WF_HOME_SECURITY_SRV"
    },
    {
        code: "LIFT_SAFETY_SRV",
        active: true,
        roles: [
            "LIFT_SAFETY_APPROVER"
        ],
        i18nKey: "WF_LIFT_SAFETY_SRV"
    },
    {
        code: "POLICE_CLEARANCE_SRV",
        active: true,
        roles: [
            "POLICE_CLEARANCE_APPROVER"
        ],
        i18nKey: "WF_POLICE_CLEARANCE_SRV"
    },
    {
        code: "POLLUTION_CONTROL_SRV",
        active: true,
        roles: [
            "POLLUTION_CONTROL_APPROVER"
        ],
        i18nKey: "WF_POLLUTION_CONTROL_SRV"
    },
    {
        code: "SACFA_RF_SRV",
        active: true,
        roles: [
            "SACFA_RF_APPROVER"
        ],
        i18nKey: "WF_SACFA_RF_SRV"
    },
    {
        code: "SAD_CLEARANCE_SRV",
        active: true,
        roles: [
            "SAD_CLEARANCE_APPROVER"
        ],
        i18nKey: "WF_SAD_CLEARANCE_SRV"
    },
    {
        code: "STP_CLEARANCE_SRV",
        active: true,
        roles: [
            "STP_CLEARANCE_APPROVER"
        ],
        i18nKey: "WF_STP_CLEARANCE_SRV"
    },
    {
        code: "STRUCTURAL_SAFETY_SRV",
        active: true,
        roles: [
            "STRUCTURAL_SAFETY_APPROVER"
        ],
        i18nKey: "STRUCTURAL_SAFETY_SRV"
    },
    {
        code: "WATER_RESOURCES_SRV",
        active: true,
        roles: [
            "WATER_RESOURCES_APPROVER"
        ],
        i18nKey: "WF_WATER_RESOURCES_SRV"
    }
];

    const newAvailableBusinessServices = [];
    const loggedInUserRoles = Digit.UserService.getUser().info.roles;
    availableBusinessServices.map(({ roles }, index) => {
        roles.map((role) => {
            loggedInUserRoles.map((el) => {
                if (el.code === role) {
                    isCode ? newAvailableBusinessServices.push(availableBusinessServices?.[index]?.code) : newAvailableBusinessServices.push(availableBusinessServices?.[index])
                }
            })
        })
    });

    return newAvailableBusinessServices;
}