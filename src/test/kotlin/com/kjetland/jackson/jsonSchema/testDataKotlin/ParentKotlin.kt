package com.kjetland.jackson.jsonSchema.testDataKotlin

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = Child1Kotlin::class, name = "child1"),
        JsonSubTypes.Type(value = Child2Kotlin::class, name = "child2"))
interface ParentKotlin
