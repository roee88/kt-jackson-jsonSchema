[![Download](https://api.bintray.com/packages/com-dr/dr-public/KtJsonSchema/images/download.svg) ](https://bintray.com/com-dr/dr-public/KtJsonSchema/_latestVersion)
[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Jackson jsonSchema Generator /Kotlin
=====================================

This projects is a quick re-implementation of [mbknor-jackson-jsonSchema](https://github.com/mbknor/mbknor-jackson-jsonSchema), in Kotlin.

**Why?**
I wanted to use it in Android. 
While mbknor-jackson-jsonSchema can easily be modified to target Java6, the Scala runtime is still quite an overhead. 

**Gradle**
```gradle
compile 'com.dr:ktjsonschema:1.0.0'
```

**Maven**
```maven
<dependency>
  <groupId>com.dr</groupId>
  <artifactId>ktjsonschema</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

**Highlights**

* JSON Schema Draft v4
* Supports polymorphism (**@JsonTypeInfo**, **MixIn**, and **registerSubtypes()**) using JsonSchema's **oneOf**-feature.
* Supports schema customization using:
  - **@JsonSchemaDescription**/**@JsonPropertyDescription**
  - **@JsonSchemaFormat**
  - **@JsonSchemaTitle**
  - **@JsonSchemaDefault**
* Supports many Javax-validation @Annotations
* Works well with Generated GUI's using [https://github.com/jdorn/json-editor](https://github.com/jdorn/json-editor)
  - (Must be configured to use this mode)
* Supports custom Class-to-format-Mapping

**Status**

The translation from the Scala codebase was blind and quick, so some bugs may be present, though I haven't found any (yet).

**Dependency**

This project targets Java 6, so no support for java.util.Optional fields.

**Example (Java)**

```
    ObjectMapper objectMapper = new ObjectMapper();
    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);

    // If using JsonSchema to generate HTML5 GUI:
    // JsonSchemaGenerator html5 = new JsonSchemaGenerator(objectMapper, JsonSchemaConfig.getHtml5EnabledSchema() );

    // If you want to confioure it manually:
    // JsonSchemaConfig config = new JsonSchemaConfig(...);
    // JsonSchemaGenerator generator = new JsonSchemaGenerator(objectMapper, config);

    JsonNode jsonSchema = jsonSchemaGenerator.generateJsonSchema(YourPOJO.class);

    String jsonSchemaAsString = objectMapper.writeValueAsString(jsonSchema);
```
