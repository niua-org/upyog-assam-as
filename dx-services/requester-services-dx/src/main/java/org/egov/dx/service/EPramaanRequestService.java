package org.egov.dx.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.AESDecrypter;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeChallengeMethod;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCScopeValue;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.dx.repository.EPramaanMapper;
import org.egov.dx.util.Configurations;
import org.egov.dx.web.models.*;
import org.joda.time.DateTimeUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.io.*;
import java.net.URI;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

@Service
@Slf4j
public class EPramaanRequestService {


    private static final User User = null;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Configurations configurations;

    @Autowired
    private EPramaanMapper ePramaanMapper;

    private static Map<String, EPramaanData> stateCodeMap = new HashMap<>();


    public AuthResponse getRedirectionURL(String module) throws Exception
    {

        String authGrantRequestUri = configurations.getEpAuthGrantRequestUri();
        String clientId = configurations.getEpClientId();
        String redirectUri = configurations.getEpRedirectUri();
        String scopeConfig = configurations.getEpScope();
        String responseTypeConfig = configurations.getEpResponseType();
        String codeChallengeMethodConfig = configurations.getEpCodeChallengeMethod();

        validateConfig("epramaan.authGrantRequestUri", authGrantRequestUri);
        validateConfig("epramaan.clientId", clientId);
        validateConfig("epramaan.redirectUri", redirectUri);
        validateConfig("epramaan.scope", scopeConfig);
        validateConfig("epramaan.responseType", responseTypeConfig);
        validateConfig("epramaan.codeChallengeMethod", codeChallengeMethodConfig);

//        TODO : Remove this log after testing
        log.info("Preparing ePramaan authorization request. authGrantRequestUri={}, clientIdHash={}, redirectUri={}, scope={}, responseType={}, codeChallengeMethod={}",
                authGrantRequestUri, clientId, redirectUri, scopeConfig, responseTypeConfig, codeChallengeMethodConfig);

        //1. save codeVerifier, stateID, nonce in db
        State stateID = new State(UUID.randomUUID().toString());
        Nonce nonce = new Nonce();
        CodeVerifier codeVerifier = new CodeVerifier();

//        TODO : Remove this log after testing
        log.debug("Generated PKCE parameters. state={}, nonce={}, codeVerifier={}", stateID.getValue(), nonce.getValue(), codeVerifier.getValue());

        Scope scope = new Scope();
        scope.add(OIDCScopeValue.OPENID);

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest.Builder(URI.create(authGrantRequestUri), new ClientID(clientId))
                        .scope(scope)
                        .state(stateID)
                        .redirectionURI(URI.create(redirectUri))
                        .endpointURI(URI.create(authGrantRequestUri))
                        .codeChallenge(codeVerifier, CodeChallengeMethod.parse(codeChallengeMethodConfig))
                        .nonce(nonce)
                        .responseType(new ResponseType(responseTypeConfig)).build();

        String inputValue = clientId + configurations.getEpAesKey() + stateID + nonce + redirectUri
                + scopeConfig + authenticationRequest.getCodeChallenge();
        String apiHmac = hashHMACHex(inputValue, configurations.getEpAesKey());

//        TODO : Remove this log after testing
        log.debug("Computed apiHmac for state {}: {}", stateID.getValue(), apiHmac);
        String finalUrl = authenticationRequest.toURI().toString() + "&apiHmac=" + apiHmac;
        //return finalUrl;
        UriComponents uriComponents = UriComponentsBuilder
                .fromHttpUrl(finalUrl)
                .build();

        EPramaanData ePramaanData = EPramaanData.builder()
                .codeVerifier(codeVerifier.getValue())
                .nonce(nonce.getValue())
                .state(stateID.getValue())
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .redirectURL(uriComponents.toUri().toString())
                .epramaanData(ePramaanData).build();

        stateCodeMap.put(stateID.getValue(), ePramaanData);
        log.info("Generated ePramaan redirect URL for state={} and module={}", stateID.getValue(), module);
        log.debug("ePramaan redirect URL: {}", authResponse.getRedirectURL());

        return authResponse;
    }

    public URI getCitizenRedirectionURL(String module,AuthResponse authResponse) throws NoSuchAlgorithmException
    {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("response_type", configurations.getResponseType());
        params.add("state", configurations.getState());
        if(module.equalsIgnoreCase("SSO")) {
            params.add("redirect_uri", configurations.getRegisterRedirectURL());
            params.add("client_id", configurations.getRegisterClientId());}

        else {
            params.add("redirect_uri", configurations.getPtRedirectURL());
            params.add("client_id", configurations.getClientId());
        }
        params.add("code_challenge",getCodeChallenge(authResponse));
        params.add("code_challenge_method", "S256");
        params.add("dl_flow", configurations.getDlFlow());
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(configurations.getAuthorizationURL()).queryParams(params)
                .build();



        return uriComponents.toUri();
    }


    private String buildAuthorizationUrl(String baseUrl, Map<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?");

        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                urlBuilder.append("&");
            }
            try {
                urlBuilder.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported", e);
            }
            first = false;
        }

        return urlBuilder.toString();
    }



    private void validateConfig(String propertyName, String value) {
        if (value == null || value.trim().isEmpty()) {
            log.error("Missing or empty configuration value for {}", propertyName);
            throw new IllegalArgumentException("Configuration property " + propertyName + " must not be null or empty");
        }
    }

    private static String hashHMACHex(String inputValue, String hMACKey) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(hMACKey.getBytes(StandardCharsets.US_ASCII), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Base64.getUrlEncoder().encodeToString(sha256_HMAC.doFinal(inputValue.getBytes(StandardCharsets.US_ASCII)));
    }


    private String getCodeChallenge(AuthResponse authResponse) throws NoSuchAlgorithmException
    {
        String codeVerifier=getCodeVerifier();
        log.info("verifier is: " +codeVerifier );
        //authResponse.setDlReqRef(codeVerifier);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
        String encoded = Base64.getEncoder().withoutPadding().encodeToString(hash);
        encoded = encoded.replace("+", "-"); //Replace ’+’ with ’-’
        encoded= encoded.replace("/", "_");
        log.info("challenge is: " +encoded );
        EncReqObject encReqObject = EncReqObject.builder().tenantId("pg").type("Normal").value(codeVerifier).build();
        EncryptionRequest encryptionRequest = EncryptionRequest.builder().encryptionRequests(Collections.singletonList(encReqObject)).build();
        String responseBody= restTemplate.postForEntity(configurations.getEncHost() + configurations.getEncEncryptURL(), encryptionRequest, String.class).getBody();
        try {
            String value = new ObjectMapper().readValue(responseBody, String[].class)[0];
            authResponse.setDlReqRef(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encoded;
    }

    private String getCodeVerifier()
    {
        int leftLimit = 45; // numeral '0'
        int rightLimit = 126; // letter 'z'
        int targetStringLength = 60;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 95) && i!=47 && i!=96 && i!=123 && i!=124 && i!=125)
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    public EPramaanTokenRes getToken(TokenReq tokenReq) {

        EPramaanData ePramaanData = tokenReq.getEPramaanData();

        JSONObject data = new JSONObject();
        data.put("code", new String[] { tokenReq.getCode() });
        data.put("grant_type", new String[] {configurations.getEpGrantType() });
        data.put("scope", new String[] { configurations.getEpScope() });
        data.put("redirect_uri", new String[] {configurations.getEpTokenRequestUri()  });
        data.put("request_uri", new String[] {configurations.getEpRedirectUri() });
        data.put("code_verifier", new String[] { ePramaanData.getCodeVerifier() });
        data.put("client_id", new String[] {configurations.getEpClientId() });
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<String>(data.toString(), headers);
        log.info("Request to ePramaan Token API: " + data.toString());
        ResponseEntity<String> responseData = restTemplate.exchange(configurations.getEpTokenRequestUri(), HttpMethod.POST, entity, String.class);
        log.info("Response from ePramaan Token API: " + responseData.getBody());
        String jweToken = responseData.getBody();
        SecretKeySpec secretKeySpec = null;
        try {
            secretKeySpec = (SecretKeySpec) generateAES256Key(ePramaanData.getNonce());
        } catch (Exception e) {
            log.error("Error in generating AES key: ", e);
            throw new RuntimeException(e);
        }
        JWEObject jweObject = null;
        try {
            jweObject = JWEObject.parse(jweToken);
        } catch (ParseException e) {
            log.error("Error in parsing JWE token: ", e);
            throw new RuntimeException(e);
        }
        try {
            jweObject.decrypt(new AESDecrypter(secretKeySpec));
        } catch (JOSEException e) {
            log.error("Error in decrypting JWE token: ", e);
            throw new RuntimeException(e);
        }
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        JWSVerifier jwsVerifier = null;
        try {
            jwsVerifier = new RSASSAVerifier((RSAPublicKey) getPublicKey());
        } catch (Exception e) {
            log.error("Error in getting public key: ", e);
            throw new RuntimeException(e);
        }
        boolean signatureVerified = false;
        try {
            signatureVerified = signedJWT.verify(jwsVerifier);
        } catch (JOSEException e) {
            log.error("Error in verifying JWS signature: ", e);
            throw new RuntimeException(e);
        }
        Map<String, Object> objectObjectMap = null;
        if (signatureVerified) {
            Map<String, Object> JWS = signedJWT.getPayload().toJSONObject();
            System.out.println("JWT: " + JWS);
            objectObjectMap = signedJWT.getPayload().toJSONObject();
            log.info("Token Response from ePramaan: " + objectObjectMap);
        }
        if(objectObjectMap == null) {
            throw new RuntimeException("Invalid ePramaan token");
        }
        EPramaanTokenRes ePramaanTokenRes = ePramaanMapper.mapClaimsToResponse(objectObjectMap);
        return ePramaanTokenRes;
    }

    public Key generateAES256Key(String seed) throws Exception {
        MessageDigest sha256 = null;
        sha256 = MessageDigest.getInstance("SHA-256");
        byte[] passBytes = seed.getBytes();
        byte[] passHash = sha256.digest(passBytes);
        SecretKeySpec secretKeySpec = new SecretKeySpec(passHash, "AES");
        return secretKeySpec;
    }

    /**
     * Get public key from certificate for JWT signature verification
     * Certificate value is read from application.properties
     * 
     * @return PublicKey for JWT verification
     * @throws Exception if certificate parsing fails
     */
    public PublicKey getPublicKey() throws Exception {
        String certificateValue = configurations.getEpCertificateValue();
        if (certificateValue == null || certificateValue.isEmpty()) {
            throw new IllegalArgumentException("epramaan.certificate.value is not configured");
        }

        String normalizedCertificate = certificateValue.replace("\\n", "\n");
        CertificateFactory certFac = CertificateFactory.getInstance("X.509");
        try (ByteArrayInputStream certStream = new ByteArrayInputStream(normalizedCertificate.getBytes(StandardCharsets.UTF_8))) {
            X509Certificate cer = (X509Certificate) certFac.generateCertificate(certStream);
            return cer.getPublicKey();
        }
    }


    public UserRes getUser(TokenReq tokenReq)
    {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
                headers);
        UserRes userRes= restTemplate.postForEntity(configurations.getApiHost() + configurations.getUserOauthURI(), request, UserRes.class).getBody();
        return userRes;
    }

    public  List<IssuedDocument> getIssuedDocument(TokenReq tokenReq)
    {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
                headers);
        IssuedDocumentList issuedDocumentList= restTemplate.postForEntity(configurations.getApiHost() + configurations.getIssuedFilesURI(), request, IssuedDocumentList.class).getBody();
        return issuedDocumentList.getItems();
    }
    public byte[] getDoc(TokenReq tokenReq,String uri)
    {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());
        headers.set("Authorization", "Bearer "+tokenReq.getAuthToken());
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();

        map.add("uri",uri);


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(null,
                headers);
        //MultipartFile doc= restTemplate.getForEntity(configurations.getApiHost() + configurations.getGetFileURI()+"/"+uri, request);
        ResponseEntity<String> entity = restTemplate.exchange(configurations.getApiHost() + configurations.getGetFileURI()+"/"+uri, HttpMethod.GET,
                request, String.class);


        return entity.getBody().getBytes();
    }

    public Object getOauthToken(RequestInfo requestinfo , EPramaanTokenRes tokenRes)
    {
        UserRequest user = new UserRequest();
        user.setMobileNumber(tokenRes.getMobileNumber());
        user.setName(tokenRes.getName());
        
        // Set generic SSO fields
        user.setSsoId(tokenRes.getEpramaanId());
        user.setSsoType("EPRAMAAN"); // Set SSO type as EPRAMAAN
        
        // Also set digilockerid for backward compatibility
        user.setDigilockerid(tokenRes.getEpramaanId());
        user.setTenantId(configurations.getStateLevelTenantId());
        user.setAccess_token(tokenRes.getSessionId());
        //user.setDob(tokenRes.getDob());

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setRequestInfo(requestinfo);
        createUserRequest.setUser(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Use the new generic SSO endpoint with ssoType parameter
        String url = configurations.getUserHost() + configurations.getUserSsoEndpoint() + "?ssoType=EPRAMAAN";
        log.info("Calling SSO endpoint: {}", url);
        
        // Use autowired restTemplate for user service call (typically HTTP, not HTTPS)
        Object userOauth = this.restTemplate.postForEntity(url, createUserRequest, Object.class).getBody();
        log.info("Received user object from user service: {}", userOauth.toString());
        return userOauth;
    }

    public HttpEntity<String> decryptReq(List<String> decReqObject){
        HttpHeaders decryptHeaders = new HttpHeaders();
        decryptHeaders.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = null;
        try {
            jsonPayload = objectMapper.writeValueAsString(decReqObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new HttpEntity<String>(jsonPayload, decryptHeaders);

    }

    public Object getToken(@Valid EparmaanRequest eparmaanRequest) {
        String stateId = eparmaanRequest.getState();
        log.info("State ID received in callback: " + stateId);
        
        // Retrieve stored ePramaanData using state
        EPramaanData ePramaanData = stateCodeMap.get(stateId);
        
        // Validate that state exists
        if (ePramaanData == null) {
            log.error("No ePramaanData found for state: {}. State may have expired or is invalid.", stateId);
            throw new RuntimeException("Invalid state parameter - session expired or not found. Please try logging in again.");
        }
        
        log.info("Found ePramaanData for state. Code verifier and nonce retrieved successfully.");
        
        // Build token request with code and stored PKCE parameters
        TokenReq tokenReq = TokenReq.builder()
                .code(eparmaanRequest.getCode())
                .ePramaanData(ePramaanData)
                .build();

        // Exchange authorization code for ePramaan token
        EPramaanTokenRes tokenRes = getToken(tokenReq);
        log.info("Successfully retrieved ePramaan token for user: {}", tokenRes.getName());
        
        // Create default RequestInfo for callback scenario (no user session yet)
        RequestInfo requestInfo = createDefaultRequestInfo();
        
        // Call user service to create/update user and get OAuth token
        Object user = getOauthToken(requestInfo, tokenRes);
        
        // Clean up state from map after successful token exchange
        stateCodeMap.remove(stateId);
        log.info("State cleaned up from memory. User authentication completed.");
        
        return user;
    }

    /**
     * Generate ePramaan logout form data for frontend to submit
     * As per ePramaan SLO specification, frontend submits form with "data" key containing JSON
     * 
     * @param sessionId - ePramaan session ID from JWT token
     * @param sub - Subject from JWT token (ePramaan user identifier)
     * @param tenantId - Tenant ID
     * @return EPramaanLogoutFormData containing all fields for form submission
     */
    public EPramaanLogoutFormData generateEPramaanLogoutFormData(String sessionId, String sub, String tenantId) throws Exception {
        String logoutRequestId = UUID.randomUUID().toString();
        String clientId = configurations.getEpClientId();
        String iss = configurations.getEpIss();
        String redirectUrl = configurations.getEpServiceLogoutUri();
        String customParameter = "UPYOG-Logout";
        
        log.info("ePramaan logout clientId: [{}], sessionId: [{}], iss: [{}], logoutRequestId: [{}], sub: [{}], redirectUrl: [{}]", clientId, sessionId, iss, logoutRequestId, sub, redirectUrl);
        // Generate HMAC: input = clientId + sessionId + iss + logoutRequestId + sub + redirectUrl, key = logoutRequestId
        String inputValue = clientId + sessionId + iss + logoutRequestId + sub + redirectUrl;
        String hmac = hashHMACHex(inputValue, logoutRequestId);

        return EPramaanLogoutFormData.builder()
            .sub(sub)
            .clientId(clientId)
            .redirectUrl(redirectUrl)
            .logoutRequestId(logoutRequestId)
            .hmac(hmac)
            .iss(iss)
            .customParameter(customParameter)
            .sessionId(sessionId)
            .build();
    }

    /**
     * Creates a default RequestInfo for SSO callback scenarios
     * where no user session exists yet
     * 
     * @return RequestInfo with default values
     */
    private RequestInfo createDefaultRequestInfo() {
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setApiId("epramaan-callback");
        requestInfo.setVer("1.0");
        requestInfo.setTs(Long.valueOf(DateTimeUtils.currentTimeMillis()));
        requestInfo.setAction("create");
        requestInfo.setMsgId(UUID.randomUUID().toString());
        log.debug("Created default RequestInfo for callback with msgId: {}", requestInfo.getMsgId());
        return requestInfo;
    }

    /**
     * Add ePramaan session info (sessionId and sub) to the OAuth response
     * This allows frontend to store these values for later use in logout
     * 
     * @param userOauthResponse - OAuth response from user service
     * @param tokenRes - ePramaan token response containing sessionId and sub
     * @return Modified OAuth response with sessionId and sub added
     */
    @SuppressWarnings("unchecked")
    public Object addEPramaanUserSessionInfo(Object userOauthResponse, EPramaanTokenRes tokenRes) {
        try {
            log.info("Adding ePramaan session info (sessionId and sub) to OAuth response");
            
            if (userOauthResponse instanceof Map) {
                Map<String, Object> responseMap = (Map<String, Object>) userOauthResponse;
                
                responseMap.put("sessionId", tokenRes.getSessionId());
                responseMap.put("sub", tokenRes.getSub());
                
                log.info("Successfully added - sessionId: {}, sub: {}", 
                    tokenRes.getSessionId(), tokenRes.getSub());
                
                return responseMap;
            } else {
                log.warn("OAuth response is not a Map (Type: {}), cannot add ePramaan session info", 
                    userOauthResponse.getClass().getName());
                return userOauthResponse;
            }
            
        } catch (Exception e) {
            log.error("Error adding ePramaan session info to user object: {}", e.getMessage(), e);
            log.warn("Returning original OAuth response. Frontend won't have sessionId/sub for logout.");
            return userOauthResponse;
        }
    }
}
