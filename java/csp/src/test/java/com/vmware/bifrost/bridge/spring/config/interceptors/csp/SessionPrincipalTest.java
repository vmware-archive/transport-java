package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

public class SessionPrincipalTest {
    private SessionPrincipal principal;

    @Before
    public void setUp() {
        this.principal = new SessionPrincipal("test");
    }

    @Test
    public void verifyGetterSetter() {
        Assert.assertEquals(principal.getName(), "test");

        principal.setName("test2");
        Assert.assertEquals(principal.getName(), "test2");
    }

    @Test
    public void verifyEquals() {
        SessionPrincipal principal2 = new SessionPrincipal("test-2");
        SessionPrincipal principal3 = new SessionPrincipal("test");

        Assert.assertFalse(principal.equals(principal2));
        Assert.assertTrue(principal.equals(principal3));

        Object object = new Object();
        Assert.assertFalse(principal.equals(object));
    }

    @Test
    public void verifyHashCode() {
        Assert.assertEquals(principal.hashCode(), Objects.hash("test"));
    }
}
