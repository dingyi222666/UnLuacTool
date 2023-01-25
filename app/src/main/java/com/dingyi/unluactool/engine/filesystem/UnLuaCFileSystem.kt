package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.ServiceRegistry
import com.dingyi.unluactool.core.service.get
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem

class UnLuaCFileSystem(
    rootFileName: FileName,
    private val provider: UnLuaCVirtualFileProvider,
    fileSystemOptions: FileSystemOptions?
) : AbstractFileSystem(rootFileName, null, fileSystemOptions) {


    private lateinit var serviceRegistry: ServiceRegistry

    private val selfCapabilities = mutableListOf<Capability>()

    private val cacheParsedFileObject = mutableMapOf<String, UnLuacParsedFileObject>()

    //unluac://project/file (lasm)
    override fun createFile(name: AbstractFileName): FileObject {
        var path = name.pathDecoded.substring(1)
        var isDecompileFunction = false
        if (path.endsWith("_decompile")) {
            isDecompileFunction = true
            path = path.replace("_decompile", "")
        }

        val projectName = path.substringBefore("/", path)
        val targetFilePaths = path.replace("$projectName/", "")
            .split("/")
            .filter(String::isNotEmpty)
            .toMutableList()

        val project = serviceRegistry.get<ProjectManager>()
            .getProjectByName(projectName)

        checkNotNull(project)

        val projectSourceSrc = project.getProjectPath(Project.PROJECT_INDEXED_NAME)

        if (targetFilePaths.isEmpty()) {
            return createEmptyFileObject(projectSourceSrc, projectSourceSrc, projectName)
        }


        var currentFileObject = projectSourceSrc

        // Loop through the detection until the file cannot be matched or until the path is matched

        while (targetFilePaths.isNotEmpty()) {
            val currentValue = targetFilePaths.removeAt(0)

            val forEachFileObject =
                kotlin.runCatching { projectSourceSrc.resolveFile(currentValue) }
                    .getOrNull()

            if (forEachFileObject == null || !forEachFileObject.exists()) {
                targetFilePaths.add(0, currentValue)
                break
            } else {
                currentFileObject = forEachFileObject
            }


        }

        if (currentFileObject.isFolder) {
            return createEmptyFileObject(currentFileObject, projectSourceSrc, projectName)
        }

        val parsedFileObject = cacheParsedFileObject.getOrPut(projectSourceSrc.name.friendlyURI)
        { UnLuacParsedFileObject(currentFileObject) }

        parsedFileObject.init()

        val extra = UnLuacFileObjectExtra(
            chunk = parsedFileObject.lasmChunk,
            path = targetFilePaths.joinToString(separator = "/"),
            fileObject = parsedFileObject,
            currentFunction = null,
            project = project,
            isDecompile = isDecompileFunction
        )

        if (targetFilePaths.isEmpty()) {
            return createParsedFileObject(currentFileObject, projectSourceSrc, extra, projectName)
        }

        val chunk = parsedFileObject.lasmChunk

        // If it's not a directory and not a file object, we will try to parse the file to see if it's a function or an index
        val findFunction = chunk.resolveFunction(extra.path)
        if (findFunction != null) {
            extra.currentFunction = findFunction
            return createParsedFileObject(currentFileObject, projectSourceSrc, extra, projectName)
        }


        // If the path does not match any of the above, then return an empty file object
        return createEmptyFileObject(currentFileObject, projectSourceSrc, projectName)

    }


    override fun hasCapability(capability: Capability): Boolean {
        return selfCapabilities.contains(capability)
    }

    override fun addCapabilities(caps: MutableCollection<Capability>) {
        selfCapabilities.addAll(caps)
    }


    internal fun refresh(fileObject: UnLuaCFileObject) {
        val proxyFileObject = fileObject.proxyFileObject
        cacheParsedFileObject[proxyFileObject.name.friendlyURI] =
            UnLuacParsedFileObject(proxyFileObject).apply {
                init()
            }
    }

    private fun convertFileName(
        fileName: FileName,
        projectSourceSrc: FileObject,
        projectName: String,
        extra: UnLuacFileObjectExtra? = null
    ): AbstractFileName {
        // ?
        var uri = fileName.friendlyURI.replace(
            projectSourceSrc.name.friendlyURI/* "file:/"*/,
            "unluac://$projectName"
        )
        if (extra != null) {
            uri = "$uri/${extra.path}"
            if (extra.isDecompile) {
                uri += "_decompile"
            }
        }

        return provider.parseUri(null, uri) as AbstractFileName
        // return fileName as AbstractFileName
    }

    private fun createParsedFileObject(
        targetFileObject: FileObject,
        projectSourceSrc: FileObject,
        extra: UnLuacFileObjectExtra,
        projectName: String
    ): FileObject {
        return UnLuaCFileObject(
            proxyFileObject = targetFileObject,
            name = convertFileName(
                targetFileObject.name,
                projectSourceSrc,
                projectName,
                extra
            ),
            data = extra,
            fileSystem = this
        )
    }

    private fun createEmptyFileObject(
        targetFileObject: FileObject,
        projectSourceSrc: FileObject,
        projectName: String
    ): FileObject {
        return UnLuaCFileObject(
            proxyFileObject = targetFileObject,
            name = convertFileName(targetFileObject.name, projectSourceSrc, projectName),
            fileSystem = this
        )
    }

    override fun init() {
        serviceRegistry = MainApplication.instance.globalServiceRegistry
        addCapabilities(UnLuaCVirtualFileProvider.allCapability.toMutableList())
    }


}