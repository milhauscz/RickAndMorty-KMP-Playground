package cz.cernilovsky.kmp.rickandmorty.characters.ui.detail

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import cz.cernilovsky.kmp.rickandmorty.episode.domain.model.Episode
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.error_unknown
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CharacterDetailScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val detail =
        UiCharacterDetail(
            id = 1,
            name = "Rick Sanchez",
            image = "",
            status = CharacterStatus.Alive,
            species = "Human",
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
                ),
        )

    @Test
    fun content_showsOriginAndEpisode() {
        composeTestRule.setContent {
            MaterialTheme {
                CharacterDetailScreen(
                    uiState = CharacterDetailUiState(detail = detail, isLoading = false),
                    onBack = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule
            .onNodeWithTag(CharacterDetailContentTestTag)
            .performScrollToNode(hasText("Earth (C-137)"))
        composeTestRule.onNodeWithText("Earth (C-137)").assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(CharacterDetailContentTestTag)
            .performScrollToNode(hasText("S01E01 - Pilot"))
        composeTestRule.onNodeWithText("S01E01 - Pilot").assertIsDisplayed()
    }

    @Test
    fun loading_showsProgressIndicator() {
        composeTestRule.setContent {
            MaterialTheme {
                CharacterDetailScreen(
                    uiState = CharacterDetailUiState(detail = null, isLoading = true),
                    onBack = {},
                    onRetry = {},
                )
            }
        }

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun error_showsRetryAndInvokesCallback() {
        var retried = false
        composeTestRule.setContent {
            MaterialTheme {
                CharacterDetailScreen(
                    uiState =
                        CharacterDetailUiState(
                            detail = null,
                            isLoading = false,
                            errorMessage = Res.string.error_unknown,
                        ),
                    onBack = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue(retried)
    }
}
