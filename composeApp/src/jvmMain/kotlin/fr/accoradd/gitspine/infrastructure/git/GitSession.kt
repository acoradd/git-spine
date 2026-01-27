package fr.accoradd.gitspine.infrastructure.git

import fr.accoradd.gitspine.infrastructure.filesystem.FileWatcher
import fr.accoradd.gitspine.infrastructure.filesystem.GitIgnoreLoader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.eclipse.jgit.ignore.IgnoreNode
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.nio.file.Path

data class GitRepositoryState(
    val repository: Repository,
    val fileWatcher: FileWatcher,
    val ignoreRules: IgnoreNode
) : AutoCloseable {
    override fun close() {
        fileWatcher.close()
        repository.close()
    }
}

class GitSession(
    val gitIgnoreLoader: GitIgnoreLoader
) {
    private val _activeRepository = MutableStateFlow<GitRepositoryState?>(null)
    val activeRepository = _activeRepository.asStateFlow()

    fun setActiveRepo(repoPath: Path): GitRepositoryState {
        val gitPath = if (repoPath.fileName.toString() != ".git") {
            repoPath.resolve(".git")
        } else repoPath
        if (_activeRepository.value?.repository?.directory?.toString() == gitPath.toString()) {
            return _activeRepository.value!!
        } else if (_activeRepository.value != null) {
            close(_activeRepository.value!!)
        }
        _activeRepository.value = open(gitPath)
        return _activeRepository.value!!
    }

    private fun open(repoPath: Path): GitRepositoryState {
        println("Opening repository at $repoPath")
        val repository = FileRepositoryBuilder()
            .setGitDir(repoPath.toFile())
            .build()

        val ignoreRules = gitIgnoreLoader.loadIgnoreRules(repository)
        val fileWatcher = FileWatcher(
            ignoreRules,
            repository.workTree.toPath(),
            repository.directory.toPath()
        )

        return GitRepositoryState(repository, fileWatcher, ignoreRules)
    }

    fun close(repoPath: Path?) {
        _activeRepository.value?.let {
            if (repoPath == null) {
                close(it)
            } else {
                val gitPath = if (repoPath.fileName.toString() != ".git") {
                    repoPath.resolve(".git")
                } else repoPath
                if (it.repository.directory.toString() == gitPath.toString()) {
                    close(it)
                    _activeRepository.value = null
                }
            }
        }
    }

    private fun close(value: GitRepositoryState) {
        println("Closing repository at ${value.repository.directory}")
        value.close()
    }
}
