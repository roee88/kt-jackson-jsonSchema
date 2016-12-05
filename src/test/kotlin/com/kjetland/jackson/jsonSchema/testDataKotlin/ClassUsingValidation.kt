package com.kjetland.jackson.jsonSchema.testDataKotlin

import javax.validation.constraints.*

data class ClassUsingValidation
(
  @NotNull
  var stringUsingNotNull:String,

  @Size(min=1, max=20)
  var stringUsingSize:String,

  @Size(min=1)
  var stringUsingSizeOnlyMin:String,

  @Size(max=30)
  var stringUsingSizeOnlyMax:String,

  @Pattern(regexp = "_stringUsingPatternA|_stringUsingPatternB")
  var stringUsingPattern:String,

  @Min(1)
  var intMin:Int,
  @Max(10)
  var intMax:Int,
  @Min(1)
  var doubleMin:Double,
  @Max(10)
  var doubleMax:Double

)
