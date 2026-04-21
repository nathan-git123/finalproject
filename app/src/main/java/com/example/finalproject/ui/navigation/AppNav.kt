package com.example.finalproject.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finalproject.ui.alerts.AlertsScreen
import com.example.finalproject.ui.auth.AuthScreen
import com.example.finalproject.ui.chat.ChatScreen
import com.example.finalproject.ui.home.HomeScreen
import com.example.finalproject.ui.stock.StockDetailScreen
import com.example.finalproject.ui.watchlist.WatchlistScreen

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val WATCHLIST = "watchlist"
    const val ALERTS = "alerts?preset={preset}"
    const val CHAT = "chat/{ticker}"
    const val STOCK = "stock/{ticker}"
    fun chat(ticker: String) = "chat/$ticker"
    fun stock(ticker: String) = "stock/$ticker"
    fun alerts(presetTicker: String? = null) =
        if (presetTicker == null) "alerts?preset=" else "alerts?preset=$presetTicker"
}


// AI generated
@Composable
fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.AUTH) {
        composable(Routes.AUTH) {
            AuthScreen(onAuthenticated = {
                nav.navigate(Routes.HOME) { popUpTo(Routes.AUTH) { inclusive = true } }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenChat = { ticker -> nav.navigate(Routes.chat(ticker)) },
                onOpenWatchlist = { nav.navigate(Routes.WATCHLIST) },
                onOpenAlerts = { nav.navigate(Routes.alerts()) },
                onOpenStock = { ticker -> nav.navigate(Routes.stock(ticker)) },
                onSignedOut = { nav.navigate(Routes.AUTH) { popUpTo(0) } }
            )
        }
        composable(Routes.WATCHLIST) { // I wrote this.
            WatchlistScreen(
                onBack = { nav.popBackStack() },
                onOpenStock = { ticker -> nav.navigate(Routes.stock(ticker)) }
            )
        }
        composable(
            Routes.ALERTS,
            arguments = listOf(navArgument("preset") {
                type = NavType.StringType
                defaultValue = ""
                nullable = false
            })
        ) { backStack ->
            val preset = backStack.arguments?.getString("preset")?.takeIf { it.isNotBlank() }
            AlertsScreen(
                onBack = { nav.popBackStack() },
                presetTicker = preset
            )
        }
        composable(Routes.CHAT) { backStack -> // I wrote this part to get a feel for it.
            val ticker = backStack.arguments?.getString("ticker") ?: "GENERAL"
            ChatScreen(ticker = ticker, onBack = { nav.popBackStack() })
        }
        composable(Routes.STOCK) { backStack ->
            val ticker = backStack.arguments?.getString("ticker") ?: "AAPL"
            StockDetailScreen(
                ticker = ticker,
                onBack = { nav.popBackStack() },
                onSetAlert = { t -> nav.navigate(Routes.alerts(t)) }
            )
        }
    }
}