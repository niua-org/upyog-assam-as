import { useQuery } from "react-query"

import useBPAV2Search from "./useBPAV2Search";

const useOBPSV2Search = (selectedType, payload, tenantId, filters, params, config = {}) => {
        // if((selectedType && selectedType.includes("STAKEHOLDER")) || (Object.keys(payload).length>0 && payload?.applicationType && payload?.applicationType.includes("STAKEHOLDER")))
        // {
        //     //return useEmpBPAREGSearch(tenantId, {}, params,config);
        // }
        // else
        // {
            return useBPAV2Search(tenantId, filters, config);
        // }
    }

export default useOBPSV2Search;