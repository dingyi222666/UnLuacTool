package com.dingyi.unluactool.beans

data class HitokotoBean(
    val hitokoto: String,
    val from: String
)

data class ProjectInfoBean(
    var name: String,
    var fileCountOfLuaFile: Int,
    val path: String,
    var icon:String?
)

