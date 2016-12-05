package com.kjetland.jackson.jsonSchema.testDataScala

import com.dr.ktjsonschema.annotations.JsonSchemaDefault

case class PojoWithParentScala
(
  pojoValue:Boolean,
  child:ParentScala,

  @JsonSchemaDefault("x")
  stringWithDefault:String,
  @JsonSchemaDefault("12")
  intWithDefault:Int,
  @JsonSchemaDefault("true")
  booleanWithDefault:Boolean
)
