package com.todo.mvi.kotlin.multiplatform.data.source.local

import com.squareup.sqldelight.db.SqlDriver
import com.todo.mvi.kotlin.multiplatform.data.source.TasksDataSource
import com.todo.mvi.kotlin.multiplatform.data.source.TasksRepository
import com.todo.mvi.kotlin.multiplatform.util.SingletonHolderDoubleArg
import com.todo.mvi.kotlin.multiplatform.util.SingletonHolderSingleArg

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
