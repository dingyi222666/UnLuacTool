package com.dingyi.unluactool.engine.lasm.data.v1

/**
 * 代表一个lasm函数，所有信息都存在data里面，还包含一个函数名
 */
 class LASMFunction(
    override var data: String,
    override val name: String,
    override val fullName: String,
    val parent: AbsFunction<LASMFunction>? = null
) : AbsFunction<LASMFunction> {

    init {
        if (parent?.hasChildFunction(this) != true) {
            parent?.addChildFunction(this)
        }
    }

    override val childFunctions = mutableListOf<LASMFunction>()

    fun getDataWithChildFunctions(): String {
        val buffer = StringBuilder()

        buffer
            .append(data)
            .append("\n")

        val allFunctions = mutableListOf<LASMFunction>()

        val addDeque = ArrayDeque<LASMFunction>()

        addDeque.addAll(childFunctions)
        while (addDeque.isNotEmpty()) {
            val function = addDeque.removeFirst()
            allFunctions.add(function)
            function.childFunctions.forEach {
                addDeque.add(it)
            }
        }

        allFunctions.forEach {
            buffer.append(it.data)
        }

        return buffer.toString()
    }

    override fun addChildFunction(func: LASMFunction) {
        childFunctions.add(func)
    }

    override fun removeChildFunction(func: LASMFunction) {
        childFunctions.remove(func)
    }

    override fun removeChildFunctionByName(name: String) {
        childFunctions.removeIf { it.name == name }
    }

    override fun hasChildFunction(func: LASMFunction): Boolean {
        return childFunctions.contains(func)
    }

    override fun resolveFunction(path: String): LASMFunction? {
        val paths = path.split("/").toMutableList()
        var current: AbsFunction<LASMFunction> = this
        while (paths.isNotEmpty()) {
            val name = paths.removeAt(0)
            val now = current.childFunctions.find { it.name == name }
            if (now is AbsFunction<LASMFunction>) {
                current = now
            } else {
                return now
            }
        }
        return null
    }

    override fun asFunction(): LASMFunction = this
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LASMFunction

        if (data != other.data) return false
        if (name != other.name) return false
        if (fullName != other.fullName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + fullName.hashCode()
        return result
    }

    override fun toString(): String {
        return "LASMFunction(name='$name', fullName='$fullName', childFunctions=$childFunctions)"
    }


}