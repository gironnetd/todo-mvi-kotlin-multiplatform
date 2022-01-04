package com.todo.mvi.kotlin.multiplatform.android.statistics

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviResult

sealed class StatisticsResult : MviResult {
  sealed class LoadStatisticsResult : StatisticsResult() {
    data class Success(val activeCount: Int, val completedCount: Int) : LoadStatisticsResult()
    data class Failure(val error: Throwable) : LoadStatisticsResult()
    object InFlight : LoadStatisticsResult()
  }
}
