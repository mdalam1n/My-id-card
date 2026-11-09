package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.DocType
import com.example.ui.screens.DocumentDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PersonalCardsScreen
import com.example.ui.theme.MyIdCardTheme
import com.example.ui.viewmodel.DocumentViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyIdCardTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}

private const val ROUTE_HOME = "home"
private const val ROUTE_DETAIL = "detail/{docTypeName}"
private const val ROUTE_PERSONAL_CARDS = "personal_cards"

/**
 * String থেকে DocType খুঁজে বের করে। না পেলে NID ফেরত দেয়।
 * try-catch ছাড়াই কাজ করে।
 */
private fun docTypeFromName(name: String?): DocType =
    DocType.values().firstOrNull { it.name == name } ?: DocType.NID

@Composable
fun AppNavigation(viewModel: DocumentViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_HOME) {

        composable(ROUTE_HOME) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { docType ->
                    navController.navigate("detail/${docType.name}")
                },
                onNavigateToPersonalCards = {
                    navController.navigate(ROUTE_PERSONAL_CARDS)
                }
            )
        }

        composable(
            route = ROUTE_DETAIL,
            arguments = listOf(
                navArgument("docTypeName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val docType = docTypeFromName(
                backStackEntry.arguments?.getString("docTypeName")
            )

            DocumentDetailScreen(
                viewModel = viewModel,
                docType = docType,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(ROUTE_PERSONAL_CARDS) {
            PersonalCardsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
