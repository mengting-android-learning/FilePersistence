package com.example.filepersistencetest

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import proto.CountOuterClass.Count
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import javax.inject.Inject

private const val FILE_NAME = "data"
private val EXAMPLE_COUNTER = intPreferencesKey("example_counter")

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preference_settings")
    //
    val count: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[EXAMPLE_COUNTER] ?: 0
        }

    private val Context.protoDataStore: DataStore<Count> by dataStore(
        fileName = "count.pb",
        serializer = CountSerializer
    )

    val protoCount: Flow<Int> = context.protoDataStore.data
        .map { count ->
            count.exampleCounter
        }


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

    fun saveBySharedPreferences() =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit {
            putString("text", text.value)
        }


    fun loadBySharedPreferences() {
        val text =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString("text", "")
        Log.d("getPreferenceValue", "text is $text")
    }


    suspend fun incrementCounter() {

        context.dataStore.edit { settings ->
            val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
            settings[EXAMPLE_COUNTER] = currentCounterValue + 1
        }
    }

    suspend fun incrementProtoCounter() {
        context.protoDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setExampleCounter(currentSettings.exampleCounter + 1)
                .build()
        }
    }

}

