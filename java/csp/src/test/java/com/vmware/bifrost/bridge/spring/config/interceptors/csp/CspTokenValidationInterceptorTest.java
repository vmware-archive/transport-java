package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import static org.powermock.api.mockito.PowerMockito.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.spring.config.interceptors.JwtValidator;
import com.vmware.bifrost.core.error.GeneralError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CspTokenValidationInterceptor.class, StompHeaderAccessor.class})
@SuppressWarnings("unchecked")
public class CspTokenValidationInterceptorTest {

    private CspTokenValidationInterceptor cspTokenValidationInterceptor;

    @Before
    public void setUp() {
        cspTokenValidationInterceptor = mock(CspTokenValidationInterceptor.class);
        when(cspTokenValidationInterceptor.preSend(any(Message.class))).thenCallRealMethod();
    }

    @Test
    public void verifyConstructor() {
        cspTokenValidationInterceptor = new CspTokenValidationInterceptor(
                "custom-token",
                new CspJwtValidator(CspEnvironment.TESTING));

        String accessTokenKey = Whitebox.getInternalState(cspTokenValidationInterceptor, "accessTokenKey");
        JwtValidator jwtValidator = Whitebox.getInternalState(cspTokenValidationInterceptor, "jwtValidator");
        ObjectMapper objectMapper = Whitebox.getInternalState(cspTokenValidationInterceptor, "objectMapper");

        assertEquals("custom-token", accessTokenKey);
        assertTrue(jwtValidator instanceof CspJwtValidator);
        assertNotNull(objectMapper);

        cspTokenValidationInterceptor = new CspTokenValidationInterceptor(CspEnvironment.TESTING);
        accessTokenKey = Whitebox.getInternalState(cspTokenValidationInterceptor, "accessTokenKey");
        jwtValidator = Whitebox.getInternalState(cspTokenValidationInterceptor, "jwtValidator");
        assertEquals("accessToken", accessTokenKey);
        assertTrue(jwtValidator instanceof CspJwtValidator);
    }

    @Test
    public void verifySuccessPreSend() {
        mockStatic(StompHeaderAccessor.class);
        Whitebox.setInternalState(cspTokenValidationInterceptor, "accessTokenKey", "accessToken");

        JwtValidator jwtValidatorMock = mock(JwtValidator.class);
        TokenValidationResults tokenValidationResults = new TokenValidationResults();
        tokenValidationResults.setValid(true);
        tokenValidationResults.setErrorMessage(null);
        when(jwtValidatorMock.getValidationResults(anyString())).thenReturn(tokenValidationResults);
        Whitebox.setInternalState(cspTokenValidationInterceptor, "jwtValidator", jwtValidatorMock);

        List<String> accessTokenList = new ArrayList<>();
        accessTokenList.add("abcde");

        StompHeaderAccessor stompHeaderAccessorMock = mock(StompHeaderAccessor.class);
        when(stompHeaderAccessorMock.getNativeHeader(anyString())).thenReturn(accessTokenList);
        when(StompHeaderAccessor.wrap(any(Message.class))).thenReturn(stompHeaderAccessorMock);

        GenericMessage msg = new GenericMessage("generic message");
        Message results = cspTokenValidationInterceptor.preSend(msg);
        assertEquals(msg, results);
    }

    @Test
    public void verifyInvalidPayload() throws Exception {
        cspTokenValidationInterceptor = spy(new CspTokenValidationInterceptor(CspEnvironment.TESTING));

        mockStatic(StompHeaderAccessor.class);
        Whitebox.setInternalState(cspTokenValidationInterceptor, "accessTokenKey", "accessToken");
        StompHeaderAccessor stompHeaderAccessorMock = mock(StompHeaderAccessor.class);
        when(stompHeaderAccessorMock.getNativeHeader(anyString())).thenReturn(null);
        when(StompHeaderAccessor.wrap(any(Message.class))).thenReturn(stompHeaderAccessorMock);
        doCallRealMethod().when(cspTokenValidationInterceptor,
                "buildGenericErrorMsg",
                ArgumentMatchers.any(Message.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt());

        GenericMessage msg = new GenericMessage("generic message".getBytes());
        Message results = cspTokenValidationInterceptor.preSend(msg);
        assertNull(results);
    }

    @Test
    public void verifyEmptyAccessTokenList() throws Exception {
        cspTokenValidationInterceptor = spy(new CspTokenValidationInterceptor(CspEnvironment.TESTING));

        mockStatic(StompHeaderAccessor.class);
        ObjectMapper mapper = new ObjectMapper();
        Whitebox.setInternalState(cspTokenValidationInterceptor, "accessTokenKey", "accessToken");
        StompHeaderAccessor stompHeaderAccessorMock = mock(StompHeaderAccessor.class);
        when(stompHeaderAccessorMock.getNativeHeader(anyString())).thenReturn(null);
        when(StompHeaderAccessor.wrap(any(Message.class))).thenReturn(stompHeaderAccessorMock);
        doCallRealMethod().when(cspTokenValidationInterceptor,
                "buildGenericErrorMsg",
                ArgumentMatchers.any(Message.class),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt());

        Request<String> originalRequest = new Request<>(UUID.randomUUID(), "req", "test");
        GenericMessage<Request> msg = new GenericMessage(
                mapper.writer().withDefaultPrettyPrinter().writeValueAsString(originalRequest).getBytes());
        Message results = cspTokenValidationInterceptor.preSend(msg);

        Request request = mapper.readValue((byte[]) results.getPayload(), Request.class);
        GeneralError generalError = mapper.readValue((String) request.getPayload(), GeneralError.class);
        assertEquals("No token is found", generalError.message);
        assertTrue(generalError.errorCode == 401);
    }

    @Test
    public void verifyInvalidToken() throws Exception {
        cspTokenValidationInterceptor = spy(new CspTokenValidationInterceptor(CspEnvironment.TESTING));

        mockStatic(StompHeaderAccessor.class);
        ObjectMapper mapper = new ObjectMapper();
        Whitebox.setInternalState(cspTokenValidationInterceptor, "accessTokenKey", "accessToken");

        JwtValidator jwtValidatorMock = mock(JwtValidator.class);
        TokenValidationResults tokenValidationResults = new TokenValidationResults();
        tokenValidationResults.setValid(false);
        tokenValidationResults.setErrorMessage("Token is malformed");
        when(jwtValidatorMock.getValidationResults(anyString())).thenReturn(tokenValidationResults);
        Whitebox.setInternalState(cspTokenValidationInterceptor, "jwtValidator", jwtValidatorMock);

        List<String> accessTokenList = new ArrayList<>();
        accessTokenList.add("abcde");

        StompHeaderAccessor stompHeaderAccessorMock = mock(StompHeaderAccessor.class);
        when(stompHeaderAccessorMock.getNativeHeader(anyString())).thenReturn(accessTokenList);
        when(StompHeaderAccessor.wrap(any(Message.class))).thenReturn(stompHeaderAccessorMock);

        Request<String> originalRequest = new Request<>(UUID.randomUUID(), "req", "test");
        GenericMessage<Request> msg = new GenericMessage(
                mapper.writer().withDefaultPrettyPrinter().writeValueAsString(originalRequest).getBytes());
        Message results = cspTokenValidationInterceptor.preSend(msg);

        Request request = mapper.readValue((byte[]) results.getPayload(), Request.class);
        GeneralError generalError = mapper.readValue((String) request.getPayload(), GeneralError.class);
        assertEquals("Token is malformed", generalError.message);
        assertTrue(generalError.errorCode == 401);
    }
}
