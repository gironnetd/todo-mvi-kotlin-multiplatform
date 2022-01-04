package com.todo.mvi.kotlin.multiplatform.android.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.todo.mvi.kotlin.multiplatform.android.Injection
import com.todo.mvi.kotlin.multiplatform.android.addedittask.AddEditTaskActionProcessorHolder
import com.todo.mvi.kotlin.multiplatform.android.addedittask.AddEditTaskViewModel
import com.todo.mvi.kotlin.multiplatform.android.statistics.StatisticsActionProcessorHolder
import com.todo.mvi.kotlin.multiplatform.android.statistics.StatisticsViewModel
import com.todo.mvi.kotlin.multiplatform.android.taskdetail.TaskDetailActionProcessorHolder
import com.todo.mvi.kotlin.multiplatform.android.taskdetail.TaskDetailViewModel
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksActionProcessorHolder
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksViewModel
import com.todo.mvi.kotlin.multiplatform.util.SingletonHolderSingleArg

class ToDoViewModelFactory private constructor(
    private val applicationContext: Context
) : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass == StatisticsViewModel::class.java) {
      return StatisticsViewModel(
          StatisticsActionProcessorHolder(
              Injection.provideTasksRepository(applicationContext),
              Injection.provideSchedulerProvider())) as T
    }
    if (modelClass == TasksViewModel::class.java) {
      return TasksViewModel(
          TasksActionProcessorHolder(
              Injection.provideTasksRepository(applicationContext),
              Injection.provideSchedulerProvider())) as T
    }
    if (modelClass == AddEditTaskViewModel::class.java) {
      return AddEditTaskViewModel(
          AddEditTaskActionProcessorHolder(
              Injection.provideTasksRepository(applicationContext),
              Injection.provideSchedulerProvider())) as T
    }
    if (modelClass == TaskDetailViewModel::class.java) {
      return TaskDetailViewModel(
          TaskDetailActionProcessorHolder(
              Injection.provideTasksRepository(applicationContext),
              Injection.provideSchedulerProvider())) as T
    }
    throw IllegalArgumentException("unknown model class " + modelClass)
  }

  companion object : SingletonHolderSingleArg<ToDoViewModelFactory, Context>(::ToDoViewModelFactory)
}
