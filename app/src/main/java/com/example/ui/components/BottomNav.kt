package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.NavTab

private data class NavItem(
    val tab: NavTab,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun BottomNav(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit
) {
    val items = listOf(
        NavItem(NavTab.ANALYZER, "تحلیل", Icons.Default.Analytics, "nav_tab_analyzer"),
        NavItem(NavTab.SAVED_WORDS, "ذخیره‌ها", Icons.Default.Bookmark, "nav_tab_saved"),
        NavItem(NavTab.FLASHCARDS, "فلش‌کارت", Icons.Default.Style, "nav_tab_flashcards"),
        NavItem(NavTab.HISTORY, "کتب", Icons.Default.History, "nav_tab_history"),
        NavItem(NavTab.STOP_WORDS, "فیلترها", Icons.Default.Block, "nav_tab_stopwords")
    )

    NavigationBar(
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        items.forEach { item ->
            val selected = currentTab == item.tab
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
