package com.aptavis.projecttrackerchallenge.data.db.entity

import androidx.room.*
import com.aptavis.projecttrackerchallenge.domain.model.Status

@Entity(
    tableName = "tasks",
    foreignKeys = [ForeignKey(
        entity = ProjectEntity::class,
        parentColumns = ["id"],
        childColumns = ["projectId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index("projectId"),
        Index("status"),
        Index("deadlineAt")
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val weight: Int = 1,
    val status: Status = Status.Draft,
    val deadlineAt: Long? = null,
    val notifyEnabled: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
