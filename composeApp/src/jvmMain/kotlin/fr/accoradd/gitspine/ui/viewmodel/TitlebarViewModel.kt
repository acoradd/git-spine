package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class TitlebarState(
    val title: String? = null
)

class TitlebarViewModel() : ViewModel() {

    private val _state = MutableStateFlow(TitlebarState())
    val state: StateFlow<TitlebarState> = _state.asStateFlow()

    fun setTitle(title: String?) {
        _state.update { it.copy(title = title) }
    }
}
