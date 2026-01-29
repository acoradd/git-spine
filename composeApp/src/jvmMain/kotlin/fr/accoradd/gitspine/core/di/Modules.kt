package fr.accoradd.gitspine.core.di

import fr.accoradd.gitspine.core.notifications.NotificationManager
import fr.accoradd.gitspine.core.settings.Settings
import fr.accoradd.gitspine.domain.repository.GitRepository
import fr.accoradd.gitspine.domain.repository.RecentRepositoryStore
import fr.accoradd.gitspine.domain.usecase.graph.GraphUseCase
import fr.accoradd.gitspine.domain.usecase.workspace.*
import fr.accoradd.gitspine.infrastructure.filesystem.GitIgnoreLoader
import fr.accoradd.gitspine.infrastructure.git.GitSession
import fr.accoradd.gitspine.infrastructure.git.JGitRepository
import fr.accoradd.gitspine.infrastructure.persistence.JsonRecentRepositoryStore
import fr.accoradd.gitspine.infrastructure.persistence.JsonSettings
import fr.accoradd.gitspine.infrastructure.system.SystemThemeDetector
import fr.accoradd.gitspine.ui.navigation.AppNavigator
import fr.accoradd.gitspine.ui.viewmodel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Core
    single { NotificationManager() }

    // Settings
    single<Settings> { JsonSettings() }

    // Persistence
    single<RecentRepositoryStore> { JsonRecentRepositoryStore() }

    // Repositories
    single<GitRepository> { JGitRepository(get(), get()) }
    single { GitIgnoreLoader() }
    single { GitSession(get()) }

    single { AppNavigator(get()) }
    single { SystemThemeDetector() }

    // UseCases - Graph
    factory { GraphUseCase() }

    // UseCases - Workspace
    factory { GetStatusUseCase(get()) }
    factory { StageFileUseCase(get()) }
    factory { UnstageFileUseCase(get()) }
    factory { StageAllUseCase(get()) }
    factory { UnstageAllUseCase(get()) }
    factory { DiscardChangesUseCase(get()) }
    factory { CloneUseCase(get()) }
    factory { OpenProjectUseCase(get(), get()) }

    factory { FetchUseCase(get()) }
    factory { PullUseCase(get()) }
    factory { PushUseCase(get()) }
    factory { StashUseCase(get()) }
    factory { UnStashUseCase(get()) }
    factory { CreateBranchUseCase(get()) }

    // ViewModels
    single { TitlebarViewModel(get()) }
    single { ProjectViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    single { AppViewModel(get(), get(), get()) }
    viewModel { WelcomeScreenViewModel(get()) }
    viewModel { RepositoryScreenViewModel(get(), get(), get()) }
    viewModel { GravatarViewModel() }
    viewModel { WorkspaceViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
