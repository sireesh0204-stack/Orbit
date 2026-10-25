package com.portfolio.jobpilot.ui.adaptive

import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.view.WindowManager
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

enum class PerformanceTier {
    HighFidelity, // Galaxy S24, Redmi Turbo 5 (120Hz VRR, GPU shaders, spring physics)
    Optimized     // Galaxy Note 9, 60Hz displays, Battery Saver (flat chamfers, 0 dropped frames)
}

data class DeviceTierConfig(
    val tier: PerformanceTier,
    val refreshRateHz: Float,
    val isPowerSaveMode: Boolean,
    val enableDynamicBlur: Boolean,
    val enableSpecularGradients: Boolean
) {
    fun <T> dynamicAnimationSpec(): AnimationSpec<T> {
        return if (tier == PerformanceTier.HighFidelity) {
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        } else {
            tween(durationMillis = 180)
        }
    }
}

val LocalDeviceTier = compositionLocalOf {
    DeviceTierConfig(
        tier = PerformanceTier.HighFidelity,
        refreshRateHz = 120f,
        isPowerSaveMode = false,
        enableDynamicBlur = true,
        enableSpecularGradients = true
    )
}

@Composable
fun ProvideAdaptiveDeviceTier(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val config = remember(context) {
        detectDeviceTier(context)
    }

    CompositionLocalProvider(LocalDeviceTier provides config) {
        content()
    }
}

fun detectDeviceTier(context: Context): DeviceTierConfig {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isPowerSave = powerManager?.isPowerSaveMode ?: false

    val refreshRate = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.refreshRate ?: 60f
        } else {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            @Suppress("DEPRECATION")
            wm?.defaultDisplay?.refreshRate ?: 60f
        }
    } catch (_: Exception) {
        60f
    }

    // Android 12 (API 31) is required for hardware-accelerated RenderEffect blurs
    val canRenderHardwareBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val isHighTier = refreshRate >= 90f && !isPowerSave

    return DeviceTierConfig(
        tier = if (isHighTier) PerformanceTier.HighFidelity else PerformanceTier.Optimized,
        refreshRateHz = refreshRate,
        isPowerSaveMode = isPowerSave,
        enableDynamicBlur = isHighTier && canRenderHardwareBlur,
        enableSpecularGradients = isHighTier
    )
}

