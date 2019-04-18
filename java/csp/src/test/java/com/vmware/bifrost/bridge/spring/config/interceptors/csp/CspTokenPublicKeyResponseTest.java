package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CspTokenPublicKeyResponseTest {
    private CspTokenPublicKeyResponse cspTokenPublicKeyResponse;

    @Before
    public void setUp() {
        cspTokenPublicKeyResponse = new CspTokenPublicKeyResponse();
    }

    @Test
    public void validateGetterSetter() {
        cspTokenPublicKeyResponse.setAlg("RSA");
        cspTokenPublicKeyResponse.setIssuer("abcde");
        cspTokenPublicKeyResponse.setValue("some_base64_encoded_string");
        Assert.assertEquals("RSA", cspTokenPublicKeyResponse.getAlg());
        Assert.assertEquals("abcde", cspTokenPublicKeyResponse.getIssuer());
        Assert.assertEquals("some_base64_encoded_string", cspTokenPublicKeyResponse.getValue());
    }
}
