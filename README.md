[![Build Status](https://travis-ci.org/roee88/kt-jackson-jsonSchema.svg?branch=master)](https://travis-ci.org/roee88/kt-jackson-jsonSchema)
[![Download](https://api.bintray.com/packages/com-dr/dr-public/KtJsonSchema/images/download.svg) ](https://bintray.com/com-dr/dr-public/KtJsonSchema/_latestVersion)
[![Licence](https://img.shields.io/badge/Licence-Apache2-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Jackson jsonSchema Generator /Kotlin
=====================================

This project is a quick re-implementation of [mbknor-jackson-jsonSchema](https://github.com/mbknor/mbknor-jackson-jsonSchema) by [@mbknor](https://github.com/mbknor/),
in Kotlin. 
Project is compatible with Java 6.

**Why?**
I wanted to use it in Android. 
While mbknor-jackson-jsonSchema can easily be modified to target Java 6, the Scala runtime is still quite an overhead.

**Gradle (jcenter)**
```gradle
compile 'com.dr:ktjsonschema:1.0.2'
```

**Maven (jcenter)**
```maven
<dependency>
  <groupId>com.dr</groupId>
  <artifactId>ktjsonschema</artifactId>
  <version>1.0.2</version>
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
* Supports java.lang.Optional and [java8.lang.Optional](https://github.com/streamsupport/streamsupport)

**Status**

The translation from the Scala codebase was blind and quick, so some bugs may be present, though I haven't found any (yet).

**Example (Java)**

```
    ObjectMapper objectMapper = new ObjectMapper();
    JsonSchemaGenerator jsonSchemaGenerator = new JsonSchemaGenerator(objectMapper);

    // If using JsonSchema to generate HTML5 GUI:
    // JsonSchemaGenerator html5 = new JsonSchemaGenerator(objectMapper, JsonSchemaConfig.getHtml5EnabledSchema() );

    // If you want to configure it manually:
    // JsonSchemaConfig config = new JsonSchemaConfig(...);
    // JsonSchemaGenerator generator = new JsonSchemaGenerator(objectMapper, config);

    JsonNode jsonSchema = jsonSchemaGenerator.generateJsonSchema(YourPOJO.class);

    String jsonSchemaAsString = objectMapper.writeValueAsString(jsonSchema);
```
