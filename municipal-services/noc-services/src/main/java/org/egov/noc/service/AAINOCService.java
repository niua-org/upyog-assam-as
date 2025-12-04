package org.egov.noc.service;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.egov.common.contract.request.RequestInfo;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.util.NOCUtil;
import org.egov.noc.web.model.BpaApplication;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.RequestInfoWrapper;
import org.egov.noc.web.model.SiteCoordinate;
import org.egov.noc.web.model.bpa.Address;
import org.egov.noc.web.model.bpa.BPA;
import org.egov.noc.web.model.bpa.GeoLocation;
import org.egov.noc.web.model.bpa.LandInfo;
import org.egov.noc.web.model.bpa.OwnerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AAINOCService {

	@Autowired
	private NOCConfiguration config;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");

	@Autowired
	private NOCService nocService;

	@Autowired
	private NOCUtil nocUtil;

	/**
	 * Generates NOCAS XML response by fetching newly created NOC applications
	 * and their corresponding BPA details. Maps data to AAI NOCAS XML format.
	 * 
	 * @param requestInfo Authenticated request information
	 * @param tenantId Tenant ID (optional, defaults to 'as')
	 * @return XML string containing BPA application details
	 */
	public String generateNocasXml(RequestInfo requestInfo, String tenantId) {

		try {
			List<Noc> nocList = nocService.fetchNewAAINOCs(tenantId);

			if (CollectionUtils.isEmpty(nocList)) {
				log.info("No new AAI NOC applications found");
				return createEmptyResponse(config.getAuthorityName());
			}

			RequestInfoWrapper requestInfoWrapper =	new RequestInfoWrapper();
			requestInfoWrapper.setRequestInfo(requestInfo);

			// Fetch BPA details for the NOC applications
			List<BPA> bpaDetails = nocService.getBPADetails(nocList, requestInfoWrapper);
			
			Map<String, BPA> bpaMap = new HashMap<>();
			for (BPA bpa : bpaDetails) {
				if (bpa != null && bpa.getApplicationNo() != null) {
					bpaMap.put(bpa.getApplicationNo(), bpa);
				}
			}
			
			List<BpaApplication> applications = new ArrayList<>();
			for (BPA bpa : bpaDetails) {
				try {
					BpaApplication obj = mapBPAToBpaApplication(bpa);
					if (obj != null) {
						applications.add(obj);
					}
				} catch (Exception e) {
					log.error("Error mapping BPA {}: {}", 
							bpa != null ? bpa.getApplicationNo() : "null", e.getMessage());
				}
			}
			
			log.info("No. of new NOC applications sent: " + applications.size());

			// Create XML document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Root element
			Element rootElement = doc.createElement(config.getAuthorityName() + "DETAILS");
			doc.appendChild(rootElement);

			// Add each application
			for (BpaApplication app : applications) {
				Element toAAI = doc.createElement("TOAAI");
				rootElement.appendChild(toAAI);

				// Application Data
				Element appData = doc.createElement("ApplicationData");
				toAAI.appendChild(appData);

				addElement(doc, appData, "AUTHORITY", config.getAuthorityName());
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
				BPA correspondingBPA = bpaMap.get(app.getApplicationNo());
				List<SiteCoordinate> coordinates = extractCoordinatesFromBPA(correspondingBPA);

				Element siteDetails = doc.createElement("SiteDetails");
				toAAI.appendChild(siteDetails);

				if (!coordinates.isEmpty()) {
					for (SiteCoordinate coord : coordinates) {
						Element coordElement = doc.createElement("Coordinates");
						siteDetails.appendChild(coordElement);

						addElement(doc, coordElement, "LATITUDE", 
								coord.getLatitude() != null ? coord.getLatitude() : "");
						addElement(doc, coordElement, "LONGITUDE", 
								coord.getLongitude() != null ? coord.getLongitude() : "");
						addElement(doc, coordElement, "SITEELEVATION", 
								coord.getSiteElevation() != null ? String.valueOf(coord.getSiteElevation()) : "");
						addElement(doc, coordElement, "BUILDINGHEIGHT", 
								coord.getBuildingHeight() != null ? String.valueOf(coord.getBuildingHeight()) : "");
						addElement(doc, coordElement, "STRUCTURENO", 
								coord.getStructureNo() != null ? String.valueOf(coord.getStructureNo()) : "");
					}
				}

				// Files (Document paths)
				Element files = doc.createElement("FILES");
				toAAI.appendChild(files);

				addElement(doc, files, "UNDERTAKING1A", config.getAuthorityPlaceholderFileUrl());
				addElement(doc, files, "SITEELEVATION",  "");
				addElement(doc, files, "SITECORDINATES",  "");
				addElement(doc, files, "AUTHORIZATION",  "");

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

	/**
	 * Adds XML element with tag and value to parent element
	 * 
	 * @param doc XML document
	 * @param parent Parent element
	 * @param tagName Element tag name
	 * @param value Element value
	 */
	private void addElement(Document doc, Element parent, String tagName, String value) {
		Element element = doc.createElement(tagName);
		element.appendChild(doc.createTextNode(value != null ? value : ""));
		parent.appendChild(element);
	}

//	/**
//	 * Fetches NOC applications in CREATED status and maps to BpaApplication objects
//	 *
//	 * @return List of BpaApplication objects
//	 */
//	public List<BpaApplication> getCreatedApplications() {
//
//		List<BpaApplication> result = new ArrayList<>();
//		List<Noc> nocList = nocService.fetchNewAAINOCs(null);
//
//		if (CollectionUtils.isEmpty(nocList)) {
//			log.info("No new AAI NOC applications found");
//			return result;
//		}
//
//		List<BPA> bpaDetails = nocService.getBPADetails(nocList, nocUtil.createDefaultRequestInfo());
//
//		for (BPA bpa : bpaDetails) {
//			try {
//				BpaApplication obj = mapBPAToBpaApplication(bpa);
//				if (obj != null) {
//					result.add(obj);
//				}
//			} catch (Exception e) {
//				log.error("Error mapping BPA {} to BpaApplication: {}",
//						bpa.getApplicationNo(), e.getMessage(), e);
//				// Continue processing other applications even if one fails
//			}
//		}
//
//		return result;
//
//	}

	/**
	 * Maps BPA to BpaApplication with null safety and data formatting
	 * 
	 * @param bpa BPA object
	 * @return BpaApplication object or null if data missing
	 */
	private BpaApplication mapBPAToBpaApplication(BPA bpa) {
		if (bpa == null || bpa.getApplicationNo() == null) {
			return null;
		}

		BpaApplication obj = new BpaApplication();
		obj.setUniqueId(bpa.getApplicationNo());
		obj.setApplicationNo(bpa.getApplicationNo());

		Long appDate = bpa.getApplicationDate();
		if (appDate != null && appDate > 0) {
			LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(appDate), ZoneId.systemDefault());
			obj.setApplicationDate(dateTime.format(DATE_FORMATTER));
		} else {
			obj.setApplicationDate(LocalDateTime.now().format(DATE_FORMATTER));
		}

		LandInfo landInfo = bpa.getLandInfo();
		if (landInfo != null && !CollectionUtils.isEmpty(landInfo.getOwners())) {
			OwnerInfo owner = landInfo.getOwners().get(0);
			obj.setApplicantName(owner.getName() != null ? owner.getName() : "");
			obj.setOwnerName(owner.getName() != null ? owner.getName() : "");
			obj.setApplicantContact(owner.getMobileNumber() != null ? owner.getMobileNumber() : "");
			obj.setApplicantEmail(owner.getEmailId() != null ? owner.getEmailId() : "");

			Address permanentAddr = owner.getPermanentAddress();
			String formattedAddr = permanentAddr != null ? formatAddress(permanentAddr) : "";
			obj.setApplicantAddress(formattedAddr);
			obj.setOwnerAddress(formattedAddr);
		}

		if (landInfo != null) {
			Address landAddress = landInfo.getAddress();
			if (landAddress != null) {
				obj.setSiteAddress(formatAddress(landAddress));
				obj.setSiteCity(landAddress.getCity() != null ? landAddress.getCity() : "");
				obj.setSiteState(landAddress.getState() != null ? landAddress.getState() : "");
			}

			obj.setPlotSize(landInfo.getTotalPlotArea() != null ? landInfo.getTotalPlotArea().doubleValue() : 0.0);
		}

		obj.setStructureType(extractFromAdditionalDetails(bpa, "structureType", "Building"));
        String OccupancyType = landInfo.getUnits().get(0).getOccupancyType();
		obj.setStructurePurpose(OccupancyType != null ? OccupancyType : "Residential");
		obj.setIsInAirportPremises(extractFromAdditionalDetails(bpa, "isInAirportPremises", "No"));
		obj.setPermissionTaken(extractFromAdditionalDetails(bpa, "permissionTaken", "No"));

		return obj;
	}

	/**
	 * Formats Address object to readable string
	 * 
	 * @param address Address object
	 * @return Formatted address
	 */
	private String formatAddress(Address address) {
		if (address == null) {
			return "";
		}

		StringBuilder addr = new StringBuilder();
		
		if (!StringUtils.isEmpty(address.getDoorNo())) {
			addr.append(address.getDoorNo()).append(", ");
		}
		if (!StringUtils.isEmpty(address.getPlotNo())) {
			addr.append("Plot No. ").append(address.getPlotNo()).append(", ");
		}
		if (!StringUtils.isEmpty(address.getStreet())) {
			addr.append(address.getStreet()).append(", ");
		}
		if (!StringUtils.isEmpty(address.getBuildingName())) {
			addr.append(address.getBuildingName()).append(", ");
		}
		if (address.getLocality() != null && !StringUtils.isEmpty(address.getLocality().getName())) {
			addr.append(address.getLocality().getName()).append(", ");
		}
		if (!StringUtils.isEmpty(address.getCity())) {
			addr.append(address.getCity()).append(", ");
		}
		if (!StringUtils.isEmpty(address.getDistrict())) {
			addr.append(address.getDistrict()).append(", ");
		}
		if (!StringUtils.isEmpty(address.getState())) {
			addr.append(address.getState());
		}
		if (!StringUtils.isEmpty(address.getPincode())) {
			addr.append(" - ").append(address.getPincode());
		}

		return addr.toString().replaceAll(", $", "").trim();
	}

	/**
	 * Extracts value from BPA additionalDetails
	 * 
	 * @param bpa BPA object
	 * @param key Key to extract
	 * @param defaultValue Default if not found
	 * @return Extracted value or default
	 */
	@SuppressWarnings("unchecked")
	private String extractFromAdditionalDetails(BPA bpa, String key, String defaultValue) {
		if (bpa == null || bpa.getAdditionalDetails() == null) {
			return defaultValue;
		}

		try {
			if (bpa.getAdditionalDetails() instanceof java.util.Map) {
				java.util.Map<String, Object> additionalDetails = (java.util.Map<String, Object>) bpa.getAdditionalDetails();
				Object value = additionalDetails.get(key);
				return value != null ? String.valueOf(value) : defaultValue;
			}
		} catch (Exception e) {
			log.warn("Error extracting {} from additionalDetails: {}", key, e.getMessage());
		}

		return defaultValue;
	}

	/**
	 * Creates empty XML response
	 * 
	 * @param authorityName Authority name
	 * @return Empty XML
	 */
	private String createEmptyResponse(String authorityName) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			
			Element rootElement = doc.createElement(authorityName + "DETAILS");
			doc.appendChild(rootElement);
			
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(doc), new StreamResult(writer));
			
			return writer.toString();
		} catch (Exception e) {
			log.error("Error creating empty response", e);
			return "<" + authorityName + "DETAILS></" + authorityName + "DETAILS>";
		}
	}

	/**
	 * Extracts coordinates from BPA GeoLocation
	 * 
	 * @param bpa BPA object
	 * @return List of coordinates
	 */
	private List<SiteCoordinate> extractCoordinatesFromBPA(BPA bpa) {
		List<SiteCoordinate> coordinates = new ArrayList<>();

		/*if (bpa == null || bpa.getLandInfo() == null || bpa.getLandInfo().getAddress() == null) {
			return coordinates;
		}*/

		/*GeoLocation geoLocation = bpa.getLandInfo().getAddress().getGeoLocation();
		if (geoLocation != null && geoLocation.getLatitude() != null && geoLocation.getLongitude() != null) {
			SiteCoordinate coord = SiteCoordinate.builder()
					.latitude(String.valueOf(geoLocation.getLatitude()))
					.longitude(String.valueOf(geoLocation.getLongitude()))
					.siteElevation(null)
					.buildingHeight(null)
					.structureNo(1)
					.build();
		}*/

		SiteCoordinate coord = SiteCoordinate.builder()
				.latitude("26 26 27.9")
				.longitude("91 26 27.6")
				.siteElevation(42.0)
				.buildingHeight(12.0)
				.structureNo(1)
				.build();
		coordinates.add(coord);
		return coordinates;
	}

}
