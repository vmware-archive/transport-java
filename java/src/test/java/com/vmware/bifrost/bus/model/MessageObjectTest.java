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
        Assert.assertEquals(messageObject.getPayloadClass(), String.class);
        Assert.assertEquals(messageObject.getPayload(), "#test-me");

        MessageObject<Channel> messageObject2 = new MessageObject<>(MessageType.MessageTypeRequest, new Channel("#magic-shoes"));
        Assert.assertEquals(messageObject2.getPayloadClass(), Channel.class);
        Assert.assertEquals(messageObject2.getPayload().getName(), "#magic-shoes");
    }

    @Test
    public void testMessageProperties() {
        MessageObject messageObject = new MessageObject(MessageType.MessageTypeRequest, "park life");
        Assert.assertEquals(messageObject.getPayloadClass(), String.class);
        Assert.assertNotEquals(messageObject.getPayloadClass(), Integer.class);
        Assert.assertEquals(messageObject.toString(), "park life");

        messageObject.setPayload(new Long(1234567));
        messageObject.setPayloadClass(Long.class);

        Assert.assertNotEquals(messageObject.getPayloadClass(), String.class);
        Assert.assertEquals(messageObject.getPayloadClass(), Long.class);
        Assert.assertEquals(messageObject.getType(), MessageType.MessageTypeRequest);

        messageObject.setType(MessageType.MessageTypeResponse);

        Assert.assertEquals(messageObject.getType(), MessageType.MessageTypeResponse);
        Assert.assertNotNull(this.schema);
        Assert.assertEquals(this.schema.isObjectSchema(), true);
        Assert.assertNull(messageObject.getSchema());

        messageObject.setSchema(this.schema);

        Assert.assertNotNull(messageObject.getSchema());

    }

}