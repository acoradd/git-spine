package fr.accoradd.gitspine.infrastructure.filesystem

import fr.accoradd.gitspine.domain.model.WatcherEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.eclipse.jgit.ignore.IgnoreNode
import java.io.File
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.isDirectory
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

class FileWatcher(
    private val gitIgnoreRules: IgnoreNode,
    private val workspacePath: Path,
    private val gitDirPath: Path,
    private val scope: CoroutineScope =  CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val watchService: WatchService = FileSystems.getDefault().newWatchService()
) : AutoCloseable {
    private var initialized = false

    private val _events = MutableSharedFlow<WatcherEvent>(replay = 0)
    val events: SharedFlow<WatcherEvent> = _events

    private var watchJob: Job? = null
    private val watchKeys = mutableMapOf<WatchKey, Path>()

    private var debounceJob: Job? = null
    private var pendingGitDirChange = false
    private var pendingWorkspaceChange = false

    fun watch(force: Boolean = false) {
        if (!force && initialized) {
            return
        }
        scope.launch {
            try {
                watchInternal(force)
            } catch (e: Exception) {
                println("Error starting file watcher: ${e.message}")
            }
        }
    }

    private fun watchInternal(force: Boolean = false) {
        if (force) {
            close()
        } else if (initialized) {
            return
        }
        initialized = true

        registerRecursive(workspacePath, gitDirPath, gitIgnoreRules)

        // Register .git/refs directory recursively
        val refsPath = gitDirPath.resolve("refs")
        if (Files.exists(refsPath)) {
            registerRecursive(refsPath, gitDirPath, gitIgnoreRules, isGitDir = true)
        }

        // Also watch .git/HEAD for branch changes
        registerDirectory(gitDirPath, isGitDir = true)

        watchJob = scope.launch(Dispatchers.IO) {
            try {
                processEvents(workspacePath, gitDirPath)
            } catch (e: ClosedWatchServiceException) {
                // Normal shutdown
            } catch (e: Exception) {
                _events.emit(WatcherEvent.WatcherError(e.message ?: "Unknown error"))
            }
        }
    }

    override fun close() {
        debounceJob?.cancel()
        watchJob?.cancel()
        watchKeys.keys.forEach { it.cancel() }
        watchKeys.clear()
        watchService.close()
        scope.cancel()
        pendingGitDirChange = false
        pendingWorkspaceChange = false
    }

    private fun registerRecursive(
        path: Path,
        gitDirPath: Path,
        gitIgnoreRules: IgnoreNode,
        isGitDir: Boolean = false
    ) {
        if (!Files.exists(path)) return

        Files.walkFileTree(path, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                // Skip .git directory unless we're explicitly watching it
                if (!isGitDir && dir.pathString.contains(".git${File.separator}")) {
                    return FileVisitResult.SKIP_SUBTREE
                }

                // Skip ignored directories (unless it's in .git)
                if (!isGitDir && shouldIgnore(dir, path, gitIgnoreRules)) {
                    return FileVisitResult.SKIP_SUBTREE
                }

                registerDirectory(dir, isGitDir)
                return FileVisitResult.CONTINUE
            }
        })
    }

    private fun registerDirectory(dir: Path, isGitDir: Boolean) {
        try {
            val key = dir.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY
            )
            watchKeys[key] = dir
        } catch (e: Exception) {
            // Ignore registration errors (may happen for permission issues)
        }
    }

    private suspend fun processEvents(workspacePath: Path, gitDirPath: Path) {
        while (currentCoroutineContext().isActive) {
            val key = watchService?.take() ?: break
            val dir = watchKeys[key] ?: continue

            for (event in key.pollEvents()) {
                val kind = event.kind()

                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue
                }

                @Suppress("UNCHECKED_CAST")
                val ev = event as WatchEvent<Path>
                val filename = ev.context()
                val fullPath = dir.resolve(filename)

                // Determine if this is a git directory change
                val isGitDirChange = fullPath.pathString.startsWith(gitDirPath.pathString)

                if (isGitDirChange) {
                    pendingGitDirChange = true
                } else {
                    pendingWorkspaceChange = true
                }

                // Handle new directories (register them for watching)
                if (kind == StandardWatchEventKinds.ENTRY_CREATE && fullPath.isDirectory()) {
                    // We don't have gitIgnoreRules here, so we'll let the debounced refresh handle it
                }

                // Debounce: wait 500ms after last event before emitting
                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(500)
                    if (pendingGitDirChange || pendingWorkspaceChange) {
                        _events.emit(
                            WatcherEvent.WorkspaceChanged(
                                hasGitDirChanged = pendingGitDirChange
                            )
                        )
                        pendingGitDirChange = false
                        pendingWorkspaceChange = false
                    }
                }
            }

            val valid = key.reset()
            if (!valid) {
                watchKeys.remove(key)
            }
        }
    }

    private fun shouldIgnore(path: Path, basePath: Path, gitIgnoreRules: IgnoreNode): Boolean {
        try {
            val relativePath = path.relativeTo(basePath).pathString
            val isDirectory = path.isDirectory()
            return gitIgnoreRules.isIgnored(relativePath, isDirectory) == IgnoreNode.MatchResult.IGNORED
        } catch (e: Exception) {
            return false
        }
    }
}
