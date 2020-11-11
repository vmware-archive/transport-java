/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.transport.core.util;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bus.model.MessageHeaders;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

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

    @Test
    public void testCastMessageHeaders() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("header-key", "header-value");

        MessageHeaders messageHeaders = ClassMapper.CastMessageHeaders(map);
        Assert.assertEquals("header-value", messageHeaders.getHeader("header-key"));
        Assert.assertNull(ClassMapper.CastMessageHeaders(null));
    }
}
