package de.schweizer.bft.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun BftApp() {
    Navigator(HomeScreen())
}
