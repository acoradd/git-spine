package fr.accoradd.gitspine.ui.components.repository

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.nio.file.Path
import org.jetbrains.jewel.ui.component.Text

data class RecentRepository(val name: String, val path: Path)

/**
 * TODO: Réimplémenter avec l'API Dropdown de Jewel une fois l'API documentée
 * Pour l'instant, affiche simplement le nom du repository courant
 */
@Composable
fun RepositorDropdown(
    recentRepositories: List<RecentRepository>,
    onOpen: () -> Unit,
    onClone: () -> Unit,
    onSelect: (Path) -> Unit,
    modifier: Modifier = Modifier,
    currentRepoName: String
) {
    // Affichage simple du nom du repository pour l'instant
    // TODO: Implémenter le dropdown complet avec les actions et la liste des repositories récents
    Text(
        text = currentRepoName,
        modifier = modifier
    )
}
