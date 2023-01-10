package com.dingyi.unluactool.ui.editor


import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.progress.ProgressState
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.core.project.ProjectManager
import com.dingyi.unluactool.core.service.get
import com.dingyi.unluactool.ui.dialog.progressDialogWithState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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


    suspend fun openProject(context: Context, lifecycleOwner: LifecycleOwner) =
        withContext(Dispatchers.Main) {
            val (dialog, state) = progressDialogWithState(
                context = context,
                lifecycleOwner = lifecycleOwner
            )
            //launch in new CoroutineScope
            dialog to suspend {
                withContext(Dispatchers.IO) {
                    runCatching {
                        openProject(state)
                    }.onSuccess {
                        withContext(Dispatchers.Main) {
                            dialog.dismiss()
                        }
                    }.onFailure {
                        it.printStackTrace()
                    }
                }
            }
        }

    private suspend fun openProject(progressState: ProgressState) {
        project.value?.open(progressState)
    }
}

data class EditorFragmentData(
    val fileUri:String?
)