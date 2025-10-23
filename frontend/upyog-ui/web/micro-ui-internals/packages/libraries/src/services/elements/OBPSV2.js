import Urls from "../atoms/urls";
import { Request } from "../atoms/Utils/Request";
import {
  Card,
  CardSubHeader,
  Header,
  Loader,
  Row,
  StatusTable,
  MultiLink,
  Toast,
  CheckBox,
  PopUp,
  HeaderBar,
  ActionBar,
  Menu,
  Modal,
  SubmitBar,
  CardLabel,
  TextInput,
  TextArea,
  CardLabelDesc,
  UploadFile,
} from "@upyog/digit-ui-react-components";
import React, { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import get from "lodash/get";
import { isError, useQueryClient } from "react-query";
import WFApplicationTimeline from "../../../../modules/obpsv2/src/pageComponents/WFApplicationTimeline";
import DocumentsPreview from "../../../../modules/templates/ApplicationDetails/components/DocumentsPreview";
import { UploadServices } from "../atoms/UploadServices";

// This file defines the OBPSV2Services object, providing methods for creating, searching, and updating OBPSV2 resources through structured API requests.
export const OBPSV2Services = {
  create: (details) =>
    Request({
      url: Urls.obpsv2.create,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),
  rtpcreate: (data, tenantId) =>
    Request({
      url: Urls.edcr.create,
      // data: data,
      multipartData: data,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: { tenantId },
      auth: true,
      multipartFormData: true,
    }),
  anonymousCreate: (data, tenantId) =>
    Request({
      url: Urls.edcr.anonymousCreate,
      // data: data,
      multipartData: data,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: { tenantId },
      auth: false,
      multipartFormData: true,
    }),

  update: (details) =>
    Request({
      url: Urls.obpsv2.update,
      data: details,
      useCache: false,
      setTimeParam: false,
      userService: true,
      method: "POST",
      params: {},
      auth: true,
    }),

  search: ({ tenantId, filters, auth }) =>
    Request({
      url: Urls.obpsv2.search,
      useCache: false,
      method: "POST",
      auth: auth === false ? auth : true,
      userService: auth === false ? auth : true,
      params: { tenantId, ...filters },
    }),
  BPAApplicationDetails: async (tenantId, filters) => {
    const response = await OBPSV2Services.search({
      tenantId,
      filters,
      config: { staleTime: Infinity, cacheTime: Infinity },
    });
    let appDocumentFileStoreIds = response?.bpa?.[0]?.documents?.map(
      (docId) => docId.fileStoreId
    );
    if (!appDocumentFileStoreIds) appDocumentFileStoreIds = [];
    response?.bpa?.[0]?.additionalDetails?.fieldinspection_pending?.map(
      (fiData) => {
        fiData?.docs?.map((fiDoc) => {
          if (fiDoc?.fileStoreId)
            appDocumentFileStoreIds.push(fiDoc?.fileStoreId);
        });
      }
    );

    if (!response?.bpa?.length) {
      return;
    }
    const [bpa] = response?.bpa;
    let fileDetails = {};
    if (appDocumentFileStoreIds?.length > 0) {
      fileDetails = await UploadServices.Filefetch(
        appDocumentFileStoreIds,
        Digit.ULBService.getStateId()
      );
    }

    let appBusinessService = [],
      collectionBillDetails = [],
      collectionBillArray = [],
      totalAmount = 0,
      collectionBillRes = [];
    appBusinessService = ["BPA.PLANNING_PERMIT_FEE"];
    let fetchBillRes = {};

    if (appBusinessService?.[1]) {
      fetchBillRes = await Digit.PaymentService.fetchBill(bpa?.tenantId, {
        consumerCode: bpa?.applicationNo,
        businessService: appBusinessService[1],
      });
    }
    // for (let i = 0; i < appBusinessService?.length; i++) {
    //   let collectionres = await Digit.PaymentService.recieptSearch(bpa?.tenantId, appBusinessService[i], { consumerCodes: bpa?.applicationNo, isEmployee: true });
    //   if (collectionres?.Payments?.length > 0) {
    //     collectionres?.Payments?.map(res => {
    //       res?.paymentDetails?.map(resData => {
    //         if (resData?.businessService == appBusinessService[i]) {
    //           collectionBillRes.push(res);
    //         }
    //       })
    //     })
    //   }
    //   if (collectionres?.Payments?.length > 0) collectionBillDetails.push(...collectionres?.Payments);
    // }
    // if (collectionBillRes?.length > 0) {
    //   collectionBillRes?.map(ob => {
    //     ob?.paymentDetails?.[0]?.bill?.billDetails?.[0]?.billAccountDetails.map((bill, index) => {
    //       collectionBillArray.push(
    //         { title: `${bill?.taxHeadCode}_DETAILS`, value: "", isSubTitle: true },
    //         { title: bill?.taxHeadCode, value: `₹${bill?.amount}` },
    //         { title: "BPA_STATUS_LABEL", value: "Paid" }
    //       );
    //       totalAmount = totalAmount + parseInt(bill?.amount);
    //     })
    //   })
    // }
    // if (fetchBillRes?.Bill?.length > 0) {
    //   collectionBillArray.push(
    //     { title: `${fetchBillRes?.Bill?.[0]?.billDetails?.[0]?.billAccountDetails?.[0]?.taxHeadCode}_DETAILS` || `BPA_SANC_FEE_DETAILS`, value: "", isSubTitle: true},
    //     { title: `BPA_SANC_FEE_LABEL`, value: `₹${fetchBillRes?.Bill?.[0]?.totalAmount}` },
    //     { title: "BPA_STATUS_LABEL", value: `${fetchBillRes?.Bill?.[0]?.totalAmount == 0 ? "Paid" : "Unpaid"}` }
    //   )
    // }
    // totalAmount > 0 && collectionBillArray.push({ title: "BPA_TOT_AMT_PAID", value: `₹${totalAmount}` });

    const billDetails = {
      title: "BPA_FEE_DETAILS_LABEL",
      isFeeDetails: true,
      additionalDetails: {
        inspectionReport: [],
        values: [...collectionBillArray],
      },
    };
    function ConvertEpochToValidityDate(dateEpoch) {
      if (dateEpoch == null || dateEpoch == undefined || dateEpoch == "") {
        return "NA";
      }
      const dateFromApi = new Date(dateEpoch);
      let month = dateFromApi.getMonth() + 1;
      let day = dateFromApi.getDate();
      let year = dateFromApi.getFullYear() - 3;
      month = (month > 9 ? "" : "0") + month;
      day = (day > 9 ? "" : "0") + day;
      return `${day}/${month}/${year}`;
    }
    let details = [];
    let applicationDetailsInfo = {
      title: " ",
      isCommon: true,
      values: [
        {
          title: "BPA_APPLICATION_NUMBER_LABEL",
          value: bpa?.applicationNo || "NA",
        },
      ],
    };
    const landInfo = bpa?.landInfo || {};
    const owners = landInfo?.owners || [];

    const primaryOwner = owners.length > 0 ? owners[0] : {};
    const address = landInfo?.address || {};
    const permanentAddress = primaryOwner?.permanentAddress || {};
    const additionalDetails = bpa?.additionalDetails || {};
    const adjoiningOwners = additionalDetails?.adjoiningOwners || {};
    const areaMapping = {
      title: "BPA_AREA_MAPPING",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        { title: "DISTRICT", value: bpa?.areaMapping?.district || "NA" },
        {
          title: "PLANNING_AREA",
          value: bpa?.areaMapping?.planningArea || "NA",
        },
        {
          title: "PP_AUTHORITY",
          value: bpa?.areaMapping?.planningPermitAuthority || "NA",
        },
        {
          title: "BP_AUTHORITY",
          value: bpa?.areaMapping?.buildingPermitAuthority || "NA",
        },
        {
          title: "REVENUE_VILLAGE",
          value: bpa?.areaMapping?.revenueVillage || "NA",
        },
        { title: "MOUZA", value: bpa?.areaMapping?.mouza || "NA" },
        { title: "WARD", value: bpa?.areaMapping?.ward || "NA" },
      ],
    };
    const applicantDetails = {
      title: "BPA_APPLICANT_DETAILS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        { title: "BPA_APPLICANT_NAME", value: primaryOwner?.name || "NA" },
        { title: "BPA_MOBILE_NO", value: primaryOwner?.mobileNumber || "NA" },
        {
          title: "BPA_ALT_MOBILE_NO",
          value: primaryOwner?.altContactNumber || "NA",
        },
        { title: "BPA_EMAIL_ID", value: primaryOwner?.emailId || "NA" },
        { title: "BPA_GENDER", value: primaryOwner?.gender || "NA" },
        {
          title: "BPA_GUARDIAN",
          value: primaryOwner?.fatherOrHusbandName || "NA",
        },
        {
          title: "BPA_RELATIONSHIP",
          value: primaryOwner?.relationship || "NA",
        },
        { title: "BPA_MOTHER_NAME", value: primaryOwner?.motherName || "NA" },
        { title: "BPA_PAN_CARD", value: primaryOwner?.pan || "NA" },
        {
          title: "BPA_AADHAAR_CARD",
          value: primaryOwner?.aadhaarNumber || "NA",
        },
      ],
    };

    const addressDetails = {
      title: "BPA_ADDRESS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        { title: "BPA_HOUSE_NO", value: permanentAddress?.houseNo || "NA" },
        {
          title: "BPA_ADDRESS_LINE_1",
          value: permanentAddress?.addressLine1 || "NA",
        },
        {
          title: "BPA_ADDRESS_LINE_2",
          value: permanentAddress?.addressLine2 || "NA",
        },
        { title: "BPA_LANDMARK", value: permanentAddress?.landmark || "NA" },
        { title: "BPA_DISTRICT", value: permanentAddress?.district || "NA" },
        { title: "BPA_STATE", value: permanentAddress?.state || "NA" },
        { title: "BPA_PIN_CODE", value: permanentAddress?.pincode || "NA" },
      ],
    };

    const correspondenceAddressDetails = {
      title: "BPA_CORRESPONDENCE_ADDRESS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: primaryOwner?.correspondenceAddress
        ? [
            {
              title: "BPA_HOUSE_NO",
              value: primaryOwner.correspondenceAddress?.houseNo || "NA",
            },
            {
              title: "BPA_ADDRESS_LINE_1",
              value: primaryOwner.correspondenceAddress?.addressLine1 || "NA",
            },
            {
              title: "BPA_ADDRESS_LINE_2",
              value: primaryOwner.correspondenceAddress?.addressLine2 || "NA",
            },
            {
              title: "BPA_DISTRICT",
              value: primaryOwner.correspondenceAddress?.district || "NA",
            },
            {
              title: "BPA_STATE",
              value: primaryOwner.correspondenceAddress?.state || "NA",
            },
            {
              title: "BPA_PIN_CODE",
              value: primaryOwner.correspondenceAddress?.pincode || "NA",
            },
          ]
        : [{ title: "BPA_SAME_AS_SITE_ADDRESS", value: true }],
    };

    const landDetails = {
      title: "BPA_LAND_DETAILS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        { title: "BPA_OLD_DAG_NUMBER", value: landInfo?.oldDagNumber || "NA" },
        { title: "BPA_NEW_DAG_NUMBER", value: landInfo?.newDagNumber || "NA" },
        {
          title: "BPA_OLD_PATTA_NUMBER",
          value: landInfo?.oldPattaNumber || "NA",
        },
        {
          title: "BPA_NEW_PATTA_NUMBER",
          value: landInfo?.newPattaNumber || "NA",
        },
        {
          title: "BPA_TOTAL_PLOT_AREA",
          value: landInfo?.totalPlotArea
            ? `${landInfo.totalPlotArea} sq. m.`
            : "NA",
        },
      ],
    };

    const adjoiningLandOwners = {
      title: "BPA_ADJOINING_LAND_OWNERS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        { title: "BPA_NORTH", value: adjoiningOwners?.north || "NA" },
        { title: "BPA_SOUTH", value: adjoiningOwners?.south || "NA" },
        { title: "BPA_EAST", value: adjoiningOwners?.east || "NA" },
        { title: "BPA_WEST", value: adjoiningOwners?.west || "NA" },
      ],
    };

    const futureProvisions = {
      title: "BPA_FUTURE_PROVISIONS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        {
          title: "BPA_VERTICAL_EXTENSION",
          value:
            additionalDetails?.futureProvisions?.verticalExtension?.code ||
            "NA",
        },
        ...(additionalDetails?.futureProvisions?.verticalExtension?.code ===
        "YES"
          ? [
              {
                title: "BPA_VERTICAL_EXTENSION_AREA",
                value:
                  additionalDetails.futureProvisions.verticalExtensionArea ||
                  "NA",
              },
            ]
          : []),
        {
          title: "BPA_HORIZONTAL_EXTENSION",
          value:
            additionalDetails?.futureProvisions?.horizontalExtension?.code ||
            "NA",
        },
        ...(additionalDetails?.futureProvisions?.horizontalExtension?.code ===
        "YES"
          ? [
              {
                title: "BPA_HORIZONTAL_EXTENSION_AREA",
                value:
                  additionalDetails.futureProvisions.horizontalExtensionArea ||
                  "NA",
              },
            ]
          : []),
        {
          title: "BPA_TOD_BENEFITS",
          value: additionalDetails?.todBenefits || "NA",
        },
        { title: "BPA_TDR_USED", value: additionalDetails?.tdrUsed || "NA" },
        ...(additionalDetails?.todBenefits === "YES" &&
        additionalDetails?.todZone
          ? [
              {
                title: "BPA_TOD_ZONE",
                value: additionalDetails.todZone?.code || "NA",
              },
            ]
          : []),
      ],
    };

    const rtpDetails = {
      title: "BPA_RTP_DETAILS",
      asSectionHeader: true,
      isInsert: true,
      isCommon: true,
      values: [
        {
          title: "BPA_RTP_CATEGORY",
          value: bpa?.rtpDetails?.rtpCategory || "NA",
        },
        {
          title: "BPA_REGISTERED_TECHNICAL_PERSON",
          value: bpa?.rtpDetails?.rtpName || "NA",
        },
        {
          title: "BPA_OCCUPANCY_TYPE",
          value: landInfo?.units?.[0]?.occupancyType || "NA",
        },
      ],
    };
    const documentDetails =  {
      title: "BPA_DOCUMENT_DETAILS_LABEL",
      asSectionHeader: true,
      isDocumentDetails: true,
      additionalDetails: {
        obpsDocuments: [{
          title: "",
          values: bpa?.documents?.map(doc => ({
            title: doc?.documentType?.replaceAll('.', '_'),
            documentType: doc?.documentType,
            documentUid: doc?.documentUid,
            fileStoreId: doc?.fileStoreId,
            id: doc?.id,
            url: fileDetails?.data?.[doc?.fileStoreId] ? fileDetails?.data?.[doc?.fileStoreId]?.split(',')[0] : ""
          }))
        }]
      },
    };
    details = [
      ...details,
      applicationDetailsInfo,
      areaMapping,
      applicantDetails,
      addressDetails,
      correspondenceAddressDetails,
      landDetails,
      adjoiningLandOwners,
      futureProvisions,
      rtpDetails,
      documentDetails
    ];
    let bpaFilterDetails = details?.filter((data) => data);
    let envCitizenName = window.location.href.includes("/employee")
      ? "employee"
      : "citizen";
    return {
      applicationData: bpa,
      applicationDetails: bpaFilterDetails,
      tenantId: bpa?.tenantId,
      businessService: bpa?.businessService,
      applicationNo: bpa?.applicationNo,
      applicationStatus: bpa?.status,
      collectionBillDetails: collectionBillDetails,
    };
  },
};
