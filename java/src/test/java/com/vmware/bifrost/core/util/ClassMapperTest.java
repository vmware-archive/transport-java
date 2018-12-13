/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.bridge.Request;
import org.junit.Assert;
import org.junit.Test;

public class ClassMapperTest {

    @Test
    public void testCastPayload() {

        Request req = new Request();
        req.setPayload("my payload");


        Assert.assertEquals("my payload", ClassMapper.CastPayload(String.class, req));

    }

    @Test
    public void testCastPayloadError() {

        Request req = new Request();
        req.setPayload("my payload");


        try {
            ClassMapper.CastPayload(Integer.class, req);
        } catch (IllegalArgumentException exp) {
            Assert.assertTrue(exp.getMessage().contains("not a valid Integer value"));
        }

    }
}
