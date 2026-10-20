package io.github.nodyssey.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nodyssey.ui.resources.Res
import io.github.nodyssey.ui.resources.author_joined_days_short
import io.github.nodyssey.ui.resources.author_risk
import io.github.nodyssey.ui.resources.author_risk_description
import io.github.plaza.designsys.component.BadgeChip
import io.github.plaza.designsys.component.BadgeTone
import io.github.plaza.designsys.component.PlazaIcons
import io.github.plaza.designsys.theme.LocalEinkMode
import io.github.plaza.designsys.theme.LocalPlazaExtraColors
import io.github.plaza.designsys.theme.LocalPlazaFontScale
import org.jetbrains.compose.resources.stringResource

/**
 * What `/api/account/getInfo` says about a floor's author, beside the name: `376天`, `Lv.5`, and 风险
 * for an account low or new enough to be worth a second look — the same three the site's own header
 * shows, in the same order, ahead of the role badges.
 *
 * The two stats are purple on purpose rather than a [BadgeTone]: they describe the account, not its
 * standing, and borrowing a role tone would make `Lv.5` read like 楼主 or 管理. 风险 is ink and an icon
 * with no fill, so it reads as a remark about the person rather than one more title. On an e-ink panel
 * colour means nothing, so all three fall back to the plain chips.
 *
 * Draws nothing for a level the page already printed as a badge, so a header never says `Lv.5` twice.
 */
@Composable
fun AuthorStatBadges(
    level: Int?,
    joinedDays: Int?,
    isRisky: Boolean,
    siteBadges: List<String> = emptyList(),
) {
    val showLevel = level != null && siteBadges.none { it.equals("Lv.$level", true) || it.equals("Lv$level", true) }
    val eink = LocalEinkMode.current
    if (joinedDays != null) {
        val text = stringResource(Res.string.author_joined_days_short, joinedDays)
        if (eink) BadgeChip(text, BadgeTone.Neutral) else StatPill(text, Modifier.background(daysContainer()))
    }
    if (showLevel) {
        val text = "Lv.$level"
        if (eink) BadgeChip(text, BadgeTone.Neutral) else StatPill(text, Modifier.background(LevelGradient), Color.White)
    }
    if (isRisky) {
        RiskMark(eink)
    }
}

@Composable
private fun StatPill(
    text: String,
    background: Modifier,
    color: Color = daysContent(),
) {
    Text(
        text = text,
        style = statTextStyle(),
        color = color,
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .then(background)
            .padding(horizontal = 5.dp, vertical = 1.dp),
    )
}

@Composable
private fun RiskMark(eink: Boolean) {
    val description = stringResource(Res.string.author_risk_description)
    val color = if (eink) MaterialTheme.colorScheme.onSurface else LocalPlazaExtraColors.current.warning
    Row(
        modifier = Modifier.semantics { contentDescription = description },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = PlazaIcons.Shield,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp * LocalPlazaFontScale.current),
        )
        Text(text = stringResource(Res.string.author_risk), style = statTextStyle(), color = color)
    }
}

/** [BadgeChip]'s type, so these sit on the name's line at the same height as the role badges. */
@Composable
private fun statTextStyle(): TextStyle {
    val scale = LocalPlazaFontScale.current
    return TextStyle(fontSize = 10.sp * scale, lineHeight = 14.sp * scale, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun isDark(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f

@Composable
private fun daysContainer(): Color = if (isDark()) Color(0xFF2E2A52) else Color(0xFFEDE9FE)

@Composable
private fun daysContent(): Color = if (isDark()) Color(0xFFA99CFF) else Color(0xFF5B3FD6)

private val LevelGradient = Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFC026D3)))
