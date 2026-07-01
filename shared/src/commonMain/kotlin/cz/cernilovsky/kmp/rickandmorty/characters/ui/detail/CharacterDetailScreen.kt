package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.characters.ui.ErrorMessage
import cz.cernilovsky.kmp.rickandmorty.characters.ui.LoadingIndicator
import cz.cernilovsky.kmp.rickandmorty.characters.ui.toStringResource
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.button_back
import rickandmorty.shared.generated.resources.detail_air_date
import rickandmorty.shared.generated.resources.detail_current_location
import rickandmorty.shared.generated.resources.detail_dimension
import rickandmorty.shared.generated.resources.detail_episodes
import rickandmorty.shared.generated.resources.detail_gender
import rickandmorty.shared.generated.resources.detail_origin
import rickandmorty.shared.generated.resources.detail_species
import rickandmorty.shared.generated.resources.detail_type

@Composable
fun CharacterDetailScreen(
    characterId: Int,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<CharacterDetailViewModel> { parametersOf(characterId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharacterDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::refresh,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    uiState: CharacterDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.detail?.name.orEmpty()) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(text = stringResource(Res.string.button_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            val detail = uiState.detail
            when {
                detail != null -> CharacterDetailContent(detail, uiState.isLoading)
                uiState.isLoading -> LoadingIndicator()
                uiState.errorMessage != null ->
                    ErrorMessage(
                        error = uiState.errorMessage,
                        onRetryClicked = onRetry,
                    )

                else -> LoadingIndicator()
            }
        }
    }
}

@Composable
private fun CharacterDetailContent(
    detail: UiCharacterDetail,
    isLoading: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        item {
            CharacterHeader(detail)
        }
        item {
            LocationSection(
                title = stringResource(Res.string.detail_origin),
                name = detail.originName,
                location = detail.origin,
            )
        }
        item {
            LocationSection(
                title = stringResource(Res.string.detail_current_location),
                name = detail.locationName,
                location = detail.location,
            )
        }
        item {
            SectionTitle(stringResource(Res.string.detail_episodes))
        }
        items(
            items = detail.episodes,
            key = { episode -> episode.id },
        ) { episode ->
            EpisodeRow(episode)
        }
    }
}

@Composable
private fun CharacterHeader(detail: UiCharacterDetail) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        AsyncImage(
            model = detail.image,
            contentDescription = detail.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(280.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = detail.name,
            style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(10.dp)
                        .background(color = detail.status.dotColor(), shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${stringResource(detail.status.toStringResource())} - ${detail.species}",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LabeledValue(
            label = stringResource(Res.string.detail_species),
            value = detail.species,
        )
        LabeledValue(
            label = stringResource(Res.string.detail_gender),
            value = stringResource(detail.gender.toStringResource()),
        )
    }
}

@Composable
private fun LocationSection(
    title: String,
    name: String,
    location: Location?,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        SectionTitle(title)
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
        )
        if (location != null) {
            LabeledValue(
                label = stringResource(Res.string.detail_type),
                value = location.type,
            )
            LabeledValue(
                label = stringResource(Res.string.detail_dimension),
                value = location.dimension,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

@Composable
private fun EpisodeRow(episode: Episode) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = "${episode.episode} - ${episode.name}",
            style = MaterialTheme.typography.titleSmall,
        )
        LabeledValue(
            label = stringResource(Res.string.detail_air_date),
            value = episode.airDate,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

@Composable
private fun LabeledValue(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun CharacterStatus.dotColor(): Color =
    when (this) {
        CharacterStatus.Alive -> Color.Green
        CharacterStatus.Dead -> Color.Red
        CharacterStatus.Unknown -> Color.Gray
    }
