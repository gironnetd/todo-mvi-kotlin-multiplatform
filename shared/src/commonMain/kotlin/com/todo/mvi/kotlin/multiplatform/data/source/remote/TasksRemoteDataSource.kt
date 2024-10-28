package com.todo.mvi.kotlin.multiplatform.data.source.remote

import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.freeze
import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.CompletableWrapper
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.wrap
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.SingleWrapper
import com.badoo.reaktive.single.singleOf
import com.badoo.reaktive.single.wrap
import com.todo.mvi.kotlin.multiplatform.TasksDatabase
import com.todo.mvi.kotlin.multiplatform.data.source.TasksDataSource
import com.todo.mvi.kotlin.multiplatform.data.Task
import kotlin.native.concurrent.ThreadLocal

/**
 * Implementation of a remote data source with static access to the data for easy testing.
 */
class TasksRemoteDataSource : TasksDataSource {

  private val tasksServiceData = AtomicReference(linkedMapOf<String, Task>().freeze())

  override fun getTasks(): SingleWrapper<List<Task>> {
    return singleOf(tasksServiceData.value.values.toList()).wrap()
  }

  override fun getTask(taskId: String): SingleWrapper<Task> {
    return singleOf(tasksServiceData.value[taskId]!!).wrap()
  }

  override fun saveTask(task: Task): CompletableWrapper {
    addTask(task)
    return completableOfEmpty().wrap()
  }

  override fun completeTask(task: Task): CompletableWrapper {
    val completedTask = Task(task.id, task.title, task.description, true)
    addTask(completedTask)
    return completableOfEmpty().wrap()
  }

  override fun completeTask(taskId: String): CompletableWrapper {
    val task = tasksServiceData.value[taskId]!!
    val completedTask = Task(task.id, task.title, task.description, true)
    addTask(completedTask)
    return completableOfEmpty().wrap()
  }

  override fun activateTask(task: Task): CompletableWrapper {
    val activeTask = Task(task.id, task.title, task.description, false)
    addTask(activeTask)
    return completableOfEmpty().wrap()
  }

  override fun activateTask(taskId: String): CompletableWrapper {
    val task = tasksServiceData.value[taskId]!!
    val activeTask = Task(task.id, task.title, task.description, false)
    addTask(activeTask)
    return completableOfEmpty().wrap()
  }

  override fun clearCompletedTasks(): CompletableWrapper {
    val newTasks = linkedMapOf<String, Task>()
    newTasks.putAll(tasksServiceData.value)

    newTasks.forEach { (taskId, task) -> if(task.completed) { newTasks.remove(taskId)} }

    tasksServiceData.value = newTasks.freeze()
    return completableOfEmpty().wrap()
  }

  override fun refreshTasks() {
    // Not required because the {@link TasksRepository} handles the logic of refreshing the
    // tasks from all the available data sources.
  }

  override fun deleteTask(taskId: String): CompletableWrapper {

    val tasks = tasksServiceData.value
    val newTasks = linkedMapOf<String, Task>()
    newTasks.putAll(tasks)
    if(newTasks.containsKey(taskId)) {
      newTasks.remove(taskId)
    }
    tasksServiceData.value = newTasks.freeze()
    return completableOfEmpty().wrap()
  }

  override fun deleteAllTasks() {
    tasksServiceData.value = linkedMapOf<String, Task>().freeze()
  }

  private fun addTask(task : Task) {
    val tasks = tasksServiceData.value
    val newTasks = linkedMapOf<String, Task>()
    newTasks.putAll(tasks)
    if(!newTasks.containsValue(task)) {
      newTasks[task.id] = task
    }
    tasksServiceData.value = newTasks.freeze()
  }
}
