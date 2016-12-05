package com.kjetland.jackson.jsonSchema.testDataKotlin

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime

data class ManyDates
(
        var javaLocalDateTime:LocalDateTime,
        var javaOffsetDateTime:OffsetDateTime,
        var javaLocalDate:LocalDate,
        var jodaLocalDate:org.joda.time.LocalDate
)
