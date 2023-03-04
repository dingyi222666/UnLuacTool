package com.dingyi.unluactool.core.editor

import android.os.Bundle
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.engine.lua.decompile.DecompileService
import com.dingyi.unluactool.engine.suggest.CodeSuggestService
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.completion.CompletionHelper
import io.github.rosemoe.sora.lang.completion.CompletionPublisher
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.text.CharPosition
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.util.MyCharacter
import io.github.rosemoe.sora.widget.SymbolPairMatch
import org.apache.commons.vfs2.FileObject

class WrapperLanguage(
    private val wrapperLanguage: Language,
    private val targetFile: FileObject
) : EmptyLanguage() {

    private val codeSuggestService by lazy {
        MainApplication.instance
            .globalServiceRegistry
            .get<CodeSuggestService>()
    }


    var isRequireAutoComplete = true

    override fun getAnalyzeManager(): AnalyzeManager {
        return wrapperLanguage.analyzeManager
    }

    override fun getSymbolPairs(): SymbolPairMatch = wrapperLanguage.symbolPairs

    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int {
        return wrapperLanguage.getIndentAdvance(content, line, column)
    }

    override fun requireAutoComplete(
        content: ContentReference,
        position: CharPosition,
        publisher: CompletionPublisher,
        extraArguments: Bundle
    ) {
        if (!isRequireAutoComplete) {
            return
        }
        val prefix =
            CompletionHelper.computePrefix(content, position, MyCharacter::isJavaIdentifierPart)
        publisher.addItems(codeSuggestService.completion(targetFile, prefix, position))

        publisher.updateList()
    }

    override fun getInterruptionLevel(): Int {
        return wrapperLanguage.interruptionLevel
    }

    override fun getNewlineHandlers(): Array<NewlineHandler>? {
        return wrapperLanguage.newlineHandlers
    }


}