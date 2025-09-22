package com.aptavis.projecttrackerchallenge.domain.model

data class ProjectComputed(
    val id: Long,
    val name: String,
    val status: Status,
    val progress: Int,
    val taskCount: Int,
    val deadlineSoonCount: Int
)
