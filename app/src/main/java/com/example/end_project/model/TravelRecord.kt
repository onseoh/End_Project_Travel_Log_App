package com.example.end_project.model

data class TravelRecord(
    val no: Int = 0, // PRIMARY KEY
    val place: String,
    val visitDate: String,
    val memo: String,
    val photoUri: String?
)