package com.todo.mvi.kotlin.multiplatform.data.source

import co.touchlab.stately.concurrency.AtomicBoolean
import co.touchlab.stately.concurrency.AtomicReference
import co.touchlab.stately.concurrency.value
import co.touchlab.stately.ensureNeverFrozen
import co.touchlab.stately.freeze
import com.badoo.reaktive.completable.CompletableWrapper
import com.badoo.reaktive.completable.completableOfEmpty
import com.badoo.reaktive.completable.threadLocal
import com.badoo.reaktive.completable.wrap
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.single.*
import com.todo.mvi.kotlin.multiplatform.data.source.local.DatabaseDriverFactory
import com.todo.mvi.kotlin.multiplatform.data.source.local.TasksLocalDataSource
import com.todo.mvi.kotlin.multiplatform.util.SingletonHolderDoubleArg
import comtodomvikotlinmultiplatform.Task
import kotlin.native.concurrent.ThreadLocal


/**
 * Concrete implementation to load tasks from the data sources into a cache.
 *
 *
 * For simplicity, this implements a dumb synchronisation between locally persisted data and data
 * obtained from the server, by using the remote data source only if the local database doesn't
 * exist or is empty.
 *
 * The class is open to allow mocking.
 */

class TasksRepository(
    private val tasksRemoteDataSource: TasksDataSource,
    private val tasksLocalDataSource: TasksDataSource
) : TasksDataSource {

    /**
     * This variable has package local visibility so it can be accessed from tests.
     */

    private var cachedTasks: AtomicReference<MutableMap<String, Task>?>? = null

    /**
     * Marks the cache as invalid, to force an update the next time data is requested. This variable
     * has package local visibility so it can be accessed from tests.
     */
    private var cacheIsDirty = AtomicBoolean(false)

    private fun getAndCacheLocalTasks(): Single<List<Task>> {
        return tasksLocalDataSource.getTasks()
            .flatMapIterable { tasks ->
                tasks.forEach { task -> addTask(task) }
                tasks
            }.toList()
    }

    private fun getAndSaveRemoteTasks(): Single<List<Task>> {
        return tasksRemoteDataSource.getTasks()
            .flatMapIterable { tasks ->
                tasks.forEach { task ->
                    tasksLocalDataSource.saveTask(task)
                    addTask(task)
                }
                tasks
            }.toList()
            .doOnBeforeSuccess { cacheIsDirty.value = false }
    }

    /**
     * Gets tasks from cache, local data source (SQLite) or remote data source, whichever is
     * available first.
     */
    override fun getTasks(): SingleWrapper<List<Task>> {
        // Respond immediately with cache if available and not dirty
        if (cachedTasks != null && !cacheIsDirty.value) {
            return singleOf(cachedTasks!!.value!!.values.toList()).wrap().freeze()
        } else if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }

        val remoteTasks = getAndSaveRemoteTasks()

        return if (cacheIsDirty.value) {
            remoteTasks.wrap().freeze()
        } else {
            // Query the local storage if available. If not, query the network.
            val localTasks = getAndCacheLocalTasks()

            concat(localTasks, remoteTasks)
                .filter { tasks -> tasks.isNotEmpty() }
                .firstOrError()
                .wrap()
                .freeze()
        }
    }

    override fun saveTask(task: Task): CompletableWrapper {
        tasksRemoteDataSource.saveTask(task)//.threadLocal()//.freeze()
        tasksLocalDataSource.saveTask(task)//.threadLocal()//.freeze()

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }
        addTask(task)
        return completableOfEmpty().wrap().freeze()
    }

    override fun completeTask(task: Task): CompletableWrapper {
        tasksRemoteDataSource.completeTask(task)
        tasksLocalDataSource.completeTask(task)

        val completedTask =
            Task(id = task.id, title = task.title, description = task.description,  completed = true)

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }
        addTask(completedTask)
        return completableOfEmpty().wrap().freeze()
    }

    override fun completeTask(taskId: String): CompletableWrapper {
        val taskWithId = getTaskWithId(taskId)
        return if (taskWithId != null) {
            completeTask(taskWithId)
        } else {
            completableOfEmpty().wrap().freeze()
        }
    }

    override fun activateTask(task: Task): CompletableWrapper {
        tasksRemoteDataSource.activateTask(task)
        tasksLocalDataSource.activateTask(task)

        val activeTask =
            Task(id = task.id,title = task.title, description = task.description, completed = false)

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }
        addTask(activeTask)
        return completableOfEmpty().wrap().freeze()
    }

    override fun activateTask(taskId: String): CompletableWrapper {
        val taskWithId = getTaskWithId(taskId)
        return if (taskWithId != null) {
            activateTask(taskWithId)
        } else {
            completableOfEmpty().wrap().freeze()
        }
    }

    override fun deleteTask(taskId: String): CompletableWrapper {
        tasksRemoteDataSource.deleteTask(checkNotNull(taskId))
        tasksLocalDataSource.deleteTask(checkNotNull(taskId))

        val newTasks = linkedMapOf<String, Task>()
        cachedTasks!!.value?.let { newTasks.putAll(it) }
        if(newTasks.containsKey(taskId)) {
            newTasks.remove(taskId)
        }
        cachedTasks!!.value = newTasks.freeze()

        return completableOfEmpty().wrap().freeze()
    }

    override fun clearCompletedTasks(): CompletableWrapper {
        tasksRemoteDataSource.clearCompletedTasks()
        tasksLocalDataSource.clearCompletedTasks()

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }

        val newTasks = linkedMapOf<String, Task>()
        cachedTasks!!.value?.let { tasks -> newTasks.putAll(tasks) }

        newTasks.forEach { (taskId, task) -> if(task.completed) { newTasks.remove(taskId)} }
        cachedTasks!!.value = newTasks.freeze()

        return completableOfEmpty().wrap().freeze()
    }

    /**
     * Gets tasks from local data source (sqlite) unless the table is new or empty. In that case it
     * uses the network data source. This is done to simplify the sample.
     */
    override fun getTask(taskId: String): SingleWrapper<Task> {
        val cachedTask = getTaskWithId(taskId)

        // Respond immediately with cache if available
        if (cachedTask != null) {
            return singleOf(cachedTask).wrap().freeze() //Single.just(cachedTask)
        }

        // LoadAction from server/persisted if needed.

        // Do in memory cache update to keep the app UI up to date
        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }

        // Is the task in the local data source? If not, query the network.
        val localTask = getTaskWithIdFromLocalRepository(taskId)
        val remoteTask = tasksRemoteDataSource.getTask(taskId)
            .doOnBeforeSuccess { task ->
                tasksLocalDataSource.saveTask(task)
                addTask(task)
            }

        return concat(localTask, remoteTask).firstOrError().wrap().freeze()
    }

    override fun refreshTasks() {
        cacheIsDirty.value = true
    }

    override fun deleteAllTasks() {

        tasksRemoteDataSource.deleteAllTasks()
        tasksLocalDataSource.deleteAllTasks()

        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }
        cachedTasks!!.value = linkedMapOf<String, Task>().freeze()
    }

    private fun getTaskWithId(id: String): Task? = cachedTasks?.value?.get(id)

    fun getTaskWithIdFromLocalRepository(taskId: String): Single<Task> {
        return tasksLocalDataSource.getTask(taskId)
            .doOnAfterSuccess { task -> addTask(task) }
    }

    private fun addTask(task : Task) {
        if (cachedTasks == null) {
            cachedTasks = AtomicReference(linkedMapOf<String, Task>().freeze())
        }
        val tasks = cachedTasks!!.value
        val newTasks = linkedMapOf<String, Task>()
        if (tasks != null) {
            newTasks.putAll(tasks)
        }
        if(!newTasks.containsValue(task)) {
            newTasks[task.id] = task
        }
        cachedTasks!!.value = newTasks.freeze()
    }
}