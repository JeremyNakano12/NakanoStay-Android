package com.nakanostay.data.network

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.math.BigDecimal
import java.lang.reflect.Type

class BigDecimalAdapter : JsonSerializer<BigDecimal>, JsonDeserializer<BigDecimal> {
    override fun serialize(src: BigDecimal?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.toString())
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): BigDecimal {
        return BigDecimal(json?.asString)
    }
}