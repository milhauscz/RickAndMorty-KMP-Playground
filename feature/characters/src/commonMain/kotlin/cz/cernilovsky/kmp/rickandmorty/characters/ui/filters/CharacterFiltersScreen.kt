package cz.cernilovsky.kmp.rickandmorty.characters.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.characters.ui.toStringResource
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.Res
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.button_apply
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.button_back
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.character_status
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.filter_gender_label
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.filter_name_label
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.filter_species_label
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.filter_type_label
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.filters_title
import cz.cernilovsky.kmp.rickandmorty.core.ui.theme.RickAndMortyTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class CharacterFiltersActions(
    val onBack: () -> Unit = {},
    val onNameChange: (String) -> Unit = {},
    val onSpeciesChange: (String) -> Unit = {},
    val onTypeChange: (String) -> Unit = {},
    val onStatusSelect: (CharacterStatus) -> Unit = {},
    val onGenderSelect: (CharacterGender) -> Unit = {},
    val onApply: () -> Unit = {},
)

@Composable
fun CharacterFiltersScreen(onBack: () -> Unit) {
    val viewModel = koinViewModel<CharacterFiltersViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState.isApplied) {
        if (uiState.isApplied) onBack()
    }
    CharacterFiltersScreen(
        uiState = uiState,
        actions =
            CharacterFiltersActions(
                onBack = onBack,
                onNameChange = viewModel::onNameChange,
                onSpeciesChange = viewModel::onSpeciesChange,
                onTypeChange = viewModel::onTypeChange,
                onStatusSelect = viewModel::onStatusSelect,
                onGenderSelect = viewModel::onGenderSelect,
                onApply = viewModel::apply,
            ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterFiltersScreen(
    uiState: CharacterFiltersUiState,
    actions: CharacterFiltersActions = CharacterFiltersActions(),
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.filters_title)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.button_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = actions.onApply) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(Res.string.button_apply),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = actions.onNameChange,
                label = { Text(text = stringResource(Res.string.filter_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.species,
                onValueChange = actions.onSpeciesChange,
                label = { Text(text = stringResource(Res.string.filter_species_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = uiState.type,
                onValueChange = actions.onTypeChange,
                label = { Text(text = stringResource(Res.string.filter_type_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            FilterSection(
                title = stringResource(Res.string.character_status),
                options = CharacterStatus.entries,
                selected = uiState.status,
                label = { stringResource(it.toStringResource()) },
                onSelect = actions.onStatusSelect,
            )
            FilterSection(
                title = stringResource(Res.string.filter_gender_label),
                options = CharacterGender.entries,
                selected = uiState.gender,
                label = { stringResource(it.toStringResource()) },
                onSelect = actions.onGenderSelect,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <T> FilterSection(
    title: String,
    options: List<T>,
    selected: T?,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = selected == option,
                    onClick = { onSelect(option) },
                    label = { Text(text = label(option)) },
                    colors =
                        FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                )
            }
        }
    }
}

private val previewUiState =
    CharacterFiltersUiState(
        name = "Rick",
        species = "Human",
        status = CharacterStatus.Alive,
        gender = CharacterGender.Male,
    )

@Preview
@Composable
private fun CharacterFiltersScreenPreview() {
    RickAndMortyTheme {
        CharacterFiltersScreen(uiState = previewUiState)
    }
}

@Preview
@Composable
private fun CharacterFiltersScreenDarkPreview() {
    RickAndMortyTheme(darkTheme = true) {
        CharacterFiltersScreen(uiState = previewUiState)
    }
}
