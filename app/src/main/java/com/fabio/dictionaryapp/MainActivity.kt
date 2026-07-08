package com.fabio.dictionaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.fabio.dictionaryapp.feature_dictionary.presentation.WordInfoScreen
import com.fabio.dictionaryapp.feature_dictionary.presentation.WordInfoViewModel
import com.fabio.dictionaryapp.ui.theme.DictionaryAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DictionaryAppTheme {
                val viewModel: WordInfoViewModel = hiltViewModel()
                val scaffoldState = rememberScaffoldState()

                LaunchedEffect(key1 = true) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is WordInfoViewModel.UIEvent.ShowSnackbar -> {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = event.message
                                )
                            }
                        }
                    }
                }

                Scaffold(scaffoldState = scaffoldState) { innerPadding ->
                    WordInfoScreen(
                        state = viewModel.state.value,
                        searchQuery = viewModel.searchQuery.value,
                        onSearch = viewModel::onSearch,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
