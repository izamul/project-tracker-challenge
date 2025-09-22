package com.aptavis.projecttrackerchallenge.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import com.aptavis.projecttrackerchallenge.data.repository.ProjectRepository
import com.aptavis.projecttrackerchallenge.domain.model.ProjectComputed
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ProjectRepository
) : ViewModel() {

    val projects: LiveData<List<ProjectComputed>> = repo.projects()

    fun addProject(name: String) = viewModelScope.launch {
        repo.addProject(name)
    }
}
