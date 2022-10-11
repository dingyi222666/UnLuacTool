package com.dingyi.unluactool.engine.filesystem

import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.event.EventManager
import com.dingyi.unluactool.core.project.CompositeProjectIndexer
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.project.ProjectManagerListener
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.engine.lasm.indexer.LasmIndexer
import org.apache.commons.vfs2.Capability
import org.apache.commons.vfs2.FileName
import org.apache.commons.vfs2.FileObject
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.provider.AbstractFileName
import org.apache.commons.vfs2.provider.AbstractFileSystem
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider

class UnLuacFileSystem(
    rootFileName: FileName,
    rootFile: String,
    fileSystemOptions: FileSystemOptions?
) : AbstractFileSystem(rootFileName, null, fileSystemOptions), ProjectManagerListener {

    private val allOpenedFile = mutableListOf<FileObject>()

    override fun createFile(name: AbstractFileName): FileObject {
        TODO("Not yet implemented")
    }

    override fun init() {
        MainApplication
            .instance
            .globalServiceRegistry
            .get<EventManager>()
            .subscribe(ProjectManager.projectListenerType, this)
    }

    override fun addCapabilities(caps: MutableCollection<Capability>) {
        caps.addAll(UnLuacFileProvider.allCapability);
    }

    override fun projectOpened(project: Project) {
        val indexer = project.getIndexer<Any>() as CompositeProjectIndexer
        if (!indexer.hasIndexer<LasmIndexer>()) {
            indexer.addIndexer(LasmIndexer())
        }
    }

    override fun projectClosed(project: Project) {

    }
}