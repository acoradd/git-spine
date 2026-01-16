package fr.accoradd.gitspine.core.tabs

import fr.accoradd.gitspine.domain.model.Tab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.nio.file.Path

class TabsManager {

    private val _tabs = MutableStateFlow<List<Tab>>(emptyList())
    val tabs: StateFlow<List<Tab>> = _tabs.asStateFlow()

    private val _activeTabId = MutableStateFlow<String?>(null)
    val activeTabId: StateFlow<String?> = _activeTabId.asStateFlow()

    val activeTab: Tab?
        get() = _tabs.value.find { it.id == _activeTabId.value }

    fun openTab(path: Path): Tab {
        // Check if tab already exists for this path
        val existing = _tabs.value.find { it.path == path }
        if (existing != null) {
            _activeTabId.value = existing.id
            return existing
        }

        // Create new tab
        val tab = Tab(path = path)
        _tabs.update { it + tab }
        _activeTabId.value = tab.id
        return tab
    }

    fun closeTab(tabId: String) {
        val tabIndex = _tabs.value.indexOfFirst { it.id == tabId }
        if (tabIndex == -1) return

        _tabs.update { it.filter { tab -> tab.id != tabId } }

        // Update active tab if we closed the active one
        if (_activeTabId.value == tabId) {
            _activeTabId.value = when {
                _tabs.value.isEmpty() -> null
                tabIndex > 0 -> _tabs.value[tabIndex - 1].id
                else -> _tabs.value.firstOrNull()?.id
            }
        }
    }

    fun selectTab(tabId: String) {
        if (_tabs.value.any { it.id == tabId }) {
            _activeTabId.value = tabId
        }
    }
}
