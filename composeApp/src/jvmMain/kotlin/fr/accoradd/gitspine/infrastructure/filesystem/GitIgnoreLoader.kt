package fr.accoradd.gitspine.infrastructure.filesystem

import org.eclipse.jgit.ignore.IgnoreNode
import org.eclipse.jgit.lib.Repository
import java.io.File
import java.io.FileInputStream

class GitIgnoreLoader {
    fun loadIgnoreRules(repository: Repository): IgnoreNode {
        val ignoreNode = IgnoreNode()

        // Load .gitignore from workspace root
        val gitignoreFile = File(repository.workTree, ".gitignore")
        if (gitignoreFile.exists()) {
            FileInputStream(gitignoreFile).use { input ->
                ignoreNode.parse(input)
            }
        }

        // Load global gitignore if configured
        val config = repository.config
        val excludesFile = config.getString("core", null, "excludesfile")
        if (excludesFile != null) {
            val globalIgnoreFile = File(excludesFile)
            if (globalIgnoreFile.exists()) {
                FileInputStream(globalIgnoreFile).use { input ->
                    ignoreNode.parse(input)
                }
            }
        }

        // Load .git/info/exclude
        val excludeFile = File(repository.directory, "info/exclude")
        if (excludeFile.exists()) {
            FileInputStream(excludeFile).use { input ->
                ignoreNode.parse(input)
            }
        }

        return ignoreNode
    }
}
