/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

class CspTokenPublicKeyResponse {
    private String alg;
    private String value;
    private String issuer;

    String getAlg() {
        return alg;
    }

    void setAlg(String alg) {
        this.alg = alg;
    }

    String getValue() {
        return value;
    }

    void setValue(String value) {
        this.value = value;
    }

    String getIssuer() {
        return issuer;
    }

    void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
