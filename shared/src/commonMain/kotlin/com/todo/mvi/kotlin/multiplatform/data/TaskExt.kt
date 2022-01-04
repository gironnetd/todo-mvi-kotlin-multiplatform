package com.todo.mvi.kotlin.multiplatform.data

import com.todo.mvi.kotlin.multiplatform.util.isNotNullNorEmpty
import comtodomvikotlinmultiplatform.Task

val Task.titleForList : String
        get() {
            return if (title.isNotNullNorEmpty()) {
                title!!
            } else {
                description ?: ""
            }
        }

val Task.active
    get() = !completed

val Task.empty
    get() = title.isNullOrEmpty() && description.isNullOrEmpty()
