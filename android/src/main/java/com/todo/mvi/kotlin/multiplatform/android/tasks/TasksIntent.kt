package com.todo.mvi.kotlin.multiplatform.android.tasks

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviIntent
import comtodomvikotlinmultiplatform.Task

sealed class TasksIntent : MviIntent {
  object InitialIntent : TasksIntent()

  data class RefreshIntent(val forceUpdate: Boolean) : TasksIntent()

  data class ActivateTaskIntent(val task: Task) : TasksIntent()

  data class CompleteTaskIntent(val task: Task) : TasksIntent()

  object ClearCompletedTasksIntent : TasksIntent()

  data class ChangeFilterIntent(val filterType: TasksFilterType) : TasksIntent()
}
