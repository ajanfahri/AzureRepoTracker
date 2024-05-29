package com.example.azurerepotracker.data

data class Repository(
    val name: String,
    val commits: List<Commit>
)