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
  return checkForNotNull(value) ? value : "NA";
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
    localityCode: data?.address?.permanent?.city?.code,
    district: data?.address?.permanent?.district?.code,
    houseNo: data?.address?.permanent?.houseNo,
    pincode: data?.address?.permanent?.pincode,
    state: data?.address?.permanent?.state?.code,
    tenantId: data?.tenantId,
  };

  // Correspondence Address
  const correspondenceAddress = data?.address?.sameAsPermanent
    ? { ...permanentAddress, addressCategory: "CORRESPONDENCE",  addressType: "CORRESPONDENCE_ADDRESS", }
    : {
        addressLine1: data?.address?.correspondence?.addressLine1,
        addressLine2: data?.address?.correspondence?.addressLine2,
        addressCategory: "CORRESPONDENCE",
        addressType: "CORRESPONDENCE_ADDRESS",
        city: data?.address?.correspondence?.city?.code,
        country: "INDIA",
        localityCode: data?.address?.correspondence?.city?.code,
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
        adjoiningOwners: data?.land?.adjoiningOwners,
        futureProvisions: data?.land?.futureProvisions,
        todBenefits: data?.land?.todBenefits?.code,
        todWithTdr: data?.land?.todWithTdr,
        todZone: data?.land?.todZone,
        tdrUsed: data?.land?.tdrUsed?.code,
        todAcknowledgement: data?.land?.todAcknowledgement,
        propertyID : data?.propertyValidation?.propertyID,
        propertyDetails : data?.propertyValidation?.propertyDetails,
        gisCode: data?.areaMapping?.planningArea?.gisCode,
      },
      areaMapping: {
        buildingPermitAuthority: data?.areaMapping?.bpAuthority?.code,
        district: data?.areaMapping?.district?.code,
        mouza: data?.areaMapping?.mouza,
        planningArea: data?.areaMapping?.planningArea?.code,
        planningPermitAuthority: data?.areaMapping?.ppAuthority?.code,
        concernedAuthority: data?.areaMapping?.concernedAuthority?.code,
        ...(data?.areaMapping?.bpAuthority?.code === "MUNICIPAL_BOARD" && {
          revenueVillage: data?.areaMapping?.revenueVillage?.code,
          ward: data?.areaMapping?.ward?.code,
        }),
        ...(data?.areaMapping?.bpAuthority?.code === "GRAM_PANCHAYAT" && {
          villageName: data?.areaMapping?.villageName?.code,
        }),
        ...(data?.areaMapping?.bpAuthority?.code === "MUNICIPAL_CORPORATION" && {
          mouza: data?.areaMapping?.mouza?.code,
        }),
      },      

      rtpDetails: {
        rtpCategory: data?.land?.rtpCategory?.code,
        rtpName: data?.land?.registeredTechnicalPerson?.code,
        rtpUUID: data?.land?.registeredTechnicalPerson?.uuid,
      },

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
          locality:{code: data?.address?.permanent?.city?.code},
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
            gender: data?.applicant?.gender?.code,
            relationship:data?.applicant?.relationship?.code,
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
        assignes:[data?.land?.registeredTechnicalPerson?.uuid]
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
      altContactNumber: formData.applicant.alternateNumber ?? updated.landInfo.owners[0].altContactNumber,
      gender: formData.applicant.gender?.code ?? updated.landInfo.owners[0].gender,
      relationship: formData.applicant.relationship?.code ?? updated.landInfo.owners[0].relationship,
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
      planningPermitAuthority: formData.areaMapping.ppAuthority?.code ?? updated.areaMapping.planningPermitAuthority,
      concernedAuthority: formData.areaMapping.concernedAuthority?.code ?? updated.areaMapping.concernedAuthority,
      buildingPermitAuthority: formData.areaMapping.bpAuthority?.code ?? updated.areaMapping.buildingPermitAuthority,
      mouza: formData.areaMapping.mouza ?? updated.areaMapping.mouza,
      ...(formData.areaMapping.bpAuthority?.code === "MUNICIPAL_BOARD" && {
        ward: formData.areaMapping.ward?.code ?? updated.areaMapping.ward,
        revenueVillage: formData.areaMapping.revenueVillage?.code ?? updated.areaMapping.revenueVillage,
      }),
      ...(formData.areaMapping.bpAuthority?.code === "GRAM_PANCHAYAT" && {
        villageName: formData.areaMapping.villageName?.code ?? updated.areaMapping.villageName,
      }),
    };
  }
  if (formData?.land?.rtpCategory || formData?.land?.registeredTechnicalPerson) {
    updated.rtpDetails = {
      ...updated.rtpDetails,
      rtpCategory: formData.land.rtpCategory?.code,
      rtpName: formData.land.registeredTechnicalPerson?.code,
      rtpUUID: formData.land.registeredTechnicalPerson?.uuid,
    };
  }
  if (formData?.additionalDetails) {
    updated.additionalDetails = {
      ...updated.additionalDetails,
      ...formData.additionalDetails
    };
  }

  if (formData?.documents?.documents && Array.isArray(formData.documents.documents) && formData.documents.documents.length > 0) {
    updated.documents = formData.documents.documents.map(doc => ({
      documentType: doc.documentType,
      documentUid: doc.documentUid,
      fileStoreId: doc.fileStoreId
    }))
  }
  updated.status = "EDIT_APPLICATION";
  updated.workflow = {
    action: "EDIT",
    comments: "",
    assignes: [updated.rtpDetails?.rtpUUID]
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

export const getEstimatePayload = ({
  tenantId,
  applicationNo,
  feeType,
  applicationType = "RESIDENTIAL_RCC",
  edcrNumber
}) => {
  return {
    CalulationCriteria: [
      {
        tenantId,
        applicationNo,
        feeType,
        applicationType,
        BPA: {
          edcrNumber,
          tenantId
        }
      }
    ]
  };
};

export const formatEpochDateDMY = (epochValue) => {
  if (!epochValue) return "NA";

  // Convert to number
  let epoch = Number(epochValue);
  // Handle epoch in seconds (10 digits)
  if (epochValue.toString().length === 10) {
    epoch = epoch * 1000;
  }
  // Create Date object
  const date = new Date(epoch);

  // If invalid date
  if (isNaN(date.getTime())) return "NA";

  // Extract day, month, year
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const year = date.getFullYear();

  // Return formatted date
  return `${day}/${month}/${year}`;
};
