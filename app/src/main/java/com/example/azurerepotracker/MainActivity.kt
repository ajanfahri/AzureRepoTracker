package com.example.azurerepotracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


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
fun AzureRepoTrackerApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController() // NavHostController burada tanımlanıyor

    val uiState by viewModel.uiState.collectAsState() // uiState'i collectAsState ile alın

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
    ) {
        innerPadding -> // Scaffold'un content parametresi
        NavHost( // NavHost'u Scaffold'un içine alın
            navController = navController,
            startDestination = "repo_list",
            modifier = Modifier.padding(innerPadding) // Scaffold'un padding'ini uygulayın
        ) {
            composable("repo_list") {
                RepoListScreen(viewModel, navController)
            }
            composable("repo_details/{repoName}") { backStackEntry ->
                val repoName = backStackEntry.arguments?.getString("repoName") ?: ""
                RepoDetailsScreen(repoName, viewModel, navController)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoListScreen(viewModel: MainViewModel = viewModel(),navController: NavHostController) { // ViewModel'i parametre olarak alır

    val uiState by viewModel.uiState.collectAsState()  // collectAsState kullanın
    var showDialog by remember { mutableStateOf(false) }  // Dialog durumu
    var selectedCommit by remember { mutableStateOf<Commit?>(null) } // Seçili commit

    LazyColumn {
        items(uiState.repositories.size) { index -> // Her bir repository için bir öğe oluştur
            val repository = uiState.repositories[index] // Geçerli repository'yi al
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clickable {
                        navController.navigate("repo_details/${repository.name}")
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = repository.name, fontWeight = FontWeight.Bold,
                        fontSize = 18.sp, // Yazı boyutunu büyüt
                    modifier = Modifier.padding(bottom = 8.dp)
                ) // Repo adını göster

                repository.commits.last()?.let { commit -> // Son commit varsa
                    CommitItem(commit){
                            clickedCommit -> // Tıklama işlevi
                        selectedCommit = clickedCommit
                        showDialog = true
                    }
                } ?: Text("Bu repoda commit bulunamadı.") // Commit yoksa mesaj göster
            }
        }

        item { // Repo yoksa veya yüklenirken mesaj göster
            if (uiState.isLoading) {
                Text("Yükleniyor...")
            } else if (uiState.repositories.isEmpty()) {
                Text("Repo bulunamadı.")
            } else if (uiState.error != null) {
                Text("Hata: ${uiState.error}")
            }
        }
    }
    // Dialog
    if (showDialog && selectedCommit != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Commit Detayları") },
            text = { CommitDetailsContent(selectedCommit!!) }, // Commit detaylarını göster
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitItem(commit: Commit, onClick:(Commit)->Unit) {

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable{ onClick(commit) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(text = commit.comment ?: "Commit Mesajı Yok")

            // author ve commitId'yi ayrı ayrı göstermek için aşağıdaki gibi düzenleme yapılabilir.

            Text(text = "Author: ${commit.author?.name ?: "Bilinmiyor"}")
            //Text(text = "Commit ID: ${commit.commitId}")
            //Text(text = "Commit DATE: ${commit.committer.date}")
            // Tarih bilgisini ekliyoruz
            // Tarih bilgisini ekliyoruz
            commit.committer?.date?.let { dateString ->
                val instant = Instant.parse(dateString) // null değilse parse etmeye çalış
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withLocale(Locale.getDefault())
                val formattedDate = formatter.format(instant.atZone(ZoneId.systemDefault()))
                Text(text = "Tarih: $formattedDate")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitDetailsContent(commit: Commit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Commit ID: ${commit.commitId}")
        Text(text = "Mesaj: ${commit.comment ?: "Mesaj Yok"}")
        Text(text = "Yazar: ${commit.author?.name ?: "Bilinmiyor"}")
        // ... diğer detaylar
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailsScreen(repositoryName: String, viewModel: MainViewModel,navController: NavHostController) {
    val uiState by viewModel.uiState.collectAsState()

    // Repo'yu bul
    val repo = uiState.repositories.find { it.name == repositoryName }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = repositoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) { // Geri butonu
                        Icon(Icons.Filled.ArrowBack, "back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(repo?.commits ?: emptyList()) { commit ->
                CommitItem(commit) { clickedCommit ->
                    // Burada commit'e tıklanınca ne yapılacağını belirleyebilirsiniz.
                    // Örneğin, bir dialog açabilir veya başka bir sayfaya gidebilirsiniz.
                }
            }
        }
    }
}
/*
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
}*/