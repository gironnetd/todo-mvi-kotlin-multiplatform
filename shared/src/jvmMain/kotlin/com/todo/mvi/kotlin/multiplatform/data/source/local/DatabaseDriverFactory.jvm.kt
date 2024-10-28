package com.todo.mvi.kotlin.multiplatform.data.source.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.todo.mvi.kotlin.multiplatform.TasksDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        TasksDatabase.Schema.create(driver)
        return driver
    }
}