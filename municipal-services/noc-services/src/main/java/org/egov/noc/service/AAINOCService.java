package org.egov.noc.service;

import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.egov.noc.web.model.BpaApplication;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.SiteCoordinate;
import org.egov.noc.web.model.bpa.BPA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AAINOCService {

	@Value("${nocas.authority.name:BPA}")
	private String authorityName;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

//    @Autowired
//    private SiteCoordinateRepository coordinateRepository;

	@Autowired
	private NOCService nocService;

	public String generateNocasXml() {
		try {
			// Fetch all applications in CREATED status
			List<BpaApplication> applications = getCreatedApplications();
			log.info("No. of new NOC applications sent: " + applications.size());

			// Create XML document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Root element
			Element rootElement = doc.createElement(authorityName + "DETAILS");
			doc.appendChild(rootElement);

			// Add each application
			for (BpaApplication app : applications) {
				Element toAAI = doc.createElement("TOAAI");
				rootElement.appendChild(toAAI);

				// Application Data
				Element appData = doc.createElement("ApplicationData");
				toAAI.appendChild(appData);

				addElement(doc, appData, "AUTHORITY", authorityName);
				addElement(doc, appData, "UNIQUEID", app.getUniqueId());
				addElement(doc, appData, "APPLICATIONDATE", app.getApplicationDate());
				addElement(doc, appData, "APPLICANTNAME", app.getApplicantName());
				addElement(doc, appData, "APPLICANTADDRESS", app.getApplicantAddress());
				addElement(doc, appData, "APPLICANTNO", app.getApplicantContact());
				addElement(doc, appData, "APPLICANTEMAIL", app.getApplicantEmail());
				addElement(doc, appData, "APPLICATIONNO", app.getApplicationNo());
				addElement(doc, appData, "OWNERNAME", app.getOwnerName());
				addElement(doc, appData, "OWNERADDRESS", app.getOwnerAddress());
				addElement(doc, appData, "STRUCTURETYPE", app.getStructureType());
				addElement(doc, appData, "STRUCTUREPURPOSE", app.getStructurePurpose());
				addElement(doc, appData, "SITEADDRESS", app.getSiteAddress());
				addElement(doc, appData, "SITECITY", app.getSiteCity());
				addElement(doc, appData, "SITESTATE", app.getSiteState());
				addElement(doc, appData, "PLOTSIZE", String.valueOf(app.getPlotSize()));
				addElement(doc, appData, "ISINAIRPORTPREMISES", app.getIsInAirportPremises());
				addElement(doc, appData, "PERMISSIONTAKEN", app.getPermissionTaken());

				// Site Details (Coordinates)
				// TO_BE_CHANGED
				List<SiteCoordinate> coordinates = null;

				Element siteDetails = doc.createElement("SiteDetails");
				toAAI.appendChild(siteDetails);

				for (SiteCoordinate coord : coordinates) {
					Element coordElement = doc.createElement("Coordinates");
					siteDetails.appendChild(coordElement);

					addElement(doc, coordElement, "LATITUDE", coord.getLatitude());
					addElement(doc, coordElement, "LONGITUDE", coord.getLongitude());
					addElement(doc, coordElement, "SITEELEVATION", String.valueOf(coord.getSiteElevation()));
					addElement(doc, coordElement, "BUILDINGHEIGHT", String.valueOf(coord.getBuildingHeight()));
					addElement(doc, coordElement, "STRUCTURENO", String.valueOf(coord.getStructureNo()));
				}

				// Files (Document paths)
				Element files = doc.createElement("FILES");
				toAAI.appendChild(files);

				addElement(doc, files, "UNDERTAKING1A", app.getUniqueId() + "_UNDERTAKING1A.pdf");
				addElement(doc, files, "SITEELEVATION", app.getUniqueId() + "_SiteElevationCertificate.pdf");
				addElement(doc, files, "SITECORDINATES", app.getUniqueId() + "_SiteCoordinatesCertificate.pdf");
				addElement(doc, files, "AUTHORIZATION", app.getUniqueId() + "_AuthorizationLetter.pdf");
				if ("Yes".equalsIgnoreCase(app.getIsInAirportPremises())) {
					addElement(doc, files, "PERMISSION", app.getUniqueId() + "_PermissionLetter.pdf");
				}
			}

			// Convert Document to String
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));

			return writer.toString();

		} catch (Exception e) {
			throw new RuntimeException("Error generating NOCAS XML", e);
		}
	}

	private void addElement(Document doc, Element parent, String tagName, String value) {
		Element element = doc.createElement(tagName);
		element.appendChild(doc.createTextNode(value != null ? value : ""));
		parent.appendChild(element);
	}

	public List<BpaApplication> getCreatedApplications() {

		List<BpaApplication> result = new ArrayList<>();
		List<Noc> nocList = nocService.fetchNewAAINOCs();
		List<BPA> bpaDetails = nocService.getBPADetails(nocList, null);

		for (BPA bpa : bpaDetails) {
			BpaApplication obj = new BpaApplication();
			obj.setUniqueId(bpa.getApplicationNo());
			Long appDate = bpa.getApplicationDate();
			obj.setApplicationDate(appDate != null ? String.valueOf(appDate) : null);
			obj.setApplicantName(bpa.getLandInfo().getOwners().get(0).getName());
			obj.setApplicantAddress(bpa.getLandInfo().getOwners().get(0).getName());
			obj.setApplicantContact(bpa.getLandInfo().getOwners().get(0).getName());
			obj.setApplicantEmail(bpa.getLandInfo().getOwners().get(0).getEmailId());
			obj.setApplicationNo(bpa.getApplicationNo());
			obj.setOwnerName(bpa.getLandInfo().getOwners().get(0).getName());
			obj.setOwnerAddress(bpa.getLandInfo().getOwners().get(0).getPermanentAddress());
			obj.setStructureType("");
			obj.setStructurePurpose("");
			obj.setSiteAddress(bpa.getLandInfo().getOwners().get(0).getPermanentAddress());
			obj.setSiteCity(bpa.getLandInfo().getOwners().get(0).getPermanentCity());
			obj.setSiteState("");
			obj.setPlotSize(null);
			obj.setIsInAirportPremises("");
			obj.setPermissionTaken("");
			result.add(obj);
		}

		return result;

	}
}
