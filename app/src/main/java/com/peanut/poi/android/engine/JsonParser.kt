package com.peanut.poi.android.engine

import android.content.Context
import com.peanut.poi.android.template.Parser
import org.json.JSONArray
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.net.URLDecoder

class JsonParser: Parser() {
    override fun run(context: Context, cmd: String, func: (finish: Boolean, state: String, result: Boolean) -> Unit) {
        try {
            func.invoke(false,"调用Json解析器",false)
            @Suppress("DEPRECATION")
            func.invoke(false,"源题库 : ${URLDecoder.decode(cmd)} \n大小: ${File(cmd).length()/1024}KB",false)
            val jsonArray = JSONArray(File(cmd).readText())
            func.invoke(false, "总共检测到的题目数量 : ${jsonArray.length()}", false)
            createDatabase(context)
            for (i in 0 until jsonArray.length()){
                if (i<10||i%5==0)
                    func.invoke(false,"处理第${i}题",false)
                val question = jsonArray.getJSONObject(i)
                topic = question.getString("Topic").encode64()
                optionList = question.getJSONArray("Options").toString(4).encode64()
                explain = question.getString("Explain").encode64()
                chapter = getChapterId(question.getString("Chapter"))
                answer = question.getString("Answer")
                type = question.getString("Type")
                save()
            }
            if(escape > 0) {
                func.invoke(false,"总共跳过了${escape}个不符合规则的题目",false)
            }
            saveChapterName()
            func.invoke(false,"处理完成",true)
            func.invoke(true,"处理完成",true)
        }catch (e:Exception){
            val a = StringWriter()
            e.printStackTrace(PrintWriter(a))
            func.invoke(false,"错误:$a",false)
            func.invoke(false,"错误:${a.toString().reportErrorToUser(github = "https://github.com/PPeanutButter/exercise-convertor-android/blob/master/app/src/main/java/com/peanut/poi/android/engine/ExcelParser.kt#L",regex = "JsonParser.kt:(.*)\\)")}",false)
            func.invoke(true,"错误:${e.message}",false)
        }
    }
}