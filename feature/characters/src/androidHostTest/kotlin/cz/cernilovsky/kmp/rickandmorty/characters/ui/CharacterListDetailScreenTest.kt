package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.characters.FakeCharactersRepository
import cz.cernilovsky.kmp.rickandmorty.characters.FakeEpisodeRepository
import cz.cernilovsky.kmp.rickandmorty.characters.FakeLocationRepository
import cz.cernilovsky.kmp.rickandmorty.characters.character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.Character
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharacterDetailUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.GetCharactersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.ObserveSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetCharacterFiltersUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.domain.usecase.SetSelectedCharacterIdUseCase
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CHARACTER_DETAIL_CONTENT_TEST_TAG
import cz.cernilovsky.kmp.rickandmorty.characters.ui.detail.CharacterDetailViewModel
import cz.cernilovsky.kmp.rickandmorty.core.ui.theme.RickAndMortyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.robolectric.annotation.Config
import kotlin.test.AfterTest
import kotlin.test.assertEquals

// The two-pane layout is only used on expanded-width windows, so give the test one.
@Config(qualifiers = "w1280dp-h800dp")
@RunWith(AndroidJUnit4::class)
class CharacterListDetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    private fun startTestKoin(
        characters: List<Character>,
        selectedId: Int? = null,
    ): FakeCharactersRepository {
        val charactersRepository =
            FakeCharactersRepository(initialSelectedId = selectedId, characters = characters)
        startKoin {
            modules(
                module {
                    factory { GetCharactersUseCase(charactersRepository) }
                    factory { ObserveCharacterFiltersUseCase(charactersRepository) }
                    factory { SetCharacterFiltersUseCase(charactersRepository) }
                    factory { ObserveSelectedCharacterIdUseCase(charactersRepository) }
                    factory { SetSelectedCharacterIdUseCase(charactersRepository) }
                    factory {
                        GetCharacterDetailUseCase(
                            charactersRepository,
                            FakeLocationRepository(),
                            FakeEpisodeRepository(),
                        )
                    }
                    viewModel { CharactersViewModel(get(), get(), get(), get(), get()) }
                    viewModel { (id: Int) -> CharacterDetailViewModel(id, get()) }
                },
            )
        }
        return charactersRepository
    }

    @Test
    fun twoPane_rendersDetailPaneForTheSelectedCharacter() {
        startTestKoin(
            characters = listOf(character(id = 1, name = "Rick Sanchez"), character(id = 2, name = "Morty Smith")),
            selectedId = 1,
        )

        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterListDetailScreen(onFilterClick = {})
            }
        }

        // The end pane renders the detail for the selected character alongside the list pane.
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(CHARACTER_DETAIL_CONTENT_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CHARACTER_DETAIL_CONTENT_TEST_TAG).assertIsDisplayed()
    }

    // Compact width forces single-pane mode, where swiping between characters is enabled. The
    // scaffold starts on the list pane regardless of the pre-seeded selection, so the flow taps
    // into the detail pane first, matching how a user would actually get there.
    @Config(qualifiers = "w400dp-h800dp")
    @Test
    fun singlePane_swipeLeft_showsNextCharacterAndUpdatesSelection() {
        val charactersRepository =
            startTestKoin(
                characters = listOf(character(id = 1, name = "Rick Sanchez"), character(id = 2, name = "Morty Smith")),
            )

        composeTestRule.setContent {
            RickAndMortyTheme {
                CharacterListDetailScreen(onFilterClick = {})
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Rick Sanchez").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Rick Sanchez").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(CHARACTER_DETAIL_CONTENT_TEST_TAG).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag(CHARACTER_DETAIL_CONTENT_TEST_TAG).performTouchInput { swipeLeft() }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithText("Morty Smith").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Morty Smith").assertIsDisplayed()
        assertEquals(2, charactersRepository.currentSelectedCharacterId)
    }
}
