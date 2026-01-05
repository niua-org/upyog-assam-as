import React, { useState, useEffect } from "react";
import { CardText, FormStep, CitizenConsentForm, Loader, CheckBox, Modal, Card, LinkButton} from "@upyog/digit-ui-react-components";
import { Link } from "react-router-dom";

const SelectRtpMobileNumber = ({ t, onSelect, showRegisterLink, mobileNumber, onMobileChange, config, canSubmit }) => {

  const [isCheckBox, setIsCheckBox] = useState(false);
  const [isCCFEnabled, setisCCFEnabled] = useState(false);
  const [mdmsConfig, setMdmsConfig] = useState("");
  const [error, setError]=useState("");
  const { isLoading, data } = Digit.Hooks.useCustomMDMS(Digit.ULBService.getStateId(), "common-masters", [{ name: "CitizenConsentForm" }]);
  const [showToast, setShowToast] = useState(null);
  
  function setTermsAndPolicyDetails(e) {
    setIsCheckBox(e.target.checked)
  }

  const checkDisbaled = () => {
    if (isCCFEnabled?.isCitizenConsentFormEnabled) {
      return !(mobileNumber.length === 10 && canSubmit && isCheckBox)
    } else {
      return !(mobileNumber.length === 10 && canSubmit)
    }
  }

  useEffect(()=> {
    if (data?.["common-masters"]?.CitizenConsentForm?.[0]?.isCitizenConsentFormEnabled) {
      setisCCFEnabled(data?.["common-masters"]?.CitizenConsentForm?.[0])
    }
  }, [data]);

  const onLinkClick = (e) => {
    setMdmsConfig(e.target.id)
  }

  const checkLabels = () => {
    return (
    <span>
      {isCCFEnabled?.checkBoxLabels?.map((data, index) => {
        return <span key={index}>
          {data?.linkPrefix && <span>{t(`${data?.linkPrefix}_`)}</span>}
          {data?.link && <span id={data?.linkId} onClick={(e) => { onLinkClick(e) }} style={{ color: "#a82227", cursor: "pointer" }}>{t(`${data?.link}_`)}</span>}
          {data?.linkPostfix && <span>{t(`${data?.linkPostfix}_`)}</span>}
          {(index == isCCFEnabled?.checkBoxLabels?.length - 1) && t("LABEL")}
        </span>
      })}
    </span>
    );
  };
  
  const validateMobileNumber=()=>{
      if(/^\d{0,10}$/.test(mobileNumber)){
        setError("")
      }
  };
  
  const handleMobileChange=(e)=>{
      const value=e.target.value;
      if(/^\d{0,10}$/.test(value)|| value===""){
        onMobileChange(e);
        validateMobileNumber();
      }
      else{
        setError(t("CORE_COMMON_PROFILE_MOBILE_NUMBER_INVALID"));
      }
  };
  
  if (isLoading) return <Loader />

  const Heading = (props) => {
    return <h1 className="heading-m">{props.label}</h1>;
  };
  
  const Close = () => (
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="#FFFFFF">
      <path d="M0 0h24v24H0V0z" fill="none" />
      <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12 19 6.41z" />
    </svg>
  );
  
  const CloseBtn = (props) => {
    return (
      <div className="icon-bg-secondary" onClick={props.onClick}>
        <Close />
      </div>
    );
  };

  const closeModal =() =>{
    setShowToast(false)
  }
  
  const setModal=(e)=>{
    setShowToast(false);
  }

  return (
    <FormStep
      isDisabled={checkDisbaled()}
      onSelect={onSelect}
      config={{
        ...config,
        texts: {
          ...config.texts,
          header: t("CS_LOGIN_PROVIDE_MOBILE_NUMBER_RTP"),
        }
      }}
      t={t}
      componentInFront="+91"
      onChange={handleMobileChange}
      value={mobileNumber}
    >
     <div style={{ position: "relative" }}>
        <Link 
          to={`/upyog-ui/citizen/area-mapping`}
          style={{
            position: "absolute",
            top: "-210px",
            right: "20px",
            zIndex: 10
          }}
        >
          <LinkButton label={t("BPA_CITIZEN_LOGIN")} />
        </Link>
        
        {error && <p style={{color:"red"}}>{error}</p>}
        {isCCFEnabled?.isCitizenConsentFormEnabled && (
          <div>
            <CheckBox
              className="form-field"
              label={checkLabels()}
              value={isCheckBox}
              checked={isCheckBox}
              style={{ marginTop: "5px", marginLeft: "55px" }}
              styles={{marginBottom: "30px"}}
              onChange={setTermsAndPolicyDetails}
            />

            <CitizenConsentForm
              styles={{}}
              t={t}
              isCheckBoxChecked={setTermsAndPolicyDetails}
              labels={isCCFEnabled?.checkBoxLabels}
              mdmsConfig={mdmsConfig}
              setMdmsConfig={setMdmsConfig}
            />
          </div>
        )}
        
        <div className="col col-md-4 text-md-center p-0" style={{width:"40%", marginTop:"5px"}}>
          {showToast && (
            <Modal
              headerBarMain={<Heading label={t("Consent")} />}
              headerBarEnd={<CloseBtn onClick={closeModal} />}
              actionCancelLabel={"Cancel"}
              actionCancelOnSubmit={closeModal}
              actionSaveLabel={"Ok"}
              actionSaveOnSubmit={(e)=>setModal(e)}
              formId="modal-action"
            >
              <div style={{ width: "100%" }}>
                <Card>
                  <p>By selecting this option, I am providing my consent to associate my Upyog account with my DigiLocker ID</p>
                </Card>
              </div>
            </Modal>
          )}
        </div>
      </div>
    </FormStep>
  );
};

export default SelectRtpMobileNumber;