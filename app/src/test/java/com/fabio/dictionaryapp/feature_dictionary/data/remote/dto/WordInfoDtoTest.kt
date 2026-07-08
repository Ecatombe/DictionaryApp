package com.fabio.dictionaryapp.feature_dictionary.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class WordInfoDtoTest {

    private fun phonetic(text: String) = PhoneticDto(
        audio = "",
        license = License(name = "", url = ""),
        sourceUrl = "",
        text = text
    )

    private fun wordInfoDto(phonetic: String?, phonetics: List<PhoneticDto>) = WordInfoDto(
        meanings = emptyList(),
        origin = null,
        phonetic = phonetic,
        phonetics = phonetics,
        word = "word"
    )

    @Test
    fun toWordInfoEntity_falls_back_to_phonetics_list_when_top_level_phonetic_is_null() {
        val dto = wordInfoDto(phonetic = null, phonetics = listOf(phonetic("/wɜːd/")))

        val entity = dto.toWordInfoEntity()

        assertEquals("/wɜːd/", entity.phonetic)
    }

    @Test
    fun toWordInfoEntity_uses_top_level_phonetic_when_present() {
        val dto = wordInfoDto(phonetic = "/wɜːd/", phonetics = listOf(phonetic("/other/")))

        val entity = dto.toWordInfoEntity()

        assertEquals("/wɜːd/", entity.phonetic)
    }

    @Test
    fun toWordInfoEntity_returns_empty_string_when_no_phonetic_available_anywhere() {
        val dto = wordInfoDto(phonetic = null, phonetics = listOf(phonetic("")))

        val entity = dto.toWordInfoEntity()

        assertEquals("", entity.phonetic)
    }
}
