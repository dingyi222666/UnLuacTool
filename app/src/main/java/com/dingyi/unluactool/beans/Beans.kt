package com.dingyi.unluactool.beans

data class HitokotoBean(
    val hitokoto:String,
    val from:String
)

data class ProjectInfoBean(
    val name:String,
    var fileCountOfLuaFile:Int
)