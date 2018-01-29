package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */


public class MessageObjectTest {

    private JsonSchema schema;
    private ObjectMapper mapper;
    private JsonSchemaGenerator schemaGen;

    @Before
    public void setup() throws Exception {
        this.mapper = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);
    }

    @Test
    public void checkSchema() throws Exception {
        //System.out.println(this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
    }

    @Test
    public void createBasicMessage() {

        MessageObject<String> messageObject = new MessageObject<>(MessageType.MessageTypeRequest, "#test-me");
        Assert.assertEquals(String.class, messageObject.getPayloadClass());
        Assert.assertEquals("#test-me", messageObject.getPayload());

        MessageObject<Channel> messageObject2 = new MessageObject<>(MessageType.MessageTypeRequest, new Channel("#magic-shoes"));
        Assert.assertEquals(Channel.class, messageObject2.getPayloadClass());
        Assert.assertEquals("#magic-shoes", messageObject2.getPayload().getName());
    }

    @Test
    public void testMessageProperties() {
        MessageObject messageObject = new MessageObject(MessageType.MessageTypeRequest, "park life");
        Assert.assertEquals(String.class, messageObject.getPayloadClass());
        Assert.assertNotEquals(Integer.class, messageObject.getPayloadClass());
        Assert.assertEquals("park life", messageObject.toString());

        messageObject.setPayload(new Long(1234567));
        messageObject.setPayloadClass(Long.class);

        Assert.assertNotEquals(String.class, messageObject.getPayloadClass());
        Assert.assertEquals(Long.class, messageObject.getPayloadClass());
        Assert.assertEquals(MessageType.MessageTypeRequest, messageObject.getType());

        messageObject.setType(MessageType.MessageTypeResponse);

        Assert.assertEquals(MessageType.MessageTypeResponse, messageObject.getType());
        Assert.assertNotNull(this.schema);
        Assert.assertEquals(true, this.schema.isObjectSchema());
        Assert.assertNull(messageObject.getSchema());

        messageObject.setSchema(this.schema);

        Assert.assertNotNull(messageObject.getSchema());

    }

}