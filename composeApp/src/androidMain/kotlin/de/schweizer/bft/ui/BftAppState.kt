package de.schweizer.bft.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope

@Composable
fun rememberBftAppState(
    navController: NavHostController = rememberNavController(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): BftAppState {
    return remember(
        navController,
        coroutineScope,
    ) {
        BftAppState(
            navController,
            coroutineScope,
        )
    }
}

class BftAppState(
    val navController: NavHostController,
    val coroutineScope: CoroutineScope,
)
