package com.aptavis.projecttrackerchallenge.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.room.withTransaction
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
    // ---------- READ ----------
    fun projects(soonUntilMs: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000): LiveData<List<ProjectComputed>> =
        db.projectDao().observeProjectsComputed(soonUntilMs).asLiveData()

    fun tasks(projectId: Long): LiveData<List<TaskEntity>> =
        db.taskDao().observeTasks(projectId).asLiveData()

    // ---------- PROJECT CRUD ----------
    suspend fun addProject(name: String): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        db.projectDao().insert(
            ProjectEntity(
                name = name,
                status = Status.Draft,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun renameProject(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val current = db.projectDao().getById(id) ?: return@withContext
        db.projectDao().update(
            current.copy(
                name = newName,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        val current = db.projectDao().getById(id) ?: return@withContext
        // FK CASCADE di TaskEntity akan bersih-bersih tasks otomatis
        db.projectDao().delete(current)
    }

    // ---------- TASK CRUD (SELALU RECALC STATUS PROJECT) ----------
    suspend fun addTask(t: TaskEntity) = withContext(Dispatchers.IO) {
        db.withTransaction {
            db.taskDao().insert(t.copy(updatedAt = System.currentTimeMillis()))
            recalcProjectStatus(t.projectId)
        }
    }

    suspend fun updateTask(t: TaskEntity) = withContext(Dispatchers.IO) {
        db.withTransaction {
            db.taskDao().update(t.copy(updatedAt = System.currentTimeMillis()))
            recalcProjectStatus(t.projectId)
        }
    }

    suspend fun deleteTask(taskId: Long) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val current = db.taskDao().getById(taskId) ?: return@withTransaction
            db.taskDao().delete(current)
            recalcProjectStatus(current.projectId)
        }
    }


    // ---------- INTERNAL ----------
    private suspend fun recalcProjectStatus(projectId: Long) {
        val s = db.projectDao().deriveStatusFromTasks(
            projectId = projectId,
            draft = Status.Draft,
            inprog = Status.InProgress,
            done = Status.Done
        )
        db.projectDao().setProjectStatus(projectId, s)
    }
}
