package fr.accoradd.gitspine.domain.usecase.graph

import fr.accoradd.gitspine.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.PriorityQueue

/**
 * Use case pour calculer le graphe des commits.
 * Algorithme inspire de gitamine (https://github.com/nicola-music/gitamine)
 */
class GraphUseCase {

    companion object {
        private const val WIP_ID = "WIP"
        private const val INDEX_BRANCH = "index"
    }

    suspend operator fun invoke(
        hasWip: Boolean,
        commits: List<Commit>,
        headCommitId: String?
    ): GraphResult = withContext(Dispatchers.Default) {
        if (commits.isEmpty() && !hasWip) {
            return@withContext GraphResult(emptyMap(), emptyList(), 0)
        }

        // Construire les maps parents et enfants
        val parents = mutableMapOf<String, List<String>>()
        val children = mutableMapOf<String, MutableList<String>>()
        val shaToIndex = mutableMapOf<String, Int>()

        commits.forEachIndexed { index, commit ->
            // Index commence a 1 si WIP, sinon 0
            val i = if (hasWip) index + 1 else index
            shaToIndex[commit.id] = i
            parents[commit.id] = commit.parents

            commit.parents.forEach { parentId ->
                children.getOrPut(parentId) { mutableListOf() }.add(commit.id)
            }
        }

        // Initialiser children pour les commits sans enfants
        commits.forEach { commit ->
            if (!children.containsKey(commit.id)) {
                children[commit.id] = mutableListOf()
            }
        }

        // Si WIP, l'ajouter comme enfant du HEAD
        if (hasWip && headCommitId != null) {
            shaToIndex[WIP_ID] = 0
            parents[WIP_ID] = listOf(headCommitId)
            children.getOrPut(headCommitId) { mutableListOf() }.add(WIP_ID)
        }

        computePositions(
            hasWip = hasWip,
            commits = commits,
            parents = parents,
            children = children,
            shaToIndex = shaToIndex,
            headCommitId = headCommitId
        )
    }

    private fun computePositions(
        hasWip: Boolean,
        commits: List<Commit>,
        parents: Map<String, List<String>>,
        children: Map<String, MutableList<String>>,
        shaToIndex: Map<String, Int>,
        headCommitId: String?
    ): GraphResult {
        val positions = mutableMapOf<String, GraphPosition>()
        val edges = mutableListOf<GraphEdge>()

        // branches[j] = commitId actuellement sur la colonne j (null si libre)
        val branches = mutableListOf<String?>(INDEX_BRANCH)

        // activeNodes[commitId] = ensemble des colonnes utilisees par ce noeud et ses descendants
        val activeNodes = mutableMapOf<String, MutableSet<Int>>()

        // Priority queue pour nettoyer les activeNodes (ordre par index croissant)
        val activeNodesQueue = PriorityQueue<Pair<Int, String>>(compareBy { it.first })

        // Initialiser avec index (pour WIP)
        activeNodes[INDEX_BRANCH] = mutableSetOf()
        if (hasWip && headCommitId != null) {
            shaToIndex[headCommitId]?.let { headIndex ->
                activeNodesQueue.add(headIndex to INDEX_BRANCH)
            }
        }

        // Position du WIP
        if (hasWip) {
            positions[WIP_ID] = GraphPosition(row = 0, column = 0)
        }

        // Traiter chaque commit
        commits.forEachIndexed { index, commit ->
            val commitSha = commit.id
            val i = if (hasWip) index + 1 else index

            val commitChildren = children[commitSha] ?: emptyList()

            // Separer les enfants "branch" (premier parent = ce commit) et "merge"
            val branchChildren = commitChildren.filter { childSha ->
                parents[childSha]?.firstOrNull() == commitSha
            }
            val mergeChildren = commitChildren.filter { childSha ->
                parents[childSha]?.firstOrNull() != commitSha
            }

            // Calculer les indices interdits (colonnes des mergeChildren)
            var highestChild: String? = null
            var iMin = Int.MAX_VALUE
            for (childSha in mergeChildren) {
                val childPos = positions[childSha]
                if (childPos != null && childPos.row < iMin) {
                    iMin = childPos.row
                    highestChild = childSha
                }
            }
            val forbiddenIndices = if (highestChild != null) {
                activeNodes[highestChild] ?: emptySet()
            } else {
                emptySet()
            }

            // Trouver un commit a remplacer (reutiliser sa colonne)
            var commitToReplace: String? = null
            var jCommitToReplace = Int.MAX_VALUE

            if (commitSha == headCommitId && hasWip) {
                // Le HEAD remplace l'index (colonne 0)
                commitToReplace = INDEX_BRANCH
                jCommitToReplace = 0
            } else {
                // Essayer de remplacer un enfant branch
                for (childSha in branchChildren) {
                    val childPos = positions[childSha]
                    if (childPos != null) {
                        val jChild = childPos.column
                        if (!forbiddenIndices.contains(jChild) && jChild < jCommitToReplace) {
                            commitToReplace = childSha
                            jCommitToReplace = jChild
                        }
                    }
                }
            }

            // Inserer le commit dans les branches
            val j: Int
            if (commitToReplace != null) {
                j = jCommitToReplace
                branches[j] = commitSha
            } else {
                // Trouver une position libre
                j = if (commitChildren.isNotEmpty()) {
                    val childPos = positions[commitChildren.first()]
                    if (childPos != null) {
                        insertCommit(commitSha, childPos.column, forbiddenIndices, branches)
                    } else {
                        insertCommit(commitSha, 0, emptySet(), branches)
                    }
                } else {
                    insertCommit(commitSha, 0, emptySet(), branches)
                }
            }

            // Nettoyer les activeNodes obsoletes
            while (activeNodesQueue.isNotEmpty() && activeNodesQueue.peek().first < i) {
                val (_, sha) = activeNodesQueue.poll()
                activeNodes.remove(sha)
            }

            // Mettre a jour les activeNodes
            val columnsToAdd = mutableListOf(j)
            branchChildren.forEach { childSha ->
                positions[childSha]?.column?.let { columnsToAdd.add(it) }
            }
            for (activeNode in activeNodes.values) {
                columnsToAdd.forEach { col -> activeNode.add(col) }
            }
            activeNodes[commitSha] = mutableSetOf()

            // Ajouter ce commit a la queue
            val commitParents = parents[commitSha] ?: emptyList()
            if (commitParents.isNotEmpty()) {
                val iRemove = commitParents.mapNotNull { shaToIndex[it] }.maxOrNull() ?: i
                activeNodesQueue.add(iRemove to commitSha)
            }

            // Retirer les enfants branch des branches actives
            for (childSha in branchChildren) {
                if (childSha != commitToReplace) {
                    val childPos = positions[childSha]
                    if (childPos != null) {
                        branches[childPos.column] = null
                    }
                }
            }

            // Si le commit n'a pas de parent, le retirer des branches
            if (commitParents.isEmpty()) {
                branches[j] = null
            }

            // Enregistrer la position
            positions[commitSha] = GraphPosition(row = i, column = j)
        }

        // Construire les edges
        if (hasWip && headCommitId != null) {
            val wipPos = positions[WIP_ID]
            val headPos = positions[headCommitId]
            if (wipPos != null && headPos != null) {
                edges.add(GraphEdge(from = wipPos, to = headPos, type = EdgeType.Normal))
            }
        }

        for (commit in commits) {
            val commitPos = positions[commit.id] ?: continue
            val commitParents = parents[commit.id] ?: continue

            commitParents.forEachIndexed { parentIndex, parentId ->
                val parentPos = positions[parentId]
                if (parentPos != null) {
                    val edgeType = if (parentIndex > 0) EdgeType.Merge else EdgeType.Normal
                    edges.add(GraphEdge(from = commitPos, to = parentPos, type = edgeType))
                }
            }
        }

        val width = branches.size
        return GraphResult(positions, edges, width)
    }

    /**
     * Insere un commit dans une colonne libre, en essayant de rester proche de j.
     * Recherche en spirale: j, j+1, j-1, j+2, j-2, ...
     */
    private fun insertCommit(
        commitSha: String,
        j: Int,
        forbiddenIndices: Set<Int>,
        branches: MutableList<String?>
    ): Int {
        var dj = 1
        while (j - dj >= 0 || j + dj < branches.size) {
            // Essayer j + dj
            if (j + dj < branches.size && branches[j + dj] == null && !forbiddenIndices.contains(j + dj)) {
                branches[j + dj] = commitSha
                return j + dj
            }
            // Essayer j - dj
            if (j - dj >= 0 && branches[j - dj] == null && !forbiddenIndices.contains(j - dj)) {
                branches[j - dj] = commitSha
                return j - dj
            }
            dj++
        }
        // Pas de position libre, ajouter a la fin
        branches.add(commitSha)
        return branches.size - 1
    }
}
