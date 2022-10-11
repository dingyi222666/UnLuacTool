package com.dingyi.unluactool.ui.editor


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get

class EditorViewModel : ViewModel() {


    private val _project = MutableLiveData<Project>()

    val project: LiveData<Project>
        get() = _project


    suspend fun loadProject(uri: String) {
        _project.value =
            MainApplication
                .instance
                .globalServiceRegistry
                .get<ProjectManager>()
                .resolveProjectByPath(
                    MainApplication
                        .instance
                        .fileSystemManager
                        .resolveFile(uri)
                )

    }


    suspend fun openProject() {
        project.value?.open()
    }
}