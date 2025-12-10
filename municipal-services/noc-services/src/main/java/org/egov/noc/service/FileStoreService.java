package org.egov.noc.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Service to fetch file URLs from fileStore service
 */
@Slf4j
@Service
public class FileStoreService {

	@Autowired
	private NOCConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * Gets file URL from fileStoreId and tenantId
	 * 
	 * @param fileStoreId File store ID
	 * @param tenantId Tenant ID
	 * @return File URL or null if not found
	 */
	public String getFileUrl(String fileStoreId, String tenantId) {
		if (fileStoreId == null || fileStoreId.isEmpty()) {
			return null;
		}
		try {
			StringBuilder uri = new StringBuilder(config.getFileStoreHost());
			uri.append(config.getFileStorePath());
			uri.append("?tenantId=").append(tenantId);
			uri.append("&fileStoreIds=").append(fileStoreId);

			@SuppressWarnings("unchecked")
			LinkedHashMap<String, Object> response = (LinkedHashMap<String, Object>) 
					serviceRequestRepository.fetchResultUsingGet(uri);

			if (response != null) {
				Object directUrl = response.get(fileStoreId);
				if (directUrl != null) {
					String urlStr = directUrl.toString();
					if (urlStr.contains(",")) {
						urlStr = urlStr.substring(0, urlStr.indexOf(","));
					}
					return urlStr;
				}

				// Fallback to fileStoreIds array
				Object fileStoreIds = response.get("fileStoreIds");
				if (fileStoreIds != null && fileStoreIds instanceof List) {
					@SuppressWarnings("unchecked")
					List<Object> fileList = (List<Object>) fileStoreIds;
					if (!CollectionUtils.isEmpty(fileList)) {
						for (Object fileObj : fileList) {
							if (fileObj instanceof Map) {
								@SuppressWarnings("unchecked")
								Map<String, Object> fileMap = (Map<String, Object>) fileObj;
								Object id = fileMap.get("id");
								if (fileStoreId.equals(id)) {
									Object url = fileMap.get("url");
									if (url != null) {
										String urlStr = url.toString();
										if (urlStr.contains(",")) {
											urlStr = urlStr.substring(0, urlStr.indexOf(","));
										}
										return urlStr;
									}
								}
							}
						}
					}
				}
			}

			return null;
		} catch (Exception e) {
			log.error("Error fetching file URL for fileStoreId {}", fileStoreId, e);
			return null;
		}
	}
}

