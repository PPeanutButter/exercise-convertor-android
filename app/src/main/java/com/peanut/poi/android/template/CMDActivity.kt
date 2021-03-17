package com.peanut.poi.android.template

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.view.View
import android.widget.TextView
import java.lang.Exception

open class CMDActivity : Setting(){

    protected fun CMDActivity.text(text: String) = mPrint(SpannableString((text)))
    protected fun CMDActivity.text(text: Spanned) = mPrint(text)
    protected fun CMDActivity.text(text: SpannableString) = mPrint(text)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.appBarLayout.isLiftOnScroll = false
    }

    private fun mPrint(a: SpannableString) {
        binding.list.addView(TextView(this).apply {
            this.text = a
            this.textSize = 10f
            this.setTextIsSelectable(true)
            this.typeface = Typeface.createFromAsset(this@CMDActivity.assets, "ubuntu_mono.ttf")
        })
        binding.scroll.post {
            binding.scroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun mPrint(a: Spanned) {
        binding.list.addView(TextView(this).apply {
            this.text = a
            this.textSize = 10f
            this.setTextIsSelectable(true)
            this.typeface = Typeface.createFromAsset(this@CMDActivity.assets, "ubuntu_mono.ttf")
        })
        binding.scroll.post {
            binding.scroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    fun String.toFixedLengthString(l: Int?):String{
        if (l == null)
            return this
        return if (this.realLength()>l)
            this.substring(0, l - 3)+"..."
        else {
            this+" ".repeat(l - this.realLength())
        }
    }

    private fun String.realLength(): Int {
        var valueLength = 0
        for (i in this) {
            valueLength += if (i.toString().matches(Regex("[\u4e00-\u9fa5]"))) {
                2
            } else {
                1
            }
        }
        return valueLength
    }
}