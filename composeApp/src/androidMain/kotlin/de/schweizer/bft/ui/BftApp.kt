package de.schweizer.bft.ui

import androidx.compose.runtime.Composable
import de.schweizer.bft.navigation.BftNavHost

@Composable
fun BftApp(appState: BftAppState = rememberBftAppState()) {
    BftNavHost(appState)
}
