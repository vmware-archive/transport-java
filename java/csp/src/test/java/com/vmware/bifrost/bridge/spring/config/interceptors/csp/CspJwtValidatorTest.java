package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CspJwtValidator.class})
@SuppressWarnings("unchecked")
public class CspJwtValidatorTest {

    private CspJwtValidator cspJwtValidator;

    @Before
    public void setUp() {
        cspJwtValidator = mock(CspJwtValidator.class);

        Configuration configMock = mock(Configuration.class);
        when(configMock.getString("accessTokenPublicKeyUri")).thenReturn("/am/api/auth/token-public-key");
        Whitebox.setInternalState(cspJwtValidator, "config", configMock);
        Whitebox.setInternalState(cspJwtValidator, "cspEnvironment", CspEnvironment.DEVELOPMENT);
        Whitebox.setInternalState(cspJwtValidator, "base64Decoder", Base64.getDecoder());
    }

    @Test
    public void verifyConstructor() {
        CspJwtValidator cspJwtValidator = new CspJwtValidator(CspEnvironment.PRODUCTION);
        assertNotNull(cspJwtValidator);
    }

    @Test(expected = NullPointerException.class)
    public void verifyNullPointerIfPropertiesNotFound() {
        cspJwtValidator = new CspJwtValidator(CspEnvironment.DEVELOPMENT, "nonexistent.properties");
    }

    @Test
    public void verifyCheckIfPresent() {
        Whitebox.setInternalState(cspJwtValidator, "cspUrlsMap", new HashMap<CspEnvironment, String>() {
            {
                put(CspEnvironment.DEVELOPMENT, "https://fake-url");
            }
        });

        when(cspJwtValidator.checkIfPresent(any())).thenCallRealMethod();

        String results = cspJwtValidator.checkIfPresent(null);
        assertNotNull(results);
        assertEquals("Token is empty", results);

        results = cspJwtValidator.checkIfPresent("");
        assertNotNull(results);
        assertEquals("Token is empty", results);

        results = cspJwtValidator.checkIfPresent("abcde");
        assertNull(results);
    }

    @Test
    public void verifyCheckIfDecodable() {
        when(cspJwtValidator.checkIfDecodable(any())).thenCallRealMethod();

        String token = "abcde";

        // wrong-formatted token - number of segments not equal to 3
        String results = cspJwtValidator.checkIfDecodable(token);
        assertNotNull(results);
        assertEquals("Token malformed", results);

        // another malformed case - signature missing
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ";
        results = cspJwtValidator.checkIfDecodable(token);
        assertNotNull(results);
        assertEquals("Token malformed", results);

        // another malformed case - segment not parsable
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "Fake Claims." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        results = cspJwtValidator.checkIfDecodable(token);
        assertNotNull(results);
        assertEquals("Token malformed", results);

        // correct token
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        results = cspJwtValidator.checkIfDecodable(token);
        assertNull(results);
    }

    @Test
    public void verifyCheckIfNotExpired() {
        Whitebox.setInternalState(cspJwtValidator, "objectMapper", new ObjectMapper());

        String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        when(cspJwtValidator.checkIfNotExpired(anyString())).thenCallRealMethod();

        // body does not contain exp claim thus is invalid. an empty map is returned
        String results = cspJwtValidator.checkIfNotExpired(token);

        assertNotNull(results);
        assertEquals("Token is malformed. (exp missing)", results);

        // body is malformed
        token = "i am an.incorrectly formatted.token";
        results = cspJwtValidator.checkIfNotExpired(token);
        assertNotNull(results);
        assertEquals("Token is malformed. (exp missing)", results);

        // token expired
        token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ." +
                "B8_Z3aUJ8vt53bkR-1df4c4OnkZqEMFve_XyFLV8F_SG_L3JrZOdAGFCapdNQd9skjZ0zEnC8Cxdw4IuM9thvxxNuRP1HAvMD6X" +
                "-Sb3kijg87YtUy90W_5A6NyLH3KLDS78hqQjfHLrsTCmXayW0rgIMve-UoJElUfiozhWC9444WK3u3dSHezzLLgeNbZHTIOpyGD3kxe" +
                "AZf8O5xW7yT2PsY8_yWQ9Sr8q-9gFe4SLk9Pk3utcZGVwa_u2lPUjti24bP_BVbWzaetzN8VGUSvWe82meRX-EslcLDZOuHLqEfH1Bl" +
                "B0UWVt-1jNToyPVg8bjJgPkyoxm3UYgBcYoBQ";
        results = cspJwtValidator.checkIfNotExpired(token);
        assertNotNull(results);
        assertEquals("Token expired", results);

        // token valid. (valid until July 31st 2033)
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoyMDE2MjM5MDIyLCJpYXQiOjE1MTYyMzkwMjJ9." +
                "M9atZUe3ndX8aWukXifoZJlWjq-JIWb6xlKKFhwXMXg";
        results = cspJwtValidator.checkIfNotExpired(token);
        assertNull(results);
    }

    @Test
    public void verifyCheckIfNotCompromised() throws Exception {
        cspJwtValidator = spy(new CspJwtValidator(CspEnvironment.DEVELOPMENT));

        RestTemplate restTemplateMock = mock(RestTemplate.class);
        CspTokenPublicKeyResponse cspTokenPublicKeyResponse = new CspTokenPublicKeyResponse();
        cspTokenPublicKeyResponse.setAlg("RSA");
        cspTokenPublicKeyResponse.setIssuer("fakeOne");
        cspTokenPublicKeyResponse.setValue("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnzyis1ZjfNB0bBgKFMSv" +
                "vkTtwlvBsaJq7S5wA+kzeVOVpVWwkWdVha4s38XM/pa/yr47av7+z3VTmvDRyAHc" +
                "aT92whREFpLv9cj5lTeJSibyr/Mrm/YtjCZVWgaOYIhwrXwKLqPr/11inWsAkfIy" +
                "tvHWTxZYEcXLgAXFuUuaS3uF9gEiNQwzGTU1v0FqkqTBr4B8nW3HCN47XUu0t8Y0" +
                "e+lf4s4OxQawWD79J9/5d3Ry0vbV3Am1FtGJiJvOwRsIfVChDpYStTcHTCMqtvWb" +
                "V6L11BWkpzGXSW4Hv43qa+GSYOD2QU68Mb59oSk2OB+BtOLpJofmbGEGgvmwyCI9" +
                "MwIDAQAB" +
                "\n-----END PUBLIC KEY-----");
        ResponseEntity<CspTokenPublicKeyResponse> fakeResponseEntity = ResponseEntity.ok(cspTokenPublicKeyResponse);
        when(restTemplateMock.exchange(
                anyString(),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(CspTokenPublicKeyResponse.class)))
                .thenReturn(fakeResponseEntity);

        Whitebox.setInternalState(cspJwtValidator, "restTemplate", restTemplateMock);
        Whitebox.setInternalState(cspJwtValidator, "cspUrlsMap", new HashMap<CspEnvironment, String>() {
            {
                put(CspEnvironment.DEVELOPMENT, "https://fake-url");
            }
        });

//        when(cspJwtValidator.checkIfNotCompromised(any())).thenCallRealMethod();

        // valid claims with valid signature
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0." +
                "POstGetfAytaZS82wHcjoTyoqhMyxXiWdR7Nn7A29DNSl0EiXLdwJ6xC6AfgZWF1bOsS_TuYI3OG85AmiExREkrS6tDf" +
                "TQ2B3WXlrr-wp5AokiRbz3_oB4OxG-W9KcEEbDRcZc0nH3L7LzYptiy1PtAylQGxHTWZXtGz4ht0bAecBgmpdgXMguEI" +
                "coqPJ1n3pIWk_dUZegpqx0Lka21H6XxUTxiy8OcaarA8zdnPUnV6AmNP3ecFawIFYdvJB_cm-GvpCSbr8G8y_Mllj8f4" +
                "x9nBH8pQux89_6gUY618iYv7tuPWBFfEbLxtF2pZS6YC1aSfLQxeNe8djT9YjpvRZA";

        doCallRealMethod().when(cspJwtValidator, "getJwtPublicKey");
        String results = cspJwtValidator.checkIfNotCompromised(token);
        PublicKey publicKey = Whitebox.getInternalState(cspJwtValidator, "accessTokenPublicKey");
        assertNotNull(publicKey);
        assertNull(results);

        // signature doesn't match
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjoyMDE2MjM5MDIyLCJpYXQiOjE1MTYyMzkwMjJ9." +
                "M9atZUe3ndX8aWukXifoZJlWjq-JIWb6xlKKFhwXMXg";
        results = cspJwtValidator.checkIfNotCompromised(token);
        assertNotNull(results);
        assertTrue(results.contains("Token is compromised"));
    }

    @Test
    public void verifyGetValidationResults() {
        when(cspJwtValidator.getValidationResults(anyString())).thenCallRealMethod();

        TokenValidationResults results = null;

        // token is empty
        when(cspJwtValidator.checkIfPresent(anyString())).thenReturn("Token is empty");
        results = cspJwtValidator.getValidationResults("a.b.c");
        assertNotNull(results);
        assertEquals("Token is empty", results.getErrorMessage());
        assertFalse(results.isValid());

        // token is malformed
        when(cspJwtValidator.checkIfPresent(anyString())).thenReturn(null);
        when(cspJwtValidator.checkIfDecodable(anyString())).thenReturn("Token is malformed");
        results = cspJwtValidator.getValidationResults("a.b.c");
        assertNotNull(results);
        assertEquals("Token is malformed", results.getErrorMessage());
        assertFalse(results.isValid());

        // token has expired
        when(cspJwtValidator.checkIfDecodable(anyString())).thenReturn(null);
        when(cspJwtValidator.checkIfNotExpired(anyString())).thenReturn("Token expired");
        results = cspJwtValidator.getValidationResults("a.b.c");
        assertNotNull(results);
        assertEquals("Token expired", results.getErrorMessage());
        assertFalse(results.isValid());

        // token's signature doesn't match
        when(cspJwtValidator.checkIfNotExpired(anyString())).thenReturn(null);
        when(cspJwtValidator.checkIfNotCompromised(anyString())).thenReturn("Token is compromised.");
        results = cspJwtValidator.getValidationResults("a.b.c");
        assertNotNull(results);
        assertEquals("Token is compromised.", results.getErrorMessage());
        assertFalse(results.isValid());

        // token passes all validations
        when(cspJwtValidator.checkIfNotCompromised(anyString())).thenReturn(null);
        results = cspJwtValidator.getValidationResults("a.b.c");
        assertNotNull(results);
        assertTrue(results.isValid());
        assertNull(results.getErrorMessage());
    }
}
