package com.example.filepersistencetest

import android.content.Context
import android.util.Log
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
            val output = context.openFileOutput("data", Context.MODE_APPEND)
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
            val input = context.openFileInput("data")
            val reader = BufferedReader(InputStreamReader(input))
            reader.use {
                reader.forEachLine { content.append(it) }
            }
        } catch (e: IOException) {
            Log.w("InputFile", e.toString())
        }
        return content.toString()
    }
}