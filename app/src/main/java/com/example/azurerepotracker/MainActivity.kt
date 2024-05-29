package com.example.azurerepotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.azurerepotracker.data.AzureDevOpsApi
import com.example.azurerepotracker.data.Commit
import com.example.azurerepotracker.ui.theme.AzureRepoTrackerTheme
import com.example.azurerepotracker.ui.theme.MainViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AzureRepoTrackerApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzureRepoTrackerApp() {

    val retrofit = Retrofit.Builder()
        .baseUrl("https://dev.azure.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val azureDevOpsApi = retrofit.create(AzureDevOpsApi::class.java)

    val viewModel: MainViewModel = viewModel()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getRepositories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Azure Repo Tracker") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when {
                viewModel.uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                viewModel.uiState.error != null -> {
                    Text(
                        text = "Hata: ${viewModel.uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    viewModel.uiState.repository?.commits?.let { commits ->
                        LazyColumn {
                            items(commits) { commit ->
                                CommitItem(commit)
                            }
                        }
                    } ?: Text(text = "Repo bulunamadı.", modifier = Modifier.align(Alignment.Center)) // repository null ise yapılacaklar
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ayarlar") },
            text = { Text("Henüz ayar yok.") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Tamam")
                }
            }
        )
    }
}

@Composable
fun CommitItem(commit: Commit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(text = "Commit ID: ${commit.commitId}")
            Text(text = "Message: ${commit.comment}")
            Text(text = "Author: ${commit.author.name}")
            Text(text = "Date: ${commit.author.date}")
            Text(text = "Committer: ${commit.committer.name}")
            Text(text = "Commit Date: ${commit.committer.date}")
            Text(text = "Changes: +${commit.changeCounts.add} -${commit.changeCounts.delete} ~${commit.changeCounts.edit}")
        }
    }
}