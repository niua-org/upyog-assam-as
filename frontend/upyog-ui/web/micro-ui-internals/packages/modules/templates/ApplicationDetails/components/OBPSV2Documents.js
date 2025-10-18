// File: OBPSV2Documents.js

import React, { useEffect, useState } from "react";
import {
  CardLabel,
  Dropdown,
  LabelFieldPair,
  MultiUploadWrapper,
  CardSubHeader,
  Loader,
  Toast,
} from "@upyog/digit-ui-react-components";
import DocumentsPreview from "../../../templates/ApplicationDetails/components/DocumentsPreview";
import useMDMS from "../../../../libraries/src/hooks/obpsv2/useMDMS";
const OBPSV2Documents = ({ t, formData, applicationData, docs, obpsActionsDetails }) => {
  const stateId = Digit.ULBService.getStateId();
  const applicationStatus = applicationData?.status || "";
  const actions = obpsActionsDetails?.data?.nextActions || [];

  const [documents, setDocuments] = useState(formData?.documents?.documents || []);
  const [bpaTaxDocuments, setBpaTaxDocuments] = useState([]);
  const [error, setError] = useState(null);
  const [enableSubmit, setEnableSubmit] = useState(true);
  const [checkRequiredFields, setCheckRequiredFields] = useState(false);
  const [checkEnablingDocs, setCheckEnablingDocs] = useState(false);

  const { isLoading: bpaDocsLoading, data: bpaDocs } = useMDMS("as", "BPA", ["DocMapping"]);
    const { isLoading: commonDocsLoading, data: commonDocs } = useMDMS(stateId, "common-masters", ["DocumentType"]);
    let filtredBpaDocs = bpaDocs?.BPA?.DocMapping;
    useEffect(() => {  
    let documentsList = [];
    filtredBpaDocs?.[0]?.docTypes?.forEach(doc => {
        let code = doc.code; doc.dropdownData = []; doc.uploadedDocuments = [];
        commonDocs?.["common-masters"]?.DocumentType?.forEach(value => {
            let values = value.code.slice(0, code.length);
            if (code === values) {
                doc.hasDropdown = true;
                value.i18nKey = value.code;
                doc.dropdownData.push(value);
            }
        });
        
        doc.uploadedDocuments[0] = {};
        doc.uploadedDocuments[0].values = [];
        docs?.[0]?.values?.map(upDocs => {
          if (code === `${upDocs?.documentType?.split('.')[0]}.${upDocs?.documentType?.split('.')[1]}`) {
              doc.uploadedDocuments[0].values.push(upDocs)
          }
      })
      
        documentsList.push(doc);
    });
    sessionStorage.setItem("OBPS_DOCUMENTS", JSON.stringify(documentsList));
        setBpaTaxDocuments(documentsList);
      }, [!bpaDocsLoading, !commonDocsLoading]);

  useEffect(() => {
    let count = 0;
    bpaTaxDocuments.forEach((doc) => {
      let isRequiredPresent = false;
      documents.forEach((data) => {
        if (doc.required && doc.code === `${data.documentType.split(".")[0]}.${data.documentType.split(".")[1]}`) {
          isRequiredPresent = true;
        }
      });
      if (!isRequiredPresent && doc.required) count++;
    });
    if ((count === 0) && documents.length > 0) setEnableSubmit(false);
    else setEnableSubmit(true);
  }, [documents, checkRequiredFields]);


 //s if (isLoading) return <Loader />;

  return (
    <div>
        {bpaTaxDocuments?.map((document, index) => {
            return (
                <div>
                    <SelectDocument
                        key={index}
                        index={index}
                        document={document}
                        t={t}
                        error={error}
                        setError={setError}
                        setDocuments={setDocuments}
                        documents={documents}
                        setCheckRequiredFields={setCheckRequiredFields}
                        applicationStatus={applicationStatus}
                        actions={actions}
                        bpaTaxDocuments={bpaTaxDocuments}
                        checkEnablingDocs={checkEnablingDocs}
                    />
                </div>
            );
        })}
    </div>
);
};

function SelectDocument({
  t,
  document: doc,
  setDocuments,
  documents,
  error,
  setError,
  setCheckRequiredFields,
  index,
  applicationStatus,
  actions,
  bpaTaxDocuments,
  checkEnablingDocs,
}) {
  const tenantId = Digit.ULBService.getStateId();
  const filteredDocument = documents?.filter((item) => item?.documentType?.includes(doc?.code))[0];
  const [selectedDocument, setSelectedDocument] = useState(
    filteredDocument
      ? { ...filteredDocument, active: true, code: filteredDocument?.documentType, i18nKey: filteredDocument?.documentType }
      : doc?.dropdownData?.length === 1
      ? doc?.dropdownData[0]
      : {}
  );
  const [file, setFile] = useState(null);
  const [uploadedFile, setUploadedFile] = useState(() => filteredDocument?.fileStoreId || null);
  const [selectArrayFiles, setSelectArrayFiles] = useState([]);

  const handleSelectDocument = (value) => setSelectedDocument(value);

  const allowedFileTypes = /(.*?)(jpg|jpeg|png|image|pdf)$/i;

  function selectFiles(e) {
    e && setFile(e.file);
  }

  useEffect(() => {
    if (selectedDocument?.code) {
      setDocuments((prev) => {
        const filteredByDocType = prev?.filter((item) => item?.documentType !== selectedDocument?.code);
        if (uploadedFile?.length === 0 || uploadedFile === null) return filteredByDocType;

        const filteredByFileId = filteredByDocType?.filter((item) => item?.fileStoreId !== uploadedFile);
        return [
          ...filteredByFileId,
          {
            documentType: selectedDocument?.code,
            fileStoreId: uploadedFile,
            documentUid: uploadedFile,
            fileName: file?.name || "",
            id: documents ? documents.find((x) => x.documentType === selectedDocument?.code)?.id : undefined,
          },
        ];
      });
    }
  }, [uploadedFile, selectedDocument]);

  useEffect(() => {
    if (selectArrayFiles.length > 0) {
      sessionStorage.removeItem("OBPSV2_DOCUMENTS");
      doc.newUploadedDocs = [];
      selectArrayFiles.map((newDoc) => {
        if (selectedDocument?.code) {
          doc.newUploadedDocs.push({
            documentType: selectedDocument?.code,
            fileStoreId: newDoc?.fileStoreId?.fileStoreId,
            documentUid: newDoc?.fileStoreId?.fileStoreId,
            tenantId: newDoc?.fileStoreId?.tenantId,
          });
        }
      });
      bpaTaxDocuments[index] = doc;
      sessionStorage.setItem("OBPSV2_DOCUMENTS", JSON.stringify(bpaTaxDocuments));
    }
  }, [selectArrayFiles, selectedDocument]);

  const getData = (index, state) => {
    let data = Object.fromEntries(state);
    let newArr = Object.values(data);
    if (Object.keys(data).length !== 0) setSelectArrayFiles(newArr);
    selectFiles(newArr[newArr.length - 1]);
  };

  return (
    <div
      style={{
        marginBottom: "24px",
        maxWidth: "950px",
        minWidth: "280px",
        background: "#FAFAFA",
        borderRadius: "4px",
        border: "1px solid #D6D5D4",
        padding: "8px",
      }}
    >
      <CardSubHeader
        style={{
          marginBottom: "8px",
          paddingBottom: "9px",
          color: "#0B0C0C",
          fontSize: "16px",
          lineHeight: "19px",
        }}
      >
        {`${t(doc?.code)}`}
      </CardSubHeader>

      {doc?.uploadedDocuments?.length && (
        <DocumentsPreview
          documents={doc?.uploadedDocuments}
          svgStyles={{ width: "100px", height: "100px", viewBox: "0 0 25 25", minWidth: "100px" }}
        />
      )}

      {checkEnablingDocs ? (
        <div style={{ marginTop: "20px" }}>
          <LabelFieldPair style={{ width: "100%" }}>
            <CardLabel style={{ marginTop: "-10px", width: "100%" }}>
              {doc?.required ? `${t(doc?.code)}* ` : `${t(doc?.code)}`}
            </CardLabel>
            <Dropdown
              className="form-field"
              t={t}
              isMandatory={false}
              option={doc?.dropdownData}
              selected={selectedDocument}
              optionKey="i18nKey"
              select={handleSelectDocument}
              style={{ width: "100%" }}
            />
          </LabelFieldPair>

          <LabelFieldPair style={{ width: "100%" }}>
            <CardLabel className="card-label-smaller" style={{ width: "100%" }}></CardLabel>
            <div className="field" style={{ width: "100%" }}>
              <MultiUploadWrapper
                module="BPA"
                tenantId={tenantId}
                getFormState={(e) => getData(index, e)}
                t={t}
                allowedFileTypesRegex={allowedFileTypes}
                allowedMaxSizeInMB={5}
                acceptFiles="image/*, .pdf, .png, .jpeg, .jpg"
              />
            </div>
          </LabelFieldPair>
        </div>
      ) : null}
    </div>
  );
}

export default OBPSV2Documents;