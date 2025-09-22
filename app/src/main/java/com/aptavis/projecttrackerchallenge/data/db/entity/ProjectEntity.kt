package com.aptavis.projecttrackerchallenge.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aptavis.projecttrackerchallenge.domain.model.Status

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val status: Status = Status.Draft,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
