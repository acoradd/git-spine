package fr.accoradd.gitspine.core.di

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.domain.usecase.graph.GetGraphUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.*
import fr.accoradd.gitspine.infrastructure.git.JGitRepository
import fr.accoradd.gitspine.infrastructure.settings.PreferencesSettings
import fr.accoradd.gitspine.ui.viewmodel.GraphViewModel
import fr.accoradd.gitspine.ui.viewmodel.WorkspaceViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Core
    single { NotificationManager() }

    // Settings
    single<Settings> { PreferencesSettings() }

    // Repositories
    single<GitRepository> { JGitRepository() }

    // UseCases - Graph
    factory { GetGraphUseCase(get()) }

    // UseCases - Workspace
    factory { GetStatusUseCase(get()) }
    factory { StageFileUseCase(get()) }
    factory { UnstageFileUseCase(get()) }
    factory { StageAllUseCase(get()) }
    factory { UnstageAllUseCase(get()) }
    factory { DiscardChangesUseCase(get()) }

    // ViewModels
    viewModel { GraphViewModel(get()) }
    viewModel { WorkspaceViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
