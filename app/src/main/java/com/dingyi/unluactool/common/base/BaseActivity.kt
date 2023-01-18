package com.dingyi.unluactool.common.base

import android.os.Build
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BuildCompat
import java.lang.ref.WeakReference

open class BaseActivity: AppCompatActivity() {
    private var onBackPressedCallback: OnBackPressedCallback? = null

    /**
     * Inner class for handle back callback totally.
     */
    internal class OnBackPressedCallbackInner constructor(baseActivity: BaseActivity) :
        OnBackPressedCallback(true) {
        private val activity: WeakReference<BaseActivity>

        override fun handleOnBackPressed() {
            activity.get()?.apply {
                onBackEvent()
            }
        }

        init {
            activity = WeakReference(baseActivity)
        }
    }

    /**
     * Override this method and return true if child wanna handle back event.
     */
    open fun isNeedInterceptBackEvent(): Boolean = false

    /**
     * Default back operation is invoking onBackPressed().
     * Child activity could override and implement its own operation.
     */
    open fun onBackEvent() {
        onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isNeedInterceptBackEvent()) {
            onBackPressedCallback = OnBackPressedCallbackInner(this).also {
                onBackPressedDispatcher.addCallback(it)
            }
        }
    }

}