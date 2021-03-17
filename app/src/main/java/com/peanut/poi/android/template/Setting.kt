package com.peanut.poi.android.template

import android.os.Bundle
import com.peanut.poi.android.databinding.ActivitySettingsBinding

open class Setting : MaterialDayNightActivity() {
    protected lateinit var binding:ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivitySettingsBinding.inflate(layoutInflater).also { binding=it }.root)
        setSupportActionBar(binding.topAppBar)
        supportActionBar?.title = "题库插件"
    }
}