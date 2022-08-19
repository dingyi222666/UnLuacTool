package com.dingyi.unluactool.engine.lasm.data

class LASMChunk(
    var data: String,
    var versionData: String,
    val name: String,
    val fullName: String,
) : AbsFunction<LASMFunction> {


    val childFunctions = mutableListOf<LASMFunction>()

    override fun addChildFunction(func: LASMFunction) {
        childFunctions.add(func)
    }

    override fun removeChildFunction(func: LASMFunction) {
        childFunctions.remove(func)
    }

    override fun removeChildFunctionByName(name: String) {
        childFunctions.removeIf { it.name == "name" }
    }

    fun getAllData(): String {

        val buffer = StringBuilder()


        buffer
            .append(versionData)


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