package io.github.nodyssey.ui.settings.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.nodyssey.data.settings.UiStyle
import io.github.nodyssey.ui.resources.Res
import io.github.nodyssey.ui.resources.settings_ui_style
import io.github.nodyssey.ui.resources.settings_ui_style_hint
import io.github.nodyssey.ui.resources.ui_style_editorial
import io.github.nodyssey.ui.resources.ui_style_editorial_desc
import io.github.nodyssey.ui.resources.ui_style_industrial
import io.github.nodyssey.ui.resources.ui_style_industrial_desc
import io.github.nodyssey.ui.resources.ui_style_layered
import io.github.nodyssey.ui.resources.ui_style_layered_desc
import io.github.nodyssey.ui.resources.ui_style_preview_meta
import io.github.nodyssey.ui.resources.ui_style_preview_title
import io.github.nodyssey.ui.resources.ui_style_terminal
import io.github.nodyssey.ui.resources.ui_style_terminal_desc
import io.github.plaza.designsys.component.LayerCard
import io.github.plaza.designsys.component.groupedRowTitleStyle
import io.github.plaza.designsys.theme.LocalPlazaDarkTheme
import io.github.plaza.designsys.theme.LocalPlazaLayers
import io.github.plaza.designsys.theme.PlazaTheme
import io.github.plaza.designsys.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * 界面风格 — four tiles, each drawn *in* the style it names.
 *
 * A description cannot show a typeface or a corner, so every tile is a small piece of the app
 * rendered under its own [PlazaTheme]: the page colour, a card with that style's border and radius,
 * a title in its heading face and a meta line in its label face. What the reader picks is what they
 * saw. The tile's name and its one-line summary sit outside, in the theme in force, so the four read
 * as one list.
 */
@Composable
internal fun UiStyleBlock(
    selected: UiStyle,
    onSelect: (UiStyle) -> Unit,
) {
    Column(
        modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = 14.dp, bottom = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(stringResource(Res.string.settings_ui_style), style = groupedRowTitleStyle())
        Text(
            stringResource(Res.string.settings_ui_style_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        UiStyle.entries.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                pair.forEach { style ->
                    UiStyleTile(
                        style = style,
                        selected = style == selected,
                        onClick = { onSelect(style) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun UiStyleTile(
    style: UiStyle,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outline = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    val shape = MaterialTheme.shapes.medium
    Column(
        modifier =
        modifier
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, outline, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(Spacing.xs),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        StylePreview(style)
        Text(
            stringResource(style.label),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = Spacing.xs),
        )
        Text(
            stringResource(style.summary),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = Spacing.xs, end = Spacing.xs, bottom = Spacing.xs),
        )
    }
}

/** The style itself, small: its page, one card on it, a title and a meta line. */
@Composable
private fun StylePreview(style: UiStyle) {
    val plaza = style.toPlaza()
    PlazaTheme(darkTheme = plaza.forcedDarkTheme ?: LocalPlazaDarkTheme.current, uiStyle = plaza) {
        Box(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(LocalPlazaLayers.current.page)
                .padding(Spacing.sm),
        ) {
            LayerCard(contentPadding = PaddingValues(Spacing.sm)) {
                Text(
                    stringResource(Res.string.ui_style_preview_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(Res.string.ui_style_preview_meta),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Box(
                    Modifier
                        .padding(top = Spacing.xs)
                        .width(44.dp)
                        .height(16.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

private val UiStyle.label: StringResource
    get() =
        when (this) {
            UiStyle.LAYERED -> Res.string.ui_style_layered
            UiStyle.EDITORIAL -> Res.string.ui_style_editorial
            UiStyle.INDUSTRIAL -> Res.string.ui_style_industrial
            UiStyle.TERMINAL -> Res.string.ui_style_terminal
        }

private val UiStyle.summary: StringResource
    get() =
        when (this) {
            UiStyle.LAYERED -> Res.string.ui_style_layered_desc
            UiStyle.EDITORIAL -> Res.string.ui_style_editorial_desc
            UiStyle.INDUSTRIAL -> Res.string.ui_style_industrial_desc
            UiStyle.TERMINAL -> Res.string.ui_style_terminal_desc
        }
