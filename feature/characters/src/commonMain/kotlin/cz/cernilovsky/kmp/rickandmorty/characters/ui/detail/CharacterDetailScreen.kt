package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.Res
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.button_back
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.button_retry
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_status
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_air_date
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_current_location
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_dimension
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_episodes
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_gender
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_origin
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_species
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.detail_type
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.error_unknown
import cz.cernilovsky.kmp.rickandmorty.core.ui.registerSharedElement
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import cz.cernilovsky.kmp.rickandmorty.location.domain.model.Location
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

// Default hero image/app-bar height. Callers on large-height windows (e.g. the two-pane tablet
// layout) may pass a taller value so the hero image isn't cropped down to a sliver.
internal val IMAGE_HEIGHT = 280.dp

internal const val CHARACTER_DETAIL_CONTENT_TEST_TAG = "characterDetailContent"

@Composable
fun CharacterDetailScreen(
    characterId: Int,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    imageHeight: Dp = IMAGE_HEIGHT,
) {
    // Key by id so that swapping the selected character in two-pane mode creates a fresh
    // ViewModel for the new id instead of reusing the previous character's state.
    val viewModel =
        koinViewModel<CharacterDetailViewModel>(key = characterId.toString()) {
            parametersOf(characterId)
        }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharacterDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::refresh,
        showBackButton = showBackButton,
        modifier = modifier,
        imageHeight = imageHeight,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    uiState: CharacterDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
    imageHeight: Dp = IMAGE_HEIGHT,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CollapsingImageTopBar(
                name = uiState.detail?.name.orEmpty(),
                imageUrl = uiState.detail?.image,
                scrollBehavior = scrollBehavior,
                onBack = onBack,
                showBackButton = showBackButton,
                imageHeight = imageHeight,
            )
        },
    ) { innerPadding ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            val detail = uiState.detail
            when {
                detail != null -> {
                    CharacterDetailContent(
                        detail = detail,
                        isLoading = uiState.isLoading,
                        errorMessage = uiState.errorMessage,
                        onRetry = onRetry,
                    )
                }

                uiState.isLoading -> {
                    MaxSizeLoadingIndicator()
                }

                uiState.errorMessage != null -> {
                    ErrorMessage(
                        error = uiState.errorMessage,
                        onRetryClicked = onRetry,
                    )
                }

                else -> {
                    MaxSizeLoadingIndicator()
                }
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
    showBackButton: Boolean = true,
    imageHeight: Dp = IMAGE_HEIGHT,
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
                        }.registerSharedElement(createKeyForSharedTransitionAvatarUrl(imageUrl)),
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
                if (showBackButton) {
                    FilledTonalIconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.button_back),
                        )
                    }
                }
            },
            expandedHeight = imageHeight,
            scrollBehavior = scrollBehavior,
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
        )
    }
}

@Composable
private fun CharacterDetailContent(
    detail: UiCharacterDetail,
    isLoading: Boolean,
    errorMessage: StringResource?,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag(CHARACTER_DETAIL_CONTENT_TEST_TAG),
        contentPadding = PaddingValues(all = 16.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LocationCard(
                    title = stringResource(Res.string.detail_origin),
                    name = detail.originName,
                    location = detail.origin,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                )
                LocationCard(
                    title = stringResource(Res.string.detail_current_location),
                    name = detail.locationName,
                    location = detail.location,
                    isLoading = isLoading,
                    modifier = Modifier.weight(1f),
                )
                if (detail.type.isNotBlank()) {
                    TypeCard(
                        type = detail.type,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
        item {
            SectionTitle(stringResource(Res.string.detail_episodes))
        }
        if (detail.episodes.isEmpty() && isLoading) {
            item {
                MaxWidthCircularProgressIndicator()
            }
        } else if (detail.episodes.isNotEmpty()) {
            item {
                EpisodeCarousel(detail.episodes)
            }
        }
        if (errorMessage != null) {
            item {
                DetailError(
                    errorMessage = errorMessage,
                    onRetry = onRetry,
                )
            }
        }
    }
}

@Composable
private fun DetailError(
    errorMessage: StringResource,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(errorMessage),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(Res.string.button_retry))
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StatusCard(detail.status, modifier = Modifier.weight(1f))
        SpeciesCard(detail.species, modifier = Modifier.weight(1f))
        GenderCard(detail.gender, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatusCard(
    status: CharacterStatus,
    modifier: Modifier = Modifier,
) {
    DetailCard(modifier = modifier) {
        CardTitle(
            title = stringResource(Res.string.character_status),
            icon = Icons.Default.MonitorHeart,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(8.dp)
                        .background(color = status.dotColor(), shape = CircleShape),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(status.toStringResource()),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun SpeciesCard(
    species: String,
    modifier: Modifier = Modifier,
) {
    DetailCard(modifier = modifier) {
        CardTitle(
            title = stringResource(Res.string.detail_species),
            icon = Icons.Default.Pets,
        )
        Text(
            text = species,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun GenderCard(
    gender: CharacterGender,
    modifier: Modifier = Modifier,
) {
    DetailCard(modifier = modifier) {
        CardTitle(
            title = stringResource(Res.string.detail_gender),
            icon = Icons.Default.Wc,
        )
        Text(
            text = stringResource(gender.toStringResource()),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun TypeCard(
    type: String,
    modifier: Modifier = Modifier,
) {
    DetailCard(modifier = modifier) {
        CardTitle(
            title = stringResource(Res.string.detail_type),
            icon = Icons.Default.Category,
        )
        Text(
            text = type,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun LocationCard(
    title: String,
    name: String,
    location: Location?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    DetailCard(modifier = modifier) {
        CardTitle(title = title, icon = Icons.Default.LocationOn)
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
        )
        if (location != null) {
            CardLabeledValue(
                label = stringResource(Res.string.detail_type),
                value = location.type,
            )
            CardLabeledValue(
                label = stringResource(Res.string.detail_dimension),
                value = location.dimension,
            )
        } else if (isLoading) {
            MaxWidthCircularProgressIndicator()
        }
    }
}

@Composable
private fun EpisodeCarousel(
    episodes: List<Episode>,
    modifier: Modifier = Modifier,
) {
    val carouselState = rememberCarouselState { episodes.size }
    HorizontalUncontainedCarousel(
        state = carouselState,
        itemWidth = 220.dp,
        itemSpacing = 8.dp,
        modifier = modifier.fillMaxWidth().height(120.dp),
    ) { index ->
        val episode = episodes[index]
        EpisodeCard(
            episode = episode,
            modifier =
                Modifier
                    .fillMaxSize()
                    .maskClip(MaterialTheme.shapes.large),
        )
    }
}

@Composable
private fun EpisodeCard(
    episode: Episode,
    modifier: Modifier = Modifier,
) {
    DetailCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocalMovies,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${episode.episode} - ${episode.name}",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        CardLabeledValue(
            label = stringResource(Res.string.detail_air_date),
            value = episode.airDate,
        )
    }
}

@Composable
private fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            content = content,
        )
    }
}

@Composable
private fun CardTitle(
    title: String,
    icon: ImageVector? = null,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CardLabeledValue(
    label: String,
    value: String,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
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
                            type = "Genius",
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

@Preview
@Composable
fun CharacterDetailScreenErrorPreview() {
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
                            type = "Genius",
                            gender = CharacterGender.Male,
                            originName = "Earth (C-137)",
                            origin = null,
                            locationName = "Citadel of Ricks",
                            location = null,
                            episodes = emptyList(),
                        ),
                    isLoading = false,
                    errorMessage = Res.string.error_unknown,
                ),
            onBack = {},
            onRetry = {},
        )
    }
}

@Preview
@Composable
fun CharacterDetailLocationLoadingScreenPreview() {
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
                            type = "Genius",
                            gender = CharacterGender.Male,
                            originName = "Earth (C-137)",
                            origin = null,
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
                    isLoading = true,
                ),
            onBack = {},
            onRetry = {},
        )
    }
}
