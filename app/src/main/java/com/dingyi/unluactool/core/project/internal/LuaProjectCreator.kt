package com.dingyi.unluactool.core.project.internal

import android.content.ContentResolver
import android.net.Uri
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.R
import com.dingyi.unluactool.core.project.ProjectCreator
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.ktx.Paths
import com.dingyi.unluactool.ktx.encodeToJson
import com.dingyi.unluactool.ktx.getString
import com.dingyi.unluactool.ktx.isZipFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import org.apache.commons.vfs2.VFS
import java.io.File

class LuaProjectCreator : ProjectCreator {

    override suspend fun createProject(contentResolver: ContentResolver, uri: Uri) =
        withContext(Dispatchers.IO) {
            val inputStream = contentResolver
                .openInputStream(uri)


            val importPath = uri.path ?: "?.lua"

            if (!importPath.matches(Regex(".*(.lua|.zip|.apk)$"))) {
                error(getString(R.string.main_project_import_fail))
            }

            val cacheFile = checkNotNull(
                MainApplication.instance
                    .externalCacheDir
                    ?.resolve("import.cache")
            ) {
                "cache dir not found"
            }

            cacheFile.apply {
                parentFile?.mkdirs()
                createNewFile()
            }

            inputStream?.use { input ->
                cacheFile.outputStream().use {
                    input.copyTo(it)
                }
            }

            //check cacheFile is a zip file, rea

            val isZipFile = cacheFile.isZipFile()


            val projectName = getProjectName()

            val projectPath =
                VFS.getManager().resolveFile(File(Paths.projectDir.value, projectName).toURI())

            projectPath.resolveFile(LuaProject.ORIGIN_DIR_NAME).createFolder()

            if (!isZipFile) {
                projectPath.resolveFile("${LuaProject.ORIGIN_DIR_NAME}/main.lua").apply {
                    parent.createFolder()
                    createFile()
                    content.outputStream.use { outputStream ->
                        cacheFile.inputStream().use { inputStream ->

                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            } else {
                val zipFile = ZipFile(cacheFile)
                runCatching {
                    zipFile.fileHeaders
                        .filter {
                            !it.isDirectory && it.fileName.endsWith("lua")
                        }
                        .forEach {
                            val fileName = it.fileName
                            if (fileName.endsWith(".lua")) {
                                val targetFile =
                                    projectPath.resolveFile(LuaProject.ORIGIN_DIR_NAME)
                                        .resolveFile(fileName)
                                targetFile.parent.createFolder()
                                targetFile.createFile()
                                zipFile.getInputStream(it).use { inputStream ->
                                    targetFile.content.outputStream.use { outStream ->
                                        inputStream.copyTo(outStream)
                                    }
                                }
                            }
                        }
                    zipFile.close()
                }.onFailure {
                    it.printStackTrace()
                }
            }

            cacheFile.delete()

            projectPath.resolveFile(LuaProject.PROJECT_CONFIG_JSON)
                .content
                .outputStream
                .use {
                    it.write(
                        ProjectInfo(
                            iconPath = null,
                            name = projectName,
                            path = projectPath.publicURIString
                        ).encodeToJson().encodeToByteArray()
                    )
                }

        }


    private fun getProjectName(): String {
        return getString(R.string.main_temporary_project_name) + "_" + MainApplication.instance.globalServiceRegistry.get<ProjectManager>()
            .getProjectCount()
    }
}