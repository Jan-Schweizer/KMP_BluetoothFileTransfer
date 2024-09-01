package de.schweizer.bft.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bluetoothfiletransfer.composeapp.generated.resources.Res
import bluetoothfiletransfer.composeapp.generated.resources.back
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import de.schweizer.bft.ui.theme.HorizontalSpacerM
import de.schweizer.bft.ui.theme.Spacings
import de.schweizer.bft.ui.theme.VerticalSpacerL
import org.jetbrains.compose.resources.painterResource

@Composable
fun Toolbar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val navigator = LocalNavigator.currentOrThrow

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .then(modifier),
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = true),
                        onClick = {
                            onBack()
                            navigator.pop()
                        },
                    )
                    .padding(Spacings.m),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(Res.drawable.back),
                    contentDescription = null,
                    modifier = Modifier.size(Spacings.l),
                )
            }
            VerticalDivider()
            HorizontalSpacerM()
            Text(text = title, style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()
        VerticalSpacerL()
        content()
    }
}
