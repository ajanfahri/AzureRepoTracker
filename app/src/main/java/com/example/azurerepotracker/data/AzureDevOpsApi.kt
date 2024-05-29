package com.example.azurerepotracker.data

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AzureDevOpsApi {

    @GET("{organization}/{project}/_apis/git/repositories/{repository}/commits")
    fun getCommits(
        @Path("organization") organization: String,
        @Path("project") project: String,
        @Path("repository") repository: String,
        @Query("api-version") apiVersion: String = "7.0"
    ): Call<CommitResponse>
}

data class CommitResponse(
    val value: List<Commit>
)