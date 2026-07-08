package com.fabio.dictionaryapp

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fabio.dictionaryapp.feature_dictionary.domain.model.Definition
import com.fabio.dictionaryapp.feature_dictionary.domain.model.Meaning
import com.fabio.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.fabio.dictionaryapp.feature_dictionary.presentation.WordInfoScreen
import com.fabio.dictionaryapp.feature_dictionary.presentation.WordInfoState
import com.fabio.dictionaryapp.ui.theme.DictionaryAppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordInfoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun fakeWordInfo() = WordInfo(
        word = "hello",
        phonetic = "/həˈloʊ/",
        origin = null,
        meanings = listOf(
            Meaning(
                partOfSpeech = "exclamation",
                definitions = listOf(
                    Definition(
                        definition = "used as a greeting",
                        example = null,
                        synonyms = emptyList(),
                        antonyms = emptyList()
                    )
                )
            )
        )
    )

    @Test
    fun word_phonetic_and_definition_are_displayed_for_a_populated_state() {
        composeTestRule.setContent {
            DictionaryAppTheme {
                WordInfoScreen(
                    state = WordInfoState(wordInfoItems = listOf(fakeWordInfo())),
                    searchQuery = "",
                    onSearch = {}
                )
            }
        }

        composeTestRule.onNodeWithText("hello").assertIsDisplayed()
        composeTestRule.onNodeWithText("/həˈloʊ/").assertIsDisplayed()
        composeTestRule.onNodeWithText("1. used as a greeting").assertIsDisplayed()
        composeTestRule.onNode(hasProgressBarRangeInfo(androidx.compose.ui.semantics.ProgressBarRangeInfo.Indeterminate))
            .assertDoesNotExist()
    }
}
