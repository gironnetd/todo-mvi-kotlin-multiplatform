package com.todo.mvi.kotlin.multiplatform.android.statistics

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviIntent

sealed class StatisticsIntent : MviIntent {
  object InitialIntent : StatisticsIntent()
}
