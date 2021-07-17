package com.peanut.poi.android.template

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.util.Base64
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class Parser {
    var id:Int = 0
    var topic:String = ""
    var optionList:String = ""
    var answer:String? = null
    var chapter:Int? = null
    var explain:String? = null
    var type:String = ""
        set(value) {
            field = getVisibleTerm(value).also {
                when (it.toLowerCase(Locale.CHINA)) {
                    "pd" -> id = ++indexes[0]
                    "dx" -> id = ++indexes[1]
                    "dd" -> id = ++indexes[2]
                    "tk" -> id = ++indexes[3]
                    "jd" -> id = ++indexes[4]
                    "yd" -> id = ++indexes[5]
                    else -> {
                        escape++
                    }
                }
            }
        }
    private val indexes = intArrayOf(0, 0, 0, 0, 0, 0)
    var escape = 0
    private var database: DataBase? = null
    abstract fun run(context: Context, cmd: String, func: (finish: Boolean, state: String, result: Boolean) -> Unit)
    var chapList = emptyList<String>()

    fun createDatabase(context: Context): DataBase {
        database = DataBase(context, File(context.getExternalFilesDir("Database"), "parse.db").also { it.delete() }.path, null, 1)
        "a.sql".execAssetsSql(database!!.sqLiteDatabase, context)
        return database!!
    }

    fun String.encode64(): String = Base64.encodeToString(this.toByteArray(), 0)

    fun saveChapterName(){
        if (chapList.isNotEmpty()){
            database!!.execSQL("create table Chapter(chapId tinyint PRIMARY KEY,name text);")
            for ((index, value) in chapList.withIndex()){
                database!!.execSQL("insert into Chapter values ('$index','$value')")
            }
        }
    }
    fun save(){
        database!!.let { temp->
            when (type.toLowerCase(Locale.CHINA)) {
                "pd" ->
                    temp.execSQL("insert into PD values ('$id','$topic','$optionList','$answer','$explain','$chapter')")
                "dx" ->
                    temp.execSQL("insert into DX values ('$id','$topic','$optionList','$answer','$explain','$chapter')")
                "dd" ->
                    temp.execSQL("insert into DD values ('$id','$topic','$optionList','$answer','$explain','$chapter')")
                "tk" ->
                    temp.execSQL("insert into TK values ('$id','$topic','$optionList','$explain','$chapter')")
                "jd" ->
                    temp.execSQL("insert into JD values ('$id','$topic','$explain','$chapter')")
                "yd" ->{
                    temp.execSQL("insert into YD values ('$id','$topic','$optionList','$answer','$explain','$chapter')")
                }
                else -> {
                }
            }
        }
    }

    fun String.execAssetsSql(db: SQLiteDatabase?, context: Context){
        try {
            val sqls = BufferedReader(InputStreamReader(context.resources.assets.open("schema/$this"))).readLines()
            for (sql in sqls){
                if (sql.startsWith("--")) continue
                try {
                    db?.execSQL(sql)
                }catch (e: SQLiteException){
                    e.printStackTrace()
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    fun getChapterId(chapterName: String?):Int{
        if (chapList.indexOf(chapterName) == -1 && chapterName!=null && chapterName.trim().isNotEmpty())
            chapList = chapList.plus(chapterName)
        return chapList.indexOf(chapterName)
    }

    private fun getVisibleTerm(str: String?): String {
        if (str == null) throw NullPointerException("空的题目类型")
        val sb = StringBuilder()
        for (char in str) {
            if (char.toUpperCase() in 'A'..'Z')
                sb.append(char)
        }
        return sb.toString()
    }

    protected fun String.reportErrorToUser(github: String, regex: String):String{
        val pattern: Pattern = Pattern.compile(regex, Pattern.MULTILINE)
        val matcher: Matcher = pattern.matcher(this)
        if (matcher.find()) {
            for (i in 1..matcher.groupCount()) {
                return "<a href=\"${github+matcher.group(i)}\">有关出错代码请访问<br/>${github+matcher.group(i)}查看</a>"
            }
        }
        return this
    }
}