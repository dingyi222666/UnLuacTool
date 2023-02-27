package com.dingyi.unluactool.core.editor

import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import io.github.rosemoe.sora.lang.EmptyLanguage
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.lang.analysis.AnalyzeManager
import io.github.rosemoe.sora.lang.smartEnter.NewlineHandler
import io.github.rosemoe.sora.text.ContentReference
import io.github.rosemoe.sora.widget.SymbolPairMatch

class WrapperLanguage(
    private val wrapperLanguage: Language,
    private val targetFile: UnLuaCFileObject
) : EmptyLanguage() {

    override fun getAnalyzeManager(): AnalyzeManager {
        return wrapperLanguage.analyzeManager
    }

    override fun getSymbolPairs(): SymbolPairMatch = wrapperLanguage.symbolPairs

    override fun getIndentAdvance(content: ContentReference, line: Int, column: Int): Int {
        return wrapperLanguage.getIndentAdvance(content, line, column)
    }

    override fun getInterruptionLevel(): Int {
        return wrapperLanguage.interruptionLevel
    }

    override fun getNewlineHandlers(): Array<NewlineHandler>? {
        return wrapperLanguage.newlineHandlers
    }


}