package com.todo.mvi.kotlin.multiplatform.data.source.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.todo.mvi.kotlin.multiplatform.TasksDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(TasksDatabase.Schema, context, "Tasks.db")
    }
}