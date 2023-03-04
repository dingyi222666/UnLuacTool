package com.dingyi.unluactool.engine.suggest.lasm

import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.engine.filesystem.FileObjectType
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import com.dingyi.unluactool.engine.suggest.AbstractCodeSuggestProvider
import com.dingyi.unluactool.engine.suggest.CodeNavigation
import io.github.rosemoe.sora.lang.completion.CompletionItemKind
import io.github.rosemoe.sora.lang.completion.IdentifierAutoComplete
import io.github.rosemoe.sora.lang.completion.SimpleCompletionItem
import io.github.rosemoe.sora.text.CharPosition
import org.apache.commons.vfs2.FileObject

class LasmCodeSuggestProvider : AbstractCodeSuggestProvider {


    override fun canProvide(file: FileObject): Boolean {
        return file is UnLuaCFileObject && file.getFileType() != FileObjectType.DECOMPILE_FUNCTION
    }

    override fun completion(
        file: FileObject,
        prefix: String,
        cursor: CharPosition
    ): List<SimpleCompletionItem> {
        val result = mutableListOf<SimpleCompletionItem>()

        val prefixLength = prefix.length

        opKeywordList.forEach {
            if (it.startsWith(prefix)) {
                result.add(
                    SimpleCompletionItem(
                        it,
                        "OpCode",
                        prefixLength,
                        it
                    ).kind(CompletionItemKind.Keyword)
                )
            }
        }

        keywordList.forEach {
            if (it.startsWith(prefix)) {
                result.add(
                    SimpleCompletionItem(
                        it,
                        "Keyword",
                        prefixLength,
                        it
                    ).kind(CompletionItemKind.Keyword)
                )
            }
        }

        return result
    }

    override fun codeNavigation(file: FileObject): List<CodeNavigation> {
        return emptyList()
    }

    companion object {
        private val opKeywordList =
            "move|loadk|loadbool|loadnil|getupval|getglobal|gettable|setglobal|setupval|settable|newtable|self|add|sub|mul|div|mod|pow|unm|not|len|concat|jmp|eq|lt|le|test|testset|call|tailcall|forloop|forprep|tforloop|setlist|close|closure|vararg|jmp|loadnil|loadkx|gettabup|settabup|setlist|tforcall|tforloop|extraarg|newtable|setlist|setlisto|tforprep|test|idiv|band|bor|bxor|shl|shr|bnot|loadi|loadf|loadfalse|lfalseskip|loadtrue|gettabup|gettable|geti|getfield|settabup|settable|seti|setfield|newtable|self|addi|addk|subk|mulk|modk|powk|divk|idivk|bandk|bork|bxork|shri|shli|add|sub|mul|mod|pow|div|idiv|band|bor|bxor|shl|shr|mmbin|mmbini|mmbink|concat|tbc|jmp|eq|lt|le|eqk|eqi|lti|lei|gti|gei|test|testset|tailcall|return|return0|return1|forloop|forprep|tforprep|tforcall|tforloop|setlist|vararg|varargprep|extrabyte".split(
                "|"
            ).distinct()
        private val keywordList =
            "label|function|constant|local|upvalue|line|version|format|int_size|size_t_size|instruction_size|integer_format|float_format|endianness|linedefined|lastlinedefined|numparams|is_vararg|maxstacksize|source".split(
                "|"
            )


    }
}