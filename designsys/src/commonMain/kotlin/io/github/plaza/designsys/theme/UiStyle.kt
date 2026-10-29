package io.github.plaza.designsys.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

/**
 * 界面风格 — the visual language the whole app is drawn in, not a colour.
 *
 * A colour choice tints one world; a style is a different world. Each one decides the four things
 * every screen is built from — the palette, the page/card layering ([PlazaLayers]), the corner
 * geometry ([Shapes], [ControlShape], [StatusShapes]) and the type families — so the forty-odd
 * screens change together without one of them being told.
 *
 * - [LAYERED] 轻盈层叠: the original direction. Grey page, white cards lifted by a barely-there
 *   shadow, generous rounding; the colour comes from the reader's seed.
 * - [EDITORIAL] 纸刊: a printed journal. Warm bone paper, cards set flat on it and told apart by a
 *   single hairline, crisp 8dp corners, serif headings, charcoal ink and washed-out pastel tags.
 * - [INDUSTRIAL] 工业: a Swiss machine manual. Unbleached newsprint, every corner square, 2dp
 *   carbon-ink rules around each block, black-weight headings, monospace meta lines and hazard red
 *   as the only accent. Light only: it is ink on paper.
 * - [TERMINAL] 终端: a mainframe console. A dead CRT's near-black, phosphor-white monospace for
 *   everything, square cells ruled by dim grid lines and the same hazard red. Dark only.
 *
 * The three new worlds bring their own palettes, so the seed, 角色预设 and the system palette do not
 * reach them; 墨水屏模式 still wins over every style, for the reason it wins over every colour.
 */
enum class PlazaUiStyle {
    LAYERED,
    EDITORIAL,
    INDUSTRIAL,
    TERMINAL,
    ;

    /** The style's own answer to 明暗, or null when it follows the reader's. */
    val forcedDarkTheme: Boolean?
        get() =
            when (this) {
                INDUSTRIAL -> false
                TERMINAL -> true
                else -> null
            }

    /** Whether this style's palette replaces the seed-grown one. */
    val ownsPalette: Boolean get() = this != LAYERED

    /** Square corners everywhere, the brutalist rule. */
    val isSquare: Boolean get() = this == INDUSTRIAL || this == TERMINAL
}

/** The style [PlazaTheme] was given; read by the few shapes that are not on Material's scale. */
val LocalPlazaUiStyle = staticCompositionLocalOf { PlazaUiStyle.LAYERED }

/** The style's palette, or null for [PlazaUiStyle.LAYERED], whose colour is the reader's seed. */
internal fun PlazaUiStyle.colorScheme(darkTheme: Boolean): ColorScheme? =
    when (this) {
        PlazaUiStyle.LAYERED -> null
        PlazaUiStyle.EDITORIAL -> if (darkTheme) EditorialDark else EditorialLight
        PlazaUiStyle.INDUSTRIAL -> IndustrialScheme
        PlazaUiStyle.TERMINAL -> TerminalScheme
    }

/** The page/card layering for this style; [plazaLayers] for [PlazaUiStyle.LAYERED]. */
internal fun PlazaUiStyle.layers(
    scheme: ColorScheme,
    darkTheme: Boolean,
): PlazaLayers =
    when (this) {
        PlazaUiStyle.LAYERED -> plazaLayers(scheme, darkTheme, einkMode = false)

        // Flat paper: the card is the page's lighter sheet and a hairline is all that separates it.
        PlazaUiStyle.EDITORIAL ->
            PlazaLayers(
                page = scheme.background,
                card = scheme.surface,
                raised = scheme.surface,
                inset = scheme.surfaceContainer,
                divider = scheme.outlineVariant,
                shadows = false,
                cardBorder = scheme.outlineVariant,
                cardRadius = 8.dp,
            )

        // Ink rules: a heavy carbon outline boxes every block, and the page shows between them.
        PlazaUiStyle.INDUSTRIAL ->
            PlazaLayers(
                page = scheme.background,
                card = scheme.surface,
                raised = scheme.surface,
                inset = scheme.surfaceContainerHigh,
                divider = scheme.outline,
                shadows = false,
                cardBorder = scheme.outline,
                cardBorderWidth = 2.dp,
                cardRadius = 0.dp,
                selected = scheme.tertiaryContainer,
            )

        // A grid of cells: the card is barely off the background and the dim rule does the work.
        PlazaUiStyle.TERMINAL ->
            PlazaLayers(
                page = scheme.background,
                card = scheme.surface,
                raised = scheme.surfaceContainerHigh,
                inset = scheme.surfaceContainerLowest,
                divider = scheme.outlineVariant,
                shadows = false,
                cardBorder = scheme.outline,
                cardRadius = 0.dp,
                selected = scheme.primaryContainer,
            )
    }

/** The corner scale. Brutalist styles have none; 纸刊 caps at a crisp 12dp. */
internal val PlazaUiStyle.shapes: Shapes
    get() =
        when {
            this == PlazaUiStyle.EDITORIAL ->
                Shapes(
                    extraSmall = RoundedCornerShape(3.dp),
                    small = RoundedCornerShape(4.dp),
                    medium = RoundedCornerShape(6.dp),
                    large = RoundedCornerShape(8.dp),
                    extraLarge = RoundedCornerShape(12.dp),
                    largeIncreased = RoundedCornerShape(10.dp),
                    extraLargeIncreased = RoundedCornerShape(12.dp),
                    extraExtraLarge = RoundedCornerShape(12.dp),
                )

            isSquare ->
                Shapes(
                    extraSmall = Square,
                    small = Square,
                    medium = Square,
                    large = Square,
                    extraLarge = Square,
                    largeIncreased = Square,
                    extraLargeIncreased = Square,
                    extraExtraLarge = Square,
                )

            else -> PlazaShapes
        }

/**
 * A component's own corner under the style in force: none in the brutalist styles, at most 8dp in
 * 纸刊, [default] otherwise. For the few components whose corner is not on Material's scale.
 */
@Composable
@ReadOnlyComposable
fun styledCorner(default: Dp): Dp {
    val style = LocalPlazaUiStyle.current
    return when {
        style.isSquare -> 0.dp
        style == PlazaUiStyle.EDITORIAL -> minOf(default, 8.dp)
        else -> default
    }
}

/** Square in the brutalist styles, [shape] otherwise — for corners given as a percentage. */
@Composable
@ReadOnlyComposable
fun styledShape(shape: Shape): Shape = if (LocalPlazaUiStyle.current.isSquare) Square else shape

/** A corner-based square: Material's scale takes only [androidx.compose.foundation.shape.CornerBasedShape]s. */
private val Square = RoundedCornerShape(0.dp)

/** [ControlShape]'s radius in this style. */
internal val PlazaUiStyle.controlCorner: Dp
    get() =
        when {
            isSquare -> 0.dp
            this == PlazaUiStyle.EDITORIAL -> 6.dp
            else -> 14.dp
        }

/**
 * The style's type families over the scale [plazaTypography] already scaled.
 *
 * No italics and no all-caps in any of them, the same rule [PlazaTypography] keeps: neither renders
 * acceptably in Chinese. What changes is the family and the weight contrast.
 */
internal fun PlazaUiStyle.typography(base: Typography): Typography =
    when (this) {
        PlazaUiStyle.LAYERED -> base

        // Serif for every heading, tightened the way a printed headline is; sans stays for reading.
        PlazaUiStyle.EDITORIAL ->
            base.copy(
                displayLarge = base.displayLarge.serifHeading(),
                displayMedium = base.displayMedium.serifHeading(),
                displaySmall = base.displaySmall.serifHeading(),
                headlineLarge = base.headlineLarge.serifHeading(),
                headlineMedium = base.headlineMedium.serifHeading(),
                headlineSmall = base.headlineSmall.serifHeading(),
                titleLarge = base.titleLarge.serifHeading(),
                titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
                titleSmall = base.titleSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
            )

        // Black-weight blocks for headings, typewriter monospace for every label and meta line.
        PlazaUiStyle.INDUSTRIAL ->
            base.copy(
                displayLarge = base.displayLarge.blockHeading(),
                displayMedium = base.displayMedium.blockHeading(),
                displaySmall = base.displaySmall.blockHeading(),
                headlineLarge = base.headlineLarge.blockHeading(),
                headlineMedium = base.headlineMedium.blockHeading(),
                headlineSmall = base.headlineSmall.blockHeading(),
                titleLarge = base.titleLarge.blockHeading(),
                titleMedium = base.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                titleSmall = base.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                labelLarge = base.labelLarge.telemetry(),
                labelMedium = base.labelMedium.telemetry(),
                labelSmall = base.labelSmall.telemetry(),
            )

        // Monospace throughout: a console has one face.
        PlazaUiStyle.TERMINAL ->
            Typography(
                displayLarge = base.displayLarge.mono(),
                displayMedium = base.displayMedium.mono(),
                displaySmall = base.displaySmall.mono(),
                headlineLarge = base.headlineLarge.mono(),
                headlineMedium = base.headlineMedium.mono(),
                headlineSmall = base.headlineSmall.mono(),
                titleLarge = base.titleLarge.mono(),
                titleMedium = base.titleMedium.mono(),
                titleSmall = base.titleSmall.mono(),
                bodyLarge = base.bodyLarge.mono(),
                bodyMedium = base.bodyMedium.mono(),
                bodySmall = base.bodySmall.mono(),
                labelLarge = base.labelLarge.telemetry(),
                labelMedium = base.labelMedium.telemetry(),
                labelSmall = base.labelSmall.telemetry(),
            )
    }

private fun TextStyle.serifHeading() = copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, letterSpacing = (-0.02).em)

private fun TextStyle.blockHeading() = copy(fontWeight = FontWeight.Black, letterSpacing = (-0.03).em)

private fun TextStyle.mono() = copy(fontFamily = FontFamily.Monospace)

private fun TextStyle.telemetry() = copy(fontFamily = FontFamily.Monospace, letterSpacing = 0.05.em)

// 纸刊: warm monochrome with spot pastels. Ink is charcoal, never pure black; colour is for meaning.
private val EditorialLight =
    lightColorScheme(
        primary = Color(0xFF111111),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEDEBE7),
        onPrimaryContainer = Color(0xFF111111),
        secondary = Color(0xFF787774),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE1F3FE),
        onSecondaryContainer = Color(0xFF1F6C9F),
        tertiary = Color(0xFF346538),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFEDF3EC),
        onTertiaryContainer = Color(0xFF346538),
        error = Color(0xFF9F2F2D),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFDEBEC),
        onErrorContainer = Color(0xFF9F2F2D),
        background = Color(0xFFF7F6F3),
        onBackground = Color(0xFF2F3437),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF2F3437),
        surfaceVariant = Color(0xFFF1F0EC),
        onSurfaceVariant = Color(0xFF787774),
        surfaceTint = Color(0xFF111111),
        outline = Color(0xFFD3D1CB),
        outlineVariant = Color(0xFFEAEAEA),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFFBFBFA),
        surfaceContainer = Color(0xFFF7F6F3),
        surfaceContainerHigh = Color(0xFFF1F0EC),
        surfaceContainerHighest = Color(0xFFEBEAE5),
        inverseSurface = Color(0xFF2F3437),
        inverseOnSurface = Color(0xFFF7F6F3),
        inversePrimary = Color(0xFFE6E5E3),
    )

private val EditorialDark =
    darkColorScheme(
        primary = Color(0xFFE6E5E3),
        onPrimary = Color(0xFF191919),
        primaryContainer = Color(0xFF2F2F2F),
        onPrimaryContainer = Color(0xFFE6E5E3),
        secondary = Color(0xFF9B9A97),
        onSecondary = Color(0xFF191919),
        secondaryContainer = Color(0xFF1C3A4D),
        onSecondaryContainer = Color(0xFFB7DDF5),
        tertiary = Color(0xFF9CC29F),
        onTertiary = Color(0xFF191919),
        tertiaryContainer = Color(0xFF24362A),
        onTertiaryContainer = Color(0xFFC8E0CA),
        error = Color(0xFFE8A3A1),
        onError = Color(0xFF191919),
        errorContainer = Color(0xFF4A2423),
        onErrorContainer = Color(0xFFF3C9C7),
        background = Color(0xFF191919),
        onBackground = Color(0xFFE6E5E3),
        surface = Color(0xFF202020),
        onSurface = Color(0xFFE6E5E3),
        surfaceVariant = Color(0xFF2A2A2A),
        onSurfaceVariant = Color(0xFF9B9A97),
        surfaceTint = Color(0xFFE6E5E3),
        outline = Color(0xFF454545),
        outlineVariant = Color(0xFF2F2F2F),
        surfaceContainerLowest = Color(0xFF151515),
        surfaceContainerLow = Color(0xFF1D1D1D),
        surfaceContainer = Color(0xFF191919),
        surfaceContainerHigh = Color(0xFF262626),
        surfaceContainerHighest = Color(0xFF2F2F2F),
        inverseSurface = Color(0xFFE6E5E3),
        inverseOnSurface = Color(0xFF191919),
        inversePrimary = Color(0xFF111111),
    )

// 工业: carbon ink on unbleached newsprint; hazard red is the single accent and marks what is chosen.
private val IndustrialScheme =
    lightColorScheme(
        primary = Color(0xFF111111),
        onPrimary = Color(0xFFF4F4F0),
        primaryContainer = Color(0xFFE61919),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF3A3A38),
        onSecondary = Color(0xFFF4F4F0),
        secondaryContainer = Color(0xFF111111),
        onSecondaryContainer = Color(0xFFF4F4F0),
        tertiary = Color(0xFFE61919),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF6D3D0),
        onTertiaryContainer = Color(0xFF7A0B0B),
        error = Color(0xFFE61919),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFF6D3D0),
        onErrorContainer = Color(0xFF7A0B0B),
        background = Color(0xFFEAE8E3),
        onBackground = Color(0xFF111111),
        surface = Color(0xFFF4F4F0),
        onSurface = Color(0xFF111111),
        surfaceVariant = Color(0xFFDEDCD6),
        onSurfaceVariant = Color(0xFF4A4A47),
        surfaceTint = Color(0xFF111111),
        outline = Color(0xFF111111),
        outlineVariant = Color(0xFFBDBAB2),
        surfaceContainerLowest = Color(0xFFF8F8F5),
        surfaceContainerLow = Color(0xFFF4F4F0),
        surfaceContainer = Color(0xFFEAE8E3),
        surfaceContainerHigh = Color(0xFFDEDCD6),
        surfaceContainerHighest = Color(0xFFD2D0C9),
        inverseSurface = Color(0xFF111111),
        inverseOnSurface = Color(0xFFF4F4F0),
        inversePrimary = Color(0xFFFF2A2A),
    )

// 终端: phosphor white on a dead CRT, dim grid rules, the same hazard red for what is live.
private val TerminalScheme =
    darkColorScheme(
        primary = Color(0xFFFF2A2A),
        onPrimary = Color(0xFF0A0A0A),
        primaryContainer = Color(0xFF3A0A0A),
        onPrimaryContainer = Color(0xFFFF8A80),
        secondary = Color(0xFFB0B0B0),
        onSecondary = Color(0xFF0A0A0A),
        secondaryContainer = Color(0xFFEAEAEA),
        onSecondaryContainer = Color(0xFF0A0A0A),
        tertiary = Color(0xFF4AF626),
        onTertiary = Color(0xFF0A0A0A),
        tertiaryContainer = Color(0xFF13330C),
        onTertiaryContainer = Color(0xFF9DFA88),
        error = Color(0xFFFF2A2A),
        onError = Color(0xFF0A0A0A),
        errorContainer = Color(0xFF3A0A0A),
        onErrorContainer = Color(0xFFFF8A80),
        background = Color(0xFF0A0A0A),
        onBackground = Color(0xFFEAEAEA),
        surface = Color(0xFF111111),
        onSurface = Color(0xFFEAEAEA),
        surfaceVariant = Color(0xFF1A1A1A),
        onSurfaceVariant = Color(0xFF8A8A8A),
        surfaceTint = Color(0xFFEAEAEA),
        outline = Color(0xFF3A3A3A),
        outlineVariant = Color(0xFF262626),
        surfaceContainerLowest = Color(0xFF050505),
        surfaceContainerLow = Color(0xFF0E0E0E),
        surfaceContainer = Color(0xFF111111),
        surfaceContainerHigh = Color(0xFF1A1A1A),
        surfaceContainerHighest = Color(0xFF242424),
        inverseSurface = Color(0xFFEAEAEA),
        inverseOnSurface = Color(0xFF0A0A0A),
        inversePrimary = Color(0xFFB00000),
    )
