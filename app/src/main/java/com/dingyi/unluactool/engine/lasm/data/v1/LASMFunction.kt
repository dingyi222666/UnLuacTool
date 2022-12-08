package com.dingyi.unluactool.engine.lasm.data.v1

/**
 * 代表一个lasm函数，所有信息都存在data里面，还包含一个函数名
 */
data class LASMFunction(
    override var data: String,
    override val name: String,
    override val fullName: String,
    val parent: AbsFunction<LASMFunction>? = null
) : AbsFunction<LASMFunction> {

    init {
        parent?.addChildFunction(this)
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
        childFunctions.removeIf { it.name == "name" }
    }

    override fun resolveFunction(path: String): LASMFunction {
        TODO("Not yet implemented")
    }

    override fun asFunction(): LASMFunction  = this


}