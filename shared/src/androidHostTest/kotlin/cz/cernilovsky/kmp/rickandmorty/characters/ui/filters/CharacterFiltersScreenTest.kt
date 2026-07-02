package cz.cernilovsky.kmp.rickandmorty.characters.ui.filters

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterGender
import cz.cernilovsky.kmp.rickandmorty.characters.domain.model.CharacterStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CharacterFiltersScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fields_arePrefilledFromState() {
        val uiState = CharacterFiltersUiState(name = "rick", species = "Human", type = "Scientist")
        composeTestRule.setContent {
            MaterialTheme {
                CharacterFiltersScreen(uiState = uiState)
            }
        }

        composeTestRule.onNodeWithText("rick").assertIsDisplayed()
        composeTestRule.onNodeWithText("Human").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scientist").assertIsDisplayed()
    }

    @Test
    fun nameField_textInput_invokesOnNameChange() {
        var newName: String? = null
        composeTestRule.setContent {
            MaterialTheme {
                CharacterFiltersScreen(
                    uiState = CharacterFiltersUiState(),
                    actions = CharacterFiltersActions(onNameChange = { newName = it }),
                )
            }
        }

        composeTestRule.onNodeWithText("Name").performTextInput("rick")

        assertEquals("rick", newName)
    }

    @Test
    fun statusChip_click_invokesOnStatusSelect() {
        var selectedStatus: CharacterStatus? = null
        composeTestRule.setContent {
            MaterialTheme {
                CharacterFiltersScreen(
                    uiState = CharacterFiltersUiState(),
                    actions = CharacterFiltersActions(onStatusSelect = { selectedStatus = it }),
                )
            }
        }

        composeTestRule.onNodeWithText("Alive").performClick()

        assertEquals(CharacterStatus.Alive, selectedStatus)
    }

    @Test
    fun genderChip_click_invokesOnGenderSelect() {
        var selectedGender: CharacterGender? = null
        composeTestRule.setContent {
            MaterialTheme {
                CharacterFiltersScreen(
                    uiState = CharacterFiltersUiState(),
                    actions = CharacterFiltersActions(onGenderSelect = { selectedGender = it }),
                )
            }
        }

        composeTestRule.onNodeWithText("Male").performScrollTo().performClick()

        assertEquals(CharacterGender.Male, selectedGender)
    }

    @Test
    fun applyButton_click_invokesOnApply() {
        var applied = false
        composeTestRule.setContent {
            MaterialTheme {
                CharacterFiltersScreen(
                    uiState = CharacterFiltersUiState(),
                    actions = CharacterFiltersActions(onApply = { applied = true }),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Apply").performClick()

        assertTrue(applied)
    }

    @Test
    fun backButton_click_invokesOnBack() {
        var backClicked = false
        composeTestRule.setContent {
            MaterialTheme {
                CharacterFiltersScreen(
                    uiState = CharacterFiltersUiState(),
                    actions = CharacterFiltersActions(onBack = { backClicked = true }),
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backClicked)
    }
}
