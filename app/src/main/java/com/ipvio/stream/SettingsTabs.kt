package com.ipvio.stream

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Tabs at the top of a long settings page: one chip per section, only the chosen section is shown below.
 * [names] are the chip labels (German dictionary keys, translated like every other text). [key] keeps the chosen tab
 * per page while the page is open. The content is placed in the parent column of the page, below the chips.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SettingsTabs(names: List<String>, key: String, content: @Composable (Int) -> Unit) {
    var tab by remember(key) { mutableIntStateOf(0) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier) {
        names.forEachIndexed { i, n -> ChoiceChip(n, tab == i, check = false) { tab = i } }
    }
    content(tab.coerceIn(0, names.lastIndex))
}
