import { LocationService } from "../elements/Location";
import { StoreService } from "./Store/service";

export const getLocalities = {
  admin: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getLocalities(tenant)).TenantBoundary[0];
  },
  revenue: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getRevenueLocalities(tenant)).TenantBoundary[0];
  },
  grampanchayats: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getGramPanchayats(tenant)).TenantBoundary[0];
  },
  village: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getVillage(tenant)).TenantBoundary[0];
  },
  revenuevillage: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getRevenueVillage(tenant)).TenantBoundary[0];
  },
  mouza: async (tenant) => {
    await StoreService.defaultData(tenant, tenant, Digit.StoreData.getCurrentLanguage());
    return (await LocationService.getMouza(tenant)).TenantBoundary[0];
  },
};