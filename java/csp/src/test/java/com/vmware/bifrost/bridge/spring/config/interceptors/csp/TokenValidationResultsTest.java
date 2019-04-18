package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TokenValidationResultsTest {
    private TokenValidationResults tokenValidationResults;

    @Before
    public void setUp() {
        tokenValidationResults = new TokenValidationResults();
    }

    @Test
    public void validateGetterSetter() {
        tokenValidationResults.setValid(false);
        Assert.assertEquals(tokenValidationResults.isValid(), false);

        tokenValidationResults.setValid(true);
        Assert.assertEquals(tokenValidationResults.isValid(), true);

        tokenValidationResults.setErrorMessage("err1");
        Assert.assertEquals(tokenValidationResults.getErrorMessage(), "err1");
    }
}
