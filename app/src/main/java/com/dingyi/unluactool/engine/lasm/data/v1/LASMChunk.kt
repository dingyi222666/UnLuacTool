package com.dingyi.unluactool.engine.lasm.data.v1

class LASMChunk(
    override var data: String,
    var versionData: String,
    override val name: String,
    override val fullName: String,
) : AbsFunction<LASMFunction> {


    override val childFunctions = mutableListOf<LASMFunction>()

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
        return if (current != this) (current as LASMFunction) else null
    }


    override fun asFunction(): LASMFunction {
        error("Not support this method")
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