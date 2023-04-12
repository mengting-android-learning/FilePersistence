package com.example.filepersistencetest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

private const val FILE_NAME = "data"

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var _text = MutableStateFlow("text")
    val text = _text.asStateFlow()
    fun setText(text: String) {
        _text.value = text
    }

    fun save() {
        try {
            val output = context.openFileOutput(FILE_NAME, Context.MODE_APPEND)
            val bufferedWriter = BufferedWriter(OutputStreamWriter(output))
            bufferedWriter.use {
                it.write(text.value)
            }
        } catch (e: IOException) {
            Log.w("OutputFile", e.toString())
        }
    }

    fun load(): String {
        val content = StringBuilder()
        try {
            val input = context.openFileInput(FILE_NAME)
            val reader = BufferedReader(InputStreamReader(input))
            reader.use {
                reader.forEachLine { content.append(it) }
            }
        } catch (e: IOException) {
            Log.w("InputFile", e.toString())
        }
        return content.toString()
    }

    fun saveBySharedPreferences() {
        val editor = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit()
        editor.putString("text", text.value)
        editor.apply()
    }

    fun loadBySharedPreferences() {
        val text =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString("text", "")
        Log.d("getPreferenceValue", "text is $text")
    }

    inner class MyDatabaseHelper(
        private val databaseContext: Context = context,
        name: String,
        version: Int
    ) : SQLiteOpenHelper(databaseContext, name, null, version) {
        private val createBook = "create table Book(" +
                "id integer primary key autoincrement," +
                "author text," +
                "price real," +
                "pages integer, " +
                "name text)"

        private val createCategory = "create table Category(" +
                "id integer primary key autoincrement," +
                "category_name text," +
                "category_code integer)"

        override fun onCreate(p0: SQLiteDatabase?) {
            p0?.execSQL(createBook)
            p0?.execSQL(createCategory)
            Toast.makeText(databaseContext, "Create succeeded", Toast.LENGTH_SHORT).show()
        }

        override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {
            p0?.execSQL("drop table if exists Book")
            p0?.execSQL("drop table if exists Category")
            onCreate(p0)
        }
    }
}