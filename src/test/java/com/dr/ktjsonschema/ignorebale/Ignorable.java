package com.dr.ktjsonschema.ignorebale;


import com.dr.ktjsonschema.JsonSchemaConfig;
import com.dr.ktjsonschema.JsonSchemaGenerator;
import com.dr.ktjsonschema.ignorebale.model.entities.IgnorableBusiness;
import com.dr.ktjsonschema.ignorebale.model.entities.IgnorableEntityWrapper;
import com.dr.ktjsonschema.ignorebale.model.entities.IgnorablePerson;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class Ignorable {
  Set<String> ignoreInputAnnotation = new HashSet<String>(Arrays.asList("JsonSchemaInputModelIgnore"));
  Set<String> ignoreOutputAnnotation = new HashSet<String>(Arrays.asList("JsonSchemaOutputModelIgnore"));

  @Test
  public void ignorableNotConfigured() throws Exception {

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);

    //this works
    JsonNode businessSchema = jsonSchemaGenerator.generateJsonSchema(IgnorableBusiness.class);
    String businessSchemaAsString = writer.writeValueAsString(businessSchema);
    System.out.println(businessSchemaAsString);
    // Should contain
    assertTrue(businessSchemaAsString.contains("first"));
    assertTrue(businessSchemaAsString.contains("street"));

    //and this
    JsonNode personSchema = jsonSchemaGenerator.generateJsonSchema(IgnorablePerson.class);
    String personSchemaAsString = writer.writeValueAsString(personSchema);
    System.out.println(personSchemaAsString);

    //but not this
    JsonNode entityWrapperSchema = jsonSchemaGenerator.generateJsonSchema(IgnorableEntityWrapper.class);
    String entityWrapperSchemaAsString = writer.writeValueAsString(entityWrapperSchema);
    System.out.println(entityWrapperSchemaAsString);
  }

  @Test
  public void ignorableJsonSchemaInputModelIgnoreConfigured() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper, JsonSchemaConfig.getVanillaJsonSchemaDraft4(), false, ignoreInputAnnotation);

    //this works
    JsonNode businessSchema = jsonSchemaGenerator.generateJsonSchema(IgnorableBusiness.class);
    String businessSchemaAsString = writer.writeValueAsString(businessSchema);
    System.out.println(businessSchemaAsString);
    // Shouldn't contain
    assertFalse(businessSchemaAsString.contains("first"));
    assertFalse(businessSchemaAsString.contains("Street"));

    //and this
    JsonNode personSchema = jsonSchemaGenerator.generateJsonSchema(IgnorablePerson.class);
    String personSchemaAsString = writer.writeValueAsString(personSchema);
    System.out.println(personSchemaAsString);

    //but not this
    JsonNode entityWrapperSchema = jsonSchemaGenerator.generateJsonSchema(IgnorableEntityWrapper.class);
    String entityWrapperSchemaAsString = writer.writeValueAsString(entityWrapperSchema);
    System.out.println(entityWrapperSchemaAsString);
  }

  @Test
  public void ignorableJsonSchemaOutputModelIgnoreConfigured() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper, JsonSchemaConfig.getVanillaJsonSchemaDraft4(), false, ignoreOutputAnnotation);

    //this works
    JsonNode businessSchema = jsonSchemaGenerator.generateJsonSchema(IgnorableBusiness.class);
    String businessSchemaAsString = writer.writeValueAsString(businessSchema);
    System.out.println(businessSchemaAsString);

    // Shouldn't contain
    assertFalse(businessSchemaAsString.contains("employment"));
    assertFalse(businessSchemaAsString.contains("zip"));
    assertFalse(businessSchemaAsString.contains("first"));
    assertFalse(businessSchemaAsString.contains("last"));

    //and this
    JsonNode personSchema = jsonSchemaGenerator.generateJsonSchema(IgnorablePerson.class);
    String personSchemaAsString = writer.writeValueAsString(personSchema);
    System.out.println(personSchemaAsString);

    //but not this
    JsonNode entityWrapperSchema = jsonSchemaGenerator.generateJsonSchema(IgnorableEntityWrapper.class);
    String entityWrapperSchemaAsString = writer.writeValueAsString(entityWrapperSchema);
    System.out.println(entityWrapperSchemaAsString);
  }
}
