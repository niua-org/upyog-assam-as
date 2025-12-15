import Urls from "../atoms/urls";
import { ServiceRequest } from "../atoms/Utils/Request";

export const LocationService = {
  getLocalities: (tenantId) => {
    return ServiceRequest({
      serviceName: "getLocalities",
      url: Urls.location.localities,
      params: { tenantId: tenantId },
      useCache: true,
    });
  },
  getRevenueLocalities: async (tenantId) => {
    const response = await ServiceRequest({
      serviceName: "getRevenueLocalities",
      url: Urls.location.revenue_localities,
      params: { tenantId: tenantId },
      useCache: true,
    });
    return response;
  },
  getGramPanchayats: async (tenantId) => {
    const response = await ServiceRequest({
      serviceName: "getGramPanchayats",
      url: Urls.location.gramPanchayats,
      params: { tenantId: tenantId },
      useCache: true,
    });
    return response;
  },

  getVillage: async (tenantId) => {
    const response = await ServiceRequest({
      serviceName: "getVillage",
      url: Urls.location.village,
      params: { tenantId: tenantId },
      useCache: true,
    });
    return response;
  },

  getRevenueVillage: async (tenantId) => {
    const response = await ServiceRequest({
      serviceName: "getRevenueVillage",
      url: Urls.location.revenueVillage,
      params: { tenantId: tenantId },
      useCache: true,
    });
    return response;
  },

  getMouza: async (tenantId) => {
    const response = await ServiceRequest({
      serviceName: "getMouza",
      url: Urls.location.mouza,
      params: { tenantId: tenantId },
      useCache: true,
    });
    return response;
  },
};