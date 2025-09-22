package com.aptavis.projecttrackerchallenge.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class ProjectFormState(
    val name: String = "",
    val nameError: String? = null,
    val isSubmitting: Boolean = false
)

sealed interface ProjectDialogEvent {
    data class Saved(val projectId: Long?, val isEdit: Boolean) : ProjectDialogEvent
    data class Deleted(val projectId: Long) : ProjectDialogEvent
    data class Error(val message: String) : ProjectDialogEvent
}

@HiltViewModel
class ProjectDialogViewModel @Inject constructor(
    private val repo: ProjectRepository,
    @IoDispatcher private val io: CoroutineDispatcher
) : ViewModel() {

    data class UiState(val name: String = "", val isSubmitting: Boolean = false)
    sealed interface Event {
        data class Saved(val projectId: Long?, val isEdit: Boolean) : Event
        data class Deleted(val projectId: Long) : Event
        data class Error(val message: String) : Event
    }

    val state = MutableStateFlow(UiState())
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    fun setInitial(name: String?) {
        state.update { it.copy(name = name.orEmpty()) }
    }

    fun onNameChange(v: String) = state.update { it.copy(name = v) }

    fun save(isEdit: Boolean, projectId: Long?) {
        val name = state.value.name.trim()
        // Tidak set nameError di VM—biarkan UI yang nentuin
        viewModelScope.launch(io) {
            try {
                state.update { it.copy(isSubmitting = true) }
                val id = if (isEdit && projectId != null) {
                    repo.renameProject(projectId, name)
                    projectId
                } else {
                    repo.addProject(name)
                }
                _events.emit(Event.Saved(id, isEdit))
            } catch (e: Exception) {
                _events.emit(Event.Error(e.message ?: "Failed to save project"))
            } finally {
                state.update { it.copy(isSubmitting = false) }
            }
        }
    }

    fun delete(projectId: Long) {
        viewModelScope.launch(io) {
            try {
                state.update { it.copy(isSubmitting = true) }
                repo.deleteProject(projectId)
                _events.emit(Event.Deleted(projectId))
            } catch (e: Exception) {
                _events.emit(Event.Error(e.message ?: "Failed to delete project"))
            } finally {
                state.update { it.copy(isSubmitting = false) }
            }
        }
    }
}
