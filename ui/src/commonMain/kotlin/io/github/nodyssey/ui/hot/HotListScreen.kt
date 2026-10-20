package io.github.nodyssey.ui.hot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.nodyssey.data.hot.HotList
import io.github.nodyssey.data.hot.HotListEntry
import io.github.nodyssey.data.hot.HotListError
import io.github.nodyssey.data.hot.HotListPeriod
import io.github.nodyssey.ui.resources.Res
import io.github.nodyssey.ui.resources.action_back
import io.github.nodyssey.ui.resources.action_refresh
import io.github.nodyssey.ui.resources.action_retry
import io.github.nodyssey.ui.resources.hot_empty_title
import io.github.nodyssey.ui.resources.hot_error_network
import io.github.nodyssey.ui.resources.hot_error_title
import io.github.nodyssey.ui.resources.hot_error_unavailable
import io.github.nodyssey.ui.resources.hot_period_daily
import io.github.nodyssey.ui.resources.hot_period_realtime
import io.github.nodyssey.ui.resources.hot_period_weekly
import io.github.nodyssey.ui.resources.hot_row_meta
import io.github.nodyssey.ui.resources.hot_source
import io.github.nodyssey.ui.resources.hot_title
import io.github.nodyssey.ui.resources.hot_updated_hours
import io.github.nodyssey.ui.resources.hot_updated_just_now
import io.github.nodyssey.ui.resources.hot_updated_minutes
import io.github.plaza.designsys.component.ChoiceSegments
import io.github.plaza.designsys.component.LayerPageGutter
import io.github.plaza.designsys.component.LoadingState
import io.github.plaza.designsys.component.MetaText
import io.github.plaza.designsys.component.OneHandTopAppBar
import io.github.plaza.designsys.component.PlazaIcons
import io.github.plaza.designsys.component.StatusAction
import io.github.plaza.designsys.component.StatusView
import io.github.plaza.designsys.component.ThreadRowTitle
import io.github.plaza.designsys.component.groupSlice
import io.github.plaza.designsys.component.rememberOneHandAppBarState
import io.github.plaza.designsys.theme.LocalPlazaExtraColors
import io.github.plaza.designsys.theme.LocalPlazaLayers
import io.github.plaza.designsys.theme.Spacing
import io.github.plaza.designsys.theme.StatusShapes
import io.github.plaza.designsys.theme.readableWidth
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HotListRoute(
    viewModel: HotListViewModel,
    onBack: () -> Unit,
    onPostClick: (HotListEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HotListScreen(
        state = state,
        onBack = onBack,
        onSelect = viewModel::select,
        onRefresh = viewModel::refresh,
        onPostClick = onPostClick,
        modifier = modifier,
    )
}

/**
 * 热榜: the three lists behind one set of segments, each a single card of ranked rows in the grouped
 * style the rest of the app's lists use. The top three ranks are the primary colour and the rest
 * quiet, which is all the decoration a ranking needs.
 *
 * The footer says whose ranking this is. It is not NodeSeek's, and a reader should not take it for
 * the site's own judgement of what is popular.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotListScreen(
    state: HotListUiState,
    onBack: () -> Unit,
    onSelect: (HotListPeriod) -> Unit,
    onRefresh: () -> Unit,
    onPostClick: (HotListEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appBarState = rememberOneHandAppBarState()
    val list = state.current
    Scaffold(
        modifier = modifier.nestedScroll(appBarState.nestedScrollConnection),
        topBar = {
            OneHandTopAppBar(
                title = stringResource(Res.string.hot_title),
                state = appBarState,
                subtitle = list?.let { updatedLabel(it, state.nowMillis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.isLoading) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(Res.string.action_refresh))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().readableWidth()) {
            ChoiceSegments(
                labels = HotListPeriod.entries.map { stringResource(it.labelRes()) },
                selectedIndex = HotListPeriod.entries.indexOf(state.period),
                onSelect = { onSelect(HotListPeriod.entries[it]) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = LayerPageGutter, vertical = Spacing.sm),
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    list == null && state.error != null -> HotListErrorState(state.error, onRetry = onRefresh)

                    list == null -> LoadingState()

                    list.entries.isEmpty() ->
                        StatusView(
                            icon = PlazaIcons.LocalFireDepartment,
                            shape = StatusShapes.Empty,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            title = stringResource(Res.string.hot_empty_title),
                        )

                    else -> HotListRows(list.entries, onPostClick)
                }
            }
        }
    }
}

@Composable
private fun HotListRows(
    entries: List<HotListEntry>,
    onPostClick: (HotListEntry) -> Unit,
) {
    val layers = LocalPlazaLayers.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = LayerPageGutter, end = LayerPageGutter, bottom = Spacing.xxl),
    ) {
        itemsIndexed(entries, key = { _, entry -> entry.postId }) { index, entry ->
            HotListRow(
                rank = index + 1,
                entry = entry,
                onClick = { onPostClick(entry) },
                modifier = Modifier.groupSlice(layers, first = index == 0, last = index == entries.lastIndex),
            )
        }
        item(key = "source") {
            MetaText(
                text = stringResource(Res.string.hot_source),
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
            )
        }
    }
}

@Composable
private fun HotListRow(
    rank: Int,
    entry: HotListEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm + 2.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (rank <= TOP_RANKS) FontWeight.Bold else FontWeight.Medium,
            color = if (rank <= TOP_RANKS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 20.dp),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            ThreadRowTitle(AnnotatedString(entry.title))
            MetaText(stringResource(Res.string.hot_row_meta, entry.authorName, entry.views, entry.comments), singleLine = true)
        }
    }
}

@Composable
private fun HotListErrorState(
    error: HotListError,
    onRetry: () -> Unit,
) {
    val extra = LocalPlazaExtraColors.current
    StatusView(
        icon = PlazaIcons.LocalFireDepartment,
        shape = StatusShapes.NetworkError,
        containerColor = extra.warningContainer,
        iconColor = extra.onWarningContainer,
        title = stringResource(Res.string.hot_error_title),
        description =
        stringResource(
            if (error == HotListError.Network) Res.string.hot_error_network else Res.string.hot_error_unavailable,
        ),
        primaryAction = StatusAction(stringResource(Res.string.action_retry), onRetry),
    )
}

@Composable
private fun updatedLabel(
    list: HotList,
    nowMillis: Long,
): String? {
    val updated = list.updatedAtEpochSeconds ?: return null
    val minutes = ((nowMillis / 1000 - updated) / 60).coerceAtLeast(0)
    return when {
        minutes < 1 -> stringResource(Res.string.hot_updated_just_now)
        minutes < 60 -> stringResource(Res.string.hot_updated_minutes, minutes.toInt())
        else -> stringResource(Res.string.hot_updated_hours, (minutes / 60).toInt())
    }
}

private fun HotListPeriod.labelRes(): StringResource =
    when (this) {
        HotListPeriod.Realtime -> Res.string.hot_period_realtime
        HotListPeriod.Daily -> Res.string.hot_period_daily
        HotListPeriod.Weekly -> Res.string.hot_period_weekly
    }

/** Ranks drawn in the primary colour: the podium, as the userscript marks it. */
private const val TOP_RANKS = 3
