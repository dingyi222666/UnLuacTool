package com.dingyi.unluactool.ui.main

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dingyi.unluactool.R
import com.dingyi.unluactool.core.project.LuaProjectCreator
import com.dingyi.unluactool.ktx.getString
import com.dingyi.unluactool.ktx.showSnackBar
import kotlinx.coroutines.launch

class FileSelectCallBack(
    private val fragment: Fragment
) : ActivityResultCallback<Uri> {

    companion object {
        val ZIP_HEADER_BYTES_1 = byteArrayOf(0x50, 0x4b, 0x03, 0x04)
        val ZIP_HEADER_BYTES_2 = byteArrayOf(0x50, 0x4b, 0x05, 0x06)
    }

    override fun onActivityResult(result: Uri) {
        val inputStream = fragment.requireContext()
            .contentResolver
            .openInputStream(result)


        val importPath = result.path ?: "?.lua"

        if (!importPath.matches(Regex(".*(.lua|.zip|.apk)$"))) {
            getString(R.string.main_project_import_fail).showSnackBar(fragment.requireView())
            return
        }

        val cacheFile = checkNotNull(
            fragment.requireContext()
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

        val isZipFile = cacheFile.inputStream().use { input ->
            val header = ByteArray(4)
            input.read(header)
            header.contentEquals(ZIP_HEADER_BYTES_1) || header.contentEquals(ZIP_HEADER_BYTES_2)
        }


        fragment.lifecycleScope.launch {
            LuaProjectCreator.createLuaProjectFromFile(cacheFile, isZipFile)
            fragment.viewModels<MainViewModel>().value.refreshProjectList()
        }

    }
}