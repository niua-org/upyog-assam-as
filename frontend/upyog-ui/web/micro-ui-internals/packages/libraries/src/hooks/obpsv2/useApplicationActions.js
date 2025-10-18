import { useMutation } from "react-query";
import ApplicationUpdateActions from "../../services/molecules/OBPSV2/ApplicationUpdateAcions";

const useApplicationActions = (tenantId) => {
  return useMutation((applicationData) => ApplicationUpdateActions(applicationData, tenantId));
};

export default useApplicationActions;
