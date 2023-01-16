package com.dingyi.unluactool.core.util

import com.google.gson.JsonElement
import com.google.gson.JsonParser

object JsonConfigReader {

    fun readConfig(configPath: String): JsonElement {
        val configString = this.javaClass.classLoader
            .getResource("META-INF/$configPath")
            ?.openStream()
            ?.use { it.readBytes() }
            ?.decodeToString()
            .toString()

        return JsonParser.parseString(configString)
    }
}