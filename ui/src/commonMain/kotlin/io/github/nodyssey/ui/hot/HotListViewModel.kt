package io.github.nodyssey.ui.hot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import io.github.nodyssey.data.hot.HotList
import io.github.nodyssey.data.hot.HotListError
import io.github.nodyssey.data.hot.HotListException
import io.github.nodyssey.data.hot.HotListPeriod
import io.github.nodyssey.data.hot.HotListSource
import io.github.nodyssey.di.AppContainer
import io.github.plaza.core.AppClock
import io.github.plaza.core.runCatchingExceptCancellation
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HotListUiState(
    val period: HotListPeriod = HotListPeriod.Realtime,
    /** Every list fetched this visit, so switching back to a tab shows it at once. */
    val lists: Map<HotListPeriod, HotList> = emptyMap(),
    val isLoading: Boolean = false,
    /** Why [period]'s list could not be fetched, until the next attempt. */
    val error: HotListError? = null,
    /** The clock when the last list arrived; what "N 分钟前更新" is measured against. */
    val nowMillis: Long = 0L,
) {
    val current: HotList? get() = lists[period]
}

/**
 * 热榜 — 实时 / 日榜 / 周榜, fetched from [HotListSource].
 *
 * A tab is fetched the first time it is shown and then kept for the visit; 刷新 asks again. The
 * lists are rebuilt by the service every few minutes at most, so nothing here polls.
 */
class HotListViewModel(
    private val source: HotListSource,
    private val clock: AppClock = AppClock.System,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HotListUiState())
    val uiState: StateFlow<HotListUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(HotListPeriod.Realtime)
    }

    fun select(period: HotListPeriod) {
        _uiState.update { it.copy(period = period, error = null) }
        if (_uiState.value.lists[period] == null) load(period)
    }

    fun refresh() {
        load(_uiState.value.period)
    }

    private fun load(period: HotListPeriod) {
        loadJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadJob =
            viewModelScope.launch {
                runCatchingExceptCancellation { source.hotList(period) }
                    .onSuccess { list ->
                        _uiState.update {
                            it.copy(lists = it.lists + (period to list), isLoading = false, nowMillis = clock.nowMillis())
                        }
                    }.onFailure { failure ->
                        val error = (failure as? HotListException)?.error ?: HotListError.Unparsable
                        _uiState.update { it.copy(isLoading = false, error = error.takeIf { _ -> it.period == period }) }
                    }
            }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { HotListViewModel(container.hotListSource, container.clock) }
            }
    }
}
