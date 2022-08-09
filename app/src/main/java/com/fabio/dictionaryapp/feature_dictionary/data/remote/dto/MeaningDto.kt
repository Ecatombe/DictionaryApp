package com.fabio.dictionaryapp.feature_dictionary.data.remote.dto

data class MeaningDto(
    val antonyms: List<Any>,
    val definitions: List<DefinitionDto>,
    val partOfSpeech: String,
    val synonyms: List<String>
)