package com.aptavis.projecttrackerchallenge.data.db.dao

import androidx.room.*
import com.aptavis.projecttrackerchallenge.data.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY id DESC")
    fun observeTasks(projectId: Long): Flow<List<TaskEntity>>

    @Insert suspend fun insert(t: TaskEntity): Long
    @Update suspend fun update(t: TaskEntity)
    @Delete suspend fun delete(t: TaskEntity)

    @Query("""
        SELECT * FROM tasks
        WHERE status != 'Done'
          AND notifyEnabled = 1
          AND deadlineAt IS NOT NULL
          AND deadlineAt > :nowMs
    """)
    suspend fun pendingReminders(nowMs: Long): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?
}
