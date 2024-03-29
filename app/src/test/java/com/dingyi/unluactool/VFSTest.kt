package com.dingyi.unluactool

import org.apache.commons.vfs2.VFS
import org.apache.commons.vfs2.impl.DefaultFileSystemManager
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.junit.Test
import java.net.URI

class VFSTest {

    @Test
    fun test1() {
        val fileManager = StandardFileSystemManager()
        //need call init method
        fileManager.init()
        VFS.setManager(fileManager)
        val fileObject = fileManager.resolveFile(
            URI.create("file:///G:/IdeaProjects/adofai_macro/build/install/adofai_macro/bin/adofai_macro")
        )
        println(arrayOf(
            fileObject.uri, fileObject.name.friendlyURI,
            fileObject.publicURIString,
            fileObject.name.uri
        ).joinToString { it.toString() })


        println(fileObject.uri.path)

        /*fileObject.inputStream.use {
            it.readBytes().decodeToString()
        }.let {
            println(it)
        }*/
    }

}