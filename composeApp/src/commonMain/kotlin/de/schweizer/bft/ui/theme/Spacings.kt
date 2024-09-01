package de.schweizer.bft.ui.theme

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object Spacings {
    val s = 8.dp
    val m = 16.dp
    val l = 24.dp
}

@Composable
fun VerticalSpacerS() {
    Spacer(modifier = Modifier.requiredHeight(Spacings.s))
}

@Composable
fun VerticalSpacerM() {
    Spacer(modifier = Modifier.requiredHeight(Spacings.m))
}

@Composable
fun VerticalSpacerL() {
    Spacer(modifier = Modifier.requiredHeight(Spacings.l))
}

@Composable
fun HorizontalSpacerS() {
    Spacer(modifier = Modifier.requiredWidth(Spacings.s))
}

@Composable
fun HorizontalSpacerM() {
    Spacer(modifier = Modifier.requiredWidth(Spacings.m))
}

@Composable
fun HorizontalSpacerL() {
    Spacer(modifier = Modifier.requiredWidth(Spacings.l))
}
