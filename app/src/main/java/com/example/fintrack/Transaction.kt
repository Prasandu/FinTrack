package com.example.fintrack

data class Transaction(
    val id: Int,
    val title: String,
    val amount: String,
    val date: String,
    val type: String
)