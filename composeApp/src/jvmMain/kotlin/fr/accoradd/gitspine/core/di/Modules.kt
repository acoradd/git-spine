package fr.accoradd.gitspine.core.di

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.core.tabs.TabsManager
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.domain.usecase.graph.GetGraphUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.StageFileUseCase
import fr.accoradd.gitspine.infrastructure.git.JGitRepository
import fr.accoradd.gitspine.infrastructure.settings.PreferencesSettings
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Core
    single { TabsManager() }
    single { NotificationManager() }

    // Settings
    single<Settings> { PreferencesSettings() }

    // Repositories
    single<GitRepository> { JGitRepository() }

    // UseCases
    factory { GetGraphUseCase(get()) }
    factory { StageFileUseCase(get()) }

    // ViewModels
    viewModel { GraphViewModel(get()) }
}
