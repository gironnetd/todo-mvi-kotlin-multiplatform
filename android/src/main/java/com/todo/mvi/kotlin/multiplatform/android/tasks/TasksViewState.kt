package com.todo.mvi.kotlin.multiplatform.android.tasks

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviViewState
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksFilterType.ALL_TASKS
import com.todo.mvi.kotlin.multiplatform.data.Task

data class TasksViewState(
    val isLoading: Boolean,
    val tasksFilterType: TasksFilterType,
    val tasks: List<Task>,
    val error: Throwable?,
    val uiNotification: UiNotification?
) : MviViewState {
  enum class UiNotification {
    TASK_COMPLETE,
    TASK_ACTIVATED,
    COMPLETE_TASKS_CLEARED
  }

  companion object {
    fun idle(): TasksViewState {
      return TasksViewState(
          isLoading = false,
          tasksFilterType = ALL_TASKS,
          tasks = emptyList(),
          error = null,
          uiNotification = null
      )
    }
  }
}
