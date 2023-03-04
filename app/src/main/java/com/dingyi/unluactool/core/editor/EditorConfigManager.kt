package com.dingyi.unluactool.core.editor

import android.graphics.Typeface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dingyi.unluactool.MainApplication
import com.dingyi.unluactool.engine.filesystem.UnLuaCFileObject
import io.github.rosemoe.sora.lang.Language
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.vfs2.FileObject
import org.eclipse.tm4e.core.registry.IThemeSource
import java.nio.charset.Charset

class EditorConfigManager {

    private val _font = MutableLiveData<Typeface>()
    val font: LiveData<Typeface> = _font

    init {
        val application = MainApplication.instance
        application.applicationScope.launch(Dispatchers.IO) {
            FileProviderRegistry.getInstance()
                .addFileProvider(AssetsFileResolver(application.assets))

            GrammarRegistry.getInstance().loadGrammars("editor/textmate/languages.json")

            val themes = arrayOf("quietlight.json", "solarized_drak.json")

            themes.forEach { themeName ->
                val themePath = "editor/textmate/theme/$themeName"
                ThemeRegistry.getInstance().loadTheme(
                    IThemeSource.fromInputStream(
                        application.assets.open(themePath),
                        themePath,
                        Charset.defaultCharset()
                    )
                )
            }

            ThemeRegistry.getInstance().setTheme("Quiet Light")

            //TODO: Read config to set font
            withContext(Dispatchers.Main) {
                _font.value = Typeface.createFromAsset(
                    MainApplication.instance.assets, "editor/fonts/JetBrainsMono-Regular.ttf"
                )
            }

        }
    }

    fun getLanguage(fileObject: FileObject): Language {
        val uri = fileObject.name.friendlyURI
        if (uri.endsWith("_decompile") || uri.endsWith(".lua")) {
            return TextMateLanguage.create("source.lua", true)

        }

        return WrapperLanguage(
            TextMateLanguage.create("source.lasm", false), fileObject
        )
    }


}