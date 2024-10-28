package com.todo.mvi.kotlin.multiplatform.data.source.local

import co.touchlab.stately.freeze
import com.badoo.reaktive.completable.CompletableWrapper
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.wrap
import com.badoo.reaktive.single.SingleWrapper
import com.badoo.reaktive.single.singleOf
import com.badoo.reaktive.single.wrap
import com.todo.mvi.kotlin.multiplatform.TasksDatabase
import com.todo.mvi.kotlin.multiplatform.data.source.TasksDataSource
import com.todo.mvi.kotlin.multiplatform.data.Task

class TasksLocalDataSource(databaseDriverFactory : DatabaseDriverFactory): TasksDataSource {

    private val database by lazy {
        TasksDatabase(databaseDriverFactory.createDriver())
    }

    override fun getTasks(): SingleWrapper<List<Task>> {
        return singleOf(database.tasksDatabaseQueries.selectAll().executeAsList()).wrap()
    }

    override fun getTask(taskId: String): SingleWrapper<Task> {
        return singleOf(database.tasksDatabaseQueries.select(taskId).executeAsOne()).wrap()
    }

    override fun saveTask(task: Task): CompletableWrapper {
        database.tasksDatabaseQueries.insert(task.id, task.title, task.description, task.completed)
        return completableOfEmpty().wrap()
    }

    override fun completeTask(task: Task): CompletableWrapper {
        database.tasksDatabaseQueries.completeTask(task.id)
        return completableOfEmpty().wrap()
    }

    override fun completeTask(taskId: String): CompletableWrapper {
        database.tasksDatabaseQueries.completeTask(taskId)
        return completableOfEmpty().wrap()
    }

    override fun activateTask(task: Task): CompletableWrapper {
        database.tasksDatabaseQueries.activateTask(task.id)
        return completableOfEmpty().wrap()
    }

    override fun activateTask(taskId: String): CompletableWrapper {
        database.tasksDatabaseQueries.activateTask(taskId)
        return completableOfEmpty().wrap()
    }

    override fun clearCompletedTasks(): CompletableWrapper {
        database.tasksDatabaseQueries.clearCompletedTasks()
        return completableOfEmpty().wrap()
    }

    override fun refreshTasks() {
        // Not required because the [TasksRepository] handles the logic of refreshing the
        // tasks from all the available data sources.
    }

    override fun deleteAllTasks() {
        database.tasksDatabaseQueries.clear()
    }

    override fun deleteTask(taskId: String): CompletableWrapper {
        database.tasksDatabaseQueries.delete(taskId)
        return completableOfEmpty().wrap()
    }
}