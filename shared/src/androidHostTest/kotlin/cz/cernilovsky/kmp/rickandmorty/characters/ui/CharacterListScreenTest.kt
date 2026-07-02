package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import rickandmorty.shared.generated.resources.Res
import rickandmorty.shared.generated.resources.error_unknown
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CharacterListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingIndicator_isDisplayed() {
        composeTestRule.setContent {
            MaxSizeLoadingIndicator()
        }

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun errorMessage_showsRetryButton() {
        composeTestRule.setContent {
            MaterialTheme {
                ErrorMessage(error = Res.string.error_unknown, onRetryClicked = {})
            }
        }

        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun errorMessage_onRetryClick_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            MaterialTheme {
                ErrorMessage(error = Res.string.error_unknown, onRetryClicked = { clicked = true })
            }
        }

        composeTestRule.onNodeWithText("Retry").performClick()

        assertTrue(clicked)
    }

    @Test
    fun character_showsNameAndSpecies() {
        val character =
            UiCharacter(
                id = 1,
                name = "Rick Sanchez",
                status = CharacterStatus.Alive,
                species = "Human",
                location = CharacterLocation(name = "Citadel of Ricks", url = ""),
                image = "",
            )
        composeTestRule.setContent {
            MaterialTheme {
                Character(character)
            }
        }

        composeTestRule.onNodeWithText("Rick Sanchez").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alive - Human", substring = true).assertIsDisplayed()
    }
}
