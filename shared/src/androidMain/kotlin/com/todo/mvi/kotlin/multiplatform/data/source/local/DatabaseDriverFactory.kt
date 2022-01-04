package com.todo.mvi.kotlin.multiplatform.data.source.local

import android.content.Context
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import com.todo.mvi.kotlin.multiplatform.TasksDatabase

actual class DatabaseDriverFactory(val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(TasksDatabase.Schema, context, "Tasks.db")
    }
}
