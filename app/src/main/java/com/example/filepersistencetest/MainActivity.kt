package com.example.filepersistencetest

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.contentValuesOf
import com.example.filepersistencetest.ui.theme.FilePersistenceTestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val text = viewModel.load()
        if (text.isNotBlank()) viewModel.setText(text)
        setContent {
            FilePersistenceTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(viewModel = viewModel, this)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.save()
    }

}

@Composable
fun Greeting(viewModel: MainViewModel, context: Context) {
    val text by viewModel.text.collectAsState()
    val myDatabaseHelper = MyDatabaseHelper(context, "BookStore.db", 3)
    val db = myDatabaseHelper.writableDatabase
    Column {
        TextField(
            value = text,
            onValueChange = viewModel::setText,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = viewModel::saveBySharedPreferences) {
            Text(text = "save data")
        }
        Button(onClick = viewModel::loadBySharedPreferences) {
            Text(text = "show data")
        }
        Button(onClick = {
            val values = contentValuesOf(
                "name" to "The Da Vinci Code",
                "author" to "Dan Brown",
                "pages" to 454,
                "price" to 16.96
            )
            db.insert("Book", null, values)
        }) {
            Text(text = "add data")
        }
        Button(onClick = {
            db.delete("Book", "pages < ?", arrayOf("500"))
        }) {
            Text(text = "delete data")
        }
        Button(onClick = {
            val values = contentValuesOf("price" to 10.99)
            db.update("Book", values, "name = ?", arrayOf("The Da Vinci Code"))
        }) {
            Text(text = "update data")
        }
        Button(onClick = {
            val cursor = db.query("Book", null, null, null, null, null, null)
//            val cursor = db.rawQuery("select * from Book", null)
            if (cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow("name"))
                    Log.d("DataBase", "book name is $name")
                } while (cursor.moveToNext())
            }
            cursor.close()
        }) {
            Text(text = "query data")
        }
        Button(onClick = {
            db.beginTransaction()
            try {
                db.delete("Book", null, null)
                val values = contentValuesOf(
                    "name" to "Game of Thrones",
                    "author" to "George Martin",
                    "pages" to 720,
                    "price" to 20.85
                )
                db.insert("Book", null, values)
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }) {
            Text(text = "replace data")
        }
    }
}

