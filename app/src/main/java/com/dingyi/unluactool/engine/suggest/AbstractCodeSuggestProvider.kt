package com.dingyi.unluactool.engine.suggest

import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.text.CharPosition
import org.apache.commons.vfs2.FileObject

interface AbstractCodeSuggestProvider {


    fun canProvide(file: FileObject): Boolean

    fun completion(
        file: FileObject,
        prefix: String,
        cursor: CharPosition
    ): List<SimpleCompletionItem> {
        return emptyList()
    }

    fun codeNavigation(file: FileObject): List<CodeNavigation> {
        return emptyList()
    }
}

data class CodeNavigation(
    val kind: CompletionItemKind,
    val name: String,
    val kindName: String = kind.name.lowercase(),
    val position: CharPosition
)