package com.todo.mvi.kotlin.multiplatform.data.source

import com.badoo.reaktive.completable.CompletableWrapper
import com.badoo.reaktive.single.SingleWrapper
import com.todo.mvi.kotlin.multiplatform.TasksDatabase
import com.todo.mvi.kotlin.multiplatform.data.source.TasksDataSource
import com.todo.mvi.kotlin.multiplatform.data.Task

interface TasksDataSource {

    fun getTasks(forceUpdate: Boolean): SingleWrapper<List<Task>> {
        if (forceUpdate) refreshTasks()
        return getTasks()
    }

    fun getTasks(): SingleWrapper<List<Task>>

    fun getTask(taskId: String): SingleWrapper<Task>

    fun saveTask(task: Task): CompletableWrapper

    fun completeTask(task: Task): CompletableWrapper

    fun completeTask(taskId: String): CompletableWrapper

    fun activateTask(task: Task): CompletableWrapper

    fun activateTask(taskId: String): CompletableWrapper

    fun clearCompletedTasks(): CompletableWrapper

    fun refreshTasks()

    fun deleteAllTasks()

    fun deleteTask(taskId: String): CompletableWrapper
}