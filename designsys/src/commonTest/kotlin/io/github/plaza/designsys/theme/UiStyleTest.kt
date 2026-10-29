package io.github.plaza.designsys.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 界面风格's contract: each world decides its own palette, layering, corners and light/dark, and the
 * original one is left exactly as it was.
 */
class UiStyleTest {
    @Test
    fun `the original style is the original layering, untouched`() {
        val scheme = lightColorScheme()
        assertEquals(
            plazaLayers(scheme, darkTheme = false, einkMode = false),
            PlazaUiStyle.LAYERED.layers(scheme, darkTheme = false),
        )
        assertNull(PlazaUiStyle.LAYERED.colorScheme(darkTheme = false))
        assertNull(PlazaUiStyle.LAYERED.forcedDarkTheme)
    }

    @Test
    fun `the brutalist styles have no corners and fix their own light or dark`() {
        assertEquals(false, PlazaUiStyle.INDUSTRIAL.forcedDarkTheme)
        assertEquals(true, PlazaUiStyle.TERMINAL.forcedDarkTheme)
        listOf(PlazaUiStyle.INDUSTRIAL, PlazaUiStyle.TERMINAL).forEach { style ->
            assertTrue(style.isSquare)
            assertEquals(0.dp, style.controlCorner)
            val scheme = assertNotNull(style.colorScheme(darkTheme = style.forcedDarkTheme == true))
            assertEquals(0.dp, style.layers(scheme, style.forcedDarkTheme == true).cardRadius)
        }
    }

    @Test
    fun `industrial rules its cards in heavy ink and casts no shadow`() {
        val scheme = assertNotNull(PlazaUiStyle.INDUSTRIAL.colorScheme(darkTheme = false))
        val layers = PlazaUiStyle.INDUSTRIAL.layers(scheme, darkTheme = false)
        assertEquals(2.dp, layers.cardBorderWidth)
        assertNotNull(layers.cardBorder)
        assertFalse(layers.shadows)
    }

    @Test
    fun `editorial is flat paper with a hairline in both light and dark`() {
        listOf(false, true).forEach { dark ->
            val scheme = assertNotNull(PlazaUiStyle.EDITORIAL.colorScheme(dark))
            val layers = PlazaUiStyle.EDITORIAL.layers(scheme, dark)
            assertFalse(layers.shadows)
            assertEquals(1.dp, layers.cardBorderWidth)
            assertEquals(8.dp, layers.cardRadius)
        }
        assertNull(PlazaUiStyle.EDITORIAL.forcedDarkTheme)
    }
}
