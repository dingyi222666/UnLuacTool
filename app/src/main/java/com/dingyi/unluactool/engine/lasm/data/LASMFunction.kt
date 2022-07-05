package com.dingyi.unluactool.engine.lasm.data

/**
 * 代表一个lasm函数，所有信息都存在data里面，还包含一个函数名
 */
data class LASMFunction(
    var data: String,
    val versionData: String,
    val name: String,
    val fullName: String,
    val parent: LASMFunction? = null
) {

    init {
        parent?.childFunctions?.add(this)
    }

    val childFunctions = mutableListOf<LASMFunction>()

    fun getDataWithVersion(): String {
        val buffer = StringBuilder()

        buffer
            .append(versionData)
            .append(data)
            .append("\n")
        return buffer.toString()
    }


    fun getDataWithChildFunctions(withVersion: Boolean = false): String {
        val buffer = StringBuilder()

        if (withVersion) {
            buffer
                .append(versionData)
        }

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

}