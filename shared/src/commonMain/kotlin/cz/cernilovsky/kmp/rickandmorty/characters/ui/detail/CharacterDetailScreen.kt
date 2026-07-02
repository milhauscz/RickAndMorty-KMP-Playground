package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.characters.ui.ErrorMessage
import cz.cernilovsky.kmp.rickandmorty.characters.ui.MaxSizeLoadingIndicator
import cz.cernilovsky.kmp.rickandmorty.characters.ui.createKeyForSharedTransitionAvatarUrl
import cz.cernilovsky.kmp.rickandmorty.characters.ui.dotColor
import cz.cernilovsky.kmp.rickandmorty.characters.ui.toStringResource
import cz.cernilovsky.kmp.rickandmorty.core.ui.registerSharedElement
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.button_back
import rickandmorty.shared.generated.resources.character_status
import rickandmorty.shared.generated.resources.detail_air_date
import rickandmorty.shared.generated.resources.detail_current_location
import rickandmorty.shared.generated.resources.detail_dimension
import rickandmorty.shared.generated.resources.detail_episodes
import rickandmorty.shared.generated.resources.detail_gender
import rickandmorty.shared.generated.resources.detail_origin
import rickandmorty.shared.generated.resources.detail_species
import rickandmorty.shared.generated.resources.detail_type

private val IMAGE_HEIGHT = 280.dp

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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingImageTopBar(
                name = uiState.detail?.name.orEmpty(),
                imageUrl = uiState.detail?.image,
                scrollBehavior = scrollBehavior,
                onBack = onBack,
            )
        },
    ) { innerPadding ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            val detail = uiState.detail
            when {
                detail != null -> CharacterDetailContent(detail, uiState.isLoading)
                uiState.isLoading -> MaxSizeLoadingIndicator()
                uiState.errorMessage != null ->
                    ErrorMessage(
                        error = uiState.errorMessage,
                        onRetryClicked = onRetry,
                    )

                else -> MaxSizeLoadingIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollapsingImageTopBar(
    name: String,
    imageUrl: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
) {
    Box {
        if (imageUrl != null) {
            val fadeBrush =
                Brush.verticalGradient(
                    0f to Color.Black,
                    0.5f to Color.Black,
                    1f to Color.Transparent,
                )
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                contentScale = ContentScale.FillWidth,
                alignment = Alignment.TopCenter,
                modifier =
                    Modifier
                        .matchParentSize()
                        .graphicsLayer {
                            alpha = 1f - scrollBehavior.state.collapsedFraction
                            compositingStrategy = CompositingStrategy.Offscreen
                        }.drawWithContent {
                            drawContent()
                            drawRect(brush = fadeBrush, blendMode = BlendMode.DstIn)
                        }
                        .registerSharedElement(createKeyForSharedTransitionAvatarUrl(imageUrl)),
            )
        }
        LargeTopAppBar(
            title = {
                Text(
                    text = name,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                FilledTonalButton(
                    onClick = onBack,
                ) {
                    Text(text = stringResource(Res.string.button_back))
                }
            },
            expandedHeight = IMAGE_HEIGHT,
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
        )
    }
}

@Composable
private fun CharacterDetailContent(
    detail: UiCharacterDetail,
    isLoading: Boolean,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isLoading) {
            item {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        }
        item {
            CharacterSummary(detail)
        }
        item {
            LocationSection(
                title = stringResource(Res.string.detail_origin),
                name = detail.originName,
                location = detail.origin,
                isLoading = isLoading,
            )
        }
        item {
            LocationSection(
                title = stringResource(Res.string.detail_current_location),
                name = detail.locationName,
                location = detail.location,
                isLoading = isLoading,
            )
        }
        item {
            SectionTitle(stringResource(Res.string.detail_episodes))
        }
        if (detail.episodes.isEmpty() && isLoading) {
            item {
                MaxWidthCircularProgressIndicator()
            }
        } else {
            items(
                items = detail.episodes,
                key = { episode -> episode.id },
            ) { episode ->
                EpisodeRow(episode)
            }
        }
    }
}

@Composable
private fun MaxWidthCircularProgressIndicator() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CharacterSummary(detail: UiCharacterDetail) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        CharacterStatusListItem(detail.status)
        LabeledValue(
            label = stringResource(Res.string.detail_species),
            value = detail.species,
        )
        LabeledValue(
            label = stringResource(Res.string.detail_gender),
            value = stringResource(detail.gender.toStringResource()),
        )
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

@Composable
private fun CharacterStatusListItem(status: CharacterStatus) {
    ListItem(
        overlineContent = { Text(text = stringResource(Res.string.character_status)) },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(10.dp)
                            .background(color = status.dotColor(), shape = CircleShape),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(status.toStringResource()),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
    )
}

@Composable
private fun LocationSection(
    title: String,
    name: String,
    location: Location?,
    isLoading: Boolean,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        SectionTitle(title)
        Spacer(modifier = Modifier.height(4.dp))
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
        } else if (isLoading) {
            MaxWidthCircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider()
    }
}

@Composable
private fun EpisodeRow(episode: Episode) {
    Column(
        modifier = Modifier.fillMaxWidth(),
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledValue(
    label: String,
    value: String,
) {
    ListItem(
        overlineContent = { Text(text = label) },
        headlineContent = { Text(text = value) },
    )
}

@Preview
@Composable
fun CharacterDetailScreenPreview() {
    MaterialTheme {
        CharacterDetailScreen(
            uiState =
                CharacterDetailUiState(
                    detail =
                        UiCharacterDetail(
                            id = 1,
                            name = "Rick Sanchez",
                            image = "",
                            status = CharacterStatus.Alive,
                            species = "Human",
                            gender = CharacterGender.Male,
                            originName = "Earth (C-137)",
                            origin =
                                Location(
                                    id = 1,
                                    name = "Earth (C-137)",
                                    type = "Planet",
                                    dimension = "Dimension C-137",
                                    url = "",
                                    created = "",
                                ),
                            locationName = "Citadel of Ricks",
                            location = null,
                            episodes =
                                listOf(
                                    Episode(
                                        id = 1,
                                        name = "Pilot",
                                        airDate = "December 2, 2013",
                                        episode = "S01E01",
                                        url = "",
                                        created = "",
                                    ),
                                    Episode(
                                        id = 2,
                                        name = "Lawnmower Dog",
                                        airDate = "December 9, 2013",
                                        episode = "S01E02",
                                        url = "",
                                        created = "",
                                    ),
                                ),
                        ),
                    isLoading = false,
                ),
            onBack = {},
            onRetry = {},
        )
    }
}
