package com.peanut.poi.android.template

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.annotation.StyleRes
import com.peanut.poi.android.R

open class MaterialDayNightActivity : PeanutActivity() {
    protected var light = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lightStyleRes = "light".resolveTheme(savedInstanceState, R.style.AppThemeLight)
        val darkStyleRes = "dark".resolveTheme(savedInstanceState, R.style.AppThemeDark)
        when (intent.getStringExtra("THEME")) {
            "0" -> {
                if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                    setTheme(darkStyleRes)
                    light = false
                } else {
                    setTheme(lightStyleRes)
                    light = true
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }
            "1" -> {
                setTheme(darkStyleRes)
                light = false
            }
            else -> {
                setTheme(lightStyleRes)
                light = true
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun String.resolveTheme(savedInstanceState: Bundle?,@StyleRes defaultTheme:Int) =
        if (null == savedInstanceState || savedInstanceState.getInt(this) == 0)
            defaultTheme
        else savedInstanceState.getInt(this)

}