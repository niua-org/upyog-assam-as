import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";

export const EPramaanService = {
  authorization: ({ filters }) =>
    Request({
      url: Urls.ePramaan.authorization,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
      params: {module:"PT" },
    }),
    register: ({ filters }) =>
    Request({
      url: Urls.ePramaan.register,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
      params: {module:"SSO" },
    }),
    token: ( data ) =>
    Request({
      url: Urls.ePramaan.token,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
      data:data,
    }),
    oauth: ( data ) =>
    Request({
      url: Urls.ePramaan.oauth,
      useCache: false,
      method: "POST",
      auth: true,
      userService: true,
      //params:{tenantId:"pg"},
      data:{User:data},
    })
};


