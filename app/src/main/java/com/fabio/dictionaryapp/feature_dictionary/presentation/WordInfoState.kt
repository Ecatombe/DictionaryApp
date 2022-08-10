package com.fabio.dictionaryapp.feature_dictionary.presentation

import com.fabio.dictionaryapp.feature_dictionary.domain.model.WordInfo

data class WordInfoState(
    val wordInfoItem: List<WordInfo> = emptyList(),
    val isLoading: Boolean = false
)
