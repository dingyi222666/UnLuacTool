package com.dingyi.unluactool.engine.tree

import unluac.parse.BHeader
import unluac.parse.LFunction

class ChunkTree(
    val bHeader: BHeader
) {

    private lateinit var rootNode: ChunkNode

    fun parse() {
        val mainFunctionName = "main"
        val mainFunction = bHeader.main
        rootNode = ChunkNode(mainFunction, mainFunctionName, null)

        parseChild(rootNode, mainFunction, 1)

    }

    private fun parseChild(parentNode: ChunkNode, func: LFunction, depth: Int) {
        val functionNamePrefix = if (depth == 1) "f" else "${parentNode.name}/f"
        val willParseChild = mutableListOf<Pair<ChunkNode, LFunction>>()
        func.functions.forEachIndexed { index, function ->
            val name = functionNamePrefix + index
            val node = ChunkNode(function, name, parentNode)
            parentNode.addChild(node)
            willParseChild.add(Pair(node, function))
        }
        willParseChild.forEach { (node, function) ->
            parseChild(node, function, depth + 1)
        }
    }

    fun getRootNode(): ChunkNode {
        return rootNode
    }
}