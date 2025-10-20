import { OBPSV2Services } from "../../elements/OBPSV2";

const ApplicationUpdateActions = async (applicationData, tenantId) => {
  try {
    const response = await OBPSV2Services.update(applicationData, tenantId);
    return response;
  } catch (error) {
    throw new Error(error?.response?.data?.Errors[0].message);
  }
};

export default ApplicationUpdateActions;
