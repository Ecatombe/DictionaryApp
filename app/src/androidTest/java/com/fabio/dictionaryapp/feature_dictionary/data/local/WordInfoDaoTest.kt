package com.fabio.dictionaryapp.feature_dictionary.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.fabio.dictionaryapp.feature_dictionary.data.local.entry.WordInfoEntity
import com.fabio.dictionaryapp.feature_dictionary.data.util.GsonParser
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WordInfoDaoTest {

    private lateinit var db: WordInfoDatabase
    private lateinit var dao: WordInfoDao

    // mirrors WordInfoRepositoryImpl.escapeLikeWildcards() so the test proves the same
    // contract the repository relies on, without depending on that private function
    private fun String.escapeLikeWildcards(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private fun entity(word: String) = WordInfoEntity(
        meanings = emptyList(),
        origin = null,
        phonetic = "",
        word = word
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, WordInfoDatabase::class.java)
            .addTypeConverter(Converters(GsonParser(Gson())))
            .allowMainThreadQueries()
            .build()
        dao = db.dao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun searching_a_literal_percent_character_does_not_match_unrelated_words_containing_percent_as_wildcard() = runBlocking {
        dao.insertWordInfos(
            listOf(
                entity("50% off deal"), // contains a literal "50%"
                entity("1050 items")    // contains "50" as a substring, but not literal "50%"
            )
        )

        val results = dao.getWordInfos("50%".escapeLikeWildcards())

        assertEquals(listOf("50% off deal"), results.map { it.word })
    }

    @Test
    fun searching_a_literal_underscore_character_does_not_match_unrelated_single_character_differences() = runBlocking {
        dao.insertWordInfos(
            listOf(
                entity("a_b"), // contains a literal underscore
                entity("axb")  // would match "a_b" if "_" were treated as a single-char wildcard
            )
        )

        val results = dao.getWordInfos("a_b".escapeLikeWildcards())

        assertEquals(listOf("a_b"), results.map { it.word })
    }

    @Test
    fun an_unescaped_wildcard_character_matches_too_broadly_demonstrating_the_bug_the_escaping_fixes() = runBlocking {
        dao.insertWordInfos(
            listOf(
                entity("50% off deal"),
                entity("1050 items")
            )
        )

        // passing the raw, unescaped search term reproduces the pre-fix behavior
        val results = dao.getWordInfos("50%")

        assertEquals(2, results.size)
    }

    @Test
    fun replace_word_infos_atomically_swaps_cached_entries_for_the_given_words() = runBlocking {
        dao.insertWordInfos(listOf(entity("cat")))

        dao.replaceWordInfos(words = listOf("cat"), infos = listOf(entity("cat")))

        val results = dao.getWordInfos("cat".escapeLikeWildcards())
        assertEquals(1, results.size)
    }
}
