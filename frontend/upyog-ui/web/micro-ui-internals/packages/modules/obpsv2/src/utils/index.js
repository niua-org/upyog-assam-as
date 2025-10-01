import { v4 as uuid_v4 } from 'uuid';
import cloneDeep from "lodash/cloneDeep";
import { OBPSV2Services } from '../../../../libraries/src/services/elements/OBPSV2';
export const convertDateToEpoch = (dateString, dayStartOrEnd = "dayend") => {
  //example input format : "2018-10-02"
  try {
    const parts = dateString.match(/(\d{4})-(\d{1,2})-(\d{1,2})/);
    const DateObj = new Date(Date.UTC(parts[1], parts[2] - 1, parts[3]));
    DateObj.setMinutes(DateObj.getMinutes() + DateObj.getTimezoneOffset());
    if (dayStartOrEnd === "dayend") {
      DateObj.setHours(DateObj.getHours() + 24);
      DateObj.setSeconds(DateObj.getSeconds() - 1);
    }
    return DateObj.getTime();
  } catch (e) {
    return dateString;
  }
};

export const uuidv4 = () => {
  return uuid_v4();
};


export const convertEpochToDateDMY = (dateEpoch) => {
  if (dateEpoch == null || dateEpoch == undefined || dateEpoch == "") {
    return "NA";
  }
  const dateFromApi = new Date(dateEpoch);
  let month = dateFromApi.getMonth() + 1;
  let day = dateFromApi.getDate();
  let year = dateFromApi.getFullYear();
  month = (month > 9 ? "" : "0") + month;
  day = (day > 9 ? "" : "0") + day;
  return `${day}/${month}/${year}`;
};

export const stringReplaceAll = (str = "", searcher = "", replaceWith = "") => {
  if (searcher == "") return str;
  while (str.includes(searcher)) {
    str = str.replace(searcher, replaceWith);
  }
  return str;
};
export const checkForNotNull = (value = "") => {
  return value && value != null && value != undefined && value != "" ? true : false;
};

export const checkForNA = (value = "") => {
  return checkForNotNull(value) ? value : "CS_NA";
};

export const showHidingLinksForStakeholder = (roles = []) => {
  let userInfos = sessionStorage.getItem("Digit.citizen.userRequestObject");
  const userInfo = userInfos ? JSON.parse(userInfos) : {};
  let checkedRoles = [];
  const rolearray = roles?.map((role) => {
    userInfo?.value?.info?.roles?.map((item) => {
      if (item.code === role.code && item.tenantId === role.tenantId) {
        checkedRoles.push(item);
      }
    });
  });
  return checkedRoles?.length;
};

export const showHidingLinksForBPA = (roles = []) => {
  const userInfo = Digit.UserService.getUser();
  const tenantId = Digit.ULBService.getCurrentTenantId();
  let checkedRoles = [];
  const rolearray = roles?.map((role) => {
    userInfo?.info?.roles?.map((item) => {
      if (item.code == role && item.tenantId === tenantId) {
        checkedRoles.push(item);
      }
    });
  });
  return checkedRoles?.length;
};

export const bpaPayload = async(data) => {


  // Permanent Address
  const permanentAddress = {
    addressLine1: data?.address?.permanent?.addressLine1,
    addressLine2: data?.address?.permanent?.addressLine2,
    city: data?.address?.permanent?.city?.code,
    addressCategory: "PERMANENT",
    addressType: "PERMANENT_ADDRESS",
    country: "INDIA",
    district: data?.address?.permanent?.district?.code,
    houseNo: data?.address?.permanent?.houseNo,
    pincode: data?.address?.permanent?.pincode,
    state: data?.address?.permanent?.state?.code,
    tenantId: data?.tenantId,
  };

  // Correspondence Address
  const correspondenceAddress = data?.address?.sameAsPermanent
    ? { ...permanentAddress, addressCategory: "CORRESPONDENCE" }
    : {
        addressLine1: data?.address?.correspondence?.addressLine1,
        addressLine2: data?.address?.correspondence?.addressLine2,
        addressCategory: "CORRESPONDENCE",
        addressType: "CORRESPONDENCE_ADDRESS",
        city: data?.address?.correspondence?.city?.code,
        country: "INDIA",
        district: data?.address?.correspondence?.district?.code,
        houseNo: data?.address?.correspondence?.houseNo,
        pincode: data?.address?.correspondence?.pincode,
        state: data?.address?.correspondence?.state?.code,
        tenantId: data?.tenantId,
      };

  // Final Payload
  const formdata = {
    BPA: {
      tenantId: data?.tenantId,
      applicationType: data?.land?.constructionType?.code,
      businessService: "bpa-services",
      status: "INITIATED",
      additionalDetails: {
        constructionType: data?.land?.constructionType?.code,
        adjoiningOwners: data?.land?.adjoiningOwners,
        futureProvisions: data?.land?.futureProvisions,
        todBenefits: data?.land?.todBenefits?.code,
        todWithTdr: data?.land?.todWithTdr?.code,
        todZone: data?.land?.todZone?.code,
        tdrUsed: data?.land?.tdrUsed?.code,
        todAcknowledgement: data?.land?.todAcknowledgement,
      },
      areaMapping: {
        buildingPermitAuthority: data?.areaMapping?.bpAuthority?.code,
        district: data?.areaMapping?.district?.code,
        mouza: data?.areaMapping?.mouza?.code || data?.areaMapping?.mouza,
        planningArea: data?.areaMapping?.planningArea?.code,
        planningPermitAuthority: data?.areaMapping?.ppAuthority?.code,
        revenueVillage: data?.areaMapping?.revenueVillage?.code,
        ward: data?.areaMapping?.ward,
      },

      rtpDetails: {
        rtpCategory: data?.land?.rtpCategory?.code,
        rtpName: data?.land?.registeredTechnicalPerson?.name,
        rtpUUID: data?.land?.registeredTechnicalPerson?.code,
      },

      documents:
        data?.land?.documents?.map((doc) => ({
          documentType: doc?.documentType || "",
          documentUid: doc?.documentUid || "",
          fileStoreId: doc?.fileStoreId || "",
          id: doc?.id || "",
        })) || [],

      landInfo: {
        tenantId: data?.tenantId,
        newDagNumber: data?.land?.newDagNumber,
        newPattaNumber: data?.land?.newPattaNumber,
        oldDagNumber: data?.land?.oldDagNumber,
        oldPattaNumber: data?.land?.oldPattaNumber,
        totalPlotArea: data?.land?.totalPlotArea,
        documents:
        data?.land?.documents?.map((doc) => ({
          documentType: doc?.documentType || "",
          documentUid: doc?.documentUid || "",
          fileStoreId: doc?.fileStoreId || "",
          id: doc?.id || "",
        })) || [],
        address:{
          addressLine1: data?.address?.permanent?.addressLine1,
          addressLine2: data?.address?.permanent?.addressLine2,
          city: data?.address?.permanent?.city?.code,
          country: "INDIA",
          district: data?.address?.permanent?.district?.code,
          houseNo: data?.address?.permanent?.houseNo,
          pincode: data?.address?.permanent?.pincode,
          state: data?.address?.permanent?.state?.code,
          tenantId: data?.tenantId
        },
        owners: [
          {
            aadhaarNumber: data?.applicant?.aadhaarNumber,
            pan: data?.applicant?.panCardNumber,
            mobileNumber: data?.applicant?.mobileNumber,
            altContactNumber: data?.applicant?.alternateNumber,
            name: data?.applicant?.applicantName,
            emailId: data?.applicant?.emailId,
            fatherOrHusbandName: data?.applicant?.fatherName,
            motherName: data?.applicant?.motherName,
            permanentAddress,
            correspondenceAddress,
            active: true 
          },
        ],
        ownerAddresses: [],

        units: [
          {
            occupancyType: data?.land?.occupancyType?.code,
          },
        ],
      },

      workflow: {
        action: "APPLY",
        comments: "",
      },
    },
  };

  return formdata;
};

export const bpaEditPayload = async (formData) => { 
  const applicationNo = window.location.pathname.split("/").find((seg, i, arr) => arr[i - 1] === "editApplication");
  const tenantId = formData.tenantId
  const searchRes = await OBPSV2Services.search({
    tenantId,
    filters: { applicationNo },
    config: { staleTime: Infinity, cacheTime: Infinity }
  });
  const existingBPA = searchRes?.bpa?.[0];
  if (!existingBPA) throw new Error("BPA not found for update");
  const updated = cloneDeep(existingBPA);
  if (formData?.land) {
    updated.landInfo = {
      ...updated.landInfo,
      oldDagNumber: formData.land.oldDagNumber ?? updated.landInfo.oldDagNumber,
      newDagNumber: formData.land.newDagNumber ?? updated.landInfo.newDagNumber,
      oldPattaNumber: formData.land.oldPattaNumber ?? updated.landInfo.oldPattaNumber,
      newPattaNumber: formData.land.newPattaNumber ?? updated.landInfo.newPattaNumber,
      totalPlotArea: formData.land.totalPlotArea ?? updated.landInfo.totalPlotArea,
      units: formData.land.units ?? updated.landInfo.units,
      documents: formData.land.documents ?? updated.landInfo.documents,
      address: formData.land.address ?? updated.landInfo.address,
      ownerAddresses:[]
    };
  }
  if (formData?.applicant && updated.landInfo?.owners?.length > 0) {
    updated.landInfo.owners[0] = {
      ...updated.landInfo.owners[0],
      name: formData.applicant.applicantName ?? updated.landInfo.owners[0].name,
      mobileNumber: formData.applicant.mobileNumber ?? updated.landInfo.owners[0].mobileNumber,
      aadhaarNumber: formData.applicant.aadhaarNumber ?? updated.landInfo.owners[0].aadhaarNumber,
      pan: formData.applicant.panCardNumber ?? updated.landInfo.owners[0].pan,
      emailId: formData.applicant.emailId ?? updated.landInfo.owners[0].emailId,
      fatherOrHusbandName: formData.applicant.fatherName ?? updated.landInfo.owners[0].fatherOrHusbandName,
      motherName: formData.applicant.motherName ?? updated.landInfo.owners[0].motherName,
      permanentAddress: formData.applicant.permanentAddress ?? updated.landInfo.owners[0].permanentAddress,
      correspondenceAddress: formData.applicant.correspondenceAddress ?? updated.landInfo.owners[0].correspondenceAddress,
      //documents: searchRes?.landInfo?.owners[0]?.documents
    };
  }
  if (formData?.areaMapping) {
    updated.areaMapping = {
      ...updated.areaMapping,
      district: formData.areaMapping.district?.code ?? updated.areaMapping.district,
      planningArea: formData.areaMapping.planningArea?.code ?? updated.areaMapping.planningArea,
      ward: formData.areaMapping.ward ?? updated.areaMapping.ward,
      mouza: (formData.areaMapping.mouza?.code ? formData.areaMapping.mouza?.code : formData.areaMapping.mouza) ?? (updated.areaMapping.mouza?.code ? updated.areaMapping.mouza?.code : updated.areaMapping.mouza),
      revenueVillage: (formData.areaMapping.revenueVillage?.code ? formData.areaMapping.revenueVillage?.code : formData.areaMapping.revenueVillage) ?? (updated.areaMapping.revenueVillage?.code ? updated?.areaMapping?.revenueVillage?.code : updated?.areaMapping?.revenueVillage),
      buildingPermitAuthority: formData.areaMapping.buildingPermitAuthority ?? (updated.areaMapping.buildingPermitAuthority?.code ? updated.areaMapping.buildingPermitAuthority?.code : updated.areaMapping.buildingPermitAuthority),
      planningPermitAuthority: (formData.areaMapping.ppAuthority?.code ? formData.areaMapping.ppAuthority?.code : formData.areaMapping.ppAuthority) ?? (updated.areaMapping.planningPermitAuthority?.code ? updated.areaMapping.planningPermitAuthority?.code : updated.areaMapping.planningPermitAuthority)
    };
  }
  if (formData?.rtpDetails) {
    updated.rtpDetails = {
      ...updated.rtpDetails,
      rtpCategory: formData.rtpDetails.rtpCategory ?? updated.rtpDetails.rtpCategory,
      rtpName: formData.rtpDetails.rtpName ?? updated.rtpDetails.rtpName,
      rtpUUID: formData.rtpDetails.rtpUUID ?? updated.rtpDetails.rtpUUID
    };
  }
  if (formData?.additionalDetails) {
    updated.additionalDetails = {
      ...updated.additionalDetails,
      ...formData.additionalDetails
    };
  }
  if (formData?.documents) {
    updated.documents = formData?.documents?.documents
  }
  updated.status = "EDIT_APPLICATION";
  updated.workflow = {
    action: "EDIT",
    comments: ""
  };
  return {
    BPA: updated,
  };
};



export const sortDropdownNames = (options, optionkey, locilizationkey) => {
  return options.sort((a, b) => locilizationkey(a[optionkey]).localeCompare(locilizationkey(b[optionkey])));
};

export const getOrderDocuments = (appUploadedDocumnets, isNoc = false) => {
  let finalDocs = [];
  if (appUploadedDocumnets?.length > 0) {
    let uniqueDocmnts = appUploadedDocumnets.filter((elem, index) => appUploadedDocumnets.findIndex((obj) => obj?.documentType?.split(".")?.slice(0, 2)?.join("_") === elem?.documentType?.split(".")?.slice(0, 2)?.join("_")) === index);
    uniqueDocmnts?.map(uniDoc => {
      const resultsDocs = appUploadedDocumnets?.filter(appDoc => uniDoc?.documentType?.split(".")?.slice(0, 2)?.join("_") == appDoc?.documentType?.split(".")?.slice(0, 2)?.join("_"));
      resultsDocs?.forEach(resDoc => resDoc.title = resDoc.documentType);
      finalDocs.push({
        title: !isNoc ? resultsDocs?.[0]?.documentType?.split(".")?.slice(0, 2)?.join("_") : "",
        values: resultsDocs
      })
    });
  }
  return finalDocs;
}