package com.dr.ktjsonschema.issue_24;

import com.dr.ktjsonschema.JsonSchemaGenerator;
import com.dr.ktjsonschema.issue_24.model.entities.Business;
import com.dr.ktjsonschema.issue_24.model.entities.EntityWrapper;
import com.dr.ktjsonschema.issue_24.model.entities.Person;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;

public class Issue24 {
	@Test
	public void issue_24() throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
		JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);

		//this works
		JsonNode businessSchema = jsonSchemaGenerator.generateJsonSchema(Business.class);
		String businessSchemaAsString = writer.writeValueAsString(businessSchema);
		System.out.println(businessSchemaAsString);
		
		//and this
		JsonNode personSchema = jsonSchemaGenerator.generateJsonSchema(Person.class);
		String personSchemaAsString = writer.writeValueAsString(personSchema);
		System.out.println(personSchemaAsString);
		
		//but not this
		JsonNode entityWrapperSchema = jsonSchemaGenerator.generateJsonSchema(EntityWrapper.class);
		String entityWrapperSchemaAsString = writer.writeValueAsString(entityWrapperSchema);
		System.out.println(entityWrapperSchemaAsString);
	}

}
