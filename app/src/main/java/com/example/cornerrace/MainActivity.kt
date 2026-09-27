package com.example.cornerrace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cornerrace.feedback.LocalSoundEngine
import com.example.cornerrace.feedback.SoundEngine
import com.example.cornerrace.ui.AppViewModel
import com.example.cornerrace.ui.CornerRaceNavHost
import com.example.cornerrace.ui.theme.CornerRaceTheme

class MainActivity : ComponentActivity() {

    private val soundEngine = SoundEngine()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val settings by appViewModel.settings.collectAsStateWithLifecycle()
            CornerRaceTheme(themeMode = settings.themeMode, boardTheme = settings.boardTheme) {
                CompositionLocalProvider(LocalSoundEngine provides soundEngine) {
                    CornerRaceNavHost(appViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        soundEngine.release()
        super.onDestroy()
    }
}
