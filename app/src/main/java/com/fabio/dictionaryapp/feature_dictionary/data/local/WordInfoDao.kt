package com.fabio.dictionaryapp.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.fabio.dictionaryapp.feature_dictionary.data.local.entry.WordInfoEntity

@Dao
interface WordInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordInfos(infos: List<WordInfoEntity>)

    @Query("DELETE FROM wordinfoentity WHERE word IN (:words)")
    suspend fun deleteWordInfos(words: List<String>)

    // atomic so a failed insert can't leave a word's cache entry deleted with nothing to replace it
    @Transaction
    suspend fun replaceWordInfos(words: List<String>, infos: List<WordInfoEntity>) {
        deleteWordInfos(words)
        insertWordInfos(infos)
    }

    // word must have '%'/'_'/'\' pre-escaped by the caller, or these are treated as LIKE wildcards
    @Query("SELECT * FROM wordinfoentity WHERE word LIKE '%' || :word || '%' ESCAPE '\\'")
    suspend fun getWordInfos(word: String): List<WordInfoEntity>
}