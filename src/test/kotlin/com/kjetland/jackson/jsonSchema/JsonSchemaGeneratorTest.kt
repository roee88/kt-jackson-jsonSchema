package com.kjetland.jackson.jsonSchema

import ai.mapper.ktjsonschema.JsonSchemaConfig
import ai.mapper.ktjsonschema.JsonSchemaGenerator
import ai.mapper.ktjsonschema.issue_24.model.entities.EntityWrapper
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.kjetland.jackson.jsonSchema.testData.*
import com.kjetland.jackson.jsonSchema.testData.mixin.MixinChild1
import com.kjetland.jackson.jsonSchema.testData.mixin.MixinModule
import com.kjetland.jackson.jsonSchema.testData.mixin.MixinParent
import com.kjetland.jackson.jsonSchema.testDataKotlin.*
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

class JsonSchemaGeneratorTest() {
    val _objectMapper = ObjectMapper()
    val _objectMapperKotlin = ObjectMapper()
    val mixinModule = MixinModule()

    val jsonSchemaGenerator = JsonSchemaGenerator(_objectMapper, debug = true)
    val jsonSchemaGeneratorHTML5 = JsonSchemaGenerator(_objectMapper, debug = true, config = JsonSchemaConfig.html5EnabledSchema)
    val jsonSchemaGeneratorKotlin = JsonSchemaGenerator(_objectMapperKotlin, debug = true)
    val jsonSchemaGeneratorKotlinHTML5 = JsonSchemaGenerator(_objectMapperKotlin, debug = true, config = JsonSchemaConfig.html5EnabledSchema)

    val vanillaJsonSchemaDraft4WithIds = JsonSchemaConfig.html5EnabledSchema.copy(useTypeIdForDefinitionName = true)
    val jsonSchemaGeneratorWithIds = JsonSchemaGenerator(_objectMapperKotlin, debug = true, config = vanillaJsonSchemaDraft4WithIds)

    val testData = TestData()

    init {
        _objectMapperKotlin.registerModule(KotlinModule())
        listOf(_objectMapper, _objectMapperKotlin).forEach {
            om ->
            val simpleModule = SimpleModule()
            simpleModule.addSerializer(PojoWithCustomSerializer::class.java, PojoWithCustomSerializerSerializer())
            simpleModule.addDeserializer(PojoWithCustomSerializer::class.java, PojoWithCustomSerializerDeserializer())
            om.registerModule(simpleModule)

            om.registerModule(JavaTimeModule())
            om.registerModule(Jdk8Module())
            om.registerModule(JodaModule())

            // For the mixin-test
            om.registerModule(mixinModule)

            om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            om.setTimeZone(TimeZone.getDefault())
        }
    }

    fun asPrettyJson(node: JsonNode, om: ObjectMapper): String {
        return om.writerWithDefaultPrettyPrinter().writeValueAsString(node)
    }


    // Asserts that we're able to go from object -> json -> equal object
    fun assertToFromJson(g: JsonSchemaGenerator, o: Any): JsonNode {
        return assertToFromJson(g, o, o.javaClass)
    }

    // Asserts that we're able to go from object -> json -> equal object
    // deserType might be a class which o extends (polymorphism)
    fun assertToFromJson(g: JsonSchemaGenerator, o: Any, deserType: Class<*>): JsonNode {
        val json = g.rootObjectMapper.writeValueAsString(o)
        println("json: $json")
        val jsonNode = g.rootObjectMapper.readTree(json)
        val r = g.rootObjectMapper.treeToValue(jsonNode, deserType)
        Assert.assertEquals(o, r)
        return jsonNode
    }

    fun useSchema(jsonSchema: JsonNode, jsonToTestAgainstSchema: JsonNode? = null): Unit {
        val schemaValidator = JsonSchemaFactory.byDefault().getJsonSchema(jsonSchema)
        jsonToTestAgainstSchema?.let {
            node ->
            val r = schemaValidator.validate(node)
            if (!r.isSuccess) {
                throw Exception("json does not validate against schema: " + r)
            }

        }
    }

    // Generates schema, validates the schema using external schema validator and
    // Optionally tries to validate json against the schema.
    fun generateAndValidateSchema(g: JsonSchemaGenerator, clazz: Class<*>, jsonToTestAgainstSchema: JsonNode? = null): JsonNode {
        val schema = g.generateJsonSchema(clazz)

        println("--------------------------------------------")
        println(asPrettyJson(schema, g.rootObjectMapper))

        Assert.assertEquals(JsonSchemaGenerator.JSON_SCHEMA_DRAFT_4_URL,
                schema.at("/\$schema").asText())

        useSchema(schema, jsonToTestAgainstSchema)

        return schema
    }

    fun assertJsonSubTypesInfo(node: JsonNode, typeParamName: String, typeName: String, html5Checks: Boolean = false): Unit {
        /*
      "propertie" : {
        "type" : {
          "type" : "string",
          "enum" : [ "child1" ],
          "default" : "child1"
        },
      },
      "title" : "child1",
      "required" : [ "type" ]
    */
        Assert.assertEquals(node.at("/properties/$typeParamName/type").asText(), "string")
        Assert.assertEquals(node.at("/properties/$typeParamName/enum/0").asText(), typeName)
        Assert.assertEquals(node.at("/properties/$typeParamName/default").asText(), typeName)
        Assert.assertEquals(node.at("/title").asText(), typeName)
        Assert.assertTrue(getRequiredlistOf(node).contains(typeParamName))

        if (html5Checks) {
            Assert.assertEquals(node.at("/properties/$typeParamName/options/hidden").asBoolean(), true)
        }

    }

    fun getArrayNodeAsListOfStrings(node: JsonNode): List<String> {
        when (node.javaClass) {
            ArrayNode::class.java -> return node.map { it.asText() }
        }
        return listOf()
    }

    fun getRequiredlistOf(node: JsonNode): List<String> {
        return getArrayNodeAsListOfStrings(node.at("/required"))
    }

    fun getNodeViaArrayOfRefs(root: JsonNode, pathToArrayOfRefs: String, definitionName: String): JsonNode {
        val nodeWhereArrayOfRefsIs: ArrayNode = root.at(pathToArrayOfRefs) as ArrayNode
        val arrayItemNodes = nodeWhereArrayOfRefsIs.iterator().asSequence()
        val ref = arrayItemNodes.map { it.get("\$ref").asText() }.find { it.endsWith("/$definitionName") }
        // use ref to look the node up
        val fixedRef = ref?.substring(1) // Removing starting #
        return root.at(fixedRef)
    }

    fun getNodeViaRefs(root: JsonNode, nodeWithRef: JsonNode, definitionName: String): ObjectNode {
        val ref = nodeWithRef.at("/\$ref").asText()
        Assert.assertTrue(ref.endsWith("/$definitionName"))
        // use ref to look the node up
        val fixedRef = ref.substring(1) // Removing starting #
        return root.at(fixedRef) as ObjectNode
    }

    @Test
    fun GenerateSchemeForPlainClassNotUsingJsonTypeInfo(): Unit {
        val enumList = MyEnum.values().map(MyEnum::toString)

        run {
            val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.classNotExtendingAnything)
            val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.classNotExtendingAnything.javaClass, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/someString/type").asText(), "string")

            Assert.assertEquals(schema.at("/properties/myEnum/type").asText(), "string")
            Assert.assertEquals(getArrayNodeAsListOfStrings(schema.at("/properties/myEnum/enum")), enumList)
        }

        run {
            val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlin, testData.classNotExtendingAnythingKotlin)
            val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlin, testData.classNotExtendingAnythingKotlin.javaClass, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/someString/type").asText(), "string")

            Assert.assertEquals(schema.at("/properties/myEnum/type").asText(), "string")
            Assert.assertEquals(getArrayNodeAsListOfStrings(schema.at("/properties/myEnum/enum")), enumList)
            Assert.assertEquals(getArrayNodeAsListOfStrings(schema.at("/properties/myEnumO/enum")), enumList)
        }
    }

    @Test
    fun GeneratingSchemaForConcreteClassWhichHappensToExtendClassUsingJsonTypeInfo(): Unit {
        fun doTest(pojo: Any, clazz: Class<*>, g: JsonSchemaGenerator): Unit {
            val jsonNode = assertToFromJson(g, pojo)
            val schema = generateAndValidateSchema(g, clazz, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/parentString/type").asText(), "string")
            assertJsonSubTypesInfo(schema, "type", "child1")
        }

        doTest(testData.child1, testData.child1.javaClass, jsonSchemaGenerator)
        doTest(testData.child1Kotlin, testData.child1Kotlin.javaClass, jsonSchemaGeneratorKotlin)
    }


    @Test
    fun GenerateSchemaForRegularClassWhichHasPropertyOfClassAnnotatedWithJsonTypeInfo(): Unit {
        fun assertDefaultValues(schema: JsonNode): Unit {
            Assert.assertEquals(schema.at("/properties/stringWithDefault/type").asText(), "string")
            Assert.assertEquals(schema.at("/properties/stringWithDefault/default").asText(), "x")
            Assert.assertEquals(schema.at("/properties/intWithDefault/type").asText(), "integer")
            Assert.assertEquals(schema.at("/properties/intWithDefault/default").asInt(), 12)
            Assert.assertEquals(schema.at("/properties/booleanWithDefault/type").asText(), "boolean")
            Assert.assertEquals(schema.at("/properties/booleanWithDefault/default").asBoolean(), true)
        }

        // Java
        run {
            val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.pojoWithParent)
            val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.pojoWithParent.javaClass, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/pojoValue/type").asText(), "boolean")
            assertDefaultValues(schema)


            assertChild1(schema, "/properties/child/oneOf")
            assertChild2(schema, "/properties/child/oneOf")

        }

        // Java - html5
        run {
            val jsonNode = assertToFromJson(jsonSchemaGeneratorHTML5, testData.pojoWithParent)
            val schema = generateAndValidateSchema(jsonSchemaGeneratorHTML5, testData.pojoWithParent.javaClass, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/pojoValue/type").asText(), "boolean")
            assertDefaultValues(schema)

            assertChild1(schema, "/properties/child/oneOf", html5Checks = true)
            assertChild2(schema, "/properties/child/oneOf", html5Checks = true)

        }

        //Using fully-qualified class names
        run {

            val jsonNode = assertToFromJson(jsonSchemaGeneratorWithIds, testData.pojoWithParent)
            val schema = generateAndValidateSchema(jsonSchemaGeneratorWithIds, testData.pojoWithParent.javaClass, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/pojoValue/type").asText(), "boolean")
            assertDefaultValues(schema)

            assertChild1(schema, "/properties/child/oneOf", "com.kjetland.jackson.jsonSchema.testData.Child1", false)
            assertChild2(schema, "/properties/child/oneOf", "com.kjetland.jackson.jsonSchema.testData.Child2", false)

        }

        // Kotlin
        run {
            val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlin, testData.pojoWithParentKotlin)
            val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlin, testData.pojoWithParentKotlin.javaClass, jsonNode)

            Assert.assertEquals(false, schema.at("/additionalPropertie").asBoolean())
            Assert.assertEquals(schema.at("/properties/pojoValue/type").asText(), "boolean")
            assertDefaultValues(schema)

            assertChild1(schema, "/properties/child/oneOf", "Child1Kotlin")
            assertChild2(schema, "/properties/child/oneOf", "Child2Kotlin")

        }
    }

    fun assertChild1(node: JsonNode, path: String, defName: String = "Child1", html5Checks: Boolean = false): Unit {
        val child1 = getNodeViaArrayOfRefs(node, path, defName)
        assertJsonSubTypesInfo(child1, "type", "child1", html5Checks)
        Assert.assertEquals(child1.at("/properties/parentString/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/child1String/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String2/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String3/type").asText(), "string")
        Assert.assertTrue(getRequiredlistOf(child1).contains("_child1String3"))
    }

    fun assertChild2(node: JsonNode, path: String, defName: String = "Child2", html5Checks: Boolean = false): Unit {
        val child2 = getNodeViaArrayOfRefs(node, path, defName)
        assertJsonSubTypesInfo(child2, "type", "child2", html5Checks)
        Assert.assertEquals(child2.at("/properties/parentString/type").asText(), "string")
        Assert.assertEquals(child2.at("/properties/child2int/type").asText(), "integer")
    }

    @Test
    fun GenerateSchemaForSuperClassAnnotatedWithJsonTypeInfo(): Unit {
        // Java
        run {
            val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.child1)
            assertToFromJson(jsonSchemaGenerator, testData.child1, Parent::class.java)

            val schema = generateAndValidateSchema(jsonSchemaGenerator, Parent::class.java, jsonNode)

            assertChild1(schema, "/oneOf")
            assertChild2(schema, "/oneOf")
        }

        // Kotlin
        run {
            val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlin, testData.child1Kotlin)
            assertToFromJson(jsonSchemaGeneratorKotlin, testData.child1Kotlin, ParentKotlin::class.java)

            val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlin, ParentKotlin::class.java, jsonNode)

            assertChild1(schema, "/oneOf", "Child1Kotlin")
            assertChild2(schema, "/oneOf", "Child2Kotlin")
        }

    }

    @Test
    fun primitive(): Unit {

        // java
        run {
            val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.manyPrimitives)
            val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.manyPrimitives.javaClass, jsonNode)

            Assert.assertEquals(schema.at("/properties/_string/type").asText(), "string")

            Assert.assertEquals(schema.at("/properties/_integer/type").asText(), "integer")
            Assert.assertTrue(!getRequiredlistOf(schema).contains("_integer")) // Should allow null by default

            Assert.assertEquals(schema.at("/properties/_int/type").asText(), "integer")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_int")) // Must have a value

            Assert.assertEquals(schema.at("/properties/_booleanObject/type").asText(), "boolean")
            Assert.assertTrue(!getRequiredlistOf(schema).contains("_booleanObject")) // Should allow null by default

            Assert.assertEquals(schema.at("/properties/_booleanPrimitive/type").asText(), "boolean")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_booleanPrimitive")) // Must be required since it must have true or false - not null

            Assert.assertEquals(schema.at("/properties/_booleanObjectWithNotNull/type").asText(), "boolean")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_booleanObjectWithNotNull"))

            Assert.assertEquals(schema.at("/properties/_doubleObject/type").asText(), "number")
            Assert.assertTrue(!getRequiredlistOf(schema).contains("_doubleObject")) // Should allow null by default

            Assert.assertEquals(schema.at("/properties/_doublePrimitive/type").asText(), "number")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_doublePrimitive")) // Must be required since it must have a value - not null

            Assert.assertEquals(schema.at("/properties/myEnum/type").asText(), "string")
            Assert.assertEquals(getArrayNodeAsListOfStrings(schema.at("/properties/myEnum/enum")), MyEnum.values().map { it.toString() })
        }

        // scala
        run {
            val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlin, testData.manyPrimitivesKotlin)
            val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlin, testData.manyPrimitivesKotlin.javaClass, jsonNode)

            Assert.assertEquals(schema.at("/properties/_string/type").asText(), "string")

            Assert.assertEquals(schema.at("/properties/_integer/type").asText(), "integer")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_integer")) // Should allow null by default

            Assert.assertEquals(schema.at("/properties/_boolean/type").asText(), "boolean")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_boolean")) // Should allow null by default

            Assert.assertEquals(schema.at("/properties/_double/type").asText(), "number")
            Assert.assertTrue(getRequiredlistOf(schema).contains("_double")) // Should allow null by default
        }
    }

    @Test
    fun kotlinUsingOption(): Unit {

        val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlin, testData.pojoUsingOptionKotlin)
        val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlin, testData.pojoUsingOptionKotlin.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/_string/type").asText(), "string")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_string")) // Should allow null by default

        Assert.assertEquals(schema.at("/properties/_integer/type").asText(), "integer")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_integer")) // Should allow null by default

        Assert.assertEquals(schema.at("/properties/_boolean/type").asText(), "boolean")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_boolean")) // Should allow null by default

        Assert.assertEquals(schema.at("/properties/_double/type").asText(), "number")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_double")) // Should allow null by default

        val child1 = getNodeViaRefs(schema, schema.at("/properties/child1"), "Child1Kotlin")

        assertJsonSubTypesInfo(child1, "type", "child1")
        Assert.assertEquals(child1.at("/properties/parentString/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/child1String/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String2/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String3/type").asText(), "string")

        Assert.assertEquals(schema.at("/properties/optionalList/type").asText(), "array")
        Assert.assertEquals(schema.at("/properties/optionalList/items/\$ref").asText(), "#/definitions/ClassNotExtendingAnythingKotlin")

    }

    @Test
    fun javaUsingOption() {

        val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.pojoUsingOptionalJava)
        val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.pojoUsingOptionalJava.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/_string/type").asText(), "string")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_string")) // Should allow null by default

        Assert.assertEquals(schema.at("/properties/_integer/type").asText(), "integer")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_integer")) // Should allow null by default

        val child1 = getNodeViaRefs(schema, schema.at("/properties/child1"), "Child1")

        assertJsonSubTypesInfo(child1, "type", "child1")
        Assert.assertEquals(child1.at("/properties/parentString/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/child1String/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String2/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String3/type").asText(), "string")

        Assert.assertEquals(schema.at("/properties/optionalList/type").asText(), "array")
        Assert.assertEquals(schema.at("/properties/optionalList/items/\$ref").asText(), "#/definitions/ClassNotExtendingAnything")

    }

    @Test
    fun customSerializerNotOverridingJsonSerializerAcceptJsonFormatVisitor() {

        val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.pojoWithCustomSerializer)
        val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.pojoWithCustomSerializer.javaClass, jsonNode)
        Assert.assertEquals((schema as ObjectNode).fieldNames().asSequence().toList(),
                listOf("\$schema", "title")) // Empty schema due to custom serializer
    }

    @Test
    fun objectWithPropertyUsingCustomSerializerNotOverridingJsonSerializerAcceptJsonFormatVisitor() {

        val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.objectWithPropertyWithCustomSerializer)
        val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.objectWithPropertyWithCustomSerializer.javaClass, jsonNode)
        Assert.assertEquals(schema.at("/properties/s/type").asText(), "string")
        Assert.assertTrue(!(schema.at("/properties/child") as ObjectNode).fieldNames().hasNext())
    }


    @Test
    fun pojoWithArray() {

        fun doTest(pojo: Any, clazz: Class<*>, g: JsonSchemaGenerator): Unit {

            val jsonNode = assertToFromJson(g, pojo)
            val schema = generateAndValidateSchema(g, clazz, jsonNode)

            Assert.assertEquals(schema.at("/properties/intArray1/type").asText(), "array")
            Assert.assertEquals(schema.at("/properties/intArray1/items/type").asText(), "integer")

            Assert.assertEquals(schema.at("/properties/stringArray/type").asText(), "array")
            Assert.assertEquals(schema.at("/properties/stringArray/items/type").asText(), "string")

            Assert.assertEquals(schema.at("/properties/stringList/type").asText(), "array")
            Assert.assertEquals(schema.at("/properties/stringList/items/type").asText(), "string")

            Assert.assertEquals(schema.at("/properties/polymorphismList/type").asText(), "array")
            assertChild1(schema, "/properties/polymorphismList/items/oneOf")
            assertChild2(schema, "/properties/polymorphismList/items/oneOf")

            Assert.assertEquals(schema.at("/properties/polymorphismArray/type").asText(), "array")
            assertChild1(schema, "/properties/polymorphismArray/items/oneOf")
            assertChild2(schema, "/properties/polymorphismArray/items/oneOf")

            Assert.assertEquals(schema.at("/properties/listOfListOfStrings/type").asText(), "array")
            Assert.assertEquals(schema.at("/properties/listOfListOfStrings/items/type").asText(), "array")
            Assert.assertEquals(schema.at("/properties/listOfListOfStrings/items/items/type").asText(), "string")
        }

        doTest(testData.pojoWithArrays, testData.pojoWithArrays.javaClass, jsonSchemaGenerator)
        doTest(testData.pojoWithArraysKotlin, testData.pojoWithArraysKotlin.javaClass, jsonSchemaGeneratorKotlin)

    }

    @Test
    fun recursivePojo() {
        val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.recursivePojo)
        val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.recursivePojo.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/myText/type").asText(), "string")

        Assert.assertEquals(schema.at("/properties/children/type").asText(), "array")
        val defViaRef = getNodeViaRefs(schema, schema.at("/properties/children/items"), "RecursivePojo")

        Assert.assertEquals(defViaRef.at("/properties/myText/type").asText(), "string")
        Assert.assertEquals(defViaRef.at("/properties/children/type").asText(), "array")
        val defViaRef2 = getNodeViaRefs(schema, defViaRef.at("/properties/children/items"), "RecursivePojo")

        Assert.assertEquals(defViaRef, defViaRef2)

    }

    @Test
    fun pojoUsingMap() {
        val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.pojoUsingMaps)
        val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.pojoUsingMaps.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/string2Integer/type").asText(), "object")
        Assert.assertEquals(schema.at("/properties/string2Integer/additionalProperties/type").asText(), "integer")

        Assert.assertEquals(schema.at("/properties/string2String/type").asText(), "object")
        Assert.assertEquals(schema.at("/properties/string2String/additionalProperties/type").asText(), "string")

        Assert.assertEquals(schema.at("/properties/string2PojoUsingJsonTypeInfo/type").asText(), "object")
        Assert.assertEquals(schema.at("/properties/string2PojoUsingJsonTypeInfo/additionalProperties/oneOf/0/\$ref").asText(), "#/definitions/Child1")
        Assert.assertEquals(schema.at("/properties/string2PojoUsingJsonTypeInfo/additionalProperties/oneOf/1/\$ref").asText(), "#/definitions/Child2")
    }

    @Test
    fun pojoUsingCustomAnnotation() {
        val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.pojoUsingFormat)
        val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.pojoUsingFormat.javaClass, jsonNode)
        val schemaHTML5Date = generateAndValidateSchema(jsonSchemaGeneratorHTML5, testData.pojoUsingFormat.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/format").asText(), "grid")
        Assert.assertEquals(schema.at("/description").asText(), "This is our pojo")
        Assert.assertEquals(schema.at("/title").asText(), "Pojo using format")


        Assert.assertEquals(schema.at("/properties/emailValue/type").asText(), "string")
        Assert.assertEquals(schema.at("/properties/emailValue/format").asText(), "email")
        Assert.assertEquals(schema.at("/properties/emailValue/description").asText(), "This is our email value")
        Assert.assertEquals(schema.at("/properties/emailValue/title").asText(), "Email value")

        Assert.assertEquals(schema.at("/properties/choice/type").asText(), "boolean")
        Assert.assertEquals(schema.at("/properties/choice/format").asText(), "checkbox")

        Assert.assertEquals(schema.at("/properties/dateTime/type").asText(), "string")
        Assert.assertEquals(schema.at("/properties/dateTime/format").asText(), "date-time")
        Assert.assertEquals(schema.at("/properties/dateTime/description").asText(), "This is description from @JsonPropertyDescription")
        Assert.assertEquals(schemaHTML5Date.at("/properties/dateTime/format").asText(), "datetime")


        Assert.assertEquals(schema.at("/properties/dateTimeWithAnnotation/type").asText(), "string")
        Assert.assertEquals(schema.at("/properties/dateTimeWithAnnotation/format").asText(), "text")

        // Make sure autoGenerated title is correct
        Assert.assertEquals(schemaHTML5Date.at("/properties/dateTimeWithAnnotation/title").asText(), "Date Time With Annotation")


    }


//    @Test // TODO: this is removed because kotlin has no Option
    fun kotlinUsingOptionWithHTML5() {

        val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlinHTML5, testData.pojoUsingOptionKotlin)
        val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlinHTML5, testData.pojoUsingOptionKotlin.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/_string/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/_string/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/_string/oneOf/1/type").asText(), "string")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_string")) // Should allow null by default
        Assert.assertEquals(schema.at("/properties/_string/title").asText(), "_string")

        Assert.assertEquals(schema.at("/properties/_integer/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/_integer/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/_integer/oneOf/1/type").asText(), "integer")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_integer")) // Should allow null by default
        Assert.assertEquals(schema.at("/properties/_integer/title").asText(), "_integer")

        Assert.assertEquals(schema.at("/properties/_boolean/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/_boolean/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/_boolean/oneOf/1/type").asText(), "boolean")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_boolean")) // Should allow null by default
        Assert.assertEquals(schema.at("/properties/_boolean/title").asText(), "_boolean")

        Assert.assertEquals(schema.at("/properties/_double/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/_double/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/_double/oneOf/1/type").asText(), "number")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_double")) // Should allow null by default
        Assert.assertEquals(schema.at("/properties/_double/title").asText(), "_double")

        Assert.assertEquals(schema.at("/properties/child1/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/child1/oneOf/0/title").asText(), "Not included")
        val child1 = getNodeViaRefs(schema, schema.at("/properties/child1/oneOf/1"), "Child1Kotlin")
        Assert.assertEquals(schema.at("/properties/child1/title").asText(), "Child 1")

        assertJsonSubTypesInfo(child1, "type", "child1")
        Assert.assertEquals(child1.at("/properties/parentString/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/child1String/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String2/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String3/type").asText(), "string")

        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/1/type").asText(), "array")
        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/1/items/\$ref").asText(), "#/definitions/ClassNotExtendingAnythingKotlin")
        Assert.assertEquals(schema.at("/properties/optionalList/title").asText(), "Optional List")
    }

    @Test
    fun javaUsingOptionalWithHTML5() {
        val jsonNode = assertToFromJson(jsonSchemaGeneratorHTML5, testData.pojoUsingOptionalJava)
        val schema = generateAndValidateSchema(jsonSchemaGeneratorHTML5, testData.pojoUsingOptionalJava.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/_string/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/_string/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/_string/oneOf/1/type").asText(), "string")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_string")) // Should allow null by default
        Assert.assertEquals(schema.at("/properties/_string/title").asText(), "_string")

        Assert.assertEquals(schema.at("/properties/_integer/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/_integer/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/_integer/oneOf/1/type").asText(), "integer")
        Assert.assertTrue(!getRequiredlistOf(schema).contains("_integer")) // Should allow null by default
        Assert.assertEquals(schema.at("/properties/_integer/title").asText(), "_integer")


        Assert.assertEquals(schema.at("/properties/child1/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/child1/oneOf/0/title").asText(), "Not included")
        val child1 = getNodeViaRefs(schema, schema.at("/properties/child1/oneOf/1"), "Child1")
        Assert.assertEquals(schema.at("/properties/child1/title").asText(), "Child 1")

        assertJsonSubTypesInfo(child1, "type", "child1")
        Assert.assertEquals(child1.at("/properties/parentString/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/child1String/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String2/type").asText(), "string")
        Assert.assertEquals(child1.at("/properties/_child1String3/type").asText(), "string")

        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/0/type").asText(), "null")
        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/0/title").asText(), "Not included")
        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/1/type").asText(), "array")
        Assert.assertEquals(schema.at("/properties/optionalList/oneOf/1/items/\$ref").asText(), "#/definitions/ClassNotExtendingAnything")
        Assert.assertEquals(schema.at("/properties/optionalList/title").asText(), "Optional List")
    }

    @Test
    fun propertyOrdering() {
        run {
            val jsonNode = assertToFromJson(jsonSchemaGeneratorHTML5, testData.classNotExtendingAnything)
            val schema = generateAndValidateSchema(jsonSchemaGeneratorHTML5, testData.classNotExtendingAnything.javaClass, jsonNode)

            Assert.assertEquals(schema.at("/properties/someString/propertyOrder").asInt(), 1)
            Assert.assertEquals(schema.at("/properties/myEnum/propertyOrder").asInt(), 2)
        }

        // Make sure propertyOrder is not enabled when not using html5
        run {
            val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.classNotExtendingAnything)
            val schema = generateAndValidateSchema(jsonSchemaGenerator, testData.classNotExtendingAnything.javaClass, jsonNode)

            Assert.assertEquals(schema.at("/properties/someString/propertyOrder").isMissingNode, true)
        }
    }

    @Test
    fun date() {

        val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlinHTML5, testData.manyDates)
        val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlinHTML5, testData.manyDates.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/javaLocalDateTime/format").asText(), "datetime-local")
        Assert.assertEquals(schema.at("/properties/javaOffsetDateTime/format").asText(), "datetime")
        Assert.assertEquals(schema.at("/properties/javaLocalDate/format").asText(), "date")
        Assert.assertEquals(schema.at("/properties/jodaLocalDate/format").asText(), "date")

    }

    @Test
    fun validation() {

        val jsonNode = assertToFromJson(jsonSchemaGeneratorKotlinHTML5, testData.classUsingValidation)
        val schema = generateAndValidateSchema(jsonSchemaGeneratorKotlinHTML5, testData.classUsingValidation.javaClass, jsonNode)

        Assert.assertEquals(schema.at("/properties/stringUsingNotNull/minLength").asInt(), 1)
        Assert.assertTrue(schema.at("/properties/stringUsingNotNull/maxLength").isMissingNode)

        Assert.assertEquals(schema.at("/properties/stringUsingSize/minLength").asInt(), 1)
        Assert.assertEquals(schema.at("/properties/stringUsingSize/maxLength").asInt(), 20)

        Assert.assertEquals(schema.at("/properties/stringUsingSizeOnlyMin/minLength").asInt(), 1)
        Assert.assertTrue(schema.at("/properties/stringUsingSizeOnlyMin/maxLength").isMissingNode)

        Assert.assertEquals(schema.at("/properties/stringUsingSizeOnlyMax/maxLength").asInt(), 30)
        Assert.assertTrue(schema.at("/properties/stringUsingSizeOnlyMax/minLength").isMissingNode)

        Assert.assertEquals(schema.at("/properties/stringUsingPattern/pattern").asText(), "_stringUsingPatternA|_stringUsingPatternB")

        Assert.assertEquals(schema.at("/properties/intMin/minimum").asInt(), 1)
        Assert.assertEquals(schema.at("/properties/intMax/maximum").asInt(), 10)

        Assert.assertEquals(schema.at("/properties/doubleMin/minimum").asInt(), 1)
        Assert.assertEquals(schema.at("/properties/doubleMax/maximum").asInt(), 10)
    }

    @Test
    fun PolymorphismUsingMixin() {
        // Java
        run {
            val jsonNode = assertToFromJson(jsonSchemaGenerator, testData.mixinChild1)
            assertToFromJson(jsonSchemaGenerator, testData.mixinChild1, MixinParent::class.java)

            val schema = generateAndValidateSchema(jsonSchemaGenerator, MixinParent::class.java, jsonNode)

            assertChild1(schema, "/oneOf", defName = "MixinChild1")
            assertChild2(schema, "/oneOf", defName = "MixinChild2")
        }
    }

    @Test
    fun issue24() {
        jsonSchemaGenerator.generateJsonSchema(EntityWrapper::class.java)
    }
}

class TestData {
    val child1 = {
        val c = Child1()
        c.parentString = "pv"
        c.child1String = "c"
        c.child1String2 = "cs2"
        c.child1String3 = "cs3"
        c
    }.invoke()

    val child1Kotlin = Child1Kotlin("pv", "c", "cs2", "cs3")

    val child2 = {
        val c = Child2()
        c.parentString = "pv"
        c.child2int = 12
        c
    }.invoke()

    val child2Kotlin = Child2Kotlin("pv", 12)

    val pojoWithParent = {
        val p = PojoWithParent()
        p.pojoValue = true
        p.child = child1
        p.stringWithDefault = "y"
        p.intWithDefault = 13
        p.booleanWithDefault = true
        p
    }.invoke()

    val pojoWithParentKotlin = PojoWithParentKotlin(true, child1Kotlin, "y", 13, true)

    val classNotExtendingAnything = {
        val o = ClassNotExtendingAnything()
        o.someString = "Something"
        o.myEnum = MyEnum.C
        o
    }.invoke()

    val classNotExtendingAnythingKotlin = ClassNotExtendingAnythingKotlin("Something", MyEnum.C, MyEnum.A)

    val manyPrimitives = ManyPrimitives("s1", 1, 2, true, false, true, 0.1, 0.2, MyEnum.B)

    val manyPrimitivesKotlin = ManyPrimitivesKotlin("s1", 1, true, 0.1)

    val pojoUsingOptionKotlin = PojoUsingOptionKotlin("s1", 1, true, 0.1, child1Kotlin, listOf(classNotExtendingAnythingKotlin))

    val pojoUsingOptionalJava = PojoUsingOptionalJava(Optional.of(""),
            Optional.of(1), Optional.of(child1),
            Optional.of(Arrays.asList(classNotExtendingAnything)))

    val pojoWithCustomSerializer = {
        val p = PojoWithCustomSerializer()
        p.myString = "xxx"
        p
    }.invoke()

    val objectWithPropertyWithCustomSerializer = ObjectWithPropertyWithCustomSerializer("s1", pojoWithCustomSerializer)

    val pojoWithArrays = PojoWithArrays(
            intArrayOf(1, 2, 3),
            arrayOf("a1", "a2", "a3"),
            mutableListOf("l1", "l2", "l3"),
            listOf(child1, child2),
            arrayOf(child1, child2),
            listOf(classNotExtendingAnything, classNotExtendingAnything),
            listOf(listOf("1", "2"), listOf("3"))
    )

    val pojoWithArraysKotlin = PojoWithArraysKotlin(
            listOf(1, 2, 3),
            listOf("a1", "a2", "a3"),
            listOf("l1", "l2", "l3"),
            listOf(child1, child2),
            listOf(child1, child2),
            listOf(classNotExtendingAnything, classNotExtendingAnything),
            listOf(listOf("l11", "l12"), listOf("l21"))
    )

    val recursivePojo = RecursivePojo("t1", listOf(RecursivePojo("c1", null)))

    val pojoUsingMaps = PojoUsingMaps(
            mapOf("a" to 1, "b" to 2),
            mapOf("x" to "y", "z" to "w"),
            mapOf("1" to child1, "2" to child2)
    )

    val pojoUsingFormat = PojoUsingFormat("test@example.com", true, OffsetDateTime.now(), OffsetDateTime.now())
    val manyDates = ManyDates(LocalDateTime.now(), OffsetDateTime.now(), LocalDate.now(), org.joda.time.LocalDate.now())

    val classUsingValidation = ClassUsingValidation(
            "_stringUsingNotNull", "_stringUsingSize", "_stringUsingSizeOnlyMin", "_stringUsingSizeOnlyMax", "_stringUsingPatternA",
            1, 2, 1.0, 2.0
    )

    val mixinChild1 = {
        val c = MixinChild1()
        c.parentString = "pv"
        c.child1String = "c"
        c.child1String2 = "cs2"
        c.child1String3 = "cs3"
        c
    }.invoke()
}





















