package com.aptavis.projecttrackerchallenge.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.room.Transaction
import com.aptavis.projecttrackerchallenge.data.db.AppDb
import com.aptavis.projecttrackerchallenge.data.db.entity.ProjectEntity
import com.aptavis.projecttrackerchallenge.data.db.entity.TaskEntity
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed
import com.aptavis.projecttrackerchallenge.domain.model.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val db: AppDb
) {
    fun projects(): LiveData<List<ProjectComputed>> =
        db.projectDao().observeProjectsComputed().asLiveData()

    fun tasks(projectId: Long): LiveData<List<TaskEntity>> =
        db.taskDao().observeTasks(projectId).asLiveData()

    // --- Project CRUD ---
    suspend fun addProject(name: String): Long = withContext(Dispatchers.IO) {
        db.projectDao().insert(ProjectEntity(name = name))
    }

    suspend fun renameProject(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val current = db.projectDao().getById(id) ?: return@withContext
        db.projectDao().update(current.copy(name = newName, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        val current = db.projectDao().getById(id) ?: return@withContext
        db.projectDao().delete(current) // CASCADE ke tasks
    }

    // --- Task CRUD (selalu recalc status project setelahnya) ---
    @Transaction
    suspend fun addTask(t: TaskEntity) = withContext(Dispatchers.IO) {
        db.taskDao().insert(t.copy(updatedAt = System.currentTimeMillis()))
        recalcProjectStatus(t.projectId)
    }

    @Transaction
    suspend fun updateTask(t: TaskEntity) = withContext(Dispatchers.IO) {
        db.taskDao().update(t.copy(updatedAt = System.currentTimeMillis()))
        recalcProjectStatus(t.projectId)
    }

    @Transaction
    suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        val current = db.taskDao().getById(taskId) ?: return@withContext
        db.taskDao().delete(current)
        recalcProjectStatus(current.projectId)
    }

    private suspend fun recalcProjectStatus(projectId: Long) {
        val s = db.projectDao().deriveStatusFromTasks(projectId)
        db.projectDao().setProjectStatus(projectId, s)
    }
}
