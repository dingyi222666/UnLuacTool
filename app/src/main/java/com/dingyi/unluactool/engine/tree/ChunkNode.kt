package com.dingyi.unluactool.engine.tree

import unluac.parse.LFunction

class ChunkNode(
    var data: LFunction?,
    var name:String,
    var parent: ChunkNode?,
) {

    val child = ArrayList<ChunkNode>()

    fun addChild(node: ChunkNode) {
        child.add(node)
        node.parent = this
    }

    fun removeChild(node: ChunkNode) {
        val removeChild = child.remove(node)
        if (removeChild) {
            node.parent = null
            node.data = null
        }
    }

    fun removeAllChild() {
        child.forEach {
            it.parent = null
            it.data = null
        }
        child.clear()
    }

    fun getChild(name: String):ChunkNode? {
        val list  = ArrayDeque<ChunkNode>()
        list.add(this)
        while (!list.isEmpty()) {
            val node = list.removeFirst()
            if (node.name == name) {
                return node
            }
            node.child.forEach {
                list.add(it)
            }
        }
        return null
    }

    override fun toString(): String {
        return "ChunkNode(data=$data, name='$name', child=$child)"
    }

}