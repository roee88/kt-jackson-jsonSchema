package com.dr.ktjsonschema.Issue23;

import com.dr.ktjsonschema.Issue23.model.Dummy;
import com.dr.ktjsonschema.JsonSchemaGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Issue23 {
    @Test
    public void basic_map_test() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator generator = new JsonSchemaGenerator(mapper);
        JsonNode schema = generator.generateJsonSchema(Dummy.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema));
        assertEquals(schema.toString(),
                "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"Dummy\",\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"x\":{\"type\":\"integer\"},\"colors\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#/definitions/Color\"}}},\"required\":[\"x\"],\"definitions\":{\"Color\":{\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"color\":{\"type\":\"integer\"}},\"required\":[\"color\"]}}}");
    }
}