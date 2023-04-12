package com.example.filepersistencetest

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.content.contentValuesOf
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
    /*
       file storage
    */
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

    /*
    shared preference
     */
    fun saveBySharedPreferences() =
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit {
            putString("text", text.value)
        }

    fun loadBySharedPreferences() {
        val text =
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).getString("text", "")
        Log.d("getPreferenceValue", "text is $text")
    }

    /*
    SQLite
     */
    private val myDatabaseHelper = MyDatabaseHelper(context, "BookStore.db", 3)

    private var db: SQLiteDatabase? = null
    fun createDatabase() {
        db = myDatabaseHelper.writableDatabase
    }

    fun insert() {
        val values = contentValuesOf(
            "name" to "The Da Vinci Code",
            "author" to "Dan Brown",
            "pages" to 454,
            "price" to 16.96
        )
        db?.insert("Book", null, values)
    }

    fun delete(){
        db?.delete("Book", "pages < ?", arrayOf("500"))
    }

    fun update() {
        val values = contentValuesOf("price" to 10.99)
        db?.update("Book", values, "name = ?", arrayOf("The Da Vinci Code"))
    }

    fun query() {
        val cursor = db?.query("Book", null, null, null, null, null, null)
        if (cursor?.moveToFirst() == true) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                Log.d("DataBase", "book name is $name")
            } while (cursor.moveToNext())
        }
        cursor?.close()
    }

    fun transaction() {
        db?.beginTransaction()
        try {
            db?.delete("Book", null, null)
            val values = contentValuesOf(
                "name" to "Game of Thrones",
                "author" to "George Martin",
                "pages" to 720,
                "price" to 20.85
            )
            db?.insert("Book", null, values)
            db?.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db?.endTransaction()
        }
    }

    /*
    Preferences DataStore
     */
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preference_settings")

    val count: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[EXAMPLE_COUNTER] ?: 0
        }

    suspend fun incrementCounter() {
        context.dataStore.edit { settings ->
            val currentCounterValue = settings[EXAMPLE_COUNTER] ?: 0
            settings[EXAMPLE_COUNTER] = currentCounterValue + 1
        }
    }

    /*
    Proto DataStore
     */
    private val Context.protoDataStore: DataStore<Count> by dataStore(
        fileName = "count.pb",
        serializer = CountSerializer
    )

    val protoCount: Flow<Int> = context.protoDataStore.data
        .map { count ->
            count.exampleCounter
        }

    suspend fun incrementProtoCounter() {
        context.protoDataStore.updateData { currentSettings ->
            currentSettings.toBuilder()
                .setExampleCounter(currentSettings.exampleCounter + 1)
                .build()
        }
    }

}

