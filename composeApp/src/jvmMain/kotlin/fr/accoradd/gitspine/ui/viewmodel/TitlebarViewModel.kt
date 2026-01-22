package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.usecase.workspace.OpenProjectUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TitlebarState(
    val title: String? = null,
)

class TitlebarViewModel(
    private val openProjectUseCase: OpenProjectUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TitlebarState())
    val state: StateFlow<TitlebarState> = _state.asStateFlow()

    fun setTitle(title: String?) {
        _state.update { it.copy(title = title) }
    }


    fun openProject() {
        viewModelScope.launch {
            openProjectUseCase.invoke()
        }
    }
}
