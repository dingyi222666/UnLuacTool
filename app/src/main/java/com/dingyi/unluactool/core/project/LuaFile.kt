package com.dingyi.unluactool.core.project

import java.io.File

class LuaFile(
    private val file: File
) {


    private lateinit var fileType: LuaFileType

    init {

    }


    fun getFileType(): LuaFileType {
        return fileType
    }




}


enum class LuaFileType {
    COMPILED,RAW,UNKNOWN,ANDROLUA_BASE64_COMPILED,ANDROLUA_OLD_COMPILED
}