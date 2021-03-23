package com.peanut.poi.android.engine

import android.content.Context
import com.peanut.poi.android.template.Parser
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.util.IOUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.json.JSONArray
import java.io.*
import java.net.URLDecoder
import java.util.*

class ExcelParser : Parser() {

    override fun run(context: Context, cmd: String, func: (finish: Boolean, state: String, result: Boolean) -> Unit) {
        var position = 0
        try {
            func(false, "调用Excel解析器", false)
            System.setProperty(
                    "org.apache.poi.javax.xml.stream.XMLInputFactory",
                    "com.fasterxml.aalto.stax.InputFactoryImpl"
            )
            System.setProperty(
                    "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                    "com.fasterxml.aalto.stax.OutputFactoryImpl"
            )
            System.setProperty(
                    "org.apache.poi.javax.xml.stream.XMLEventFactory",
                    "com.fasterxml.aalto.stax.EventFactoryImpl"
            )
            @Suppress("DEPRECATION")
            func.invoke(false, "源题库 : ${URLDecoder.decode(cmd)} \n大小: ${File(cmd).length() / 1024}KB", false)
            func.invoke(false, "正在预热插件(需要几秒钟):", false)
            val workbook: Workbook? = getReadWorkBookType(cmd, func)
            val sheet = workbook!!.getSheetAt(0)
            val count = sheet.lastRowNum
            func.invoke(false, "总共检测到的题目数量 : $count", false)
            val temp = createDatabase(context)
            //check format
            val (a, b) = checkSpell(
                    real = arrayOf("Topic", "OptionList", "Result", "Explain", "Type", "Chapter"),
                    user = arrayOf(sheet[0]?.get(0)?.cellValue(), sheet[0]?.get(1)?.cellValue(), sheet[0]?.get(2)?.cellValue(), sheet[0]?.get(3)?.cellValue(), sheet[0]?.get(4)?.cellValue(), sheet[0]?.get(5)?.cellValue()))
            if (a) {
                for (i in 1..count) {
                    position = i
                    func.invoke(false, "开始处理第${position}题(行)", false)
                    val row = sheet[i]
                    if (row?.getCell(0) == null || row.getCell(4) == null) {
                        escape++
                        continue
                    }
                    //解决单元格格式导致的问题
                    topic = row[0]!!.cellValue().encode64()
                    optionList = getJsonArray(row[1]?.cellValue()).encode64()
                    explain = row[3]?.cellValue()?.encode64()
                    answer = row[2]!!.cellValue()
                    val chapterName = row[5]?.cellValue()
                    chapter = getChapterId(chapterName)
                    type = row[4]!!.cellValue().toLowerCase(Locale.ROOT)
                    save()
                }
                saveChapterName()
                if (escape > 0) {
                    func.invoke(false, "总共跳过了${escape}个不符合规则的题目", false)
                }
                temp.close()
                func.invoke(true, "", true)
            } else {
                func.invoke(false, "excel首行格式校验 : $b", true)
                temp.close()
                func.invoke(true, "excel首行格式校验 : $b", false)
            }
        } catch (e: Exception) {
            val a = StringWriter()
            e.printStackTrace(PrintWriter(a))
            func.invoke(false, "处理第${position}行(题)时出现了致命错误，原因如下：", false)
            func.invoke(false, a.toString(), false)
            func.invoke(false, a.toString().reportErrorToUser(github = "https://github.com/PPeanutButter/exercise-convertor-android/blob/master/app/src/main/java/com/peanut/poi/android/engine/ExcelParser.kt#L",regex = "ExcelParser:(.*)\\)"), false)
            func.invoke(true, "", false)
        }
    }

    private fun checkSpell(real: Array<String>, user: Array<String?>): Pair<Boolean, String> {
        if (null in user) return false to "缺少${real[user.indexOf(null)]}字段"
        for (i in real.indices){
            if (user[i]!!.l(real[i]).not())
                return false to "期待列名:${real[i]},但是读取到的是:${user[i]},请检查拼写"
        }
        return true to ""
    }

    private fun getReadWorkBookType(filePath: String, func: (finish: Boolean, state: String, result: Boolean) -> Unit): Workbook? {
        var fileInputStream: FileInputStream? = null
        try {
            fileInputStream = FileInputStream(filePath)
            if (filePath.endsWith(".xlsx", ignoreCase = true)) {
                return XSSFWorkbook(fileInputStream)
            } else if (filePath.endsWith(".xls", ignoreCase = true)) {
                return HSSFWorkbook(fileInputStream)
            }
        } catch (e: IOException) {
            val a = StringWriter()
            e.printStackTrace(PrintWriter(a))
            func(false, "读取文件失败，可能不是excel文件！", false)
        } finally {
            IOUtils.closeQuietly(fileInputStream)
        }
        return null
    }

    fun String.l(s: String) = this.equals(s, ignoreCase = true)

    private fun Cell.cellValue(): String {
        return when (this.cellTypeEnum) {
            CellType.NUMERIC -> this.numericCellValue.toString()
            CellType.BOOLEAN -> this.booleanCellValue.toString()
            CellType.ERROR -> this.errorCellValue.toString()
            CellType.STRING, CellType.FORMULA, CellType.BLANK ->
                try {
                    this.stringCellValue
                } catch (e: Exception) {
                    e.localizedMessage
                }
            else -> "单元格格式:" + this.cellTypeEnum.name + "不支持!请使用文本型单元格格式"
        }
    }

    private operator fun Sheet.get(row: Int, column: Int) = this[row]?.get(column)

    private operator fun Sheet.get(row: Int):Row? = this.getRow(row)

    private operator fun Row.get(column: Int):Cell? = this.getCell(column)

    private fun getJsonArray(optionList: String?): String {
        if (optionList == null) return "[]"
        val list = optionList.split(";;")
        val jsonArray = JSONArray()
        for (a in list) {
            jsonArray.put(a.replace("\"", "'"))
        }
        return jsonArray.toString()
    }

}