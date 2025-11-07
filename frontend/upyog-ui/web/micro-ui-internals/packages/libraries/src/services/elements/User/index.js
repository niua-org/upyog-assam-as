import Urls from "../../atoms/urls";
import { Request, ServiceRequest } from "../../atoms/Utils/Request";
import { Storage } from "../../atoms/Utils/Storage";

export const UserService = {
  authenticate: (details) => {
    const data = new URLSearchParams();
    Object.entries(details).forEach(([key, value]) => data.append(key, value));
    data.append("scope", "read");
    data.append("grant_type", "password");
    return ServiceRequest({
      serviceName: "authenticate",
      url: Urls.Authenticate,
      data,
      headers: {
        authorization: `Basic ${window?.globalConfigs?.getConfig("JWT_TOKEN")||"ZWdvdi11c2VyLWNsaWVudDo="}`,
        "Content-Type": "application/x-www-form-urlencoded",
      },
    });
  },
  logoutUser: async () => {
    let user = UserService.getUser();
    if (!user || !user.info || !user.access_token) return { ePramaanInitiated: false };
    const { type } = user.info;
    const tenantId = type === "CITIZEN" ? Digit.ULBService.getStateId() : Digit.ULBService.getCurrentTenantId();
    
    // Check if ePramaan session exists
    const sessionId = Digit.SessionStorage.get("epramaan_sessionId");
    const sub = Digit.SessionStorage.get("epramaan_sub");
    let ePramaanInitiated = false;

    // ePramaan logout flow - if session exists, get form data and submit
    if (sessionId && sub) {
      try {
        // Step 1: Get ePramaan logout form data from backend
        const formDataResponse = await ServiceRequest({
          serviceName: "getEPramaanLogoutData",
          url: Urls.EPramaanLogoutData,
          data: { sessionId, sub, tenantId },
          auth: true,
        });

        if (formDataResponse) {
          // Step 2: Call UPYOG logout WITHOUT waiting or handling response
          // Fire and forget - don't await
          ServiceRequest({
            serviceName: "logoutUser",
            url: Urls.UserLogout,
            data: { access_token: user?.access_token },
            auth: true,
            params: { tenantId },
          }).catch(err => console.error("Logout error:", err));

          // Step 3: Immediately submit form (don't wait for logout to complete)
          const form = document.createElement("form");
          form.method = "POST";
          form.action = Urls.ePramaan.logoutUrl;
          form.style.display = "none";

          const dataInput = document.createElement("input");
          dataInput.type = "hidden";
          dataInput.name = "data";
          dataInput.value = JSON.stringify(formDataResponse);
          form.appendChild(dataInput);

          document.body.appendChild(form);
          ePramaanInitiated = true;
          
          form.submit();
          return { ePramaanInitiated };
        }
      } catch (error) {
        console.error("ePramaan logout error:", error);
      }
    }

    // Normal UPYOG logout (no ePramaan session)
    await ServiceRequest({
      serviceName: "logoutUser",
      url: Urls.UserLogout,
      data: { access_token: user?.access_token },
      auth: true,
      params: { tenantId: type === "CITIZEN" ? Digit.ULBService.getStateId() : Digit.ULBService.getCurrentTenantId() },
    });

    return { ePramaanInitiated };
  },

  getType: () => {
    return Storage.get("userType") || "citizen";
  },
  setType: (userType) => {
    Storage.set("userType", userType);
    Storage.set("user_type", userType);
  },
  getUser: () => {
    return Digit.SessionStorage.get("User");
  },
  logout: async () => {
    const userType = UserService.getType();
    let ePramaanInitiated = false;
    try {
      const result = await UserService.logoutUser();
      ePramaanInitiated = result?.ePramaanInitiated;
    } catch (e) {
    }
    
    // Only clear storage and redirect if ePramaan form submission did not happen
    // (ePramaan form submission causes redirect to ePramaan, then back to redirectUrl)
    if (!ePramaanInitiated) {
      window.localStorage.clear();
      window.sessionStorage.clear();
      if (userType === "citizen") {
        window.location.replace("/upyog-ui/citizen");
      } else {
        window.location.replace("/upyog-ui/employee/user/language-selection");
      }
    }
  },

  sendOtp: (details, stateCode) =>
    ServiceRequest({
      serviceName: "sendOtp",
      url: Urls.OTP_Send,
      data: details,
      auth: false,
      params: { tenantId: stateCode },
    }),
  setUser: (data) => {
    return Digit.SessionStorage.set("User", data);
  },
  setExtraRoleDetails: (data) => {
    const userDetails = Digit.SessionStorage.get("User");
    return Digit.SessionStorage.set("User", { ...userDetails, extraRoleInfo: data });
  },
  getExtraRoleDetails: () => {
    return Digit.SessionStorage.get("User")?.extraRoleInfo;
  },
  registerUser: (details, stateCode) =>
    ServiceRequest({
      serviceName: "registerUser",
      url: Urls.RegisterUser,
      data: {
        User: details,
      },
      params: { tenantId: stateCode },
    }),
  updateUser: async (details, stateCode) =>
    ServiceRequest({
      serviceName: "updateUser",
      url: Urls.UserProfileUpdate,
      auth: true,
      data: {
        user: details,
      },
      params: { tenantId: stateCode },
    }),
   
    //create address for user
      createAddressV2: async (details, stateCode, userUuid) =>
        ServiceRequest({
          serviceName: "createAddress",
          url: Urls.UserCreateAddressV2,
          auth: true,
          data: {
            address: details,
            userUuid: userUuid,
          },
          params: { tenantId: stateCode },
        }),
  hasAccess: (accessTo) => {
    const user = Digit.UserService.getUser();
    if (!user || !user.info) return false;
    const { roles } = user.info;
    return roles && Array.isArray(roles) && roles.filter((role) => accessTo.includes(role.code)).length;
  },

  changePassword: (details, stateCode) =>
    ServiceRequest({
      serviceName: "changePassword",
      url: Digit.SessionStorage.get("User")?.info ? Urls.ChangePassword1 : Urls.ChangePassword,
      data: {
        ...details,
      },
      auth: true,
      params: { tenantId: stateCode },
    }),

  employeeSearch: (tenantId, filters) => {
    return Request({
      url: Urls.EmployeeSearch,
      params: { tenantId, ...filters },
      auth: true,
    });
  },
  userSearch: async (tenantId, data, filters) => {
    return Request({
      url: Urls.UserSearch,
      params: { ...filters },
      method: "POST",
      auth: true,
      userService: true,
      data: data.pageSize ? { tenantId, ...data } : { tenantId, ...data, pageSize: "100" },
    });
  },
  userCreate: async (tenantId, user, filters) => {
    return Request({
      url: Urls.UserCreate,
      params: { ...filters },
      method: "POST",
      auth: false,
      userService: false,
      data: { user: {tenantId, ...user} } ,
    });
  },
  // user search for user profile
  userSearchNewV2: async (tenantId, data, filters) => {
    return Request({
      url: Urls.UserSearchNewV2,
      params: { ...filters },
      method: "POST",
      auth: true,
      userService: true,
      data: data.pageSize ? { tenantId, ...data } : { tenantId, ...data, pageSize: "100" },
    });
  },
  //update address for user
  updateAddressV2: async (details, stateCode) =>
    ServiceRequest({
      serviceName: "updateAddress",
      url: Urls.UserUpdateAddressV2,
      auth: true,
      data: {
        address: details
      },
      params: { tenantId: stateCode },
    })
};
