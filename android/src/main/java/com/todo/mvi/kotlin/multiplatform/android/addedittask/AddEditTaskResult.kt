package com.todo.mvi.kotlin.multiplatform.android.addedittask

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviResult
import comtodomvikotlinmultiplatform.Task

sealed class AddEditTaskResult : MviResult {
  sealed class PopulateTaskResult : AddEditTaskResult() {
    data class Success(val task: Task) : PopulateTaskResult()
    data class Failure(val error: Throwable) : PopulateTaskResult()
    object InFlight : PopulateTaskResult()
  }

  sealed class CreateTaskResult : AddEditTaskResult() {
    object Success : CreateTaskResult()
    object Empty : CreateTaskResult()
  }

  object UpdateTaskResult : AddEditTaskResult()
}
