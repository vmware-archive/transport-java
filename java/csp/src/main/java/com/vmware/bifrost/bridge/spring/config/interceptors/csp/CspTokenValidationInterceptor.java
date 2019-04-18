/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostStompInterceptor;
import com.vmware.bifrost.bridge.spring.config.interceptors.JwtValidator;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.util.Loggable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;
import java.util.List;

/**
 * Interceptor to validate the JWT by checking if it can be decoded and it has not expired.
 */
@SuppressWarnings("unchecked")
public class CspTokenValidationInterceptor extends Loggable implements BifrostStompInterceptor {
    private static final String DEFAULT_ACCESS_TOKEN_KEY = "accessToken";
    private final String accessTokenKey;
    private final ObjectMapper objectMapper;
    private JwtValidator jwtValidator;

    /**
     * Initialize with default access token key and CSP JWT validator.
     */
    public CspTokenValidationInterceptor(CspEnvironment cspEnv) {
        this(DEFAULT_ACCESS_TOKEN_KEY, new CspJwtValidator(cspEnv));
    }

    /**
     * Initialize with the provided access token key and JWT validator.
     */
    public CspTokenValidationInterceptor(String accessTokenKey, JwtValidator jwtValidator) {
        this.accessTokenKey = accessTokenKey;
        this.jwtValidator = jwtValidator;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Build a GenericMessage object whose payload is of GeneralError type.
     * @param message original Nessage object
     * @param errorDetails error details
     * @param errorCode error code
     * @return GenericMessage
     */
    private GenericMessage buildGenericErrorMsg(Message<?> message,
                                                String errorDetails,
                                                Integer errorCode) {
        GenericMessage errorMessage = null;
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();

        try {
            String payload = new String((byte[]) message.getPayload());
            Request request = objectMapper.readValue(payload, Request.class);
            GeneralError error = new GeneralError(
                    errorDetails,
                    null,
                    errorCode);

            request.setRejected(true);
            request.setPayload(ow.writeValueAsString(error));
            errorMessage = new GenericMessage<>(ow.writeValueAsString(request).getBytes(), message.getHeaders());
        } catch (IOException e) {
            logWarnMessage(e.getMessage());
        }
        return errorMessage;
    }

    @Override
    public Message<?> preSend(Message<?> message) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        TokenValidationResults validationResults;
        List<String> accessTokenList = headerAccessor.getNativeHeader(accessTokenKey);

        if (accessTokenList != null) {
            String accessToken = accessTokenList.get(0);
            validationResults = jwtValidator.getValidationResults(accessToken);

            if (validationResults.isValid()) {
                return message;
            }
        } else {
            return buildGenericErrorMsg(
                    message,
                    "No token is found",
                    401);
        }

        return buildGenericErrorMsg(
                message,
                validationResults.getErrorMessage(),
                401);
    }
}