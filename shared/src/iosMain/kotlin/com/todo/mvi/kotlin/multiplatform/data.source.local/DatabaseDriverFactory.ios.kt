package com.todo.mvi.kotlin.multiplatform.data.source.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.todo.mvi.kotlin.multiplatform.TasksDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(TasksDatabase.Schema, "Tasks.db")
    }
}