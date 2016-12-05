package com.kjetland.jackson.jsonSchema.testDataKotlin

import com.kjetland.jackson.jsonSchema.testData.ClassNotExtendingAnything
import com.kjetland.jackson.jsonSchema.testData.Parent

data class PojoWithArraysKotlin
(
        var intArray1: List<Int>?, // We never use array in scala - use list instead to make it compatible with PojoWithArrays (java)
        var stringArray:List<String>, // We never use array in scala - use list instead to make it compatible with PojoWithArrays (java)
        var stringList:List<String>,
        var polymorphismList:List<Parent>,
        var polymorphismArray:List<Parent>, // We never use array in scala - use list instead to make it compatible with PojoWithArrays (java)
        var regularObjectList:List<ClassNotExtendingAnything>,
        var listOfListOfStrings:List<List<String>>
)
