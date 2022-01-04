package com.todo.mvi.kotlin.multiplatform.android.statistics

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviAction

sealed class StatisticsAction : MviAction {
  object LoadStatisticsAction : StatisticsAction()
}
