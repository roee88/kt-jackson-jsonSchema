package com.kjetland.jackson.jsonSchema.testDataKotlin

import com.fasterxml.jackson.annotation.JsonProperty

data class Child1Kotlin
(
        var parentString:String,
        var child1String:String,

        @JsonProperty("_child1String2")
        var child1String2:String,

        @JsonProperty(value = "_child1String3", required = true)
        var child1String3:String
) : ParentKotlin
