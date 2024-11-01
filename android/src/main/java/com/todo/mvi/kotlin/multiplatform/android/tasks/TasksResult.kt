package com.todo.mvi.kotlin.multiplatform.android.tasks

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviResult
import com.todo.mvi.kotlin.multiplatform.data.Task

sealed class TasksResult : MviResult {
  sealed class LoadTasksResult : TasksResult() {
    data class Success(val tasks: List<Task>, val filterType: TasksFilterType?) : LoadTasksResult()
    data class Failure(val error: Throwable) : LoadTasksResult()
    object InFlight : LoadTasksResult()
  }

  sealed class ActivateTaskResult : TasksResult() {
    data class Success(val tasks: List<Task>) : ActivateTaskResult()
    data class Failure(val error: Throwable) : ActivateTaskResult()
    object InFlight : ActivateTaskResult()
    object HideUiNotification : ActivateTaskResult()
  }

  sealed class CompleteTaskResult : TasksResult() {
    data class Success(val tasks: List<Task>) : CompleteTaskResult()
    data class Failure(val error: Throwable) : CompleteTaskResult()
    object InFlight : CompleteTaskResult()
    object HideUiNotification : CompleteTaskResult()
  }

  sealed class ClearCompletedTasksResult : TasksResult() {
    data class Success(val tasks: List<Task>) : ClearCompletedTasksResult()
    data class Failure(val error: Throwable) : ClearCompletedTasksResult()
    object InFlight : ClearCompletedTasksResult()
    object HideUiNotification : ClearCompletedTasksResult()
  }
}
