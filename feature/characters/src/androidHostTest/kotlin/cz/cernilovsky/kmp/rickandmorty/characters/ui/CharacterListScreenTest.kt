package cz.cernilovsky.kmp.rickandmorty.characters.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilterField
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterFilters
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterLocation
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.Res
import cz.cernilovsky.kmp.rickandmorty.core.designsystem.resources.error_unknown
import kotlin.test.assertEquals
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

    @Test
    fun emptyFilteredMessage_isDisplayed() {
        composeTestRule.setContent {
            MaterialTheme {
                EmptyFilteredMessage()
            }
        }

        composeTestRule.onNodeWithText("No characters match your filters.").assertIsDisplayed()
    }

    @Test
    fun activeFiltersRow_showsChipPerActiveFilter() {
        val filters = CharacterFilters(name = "rick", status = CharacterStatus.Alive)
        composeTestRule.setContent {
            MaterialTheme {
                ActiveFiltersRow(filters = filters, onRemoveFilter = {}, onClearFilters = {})
            }
        }

        composeTestRule.onNodeWithText("Name: rick").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alive").assertIsDisplayed()
        composeTestRule.onNodeWithText("Clear all").assertIsDisplayed()
    }

    @Test
    fun activeFiltersRow_chipClick_invokesOnRemoveFilterWithCorrectField() {
        val filters = CharacterFilters(name = "rick")
        var removedField: CharacterFilterField? = null
        composeTestRule.setContent {
            MaterialTheme {
                ActiveFiltersRow(
                    filters = filters,
                    onRemoveFilter = { removedField = it },
                    onClearFilters = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Name: rick").performClick()

        assertEquals(CharacterFilterField.Name, removedField)
    }

    @Test
    fun activeFiltersRow_clearAllClick_invokesOnClearFilters() {
        val filters = CharacterFilters(name = "rick")
        var cleared = false
        composeTestRule.setContent {
            MaterialTheme {
                ActiveFiltersRow(filters = filters, onRemoveFilter = {}, onClearFilters = { cleared = true })
            }
        }

        composeTestRule.onNodeWithText("Clear all").performClick()

        assertTrue(cleared)
    }
}
