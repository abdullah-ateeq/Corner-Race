package com.example.cornerrace.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cornerrace.game.Player
import com.example.cornerrace.ui.game.GameScreen
import com.example.cornerrace.ui.game.MatchResult
import com.example.cornerrace.ui.home.HomeScreen
import com.example.cornerrace.ui.info.AboutScreen
import com.example.cornerrace.ui.info.HowToPlayScreen
import com.example.cornerrace.ui.result.ResultScreen
import com.example.cornerrace.ui.settings.SettingsScreen
import com.example.cornerrace.ui.splash.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val HOW_TO_PLAY = "how_to_play"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val RESULT = "result/{winner}/{turns}/{jumps}/{p1}/{p2}/{duration}"

    fun result(r: MatchResult) =
        "result/${r.winner.number}/${r.turns}/${r.totalJumps}/${r.player1InTarget}/${r.player2InTarget}/${r.durationMillis}"
}

@Composable
fun CornerRaceNavHost(
    appViewModel: AppViewModel,
    navController: NavHostController = rememberNavController()
) {
    val settings by appViewModel.settings.collectAsStateWithLifecycle()
    val stats by appViewModel.stats.collectAsStateWithLifecycle()

    fun goHome() {
        navController.navigate(Routes.HOME) {
            popUpTo(Routes.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    fun startGame() {
        navController.navigate(Routes.GAME) {
            popUpTo(Routes.HOME)
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(
                stats = stats,
                onPlay = ::startGame,
                onHowToPlay = { navController.navigate(Routes.HOW_TO_PLAY) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
                onAbout = { navController.navigate(Routes.ABOUT) { launchSingleTop = true } }
            )
        }
        composable(Routes.GAME) {
            GameScreen(
                settings = settings,
                onExit = ::goHome,
                onFinished = { result ->
                    navController.navigate(Routes.result(result)) {
                        popUpTo(Routes.HOME)
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            Routes.RESULT,
            arguments = listOf(
                navArgument("winner") { type = NavType.IntType },
                navArgument("turns") { type = NavType.IntType },
                navArgument("jumps") { type = NavType.IntType },
                navArgument("p1") { type = NavType.IntType },
                navArgument("p2") { type = NavType.IntType },
                navArgument("duration") { type = NavType.LongType }
            )
        ) { entry ->
            val args = entry.arguments
            val result = MatchResult(
                winner = if (args?.getInt("winner") == 2) Player.PLAYER_TWO else Player.PLAYER_ONE,
                turns = args?.getInt("turns") ?: 0,
                totalJumps = args?.getInt("jumps") ?: 0,
                player1InTarget = args?.getInt("p1") ?: 0,
                player2InTarget = args?.getInt("p2") ?: 0,
                durationMillis = args?.getLong("duration") ?: 0L
            )
            ResultScreen(result = result, onPlayAgain = ::startGame, onHome = ::goHome)
        }
        composable(Routes.HOW_TO_PLAY) {
            HowToPlayScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                stats = stats,
                onBack = { navController.popBackStack() },
                onThemeMode = appViewModel::setThemeMode,
                onBoardTheme = appViewModel::setBoardTheme,
                onSound = appViewModel::setSound,
                onHaptics = appViewModel::setHaptics,
                onShowLegalMoves = appViewModel::setShowLegalMoves,
                onShowTargetZones = appViewModel::setShowTargetZones,
                onResetStats = appViewModel::resetStats
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(onBack = { navController.popBackStack() })
        }
    }
}
