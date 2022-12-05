package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.project.CompositeProjectIndexer
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.project.ProjectManagerListener
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.engine.lasm.indexer.LasmIndexer
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider
import org.apache.commons.vfs2.provider.url.UrlFileName

class UnLuacFileSystem(
    rootFileName: FileName,
    rootFile: String,
    fileSystemOptions: FileSystemOptions?
) : AbstractFileSystem(rootFileName, null, fileSystemOptions) {

    private val allOpenedFile = mutableListOf<FileObject>()

    private lateinit var serviceRegistry: ServiceRegistry

    //unluac://project/file (lasm)
    override fun createFile(name: AbstractFileName): FileObject {
        val path = name.pathDecoded
        val projectName = path.substringBefore("/")
        val targetFilePaths = path.substringAfter("/")
            .split("/").toMutableList()

        val project = serviceRegistry.get<ProjectManager>()
            .getProjectByName(projectName)

        checkNotNull(project)


        val projectSourceSrc = project.getProjectPath(Project.PROJECT_INDEXED_NAME)

        if (targetFilePaths.isEmpty()) {
            return createEmptyFileObject(projectSourceSrc)
        }

        var currentFileObject = projectSourceSrc

        // Loop through the detection until the file cannot be matched or until the path is matched


        while (targetFilePaths.isNotEmpty()) {
            val currentValue = targetFilePaths.removeAt(0)

            val forEachFileObject =
                kotlin.runCatching { projectSourceSrc.resolveFile(currentValue) }
                    .getOrNull()

            if (forEachFileObject == null) {
                break
            } else {
                currentFileObject = forEachFileObject
            }

        }

        if (currentFileObject.isFolder) {
            return createEmptyFileObject(currentFileObject)
        }

        val parsedFileObject = UnLuacParsedFileObject(currentFileObject)

    }


    private fun createEmptyFileObject(targetFileObject: FileObject): FileObject {
        return UnLuaCFileObject(
            proxyFileObject = targetFileObject,
            name = targetFileObject.name as UrlFileName,
            fileSystem = this
        )
    }

    override fun init() {
        serviceRegistry = MainApplication.instance.globalServiceRegistry
    }

    override fun addCapabilities(caps: MutableCollection<Capability>) {
        caps.addAll(UnLuacFileProvider.allCapability);
    }


}