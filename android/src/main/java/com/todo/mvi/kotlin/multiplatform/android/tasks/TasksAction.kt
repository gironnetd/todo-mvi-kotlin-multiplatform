package com.todo.mvi.kotlin.multiplatform.android.tasks

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviAction
import com.todo.mvi.kotlin.multiplatform.data.Task

sealed class TasksAction : MviAction {
  data class LoadTasksAction(
      val forceUpdate: Boolean,
      val filterType: TasksFilterType?
  ) : TasksAction()

  data class ActivateTaskAction(val task: Task) : TasksAction()

  data class CompleteTaskAction(val task: Task) : TasksAction()

  object ClearCompletedTasksAction : TasksAction()
}
