package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.accoradd.gitspine.ui.components.common.SearchField
import java.nio.file.Path

data class RecentRepository(val name: String, val path: Path)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepositoryDropdown(
    recentRepositories: List<RecentRepository>,
    onOpen: () -> Unit,
    onClone: () -> Unit,
    onSelect: (Path) -> Unit,
    modifier: Modifier = Modifier,
    trigger: @Composable (onClick: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = modifier) {
        trigger{ expanded = true }

        // Désactive la hauteur minimale forcée pour le tactile (48dp)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(350.dp)
            ) {
                // --- Actions ---
                // On utilise une hauteur fixe de 30dp pour un look "Desktop"
                val itemHeight = 30.dp
                val itemPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)

                DropdownMenuItem(
                    text = { Text("New Project", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { /* TODO */ expanded = false },
                    leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    contentPadding = itemPadding,
                    modifier = Modifier.height(itemHeight)
                )
                DropdownMenuItem(
                    text = { Text("Open...", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { 
                        expanded = false
                        onOpen() 
                    },
                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    contentPadding = itemPadding,
                    modifier = Modifier.height(itemHeight)
                )
                DropdownMenuItem(
                    text = { Text("Clone Repository...", style = MaterialTheme.typography.bodyMedium) },
                    onClick = { 
                        expanded = false
                        onClone() 
                    },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    contentPadding = itemPadding,
                    modifier = Modifier.height(itemHeight)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // --- Search ---
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        onDebouncedValueChange = { /* Local filtering */ },
                        placeholder = "Search...",
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // --- Recent Repositories ---
                val filteredRepos = if (searchQuery.isBlank()) {
                    recentRepositories
                } else {
                    recentRepositories.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.path.toString().contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredRepos.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No recent repositories found", style = MaterialTheme.typography.bodySmall) },
                        onClick = {},
                        enabled = false,
                        contentPadding = itemPadding,
                        modifier = Modifier.height(itemHeight)
                    )
                } else {
                    filteredRepos.forEach { repo ->
                        DropdownMenuItem(
                            text = {
                                Column(verticalArrangement = Arrangement.Center) {
                                    Text(repo.name, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        repo.path.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = { 
                                expanded = false
                                onSelect(repo.path) 
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            // Pas de hauteur fixe ici car il y a 2 lignes de texte, on laisse le contenu décider
                            // mais le padding vertical est réduit
                        )
                    }
                }
            }
        }
    }
}
