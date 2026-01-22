package fr.accoradd.gitspine.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.accoradd.gitspine.domain.model.Project
import fr.accoradd.gitspine.domain.usecase.workspace.CloneUseCase
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.nio.file.Path

class AppViewModel(
    private val cloneUseCase: CloneUseCase,
    private val navigator: AppNavigator
) : ViewModel() {

    fun cloneRepository(path: Path, url: String) {
        viewModelScope.launch {
            cloneUseCase.invoke(path, url)
                ?.run { navigator.navigateToRepository(Project(this)) }
        }
    }
}
