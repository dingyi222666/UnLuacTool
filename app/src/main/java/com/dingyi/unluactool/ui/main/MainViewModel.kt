package com.dingyi.unluactool.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.beans.ProjectInfoBean
import com.dingyi.unluactool.repository.MainRepository
import com.dingyi.unluactool.ui.main.adapter.ProjectListAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {

    private val _hitokoto = MutableLiveData<String>()

    val hitokoto: LiveData<String>
        get() = _hitokoto


    private val _projectList = MutableLiveData<List<ProjectInfoBean>>()

    val projectList: LiveData<List<ProjectInfoBean>>
        get() = _projectList

    suspend fun refreshProjectList() {
        withContext(Dispatchers.IO) {
            val projectList = MainRepository.resolveAllProject()
            _projectList.postValue(projectList)
        }
    }

    suspend fun refreshHitokoto() = withContext(Dispatchers.Main) {
        _hitokoto.value = MainRepository
            .refreshHitokoto()
    }
}