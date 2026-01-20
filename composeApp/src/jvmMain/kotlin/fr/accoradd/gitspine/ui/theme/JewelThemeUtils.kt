package fr.accoradd.gitspine.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.foundation.theme.LocalColorPalette

/**
 * Helper extensions to access Jewel theme properties with a cleaner syntax.
 *
 * In Jewel 0.15.2:
 * - Colors are accessed via LocalColorPalette.current.grey(index) etc.
 * - Typography/TextStyle is accessed via JewelTheme.defaultTextStyle
 */

/**
 * Access to the current color palette
 */
val jewelColors
    @Composable
    @ReadOnlyComposable
    get() = LocalColorPalette.current

/**
 * Access to the default text style
 */
val jewelTextStyle
    @Composable
    @ReadOnlyComposable
    get() = JewelTheme.defaultTextStyle
