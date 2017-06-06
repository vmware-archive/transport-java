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
        Assert.assertEquals(schema.getPayload(), "pup pup puppy");
        Assert.assertEquals(schema.getPayloadType(), String.class);

        schema.setPayloadType(Integer.class);
        schema.setPayload(new Integer(12345));

        Assert.assertEquals(schema.getPayloadType(), Integer.class);
        Assert.assertEquals(schema.getPayload(), new Integer(12345));
        Assert.assertNotEquals(schema.getPayload(), new Long(12345));

        MessageSchema<Integer> s = new MessageSchema<>(12345);
        Assert.assertEquals(s.getPayload(), new Integer(12345));
        Assert.assertEquals(s.getPayloadType(), Integer.class);

        s.setDescription("test");
        s.setError(false);
        s.setTitle("a schema");

        Assert.assertEquals(s.getDescription(), "test");
        Assert.assertEquals(s.getTitle(), "a schema");
        Assert.assertFalse(s.isError());


    }

}