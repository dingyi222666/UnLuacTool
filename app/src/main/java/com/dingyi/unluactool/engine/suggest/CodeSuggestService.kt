package com.dingyi.unluactool.engine.suggest

import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.engine.service.BaseServiceContainer
import io.github.rosemoe.sora.text.CharPosition

class CodeSuggestService:BaseServiceContainer<AbstractCodeSuggestProvider>() {

    override val globalConfigPath: String
        get() = "suggest-service.json"

    fun completion(file: UnLuaCFileObject, cursor: CharPosition): List<String> {
        val result = mutableListOf<String>()
        allService.forEach {
            if(it.canProvide(file)) {
                result.addAll(it.completion(file,cursor))
            }
        }
        return result
    }

    fun codeNavigation(file: UnLuaCFileObject): List<CodeNavigation> {
        val result = mutableListOf<CodeNavigation>()
        allService.forEach {
            if(it.canProvide(file)) {
                result.addAll(it.codeNavigation(file))
            }
        }
        return result
    }
}