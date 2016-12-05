package com.kjetland.jackson.jsonSchema.testDataKotlin

import com.kjetland.jackson.jsonSchema.testData.MyEnum

data class ClassNotExtendingAnythingKotlin(var someString:String,
                                          var myEnum: MyEnum,
                                          var myEnumO: MyEnum?)
