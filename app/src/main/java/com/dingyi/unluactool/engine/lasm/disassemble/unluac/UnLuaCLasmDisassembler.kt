package com.dingyi.unluactool.engine.lasm.disassemble.unluac

import com.dingyi.unluactool.engine.lasm.data.v1.AbsFunction
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.data.v1.LASMFunction
import com.dingyi.unluactool.engine.lasm.disassemble.AbstractLasmDisassembler
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import unluac.parse.BHeader
import unluac.parse.LFunction

class UnLuaCLasmDisassembler : AbstractLasmDisassembler {

    override fun disassemble(input: Any): LASMChunk {
        return disassembleMainFunction(
            if (input is BHeader) input.main
            else input as LFunction
        )
    }

    override fun isSupportDisassemble(input: Any): Boolean {
        return input is LFunction || input is BHeader
    }

    private fun disassemble(
        function: LFunction,
        parent: AbsFunction<LASMFunction>?,
        out: unluac.decompile.Output,
        name: String
    ) {

        val code = unluac.decompile.Code(function)

        val fullName = if (parent == null) {
            name
        } else {
            parent.fullName + "/" + name
        }

        out.println(".function\t$fullName")
        out.println()

        for (directive in function.header.function._directives) {
            directive.disassemble(out, function.header, function)
        }
        out.println()

        if (function.locals.isNotEmpty()) {
            for (local in 1..function.locals.size) {
                val l = function.locals[local - 1]
                out.println(".local\t" + l.name.toPrintString() + "\t" + l.start + "\t" + l.end)
            }
            out.println()
        }

        if (function.upvalues.isNotEmpty()) {
            for (upvalue in 1..function.upvalues.size) {
                val u = function.upvalues[upvalue - 1]
                out.println(".upvalue\t" + unluac.util.StringUtils.toPrintString(u.name) + "\t" + u.idx + "\t" + u.instack)
            }
            out.println()
        }

        if (function.constants.isNotEmpty()) {
            for (constant in 1..function.constants.size) {
                out.println(".constant\tk" + (constant - 1) + "\t" + function.constants[constant - 1].toPrintString())
            }
            out.println()
        }

        val label = BooleanArray(function.code.size)
        for (line in 1..function.code.size) {
            val op: unluac.decompile.Op? = code.op(line)
            if (op != null && op.hasJump()) {
                val target: Int = code.target(line)
                if (target >= 1 && target <= label.size) {
                    label[target - 1] = true
                }
            }
        }

        var abslineinfoindex = 0

        for (line in 1..function.code.size) {
            if (label[line - 1]) {
                out.println(".label\tl$line")
            }
            if (function.abslineinfo != null && abslineinfoindex < function.abslineinfo.size && function.abslineinfo[abslineinfoindex].pc == line - 1) {
                val info = function.abslineinfo[abslineinfoindex++]
                out.println(".abslineinfo\t" + info.pc + "\t" + info.line)
            }
            if (line <= function.lines.size) {
                out.print(".line\t" + function.lines[line - 1] + "\t")
            }
            val op: unluac.decompile.Op? = code.op(line)
            var cpLabel: String? = null
            if (op != null && op.hasJump()) {
                val target: Int = code.target(line)
                if (target >= 1 && target <= code.length) {
                    cpLabel = "l$target"
                }
            }
            if (op == null) {
                out.println(
                    unluac.decompile.Op.defaultToString(
                        code.codepoint(line),
                        function.header.version,
                        code.extractor
                    )
                )
            } else {
                out.println(
                    op.codePointToString(
                        code.codepoint(line),
                        code.extractor,
                        cpLabel
                    )
                )
            }
            //out.println("\t" + code.opcode(line) + " " + code.A(line) + " " + code.B(line) + " " + code.C(line) + " " + code.Bx(line) + " " + code.sBx(line) + " " + code.codepoint(line));
        }
        for (line in function.code.size + 1..function.lines.size) {
            if (function.abslineinfo != null && abslineinfoindex < function.abslineinfo.size && function.abslineinfo[abslineinfoindex].pc == line - 1) {
                val info = function.abslineinfo[abslineinfoindex++]
                out.println(".abslineinfo\t" + info.pc + "\t" + info.line)
            }
            out.println(".line\t" + function.lines[line - 1])
        }
        if (function.abslineinfo != null) {
            while (abslineinfoindex < function.abslineinfo.size) {
                val info = function.abslineinfo[abslineinfoindex++]
                out.println(".abslineinfo\t" + info.pc + "\t" + info.line)
            }
        }
        out.println()


    }

    private fun disassembleMainFunction(
        function: LFunction,
    ): LASMChunk {

        val bufferProvider = ByteArrayOutputProvider()
        val out = unluac.decompile.Output(bufferProvider)


        out.println(".version\t" + function.header.version.name)
        out.println()

        for (directive in function.header.lheader_type._directives) {
            directive.disassemble(out, function.header, function.header.lheader)
        }
        out.println()

        if (function.header.opmap !== function.header.version.opcodeMap) {
            val opmap = function.header.opmap
            for (opcode in 0 until opmap.size()) {
                val op = opmap[opcode]
                if (op != null) {
                    out.println(unluac.assemble.Directive.OP.token + "\t" + opcode + "\t" + op.name)
                }
            }
            out.println()
        }


        val versionData = bufferProvider.getBytes().decodeToString()
        bufferProvider.reset()

        disassemble(function, null, out, "main")

        val data = bufferProvider.getBytes().decodeToString()
        bufferProvider.reset()

        val func = LASMChunk(data, versionData, "main", "main")

        for ((subIndex, subFunction) in function.functions.withIndex()) {
            disassembleChildFunction(subFunction, func, bufferProvider, out, "f$subIndex", 1)
        }

        bufferProvider.close()


        return func

    }

    private fun disassembleChildFunction(
        func: LFunction,
        parent: AbsFunction<LASMFunction>,
        bufferProvider: ByteArrayOutputProvider,
        out: unluac.decompile.Output,
        name: String,
        level: Int
    ) {

        disassemble(func, parent, out, name)

        val data = bufferProvider.getBytes().decodeToString()
        bufferProvider.reset()

        val fullName = parent.fullName + "/" + name

        val asmFunction = LASMFunction(data, name, fullName, parent)


        for ((subIndex, subFunction) in func.functions.withIndex()) {
            disassembleChildFunction(
                subFunction,
                asmFunction,
                bufferProvider,
                out,
                "f$subIndex",
                level + 1
            )
        }

    }
}