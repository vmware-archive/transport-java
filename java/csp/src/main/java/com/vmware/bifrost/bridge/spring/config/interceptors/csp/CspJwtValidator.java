/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.spring.config.interceptors.JwtValidator;
import com.vmware.bifrost.core.util.Loggable;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * CSP JSON Web Token validator implementation.
 */
public class CspJwtValidator extends Loggable implements JwtValidator {
    private final CspEnvironment cspEnvironment;
    private final Map<CspEnvironment, String> cspUrlsMap;
    private volatile PublicKey accessTokenPublicKey;
    private Base64.Decoder base64Decoder;
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private Configuration config;

    private static final String PROPERTY_KEY_CSP_DEV_GATEWAY_URL = "csp.dev.gateway.url";
    private static final String PROPERTY_KEY_CSP_STG_GATEWAY_URL = "csp.stg.gateway.url";
    private static final String PROPERTY_KEY_CSP_PRD_GATEWAY_URL = "csp.prd.gateway.url";
    private static final String PROPERTY_KEY_TOKEN_PUBLIC_KEY_URI = "accessTokenPublicKeyUri";

    public CspJwtValidator(CspEnvironment cspEnv) {
        this(cspEnv, "bifrost-csp.properties");
    }

    public CspJwtValidator(CspEnvironment cspEnv, String cspPropertiesFilename) {
        base64Decoder = Base64.getDecoder();
        objectMapper = new ObjectMapper();
        cspEnvironment = cspEnv;
        restTemplate = new RestTemplate();

        // instead of forcing the user to handle throw/catch for ConfigurationException,
        // we would rather throw a null pointer if something goes wrong and
        // bifrost-csp.properties is not found.
        try {
            config = new PropertiesConfiguration(cspPropertiesFilename);
        } catch (ConfigurationException e) {
            logErrorMessage("bifrost-csp properties not found!", e.getMessage());
        }

        Map<CspEnvironment, String> map = new HashMap<CspEnvironment, String>() {
            {
                put(CspEnvironment.DEVELOPMENT, config.getString(PROPERTY_KEY_CSP_DEV_GATEWAY_URL));
                put(CspEnvironment.STAGING, config.getString(PROPERTY_KEY_CSP_STG_GATEWAY_URL));
                put(CspEnvironment.PRODUCTION, config.getString(PROPERTY_KEY_CSP_PRD_GATEWAY_URL));
            }
        };
        this.cspUrlsMap = Collections.unmodifiableMap(map);
    }

    private String getTokenPublicKeyUrl() throws URISyntaxException {
        String fullPath = String.format("%s%s", cspUrlsMap.get(cspEnvironment),
                config.getString(PROPERTY_KEY_TOKEN_PUBLIC_KEY_URI));
        URI uri = new URI(fullPath);
        return uri.toString();
    }

    /**
     * * Fetch jwt signing key for verifying tokens.
     * */
    private PublicKey getJwtPublicKey() throws InvalidKeySpecException, NoSuchAlgorithmException, URISyntaxException {
        // use cached public key
        if (accessTokenPublicKey != null) {
            return accessTokenPublicKey;
        }

        String uri = getTokenPublicKeyUrl();

        synchronized (this) {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<CspTokenPublicKeyResponse> exchange =
                    restTemplate.exchange(uri, HttpMethod.GET, httpEntity, CspTokenPublicKeyResponse.class);
            String publicKeyPem = exchange.getBody().getValue();
            publicKeyPem = publicKeyPem.replace("-----BEGIN PUBLIC KEY-----\n", "");
            publicKeyPem = publicKeyPem.replace("\n-----END PUBLIC KEY-----", "");
            logWarnMessage("Retrieved public key from CSP alg " + exchange.getBody().getAlg() + ", issuer " +
                    exchange.getBody().getIssuer());

            X509EncodedKeySpec spec = new X509EncodedKeySpec(base64Decoder.decode(publicKeyPem));
            KeyFactory kf = KeyFactory.getInstance("RSA");
            accessTokenPublicKey = kf.generatePublic(spec);
        }

        return accessTokenPublicKey;
    }

    /**
     * Return true if token is not null.
     * @param token access token
     * @return error message. null if no error is found
     */
    @Override
    public String checkIfPresent(String token) {
        if (token == null || token.isEmpty()) {
            return "Token is empty";
        }
        return null;
    }

    /**
     * Return true if token is not expired.
     * @param token access token
     * @return error message. null if no error is found
     */
    @Override
    public String checkIfNotExpired(String token) {
        Map parsed = null;
        try {
            String body = token.split("\\.")[1];
            body = new String(base64Decoder.decode(body), StandardCharsets.UTF_8);
            parsed = objectMapper.readValue(body, Map.class);
        } catch (IOException | IllegalArgumentException e) {
            logWarnMessage("parsing failed for token: " + token);
            parsed = new HashMap();
        }

        if (parsed.get("exp") == null) {
            logWarnMessage("Could not find the JWT expiry: " + token);
            return "Token is malformed. (exp missing)";
        }

        Long now = new Date().getTime();
        Long exp = ((long) (Integer) parsed.get("exp")) * 1000;

        if (exp.compareTo(now) < 0) {
            return "Token expired";
        }
        return null;
    }

    /**
     * Return true if token is parsable.
     * @param token access token.
     * @return error message. null if no error is found
     */
    @Override
    public String checkIfDecodable(String token) {
        String[] segments = token.split("\\.");
        if (segments.length != 3) {
            return "Token malformed";
        }

        try {
            for (int i = 0; i < 2; i++) {
                base64Decoder.decode(segments[i]);
            }

        } catch (IllegalArgumentException e) {
            logWarnMessage("Could not decode the token: " + token);
            return "Token malformed";
        }

        return null;
    }

    /**
     * Return true if token can be validated using public key.
     * @param token access token
     * @return error message. null if no error is found
     */
    @Override
    public String checkIfNotCompromised(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getJwtPublicKey())
                    .parseClaimsJws(token)
                    .getBody();
            logDebugMessage("Token is intact. Expires at: " + claims.getExpiration());
        } catch (Exception e) {
            logWarnMessage(e.getMessage());
            return "Token is compromised. " + e.getMessage();
        }

        return null;
    }

    /**
     * Return true if token exists, decodable, is not expired and not compromised.
     * @param token access token
     * @return token validation results
     */
    @Override
    public TokenValidationResults getValidationResults(String token) {
        TokenValidationResults results = new TokenValidationResults();
        String errorMessage = checkIfPresent(token);

        if (errorMessage == null) {
            errorMessage = checkIfDecodable(token);
        }

        if (errorMessage == null) {
            errorMessage = checkIfNotExpired(token);
        }

        if (errorMessage == null) {
            errorMessage = checkIfNotCompromised(token);
        }

        results.setValid(errorMessage == null);
        results.setErrorMessage(errorMessage);
        return results;
    }
}
