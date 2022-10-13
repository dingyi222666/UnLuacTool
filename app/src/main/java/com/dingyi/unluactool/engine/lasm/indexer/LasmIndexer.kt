package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.getString
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.engine.decompiler.BHeaderDecompiler
import com.dingyi.unluactool.engine.lasm.data.LASMChunk
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler
import com.dingyi.unluactool.engine.lasm.dump.LasmDumper
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.AllFileSelector
import org.apache.commons.vfs2.FileSelector
import org.apache.commons.vfs2.util.RandomAccessMode
import unluac.Configuration
import unluac.decompile.Output
import unluac.parse.BHeader
import java.nio.ByteBuffer
import java.util.Collections
import kotlin.contracts.contract
import kotlin.io.path.toPath

class LasmIndexer : ProjectIndexer<List<LASMChunk>> {

    override suspend fun index(project: Project, progressState: ProgressState?): List<LASMChunk> =
        withContext(Dispatchers.IO) {
            val allProjectFileList = project.getProjectFileList()

            val projectSrcDir = project.getProjectPath(Project.PROJECT_SRC_NAME)

            val projectIndexedDir = project.getProjectPath(Project.PROJECT_INDEXED_NAME)


            if (projectIndexedDir.isFolder && projectIndexedDir.findFiles(AllFileSelector())
                    .isNotEmpty()
            ) {
                //indexed, use file system to open
                return@withContext Collections.emptyList()
            }

            projectIndexedDir.createFolder()

            val size = allProjectFileList.size


            for (index in 0 until allProjectFileList.size) {
                val it = allProjectFileList.get(index)
                progressState?.progress = (index + 1 / size) * 100


                val originFile = it.uri.toPath().toFile()
                val srcDirFile = projectSrcDir.uri.toPath().toFile()
                //val indexedDirFile = projectIndexedDir.uri.toPath().toFile()

                val fileName = originFile.absolutePath.substring(
                    srcDirFile.absolutePath.lastIndex + 1
                )
                progressState?.text = getString(R.string.main_project_indexer_toast, fileName)

                val targetFile = projectIndexedDir.resolveFile(
                    fileName
                )


                val header: BHeader
                try {
                    header = BHeaderDecompiler.decompile(Configuration().apply {
                        this.rawstring = true
                        this.mode = Configuration.Mode.DECOMPILE
                        this.variable = Configuration.VariableMode.FINDER
                    } to targetFile.content.inputStream.use {
                        ByteBuffer.wrap(it.readBytes())
                    })
                } catch (e: Exception) {
                    continue
                }


                val chunk = LasmDisassembler(header.main).decompile()

                val provider = ByteArrayOutputProvider()

                val output = Output(provider)

                val dumper = LasmDumper(output, chunk)

                dumper.dump()

                val bytes = provider.getBytes()

                targetFile.content.outputStream.use {
                    it.write(bytes)
                }

            }


            return@withContext Collections.emptyList()


        }
}