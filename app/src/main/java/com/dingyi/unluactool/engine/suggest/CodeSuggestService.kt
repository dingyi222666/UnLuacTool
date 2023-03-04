package com.dingyi.unluactool.engine.suggest

import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.engine.service.BaseServiceContainer
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.text.CharPosition
import org.apache.commons.vfs2.FileObject

class CodeSuggestService:BaseServiceContainer<AbstractCodeSuggestProvider>() {

    override val globalConfigPath: String
        get() = "suggest-service.json"

    fun completion(file: FileObject,prefix:String, cursor: CharPosition): List<SimpleCompletionItem> {
        val result = mutableListOf<SimpleCompletionItem>()
        allService.forEach {
            if(it.canProvide(file)) {
                result.addAll(it.completion(file,prefix,cursor))
            }
        }
        return result
    }

    fun codeNavigation(file: FileObject): List<CodeNavigation> {
        val result = mutableListOf<CodeNavigation>()
        allService.forEach {
            if(it.canProvide(file)) {
                result.addAll(it.codeNavigation(file))
            }
        }
        return result
    }
}