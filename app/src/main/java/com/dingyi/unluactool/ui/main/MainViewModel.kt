package com.dingyi.unluactool.ui.main

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.core.project.Project
import com.dingyi.unluactool.repository.MainRepository

class MainViewModel : ViewModel() {

    private val _hitokoto = MutableLiveData<String>()

    val hitokoto: LiveData<String>
        get() = _hitokoto


    private val _projectList = MutableLiveData<List<Project>>()

    val projectList: LiveData<List<Project>>
        get() = _projectList

    suspend fun refreshProjectList() {
        val projectList = MainRepository.resolveAllProject()
        _projectList.postValue(projectList)
    }

    suspend fun refreshHitokoto() {
        _hitokoto.value = MainRepository
            .refreshHitokoto()
    }

    suspend fun createProject(uri: Uri) = MainRepository.createProject(
        MainApplication.instance.contentResolver, uri
    )
}