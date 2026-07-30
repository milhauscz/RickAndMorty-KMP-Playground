package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.Res
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.button_retry
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Shared full-size states used by the list, detail and list-detail screens (hence in the parent
 * `ui` package rather than a screen-specific subpackage).
 */
@Composable
fun MaxSizeLoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorMessage(
    error: StringResource,
    onRetryClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(error),
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(
            modifier = Modifier.height(16.dp),
        )
        Button(
            onClick = onRetryClicked,
        ) {
            Text(
                text = stringResource(Res.string.button_retry),
            )
        }
    }
}
