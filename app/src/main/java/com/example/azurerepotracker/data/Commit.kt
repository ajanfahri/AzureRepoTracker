package com.example.azurerepotracker.data

import com.google.gson.annotations.SerializedName

data class Commit(
    @SerializedName("commitId") val commitId: String,
    @SerializedName("comment") val comment: String,
    @SerializedName("author") val author: Author,
    @SerializedName("committer") val committer: Committer, // committer bilgisi de eklendi
    @SerializedName("changeCounts") val changeCounts: ChangeCounts
)

data class Author(
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String,
    @SerializedName("email") val email: String
)

data class Committer( // committer sınıfı eklendi
    @SerializedName("name") val name: String,
    @SerializedName("date") val date: String,
    @SerializedName("email") val email: String
)

data class ChangeCounts(
    @SerializedName("Add") val add: Int,
    @SerializedName("Edit") val edit: Int,
    @SerializedName("Delete") val delete: Int
)