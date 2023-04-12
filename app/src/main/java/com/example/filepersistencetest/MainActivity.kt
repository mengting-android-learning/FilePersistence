package com.example.filepersistencetest

import android.os.Bundle
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.filepersistencetest.ui.theme.FilePersistenceTestTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

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
                    Greeting(viewModel = viewModel)
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
fun Greeting(viewModel: MainViewModel) {
    val text by viewModel.text.collectAsState()
    val count by viewModel.count.collectAsState(initial = 0)
    val protoCount by viewModel.protoCount.collectAsState(initial = 0)
    val scope = rememberCoroutineScope()
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
        Button(onClick = viewModel::createDatabase){
            Text(text = "create")
        }
        Button(onClick = viewModel::insert) {
            Text(text = "add data")
        }
        Button(onClick = viewModel::delete) {
            Text(text = "delete data")
        }
        Button(onClick = viewModel::update) {
            Text(text = "update data")
        }
        Button(onClick = viewModel::query) {
            Text(text = "query data")
        }
        Button(onClick = viewModel::transaction) {
            Text(text = "replace data")
        }
        Button(onClick = {
            scope.launch {
                viewModel.incrementCounter()
            }
        }) {
            Text(text = "Preferences DataStore: $count")
        }
        Button(onClick = {
            scope.launch {
                viewModel.incrementProtoCounter()
            }
        }) {
            Text(text = "Proto DataStore: $protoCount")
        }
    }
}







