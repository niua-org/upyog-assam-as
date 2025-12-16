import React,{ useMemo } from "react";

/**
 * Custom Hook for NOC Business Service List
 * 
 * @author Shivank
 * @description This hook fetches NOC business services from MDMS and filters them based on user roles.
 *              It replaces hardcoded business service data with dynamic MDMS configuration from utils file of NOC module.
 * 
 * @param {boolean} isCode - If true, returns only service codes; if false, returns full service objects
 * @returns {Object} { businessServices: Array, isLoading: boolean }
 * 
 * @example
 * // Get full service objects
 * const { businessServices, isLoading } = useBusinessServiceList();
 * 
 * // Get only service codes
 * const { businessServices: serviceCodes, isLoading } = useBusinessServiceList(true);
 */

export const useBusinessServiceList = (isCode = false) => {
    const { data: nocBusinessServices, isLoading } = Digit.Hooks.useCustomMDMS(
        Digit.ULBService.getStateId(),
        "NOC",
        [{ name: "NOCBusinessServiceRoleMaping" }],
        {
            select: (data) => {
                const formattedData = data?.["NOC"]?.["NOCBusinessServiceRoleMaping"];
                return formattedData?.filter(item => item.active === true) || [];
            },
        }
    );

    const businessServices = useMemo(() => {
        if (!nocBusinessServices || isLoading) return [];
        
        const loggedInUserRoles = Digit.UserService.getUser().info.roles;
        const newAvailableBusinessServices = [];

        nocBusinessServices.forEach((service) => {
            service.roles?.forEach((role) => {
                loggedInUserRoles.forEach((userRole) => {
                    if (userRole.code === role) {
                        const serviceCode = service.code;
                        
                        const serviceData = {
                            ...service,
                            code: serviceCode
                        };
                        
                        const exists = newAvailableBusinessServices.some(
                            existing => existing.code === serviceCode
                        );
                        
                        if (!exists) {
                            isCode 
                                ? newAvailableBusinessServices.push(serviceCode)
                                : newAvailableBusinessServices.push(serviceData);
                        }
                    }
                });
            });
        });

        return newAvailableBusinessServices;
    }, [nocBusinessServices, isLoading, isCode]);

    return { businessServices, isLoading };
};