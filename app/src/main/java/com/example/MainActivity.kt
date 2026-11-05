package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DocType
import com.example.ui.components.FullScreenImageViewer
import com.example.ui.screens.DocumentDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PersonalCardsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DocumentViewModel

sealed interface AppScreen {
    data object Home : AppScreen
    data class DocumentDetail(val docType: DocType) : AppScreen
    data object PersonalCards : AppScreen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PersonalDocumentApp()
            }
        }
    }
}

@Composable
fun PersonalDocumentApp(
    viewModel: DocumentViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }
    val fullScreenTarget by viewModel.fullScreenTarget.collectAsState()
    val feedbackMessage by viewModel.userFeedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is AppScreen.Home -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToDocument = { docType ->
                                currentScreen = AppScreen.DocumentDetail(docType)
                            },
                            onNavigateToPersonalCards = {
                                currentScreen = AppScreen.PersonalCards
                            }
                        )
                    }

                    is AppScreen.DocumentDetail -> {
                        BackHandler {
                            currentScreen = AppScreen.Home
                        }
                        DocumentDetailScreen(
                            docType = screen.docType,
                            viewModel = viewModel,
                            onNavigateBack = {
                                currentScreen = AppScreen.Home
                            }
                        )
                    }

                    is AppScreen.PersonalCards -> {
                        BackHandler {
                            currentScreen = AppScreen.Home
                        }
                        PersonalCardsScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                currentScreen = AppScreen.Home
                            }
                        )
                    }
                }
            }

            // Full screen zoomable image preview modal
            fullScreenTarget?.let { target ->
                FullScreenImageViewer(
                    title = target.title,
                    imagePath = target.imagePath,
                    onDismiss = {
                        viewModel.closeFullScreenPreview()
                    }
                )
            }
        }
    }
}
