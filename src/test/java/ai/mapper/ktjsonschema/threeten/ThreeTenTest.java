package ai.mapper.ktjsonschema.threeten;

import ai.mapper.ktjsonschema.JsonSchemaConfig;
import ai.mapper.ktjsonschema.JsonSchemaGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.threetenbp.ThreeTenModule;
import org.junit.Test;
import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class ClassWithLocalDate {
	public LocalDate localDate;
}

public class ThreeTenTest {
	@Test
	public void three_ten_test() throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new ThreeTenModule());

		Map<String, String> mapping = new HashMap<String, String>();
        mapping.put(LocalDate.class.getName(), "date");
		JsonSchemaConfig config = new JsonSchemaConfig(false, null,
                false, false, false, false,
                false, false,
                mapping);
		JsonSchemaGenerator generator = new JsonSchemaGenerator(objectMapper, config);
		JsonNode node = generator.generateJsonSchema(ClassWithLocalDate.class);

        assertEquals(
                "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"title\":\"Class With Local Date\",\"type\":\"object\",\"additionalProperties\":false,\"properties\":{\"localDate\":{\"type\":\"array\",\"items\":{\"type\":\"integer\"},\"format\":\"date\"}}}",
                objectMapper.writeValueAsString(node));
	}

}
