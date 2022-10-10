package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.engine.decompiler.BHeaderDecompiler
import com.dingyi.unluactool.engine.lasm.data.LASMChunk
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler
import com.dingyi.unluactool.engine.lasm.dump.LasmDumper
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import org.apache.commons.vfs2.util.RandomAccessMode
import unluac.Configuration
import unluac.decompile.Output
import java.nio.ByteBuffer
import kotlin.io.path.toPath

class LasmIndexer : ProjectIndexer<List<LASMChunk>> {



    override suspend fun index(project: Project): List<LASMChunk> {
        val allProjectFileList = project.getProjectFileList()

        val projectSrcDir = project.getProjectPath(Project.PROJECT_SRC_NAME)

        val projectIndexedDir = project.getProjectPath(Project.PROJECT_INDEXED_NAME)

        return allProjectFileList.map {
            val originFile = it.uri.toPath().toFile()
            val srcDirFile = projectSrcDir.uri.toPath().toFile()
            //val indexedDirFile = projectIndexedDir.uri.toPath().toFile()

            val targetFile = projectIndexedDir.resolveFile(
                originFile.absolutePath.substring(
                    srcDirFile.absolutePath.lastIndex + 1
                )
            )

            val header = BHeaderDecompiler.decompile(Configuration().apply {
                this.rawstring = true
                this.mode = Configuration.Mode.DECOMPILE
                this.variable = Configuration.VariableMode.FINDER
            } to targetFile.content.inputStream.use {
                ByteBuffer.wrap(it.readBytes())
            })

            val chunk = LasmDisassembler(header.main).decompile()

            val provider = ByteArrayOutputProvider()

            val output = Output(provider)

            val dumper = LasmDumper(output, chunk)

            dumper.dump()

            val bytes = provider.getBytes()

            targetFile.content.outputStream.use {
                it.write(bytes)
            }
            chunk
        }


    }
}