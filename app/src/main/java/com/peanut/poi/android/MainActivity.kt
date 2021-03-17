package com.peanut.poi.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.viewbinding.BuildConfig
import com.peanut.poi.android.engine.ExcelParser
import com.peanut.poi.android.engine.JsonParser
import com.peanut.poi.android.template.CMDActivity
import com.peanut.poi.android.template.FileCompat
import com.peanut.poi.android.template.Parser
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class MainActivity : CMDActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        text("========== 设备信息 ==========")
        text("[VERSION]  :" + BuildConfig.VERSION_NAME)
        text("[DEVICE]   :" + Build.DEVICE)
        text("========== 流程日志 ==========")
        text("检查必备（存储）权限")
        requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)) { _, grantResults ->
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                text("权限申请通过，请选择excel文件！")
                fileChooser { _: Int, data: Intent? ->
                    data?.data?.let { returnUri ->
                        val (fileName, _) = FileCompat.getFileNameAndSize(this, returnUri)
                        text("========== 实时日志 ==========")
                        text("开始处理...")
                        thread {
                            val destination = this@MainActivity.getExternalFilesDir("excel_temp").toString() + "/$fileName"
                            FileCompat.copyFile(returnUri, destination, this@MainActivity)
                            var parser: Parser? = null
                            when (fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase(Locale.CHINA)) {
                                in arrayOf("xls", "xlsx") -> parser = ExcelParser()
                                in arrayOf("json") -> parser = JsonParser()
                            }
                            parser!!.run(this@MainActivity, destination) { finish, state, result ->
                                if (finish.not()) {
                                    runOnUiThread { text(state) }
                                } else {
                                    if (result) {
                                        runOnUiThread { text("处理完成！") }
                                        setResult(200, Intent().setData(FileProvider.getUriForFile(this@MainActivity, "com.peanut.poi.android", File(Objects.requireNonNull(this@MainActivity.getExternalFilesDir("Database")).toString() + "/parse.db"))).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                                        runOnUiThread { text("按返回键退出！") }
                                    } else {
                                        runOnUiThread { text("处理终止！") }
                                        setResult(-1, Intent().putExtra("msg",state))
                                        runOnUiThread { text("按返回键退出！") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


