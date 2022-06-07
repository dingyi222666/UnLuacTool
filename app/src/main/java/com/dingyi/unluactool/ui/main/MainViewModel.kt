package com.dingyi.unluactool.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.dingyi.unluactool.repository.MainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainViewModel: ViewModel() {

    private val _hitokoto = MutableLiveData<String>()

    val hitokoto: LiveData<String>
        get() = Transformations.switchMap(_hitokoto) {
            MutableLiveData(it)
        }


    suspend fun refreshHitokoto() = withContext(Dispatchers.Main) {
        _hitokoto.value = MainRepository
            .refreshHitokoto()
    }
}