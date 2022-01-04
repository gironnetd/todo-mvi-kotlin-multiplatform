package com.todo.mvi.kotlin.multiplatform.android.taskdetail

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviAction

sealed class TaskDetailAction : MviAction {
  data class PopulateTaskAction(val taskId: String) : TaskDetailAction()
  data class DeleteTaskAction(val taskId: String) : TaskDetailAction()
  data class ActivateTaskAction(val taskId: String) : TaskDetailAction()
  data class CompleteTaskAction(val taskId: String) : TaskDetailAction()
}