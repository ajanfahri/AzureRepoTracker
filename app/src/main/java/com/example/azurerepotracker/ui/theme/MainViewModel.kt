package com.example.azurerepotracker.ui.theme

import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.azurerepotracker.data.AzureDevOpsApi
import com.example.azurerepotracker.data.CommitResponse
import com.example.azurerepotracker.data.Repository
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.azurerepotracker.BuildConfig // Uygulama adınızı doğru şekilde yazın
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel : ViewModel() {

/*    var uiState by mutableStateOf(UiState())
        private set*/
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow() // StateFlow olarak expose edin


    private val azureDevOpsApi: AzureDevOpsApi

    init {
        val azureToken = BuildConfig.AZURE_TOKEN
        val personalAccessToken = azureToken // PAT'inizi buraya yapıştırın

        val credentials = ":$personalAccessToken".encodeToByteArray()
        val basic = "Basic " + Base64.encodeToString(credentials, Base64.NO_WRAP)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", basic)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://dev.azure.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        azureDevOpsApi = retrofit.create(AzureDevOpsApi::class.java)
    }




    fun getRepositories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null) // error'u null yap
            try {


                // BuildConfig'ten repository listesini okuma ve parçalama
                val repositories = BuildConfig.REPO_LIST.split(",")
                    .filter { it.isNotBlank() } // Boş öğeleri filtrele
                    .map { repoString ->
                        val parts = repoString.split("/")
                        RepositoryInfo(parts[0], parts[1])
                    } // RepositoryInfo nesnelerine dönüştürme

                // Artık repositories listesi kullanıma hazır
                repositories.forEach { repo ->
                    println("Owner: ${repo.organizationName}, Name: ${repo.repoName}")
                }

                val repositoryResults = mutableListOf<Repository>()
                for (repoInfo in repositories) {
                    azureDevOpsApi.getCommits(
                        repoInfo.organizationName,
                        repoInfo.repoName, // Proje adı tüm repolar için aynıysa
                        repoInfo.repoName
                    ).enqueue(object : Callback<CommitResponse> {
                        override fun onResponse(call: Call<CommitResponse>, response: Response<CommitResponse>) {
                            if (response.isSuccessful) {
//                                val commits = response.body()?.value ?: emptyList()
                                val commits = response.body()?.value?.toMutableList() ?: mutableListOf() // MutableList'e dönüştür

                                // Commit listesini sondan başa doğru sırala
                                commits.reverse()
                                repositoryResults.add(Repository(repoInfo.repoName, commits))
                                if (repositoryResults.size == repositories.size) {
                                    _uiState.value = _uiState.value.copy(isLoading = false, repositories = repositoryResults)
                                }
                            } else {
                                Log.e("API Error", "Hata Kodu: ${response.code()}. Hata Mesajı: ${response.errorBody()?.string()}")

                                _uiState.value = _uiState.value.copy(isLoading = false, error = "API HATASI: ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<CommitResponse>, t: Throwable) {
                            _uiState.value = _uiState.value.copy(isLoading = false, error = "Bir hata oluştu: ${t.message}")
                        }
                    })
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Bir hata oluştu: ${e.message}")
            }
        }


    }
    /*fun getRepositories() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                azureDevOpsApi.getCommits(
                    BuildConfig.ORGANIZATION_NAME, // Örnek organizasyon adı
                    BuildConfig.PROJECT_NAME,    // Örnek proje adı
                    BuildConfig.REPO_NAME     // Örnek repo adı
                ).enqueue(object : Callback<CommitResponse> {
                    override fun onResponse(call: Call<CommitResponse>, response: Response<CommitResponse>) {
                        if (response.isSuccessful) {
                            val commits = response.body()?.value ?: emptyList()
                            val repository = Repository("ajan_windows", commits)
                            uiState = uiState.copy(isLoading = false, repository = repository)
                        } else {
                            uiState = uiState.copy(isLoading = false, error = "API Hatası: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<CommitResponse>, t: Throwable) {
                        uiState = uiState.copy(isLoading = false, error = "Bir hata oluştu: ${t.message}")
                    }
                })
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = "Bir hata oluştu: ${e.message}")
            }
        }
    }*/
}

// Yardımcı veri sınıfı
data class RepositoryInfo(val organizationName: String, val repoName: String)

data class UiState(
    val isLoading: Boolean = false,
    val repositories: MutableList<Repository> = mutableListOf(),
    val error: String? = null
)
