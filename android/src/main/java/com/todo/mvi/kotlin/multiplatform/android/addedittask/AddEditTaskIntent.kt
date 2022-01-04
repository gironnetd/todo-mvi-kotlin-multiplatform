package com.todo.mvi.kotlin.multiplatform.android.addedittask

import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviIntent

sealed class AddEditTaskIntent : MviIntent {
  data class InitialIntent(val taskId: String?) : AddEditTaskIntent()

  data class SaveTask(
      val taskId: String?,
      val title: String,
      val description: String
  ) : AddEditTaskIntent()
}
