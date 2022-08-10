package com.fabio.dictionaryapp.feature_dictionary.data.repository

import com.fabio.dictionaryapp.core.util.Resource
import com.fabio.dictionaryapp.feature_dictionary.data.local.WordInfoDao
import com.fabio.dictionaryapp.feature_dictionary.data.remote.DictionaryApi
import com.fabio.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.fabio.dictionaryapp.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class WordInfoRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordInfoDao
) : WordInfoRepository {
    //Using the single source of truth pattern
    override fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow {
        // Starting the loading screen
        emit(Resource.Loading())
        //Supply the data from our DB
        val wordInfos = dao.getWordInfos(word).map { it.toWordInfo() }
        emit(Resource.Loading(data = wordInfos))
        // Initiate the api call and replace the item in oud DB with the api response
        try {
            val remoteWordInfos = api.getWordInfo(word)
            dao.deleteWordInfos(remoteWordInfos.map { it.word })
            dao.insertWordInfos(remoteWordInfos.map { it.toWordInfoEntity() })
        }
        // If wer get invalid response
        catch (e: HttpException) {
            emit(
                Resource.Error(
                    message = "Something went wrong",
                    data = wordInfos
                )
            )
        }
        // if there is something wrong with the parsing or the server is not reachable
        catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "Unable to reach the server, please check your internet connection",
                    data = wordInfos
                )
            )
        }
        // if we don't have errors we emit the success (from our DB)
        val newWordInfos = dao.getWordInfos(word).map { it.toWordInfo() }
        emit(Resource.Success(newWordInfos))

    }
}