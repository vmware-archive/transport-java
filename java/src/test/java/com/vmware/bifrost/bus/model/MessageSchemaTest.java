package com.vmware.bifrost.bus.model;


import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageSchemaTest {

    @Test
    public void testModel() {
        MessageSchema schema = new MessageSchema("pup pup puppy");
        Assert.assertEquals("pup pup puppy", schema.getPayload());
        Assert.assertEquals(String.class, schema.getPayloadType());

        schema.setPayloadType(Integer.class);
        schema.setPayload(new Integer(12345));

        Assert.assertEquals(Integer.class, schema.getPayloadType());
        Assert.assertEquals(new Integer(12345), schema.getPayload());
        Assert.assertNotEquals(new Long(12345), schema.getPayload());

        MessageSchema<Integer> s = new MessageSchema<>(12345);
        Assert.assertEquals(new Integer(12345), s.getPayload());
        Assert.assertEquals( Integer.class, s.getPayloadType());

        s.setDescription("test");
        s.setError(false);
        s.setTitle("a schema");

        Assert.assertEquals("test", s.getDescription());
        Assert.assertEquals("a schema", s.getTitle());
        Assert.assertFalse(s.isError());


    }

}