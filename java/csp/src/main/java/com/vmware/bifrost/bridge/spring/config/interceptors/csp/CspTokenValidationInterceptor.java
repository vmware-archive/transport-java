/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import static com.vmware.bifrost.bridge.spring.config.interceptors.csp.Constants.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostStompInterceptor;
import com.vmware.bifrost.bridge.spring.config.interceptors.JwtValidator;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.util.Loggable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;
import java.util.List;

/**
 * Interceptor to validate the JWT by checking if it can be decoded and it has not expired.
 */
@SuppressWarnings("unchecked")
public class CspTokenValidationInterceptor extends Loggable implements BifrostStompInterceptor {
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

            request.setIsRejected(true);
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

        // if token is empty
        if (accessTokenList == null || accessTokenList.size() == 0 || accessTokenList.get(0).length() == 0) {
            // and if this packet contains a SEND message, send an error message
            if (message.getHeaders().containsKey(HEADER_KEY_STOMP_COMMAND) &&
                message.getHeaders().get(HEADER_KEY_STOMP_COMMAND, StompCommand.class).equals(StompCommand.SEND)) {
                return buildGenericErrorMsg(message, ERR_MSG_NO_TOKEN_FOUND, 401);
            }

            // otherwise return null to drop the message.
            logWarnMessage(ERR_MSG_NO_TOKEN_FOUND);
            return null;
        }

        validationResults = jwtValidator.getValidationResults(accessTokenList.get(0));
        if (validationResults.isValid()) {
            return message;
        }

        return buildGenericErrorMsg(
                message,
                validationResults.getErrorMessage(),
                401);
    }
}
