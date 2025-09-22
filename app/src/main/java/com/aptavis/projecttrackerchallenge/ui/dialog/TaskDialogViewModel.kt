package com.aptavis.projecttrackerchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aptavis.projecttrackerchallenge.data.db.entity.TaskEntity
import com.aptavis.projecttrackerchallenge.data.repository.ProjectRepository
import com.aptavis.projecttrackerchallenge.domain.model.Status
import com.aptavis.projecttrackerchallenge.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskFormState(
    val name: String = "",
    val status: Status = Status.Draft,
    val weight: Int = 1,
    val deadlineAt: Long? = null,
    val notifyEnabled: Boolean = false,
    val nameError: String? = null,
    val weightError: String? = null,
    val isSubmitting: Boolean = false
)

sealed interface TaskDialogEvent {
    data class Saved(val taskId: Long?, val isEdit: Boolean, val projectId: Long) : TaskDialogEvent
    data class Deleted(val taskId: Long, val projectId: Long) : TaskDialogEvent
    data class Error(val message: String) : TaskDialogEvent
}

@HiltViewModel
class TaskDialogViewModel @Inject constructor(
    private val repo: ProjectRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {

    val form = MutableStateFlow(TaskFormState())
    private val _events = MutableSharedFlow<TaskDialogEvent>()
    val events = _events.asSharedFlow()

    // ---------- Form intents ----------
    fun setInitial(
        name: String?,
        status: Status?,
        weight: Int?,
        deadlineAt: Long?,
        notify: Boolean?
    ) {
        form.update {
            it.copy(
                name = name.orEmpty(),
                status = status ?: Status.Draft,
                weight = weight ?: 1,
                deadlineAt = deadlineAt,
                notifyEnabled = notify ?: false,
                nameError = null,
                weightError = null
            )
        }
    }

    fun onNameChange(v: String) = form.update { it.copy(name = v, nameError = null) }
    fun onStatusChange(s: Status) = form.update { it.copy(status = s) }
    fun onWeightChange(w: Int) = form.update { it.copy(weight = w, weightError = null) }
    fun onDeadlineChange(ts: Long?) = form.update { it.copy(deadlineAt = ts) }
    fun onNotifyChange(enabled: Boolean) = form.update { it.copy(notifyEnabled = enabled) }

    // ---------- Validation ----------
    private fun validate(): Boolean {
        var ok = true
        val cur = form.value
        if (cur.name.trim().isBlank()) {
            form.update { it.copy(nameError = "Name required") }
            ok = false
        }
        if (cur.weight !in 1..10) {
            form.update { it.copy(weightError = "Weight must be 1..10") }
            ok = false
        }
        return ok
    }

    // ---------- Actions ----------
    fun save(projectId: Long, isEdit: Boolean, taskId: Long?) {
        if (!validate()) return
        val cur = form.value

        viewModelScope.launch(io) {
            try {
                form.update { it.copy(isSubmitting = true) }

                val entity = TaskEntity(
                    id = taskId ?: 0L,
                    projectId = projectId,
                    name = cur.name.trim(),
                    weight = cur.weight,
                    status = cur.status,
                    deadlineAt = cur.deadlineAt,
                    notifyEnabled = cur.notifyEnabled
                )

                val emittedId: Long? = if (isEdit && taskId != null) {
                    repo.updateTask(entity)
                    taskId
                } else {
                    repo.addTask(entity)
                    null // kalau DAO insert mau dipakai id-nya, bisa ubah repo utk return Long
                }

                _events.emit(TaskDialogEvent.Saved(emittedId, isEdit, projectId))
            } catch (e: Exception) {
                _events.emit(TaskDialogEvent.Error(e.message ?: "Failed to save task"))
            } finally {
                form.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun delete(projectId: Long, taskId: Long) {
        viewModelScope.launch(io) {
            try {
                form.update { it.copy(isSubmitting = true) }
                repo.deleteTask(taskId)
                _events.emit(TaskDialogEvent.Deleted(taskId, projectId))
            } catch (e: Exception) {
                _events.emit(TaskDialogEvent.Error(e.message ?: "Failed to delete task"))
            } finally {
                form.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
