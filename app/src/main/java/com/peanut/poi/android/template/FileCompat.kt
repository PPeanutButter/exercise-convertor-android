package com.peanut.poi.android.template

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.provider.OpenableColumns
import androidx.core.net.toFile
import java.io.FileInputStream
import java.io.FileOutputStream

object FileCompat {

    fun copyFile(source:Uri,destination:String,context: Context){
        val sharedDB = context.contentResolver.openFileDescriptor(source, "r")
        val fd = sharedDB!!.fileDescriptor
        val fis = FileInputStream(fd)
        val fos = FileOutputStream(destination)
        copyFileUseStream(fis,fos)
    }

    fun copyFile(source:Uri,destination:String,context: Context,func:()->Unit){
        object:Thread(){
            override fun run() {
                copyFile(source, destination, context)
                Handler(context.mainLooper).post {
                    func()
                }
            }
        }.start()
    }

    private fun copyFileUseStream(fileInputStream: FileInputStream, fileOutputStream: FileOutputStream) {
        val buffer = ByteArray(1024)
        var byteRead: Int
        while (-1 != fileInputStream.read(buffer).also { byteRead = it }) {
            fileOutputStream.write(buffer, 0, byteRead)
        }
        fileInputStream.close()
        fileOutputStream.flush()
        fileOutputStream.close()
    }

    fun getFileNameAndSize(context: Context,uri: Uri?):Pair<String,Long>{
        try {
            uri?.let { returnUri ->
                when(returnUri.scheme){
                    "file"->{
                        val file = returnUri.toFile()
                        return file.name to file.length()
                    }
                    "content"->{
                        context.contentResolver.query(returnUri, null, null, null, null)?.use {
                            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                            if (it.moveToFirst()) return it.getString(nameIndex) to it.getLong(sizeIndex)
                        }
                    }
                    else-> return "不支持的协议:${returnUri.scheme}" to 0L
                }
            }
        }catch (e:Exception){
            return "错误:${e.localizedMessage}" to 0L
        }
        return "错误:获取文件信息失败" to 0L
    }

}