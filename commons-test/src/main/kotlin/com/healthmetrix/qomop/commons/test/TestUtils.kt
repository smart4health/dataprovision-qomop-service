package com.healthmetrix.qomop.commons.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

class TestUtils {

    val kMapper: ObjectMapper =
        ObjectMapper().registerModule(KotlinModule.Builder().build()).registerModule(JavaTimeModule())

    inline fun <reified T : Any> fromJsonFile(file: String): T {
        return kMapper.readValue(javaClass.classLoader.getResource(file)!!.readText(), T::class.java)
    }

    inline fun <reified T : Any> fromJsonString(content: String): T = kMapper.readValue(content, T::class.java)

    fun toJsonString(obj: Any): String = kMapper.writeValueAsString(obj)
}
