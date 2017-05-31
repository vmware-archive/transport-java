package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageObjectSchemaTest {

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

        Assert.assertNotNull(this.schema);
        Assert.assertEquals(this.schema.isObjectSchema(), true);


        //System.out.println(this.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));


    }

}