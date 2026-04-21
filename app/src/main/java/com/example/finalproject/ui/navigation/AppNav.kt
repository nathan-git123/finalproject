package com.example.finalproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finalproject.ui.auth.AuthScreen
import com.example.finalproject.ui.chat.ChatScreen
import com.example.finalproject.ui.home.HomeScreen
import com.example.finalproject.ui.watchlist.WatchlistScreen

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val WATCHLIST = "watchlist"
    const val CHAT = "chat/{ticker}"
    fun chat(ticker: String) = "chat/$ticker"
}

@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.AUTH) {
        composable(Routes.AUTH) {
            AuthScreen(onAuthenticated = {
                nav.navigate(Routes.HOME) {
                    popUpTo(Routes.AUTH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenChat = { ticker -> nav.navigate(Routes.chat(ticker)) },
                onOpenWatchlist = { nav.navigate(Routes.WATCHLIST) },
                onSignedOut = {
                    nav.navigate(Routes.AUTH) { popUpTo(0) }
                }
            )
        }
        composable(Routes.WATCHLIST) {
            WatchlistScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.CHAT) { backStack ->
            val ticker = backStack.arguments?.getString("ticker") ?: "GENERAL"
            ChatScreen(ticker = ticker, onBack = { nav.popBackStack() })
        }
    }
}
