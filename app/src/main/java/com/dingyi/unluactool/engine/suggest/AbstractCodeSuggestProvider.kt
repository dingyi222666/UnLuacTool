package com.dingyi.unluactool.engine.suggest

import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import io.github.rosemoe.sora.text.CharPosition

interface AbstractCodeSuggestProvider {


    fun canProvide(file: UnLuaCFileObject): Boolean

    fun completion(file: UnLuaCFileObject, cursor: CharPosition): List<String> {
        return emptyList()
    }

    fun codeNavigation(file: UnLuaCFileObject): List<CodeNavigation> {
        return emptyList()
    }
}

data class CodeNavigation(
    val name: String,
    val position: CharPosition
)