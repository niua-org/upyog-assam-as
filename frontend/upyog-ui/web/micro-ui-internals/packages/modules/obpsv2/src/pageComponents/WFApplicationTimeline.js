import { CardSectionHeader, CheckPoint, ConnectingCheckPoints, Loader } from "@upyog/digit-ui-react-components";
import React, { Fragment, useEffect } from "react";
import { useTranslation } from "react-i18next";
import WFCaption from "./WFCaption";

const WFApplicationTimeline = (props) => {
  
  const { t } = useTranslation();
  const businessService = props?.application?.businessService;

  const { isLoading, data, refetch } = Digit.Hooks.useWorkflowDetails({
    tenantId: props.application?.tenantId,
    id: props.application?.applicationNo,
    moduleCode: businessService,
  });
  useEffect(() => {
    if (props.application?.status) {
      refetch();
    }
  }, [props.application?.status]);


  function OpenImage(imageSource, index, thumbnailsToShow) {
    window.open(thumbnailsToShow?.fullImage?.[0], "_blank");
  }

  const getTimelineCaptions = (checkpoint) => {

    if (checkpoint.state === "OPEN") 
      {
      const caption = {
        date: checkpoint?.auditDetails?.lastModified,
        source: props.application?.channel || "",
      };
      return <WFCaption data={caption} />;
    }
    else if (checkpoint.state) {
      const caption = {
        date: checkpoint?.auditDetails?.lastModified,
        name: checkpoint?.assignes?.[0]?.name,
        mobileNumber: checkpoint?.assignes?.[0]?.mobileNumber,
        comment: t(checkpoint?.comment),
        wfComment: checkpoint.wfComment,
        thumbnailsToShow: checkpoint?.thumbnailsToShow,
      };
      return <WFCaption data={caption} OpenImage={OpenImage} />;
    }


    else {
      const caption = {
        date: Digit.DateUtils.ConvertTimestampToDate(props.application?.auditDetails.lastModified),
        name: checkpoint?.assigner?.name,
        comment: t(checkpoint?.comment),
      };
      return <WFCaption data={caption} />;
    }
  };

  if (isLoading) {
    return <Loader />;
  }

  return (
    <React.Fragment>
      {!isLoading && (
        <Fragment>
          {data?.timeline?.length > 0 && (
            <CardSectionHeader style={{ marginBottom: "16px", marginTop: "32px" }}>
              {t("CS_APPLICATION_DETAILS_APPLICATION_TIMELINE")}
            </CardSectionHeader>
          )}
          {data?.timeline && data?.timeline?.length === 1 ? (
            <CheckPoint
              isCompleted={true}
              label={t((data?.timeline[0]?.state && `WF_${businessService}_${data.timeline[0].state}`) || "NA")}
              customChild={getTimelineCaptions(data?.timeline[0])}
            />
          ) : (
            <ConnectingCheckPoints>
              {data?.timeline &&
                data?.timeline.map((checkpoint, index) => {

                  return (
                    <React.Fragment key={index}>
                      <CheckPoint
                        keyValue={index}
                        isCompleted={index === 0}
                        label={t(
                          `${data?.processInstances[index].state?.["state"]}`
                        )}
                        customChild={getTimelineCaptions(checkpoint)}
                      />
                    </React.Fragment>
                  );
                })}
            </ConnectingCheckPoints>
          )}
        </Fragment>
      )}
    </React.Fragment>
  );
};

export default WFApplicationTimeline;