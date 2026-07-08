package com.fabio.dictionaryapp.feature_dictionary.domain.use_case

import com.fabio.dictionaryapp.core.util.Resource
import com.fabio.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.fabio.dictionaryapp.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetWordInfo(
    private val repository: WordInfoRepository
) {

    operator fun invoke(word: String): Flow<Resource<List<WordInfo>>> {
        val trimmedWord = word.trim()
        if (trimmedWord.isBlank() || trimmedWord.length > MAX_WORD_LENGTH) {
            return flow { }
        }
        return repository.getWordInfo(trimmedWord)
    }

    companion object {
        // no dictionary entry is anywhere near this long; guards against pathological input
        private const val MAX_WORD_LENGTH = 100
    }
}