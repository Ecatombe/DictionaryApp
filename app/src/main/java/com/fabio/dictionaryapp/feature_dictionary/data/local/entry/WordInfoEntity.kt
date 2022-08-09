package com.fabio.dictionaryapp.feature_dictionary.data.local.entry

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fabio.dictionaryapp.feature_dictionary.domain.model.Meaning
import com.fabio.dictionaryapp.feature_dictionary.domain.model.WordInfo

@Entity
data class WordInfoEntity(
    val meanings: List<Meaning>,
    val origin: String,
    val phonetic: String,
    val word: String,
    @PrimaryKey val id: Int? = null
) {
    fun toWordInfo(): WordInfo {
        return WordInfo(
            meanings = meanings,
            origin = origin,
            phonetic = phonetic,
            word = word
        )
    }
}