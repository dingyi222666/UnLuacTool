package com.dingyi.unluactool.engine.lasm.indexer

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.common.ktx.getString
import com.dingyi.unluactool.common.ktx.inputStream
import com.dingyi.unluactool.common.ktx.outputStream
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectIndexer
import com.dingyi.unluactool.engine.decompiler.BHeaderDecompiler
import com.dingyi.unluactool.engine.lasm.data.v1.LASMChunk
import com.dingyi.unluactool.engine.lasm.disassemble.LasmDisassembler2
import com.dingyi.unluactool.engine.lasm.dump.v1.LasmDumper
import com.dingyi.unluactool.engine.util.ByteArrayOutputProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.Selectors
import java.nio.ByteBuffer
import java.util.Collections
import kotlin.io.path.toPath

class LasmIndexer : ProjectIndexer<List<LASMChunk>> {

    override suspend fun index(project: Project, progressState: ProgressState?): List<LASMChunk> =
        withContext(Dispatchers.IO) {
            val allProjectFileList = project.getProjectFileList()

            val projectSrcDir = project.getProjectPath(Project.PROJECT_SRC_NAME)

            val projectIndexedDir = project.getProjectPath(Project.PROJECT_INDEXED_NAME)


            val size = allProjectFileList.size

            progressState?.text = getString(
                R.string.editor_project_indexer_tips,
                project.name,
                0,
                size,
            )

            if (projectIndexedDir.isFolder && projectIndexedDir.findFiles(Selectors.SELECT_FILES)
                    .isNotEmpty()
            ) {
                //indexed, use file system to open
                return@withContext Collections.emptyList()
            }



            delay(2000)

            projectIndexedDir.createFolder()


            for (index in 0 until size) {

                val originFileObject = allProjectFileList[index]
                val nowProgress = (index / size) * 100
                val nextProgress = (index + 1 / size) * 100
                val rangeProgress = nextProgress - nowProgress


                val originFile = originFileObject.uri.toPath().toFile()
                val srcDirFile = projectSrcDir.uri.toPath().toFile()
                //val indexedDirFile = projectIndexedDir.uri.toPath().toFile()

                val fileName = originFile.absolutePath.substring(
                    srcDirFile.absolutePath.length + 1
                )

                progressState?.progress = progressState?.progress?.plus(rangeProgress / 3) ?: 0

                progressState?.text = getString(
                    R.string.editor_project_indexer_toast,
                    fileName,
                    index,
                    size
                )

                delay(1000)

                val targetFile = MainApplication
                    .instance
                    .fileSystemManager
                    .resolveFile(
                        projectIndexedDir.publicURIString.plus("/").plus(
                            originFile
                                .nameWithoutExtension
                        ).plus(".lasm")
                    )

                if (targetFile.exists()) {
                    progressState?.progress = nextProgress
                    continue
                }


                val header: unluac.parse.BHeader
                try {
                    header = BHeaderDecompiler.decompile(unluac.Configuration().apply {
                        this.rawstring = true
                        this.mode = unluac.Configuration.Mode.DECOMPILE
                        this.variable = unluac.Configuration.VariableMode.FINDER
                    } to originFileObject.inputStream.use {
                        ByteBuffer.wrap(it.readBytes())
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                    continue
                }

                progressState?.progress = progressState?.progress?.plus(rangeProgress / 3) ?: 0

                delay(1000)

                val chunk = LasmDisassembler2(header.main).decompile()

                val provider = ByteArrayOutputProvider()

                val output = unluac.decompile.Output(provider)

                val dumper = LasmDumper(output, chunk)

                dumper.dump()

                val bytes = provider.getBytes()

                targetFile.outputStream.use {
                    it.write(bytes)
                }

                progressState?.progress = nextProgress

            }


            return@withContext Collections.emptyList()


        }


}