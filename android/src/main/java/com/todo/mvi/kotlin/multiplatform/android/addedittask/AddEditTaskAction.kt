package com.todo.mvi.kotlin.multiplatform.android.addedittask

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviAction

sealed class AddEditTaskAction : MviAction {
  data class PopulateTaskAction(val taskId: String) : AddEditTaskAction()

  data class CreateTaskAction(val title: String, val description: String) : AddEditTaskAction()

  data class UpdateTaskAction(
      val taskId: String,
      val title: String,
      val description: String
  ) : AddEditTaskAction()

  object SkipMe : AddEditTaskAction()
}
