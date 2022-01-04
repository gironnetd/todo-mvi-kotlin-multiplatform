package com.todo.mvi.kotlin.multiplatform.android.taskdetail

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviIntent

sealed class TaskDetailIntent : MviIntent {
  data class InitialIntent(val taskId: String) : TaskDetailIntent()
  data class DeleteTask(val taskId: String) : TaskDetailIntent()
  data class ActivateTaskIntent(val taskId: String) : TaskDetailIntent()
  data class CompleteTaskIntent(val taskId: String) : TaskDetailIntent()
}
