import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";

const useScrutinyFormDetails = (edcrNumber, tenantId) => {
  const { t } = useTranslation();

  const [form22, setForm22] = useState(null);
  const [form23A, setForm23A] = useState(null);
  const [form23B, setForm23B] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchScrutinyDetails = async () => {
      try {
        setLoading(true);
        const response = await Digit.OBPSService.scrutinyDetails(tenantId, { edcrNumber });
        const scrutinyData = response?.edcrDetail?.[0];
        if (!scrutinyData) return;

        const plan = scrutinyData?.planDetail || {};
        const block = plan?.blocks?.[0] || {};
        const building = block?.building || {};
        const floors = building?.floors || [];
        const floorAreaCalculation = floors.map((f, i) => ({
          floor: `Floor ${i + 1}`,
          builtUpArea: f?.occupancies?.[0]?.builtUpArea || "",
          existingFloorArea: f?.existingFloorArea || "",
        }));

        const totalFloorAreaBeforeDeduction = floors.map((f, i) => ({
          floor: `Floor ${i + 1}`,
          area: f?.occupancies?.[0]?.builtUpArea || "",
        }));

        const totalFloorAreaAfterDeduction = floors.map((f, i) => ({
          floor: `Floor ${i + 1}`,
          area: f?.occupancies?.[0]?.floorArea || "",
        }));

        setForm22({
          plotArea: plan?.planInformation?.plotArea || "",
          existingPlinthArea: plan?.existingPlinthArea || "",
          proposedPlinthArea: block?.setBacks?.[0]?.buildingFootPrint?.area || "",
          floorAreaCalculation,
          mezzanineFloorArea: plan?.mezzanineFloorArea || "",
          deductionCalculation:
            (building?.totalArea?.[0]?.deduction + building?.totalArea?.[0]?.existingDeduction) || 0,
          totalFloorAreaAfterDeduction,
          totalFloorAreaBeforeDeduction,
          coverage: plan?.coverage || "",
          floorAreaRatio: plan?.farDetails?.providedFar || "",
        });
        const roadFacingPlot = [
          {
            existingWidth: plan?.planInformation?.roadWidth || "",
            proposedWidth: "",
            remarks: "",
          },
        ];

        const principalBylaws = [
          {
            desc: "Max Ground Coverage",
            proposed: building?.coverageArea || "",
            use: plan?.planInformation?.occupancy || "",
            permissible: "",
            carpetArea: "",
            remarks: "",
          },
          ...floors.map((floor) => ({
            desc: `Floor ${floor?.number}`,
            proposed: floor?.occupancies?.[0]?.builtUpArea || "",
            use: plan?.planInformation?.occupancy || "",
            permissible: "",
            carpetArea: "",
            remarks: "",
          })),
          {
            desc: "Total floor area",
            proposed: building?.totalFloorArea || "",
            use: plan?.planInformation?.occupancy || "",
            permissible: "",
            carpetArea: "",
            remarks: "",
          },
          {
            desc: "Floor Area Ratio",
            proposed: plan?.farDetails?.providedFar || "",
            use: "",
            permissible: "",
            carpetArea: "",
            remarks: "",
          },
          {
            desc: "No. of Dwelling units",
            proposed: "",
            use: "",
            permissible: "",
            carpetArea: "",
            remarks: "",
          },
        ];

        const setBacks = block?.setBacks?.[0] || {};
        const setbacks = [
          {
            side: "Front",
            clear: setBacks?.frontYard?.area || "",
            cantilever: "",
            reqClear: "",
            reqCantilever: "",
            remarks: "",
          },
          {
            side: "Rear",
            clear: setBacks?.rearYard?.area || "",
            cantilever: "",
            reqClear: "",
            reqCantilever: "",
            remarks: "",
          },
          {
            side: "Left",
            clear: setBacks?.sideYard1?.area || "",
            cantilever: "",
            reqClear: "",
            reqCantilever: "",
            remarks: "",
          },
          {
            side: "Right",
            clear: setBacks?.sideYard2?.area || "",
            cantilever: "",
            reqClear: "",
            reqCantilever: "",
            remarks: "",
          },
        ];

        const ducts =
          building?.ducts?.length > 0
            ? building.ducts.map((d) => ({
                no: d.no || "",
                area: d.area || "",
                width: d.width || "",
              }))
            : [{ no: "", area: "", width: "" }];

        const electricLine =
          plan?.electricLine?.length > 0
            ? plan.electricLine.map((e) => ({
                nature: e.nature || "",
                verticalDistance: e.verticalDistance || "",
                horizontalDistance: e.horizontalDistance || "",
              }))
            : [{ nature: "", verticalDistance: "", horizontalDistance: "" }];

        const parkingProvided =
          plan?.reportOutput?.scrutinyDetails
            ?.find((p) => p.key === "Common_Parking")
            ?.detail?.filter((d) => d.Description?.toLowerCase().includes("open parking"))
            ?.map((d) => ({
              open: d.Provided || "",
              stilt: "",
              basement: "",
              total: "",
            })) || [{ open: "", stilt: "", basement: "", total: "" }];

        const parkingRequired =
          plan?.reportOutput?.scrutinyDetails
            ?.find((p) => p.key === "Common_Parking")
            ?.detail?.filter((d) => d.Description?.toLowerCase().includes("car parking"))
            ?.map((d) => ({
              type: "Residential",
              car: d.Required || "",
              scooter: "",
              remarks: "",
            })) || [{ type: "", car: "", scooter: "", remarks: "" }];

        const visitorsParking = [{ type: "", car: "", scooter: "" }];

        setForm23A({
          classificationOfProposal: "",
          revenueVillage: plan?.planInformation?.revenueVillage || "",
          mouza: plan?.planInformation?.mouza || "",
          dagNo: plan?.planInfoProperties?.["DAG NO"] || "",
          pattaNo: "",
          sitePlanArea: plan?.planInformation?.plotArea || "",
          landDocumentArea: "",
          buildingHeight: building?.buildingHeight || "",
          heightofPlinth: block?.plinthHeight?.[0] || "",
          permitFee: "",
          cityInfrastructureCharges: "",
          additionalFloorSpaceCharges: "",
          peripheralCharges: "",
          otherCharges: "",
          totalAmount: "",
          receiptNo: "",
          dateValue: "",
          roadFacingPlot,
          principalBylaws,
          setbacks,
          ducts,
          electricLine,
          parkingProvided,
          parkingRequired,
          visitorsParking,
        });
        const floorData =
          plan?.blocks?.[0]?.building?.floors?.map((f, i) => {
            const occ = f?.occupancies?.[0] || {};
            return {
              label: f?.name || `Floor ${f?.number ?? i + 1}`,
              existing: occ?.builtUpArea || "",
              proposed: occ?.proposed || "",
              total: occ?.total || "",
            };
          }) || [];

        const sanitaryDetails = [
          { description: "NUMBER OF URINALS", total: plan?.totalUrinals ?? 0 },
          { description: "NUMBER OF BATHROOMS", total: plan?.totalBathrooms ?? 0 },
          { description: "NUMBER OF LATRINES", total: plan?.totalLatrines ?? 0 },
          { description: "NUMBER OF KITCHENS", total: plan?.totalKitchens ?? 0 },
        ];

        setForm23B({
          purpose: plan?.planInformation?.occupancy || "",
          noOfInhabitants: plan?.noOfInhabitants || "",
          waterSource: plan?.waterSource || "",
          distanceFromSewer: plan?.distanceFromSewer || "",
          materials: plan?.materials || "",
          architectName: plan?.architectName || "",
          registrationNumber: plan?.registrationNumber || "",
          architectAddress: plan?.architectAddress || "",
          dwellingUnitSize: plan?.dwellingUnitSize || "",
          constructionValidUpto: plan?.constructionValidUpto || null,
          leaseExtensionUpto: plan?.leaseExtensionUpto || null,
          floors: floorData,
          sanitaryDetails,
        });
      } catch (err) {
        console.error("Error fetching scrutiny details:", err);
        setError(err);
      } finally {
        setLoading(false);
      }
    };

    if (edcrNumber && tenantId) fetchScrutinyDetails();
  }, [edcrNumber, tenantId]);

  return { form22, form23A, form23B, loading, error };
};

export default useScrutinyFormDetails;