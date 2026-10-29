package io.github.plaza.designsys.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * The Material 3 shape scale, at the radii the design doc specifies.
 *
 * The four in-between slots — `largeIncreased` (20.dp) and friends — are left at their Material
 * defaults, which already match the design doc; read them as `MaterialTheme.shapes.largeIncreased`
 * rather than declaring a parallel constant.
 */
val PlazaShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )

/**
 * A 40–48dp control's corner: an icon tile, a text field, a keypad key. The artboards draw every one
 * of these at 14dp, and Material's scale steps from `medium` (12) straight to `large` (16).
 */
val ControlShape: Shape
    @Composable @ReadOnlyComposable
    get() = RoundedCornerShape(LocalPlazaUiStyle.current.controlCorner)

/**
 * The status-screen shape family.
 *
 * Every empty and error state puts its icon on a soft blob rather than a circle — that asymmetry is
 * most of what makes these screens read as *this* app. Each state gets its own corner rhythm so the
 * nine of them are distinguishable at a glance while staying one family.
 */
object StatusShapes {
    val Empty: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else EmptyBlob

    private val EmptyBlob = RoundedCornerShape(topStartPercent = 42, topEndPercent = 58, bottomEndPercent = 40, bottomStartPercent = 60)
    val NetworkError: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else NetworkErrorBlob

    private val NetworkErrorBlob = RoundedCornerShape(topStartPercent = 58, topEndPercent = 42, bottomEndPercent = 55, bottomStartPercent = 45)
    val Challenge: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else ChallengeBlob

    private val ChallengeBlob = RoundedCornerShape(topStartPercent = 38, topEndPercent = 62, bottomEndPercent = 45, bottomStartPercent = 55)
    val SignIn: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else SignInBlob

    private val SignInBlob = RoundedCornerShape(topStartPercent = 55, topEndPercent = 45, bottomEndPercent = 60, bottomStartPercent = 40)

    /** A wall the reader cannot clear today — a level floor, as opposed to [SignIn]'s one tap. */
    val Locked: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else LockedBlob

    private val LockedBlob = RoundedCornerShape(topStartPercent = 62, topEndPercent = 38, bottomEndPercent = 48, bottomStartPercent = 52)

    val Deleted: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else DeletedBlob

    private val DeletedBlob = RoundedCornerShape(topStartPercent = 45, topEndPercent = 55, bottomEndPercent = 42, bottomStartPercent = 58)
    val NoResults: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else NoResultsBlob

    private val NoResultsBlob = RoundedCornerShape(topStartPercent = 60, topEndPercent = 40, bottomEndPercent = 55, bottomStartPercent = 45)
    val Welcome: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else WelcomeBlob

    private val WelcomeBlob = RoundedCornerShape(topStartPercent = 50, topEndPercent = 50, bottomEndPercent = 58, bottomStartPercent = 42)
    val Loading: Shape
        @Composable @ReadOnlyComposable
        get() = if (LocalPlazaUiStyle.current.isSquare) RectangleShape else LoadingBlob

    private val LoadingBlob = RoundedCornerShape(percent = 50)
}
