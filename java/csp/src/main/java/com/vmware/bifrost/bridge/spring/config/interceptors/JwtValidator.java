/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors;

import com.vmware.bifrost.bridge.spring.config.interceptors.csp.TokenValidationResults;

/**
 * Interface used to check the validity of the JWT.
 */
public interface JwtValidator {
    /**
     * Return true if token is not null.
     * @param token access token
     * @return error message. null if no error is found
     */
    String checkIfPresent(String token);

    /**
     * Return true if token is not expired.
     * @param token access token
     * @return error message. null if no error is found
     */
    String checkIfNotExpired(String token);

    /**
     * Return true if token is parsable.
     * @param token access token.
     * @return error message. null if no error is found
     */
    String checkIfDecodable(String token);

    /**
     * Return true if token can be validated using public key.
     * @param token access token
     * @return error message. null if no error is found
     */
    String checkIfNotCompromised(String token);

    /**
     * Return true if token exists, decodable, is not expired and not compromised.
     * @param token access token
     * @return token validation results object
     */
    TokenValidationResults getValidationResults(String token);
}
