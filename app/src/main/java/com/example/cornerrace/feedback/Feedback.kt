package com.example.cornerrace.feedback

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSoundEngine = staticCompositionLocalOf<SoundEngine?> { null }

enum class FeedbackEvent { SELECT, STEP, JUMP, TARGET_ENTRY, WIN }

object Haptics {
    fun perform(view: View, event: FeedbackEvent) {
        val constant = when (event) {
            FeedbackEvent.SELECT -> HapticFeedbackConstants.CLOCK_TICK
            FeedbackEvent.STEP -> HapticFeedbackConstants.CLOCK_TICK
            FeedbackEvent.JUMP -> HapticFeedbackConstants.KEYBOARD_TAP
            FeedbackEvent.TARGET_ENTRY ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM
                else HapticFeedbackConstants.CONTEXT_CLICK
            FeedbackEvent.WIN -> HapticFeedbackConstants.LONG_PRESS
        }
        view.performHapticFeedback(constant)
        if (event == FeedbackEvent.WIN && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.postDelayed({ view.performHapticFeedback(HapticFeedbackConstants.CONFIRM) }, 180)
        }
    }
}

fun FeedbackEvent.toSfx(): Sfx = when (this) {
    FeedbackEvent.SELECT -> Sfx.SELECT
    FeedbackEvent.STEP -> Sfx.STEP
    FeedbackEvent.JUMP -> Sfx.JUMP
    FeedbackEvent.TARGET_ENTRY -> Sfx.TARGET
    FeedbackEvent.WIN -> Sfx.WIN
}
