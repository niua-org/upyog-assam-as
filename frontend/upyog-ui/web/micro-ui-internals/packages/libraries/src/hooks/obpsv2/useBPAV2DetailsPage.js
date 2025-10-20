import { useQuery } from "react-query";

const useBPAV2DetailsPage = (tenantId, filters, config) => {
  return useQuery(['BPA_DETAILS_PAGE', filters, tenantId], () => Digit.OBPSV2Services.BPAApplicationDetails(tenantId, filters), config);
}

export default useBPAV2DetailsPage;