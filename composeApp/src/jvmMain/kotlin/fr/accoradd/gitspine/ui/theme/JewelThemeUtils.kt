package fr.accoradd.gitspine.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import org.jetbrains.jewel.foundation.theme.JewelTheme

/**
 * Helper object to access Jewel theme colors with backward compatibility.
 *
 * In Jewel 0.33.0, the indexed color palette (grey(1), blue(4), etc.) was replaced
 * with semantic colors. This helper maps old-style color access to semantic colors.
 */
object jewelColors {
    /**
     * Grey colors mapped to semantic text colors
     * Lower indices = darker/more prominent, higher = lighter/less prominent
     */
    @Composable
    @ReadOnlyComposable
    fun grey(index: Int): Color = when {
        index <= 2 -> JewelTheme.globalColors.panelBackground
        index <= 4 -> JewelTheme.globalColors.text.disabled
        else -> JewelTheme.globalColors.text.info
    }

    /**
     * Blue colors mapped to info/link colors
     */
    @Composable
    @ReadOnlyComposable
    fun blue(index: Int): Color = JewelTheme.globalColors.text.info

    /**
     * Yellow colors mapped to warning colors
     */
    @Composable
    @ReadOnlyComposable
    fun yellow(index: Int): Color = JewelTheme.globalColors.text.warning
}

/**
 * Access to the default text style
 */
val jewelTextStyle
    @Composable
    @ReadOnlyComposable
    get() = JewelTheme.defaultTextStyle
