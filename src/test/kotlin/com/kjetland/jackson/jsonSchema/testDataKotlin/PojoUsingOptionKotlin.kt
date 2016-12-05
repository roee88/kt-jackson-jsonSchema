package com.kjetland.jackson.jsonSchema.testDataKotlin

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class PojoUsingOptionKotlin(
        var _string:String?,

        @JsonDeserialize(contentAs = Int::class)
        var _integer:Int?,

        @JsonDeserialize(contentAs = Boolean::class)
        var _boolean:Boolean?,

        @JsonDeserialize(contentAs = Double::class)
        var _double:Double?,

        var child1:Child1Kotlin?,

        var optionalList:List<ClassNotExtendingAnythingKotlin>?

 //, parent:Option[ParentKotlin] - Not using this one: jackson-scala-module does not support Option combined with Polymorphism
)
