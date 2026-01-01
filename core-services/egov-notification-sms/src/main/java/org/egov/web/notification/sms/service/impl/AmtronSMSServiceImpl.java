package org.egov.web.notification.sms.service.impl;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.egov.web.notification.sms.models.Sms;
import org.egov.web.notification.sms.service.BaseSMSService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * {@code AmtronSMSServiceImpl} is a concrete implementation of {@link BaseSMSService}
 * responsible for sending SMS messages through the <b>AMTRON SMS Gateway</b>.
 * </p>
 *
 * <p>
 * This implementation is specifically designed to comply with
 * <b>DLT (Distributed Ledger Technology)</b> regulations enforced by Indian telecom operators.
 * AMTRON requires:
 * </p>
 *
 * <ul>
 *   <li>HTTP <b>GET</b> based API invocation</li>
 *   <li>Exact message content matching the registered DLT template</li>
 *   <li>Strict control over URL encoding to avoid message hash mismatch</li>
 * </ul>
 *
 * <p>
 * To prevent double-encoding issues (which can cause silent SMS drops by DLT),
 * this class encodes only the <b>message parameter</b> explicitly and
 * builds the final URL without additional encoding.
 * </p>
 *
 * <p>
 * This service is activated only when the configuration property
 * {@code sms.provider.class=AMTRON} is set.
 * </p>
 *
 * @author
 * UPYOG Platform Team
 */

@Service
@Slf4j
@ConditionalOnProperty(value = "sms.provider.class", havingValue = "AMTRON")
public class AmtronSMSServiceImpl extends BaseSMSService {

    @Value("${sms.url.dont_encode_url:true}")
    private boolean dontEncodeURL;

    @Override
    protected void submitToExternalSmsService(Sms sms) {

        try {
            String baseUrl = smsProperties.getUrl();
            MultiValueMap<String, String> queryParams = getSmsRequestBody(sms);

            String originalMsg = queryParams.getFirst("msg");

            if (originalMsg != null && originalMsg.contains("##")) {

                String[] parts = originalMsg.split("##", 2);
                String cleanMessage = parts[0];
                String extractedTemplateId = parts[1];

             
                String templateParamKey = null;
                for (String key : smsProperties.getConfigMap().keySet()) {
                    if ("$templateid".equals(smsProperties.getConfigMap().get(key))) {
                        templateParamKey = key;
                        break;
                    }
                }

                if (templateParamKey != null) {
                    queryParams.set(templateParamKey, extractedTemplateId);
                }

                queryParams.set("msg", cleanMessage);
            }

            // Encode ONLY message
            String finalMsg = queryParams.getFirst("msg");
            if (finalMsg != null) {
                queryParams.set(
                    "msg",
                    URLEncoder.encode(finalMsg, StandardCharsets.UTF_8.name())
                );
            }

            URI finalUri = URI.create(
                    UriComponentsBuilder.fromHttpUrl(baseUrl)
                            .queryParams(queryParams)
                            .build(false)
                            .toUriString()
            );

            log.info("AMTRON SMS URL => {}", finalUri);

            executeAPI(finalUri, HttpMethod.GET, null, String.class);

        } catch (Exception e) {
            log.error("AMTRON SMS sending failed for mobile {}", sms.getMobileNumber(), e);
            throw new RuntimeException(e);
        }
    }
}

