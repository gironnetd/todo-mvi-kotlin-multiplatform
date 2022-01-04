package com.todo.mvi.kotlin.multiplatform.util

import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

/**
 * Used to allow Singleton with arguments in Kotlin while keeping the code efficient and safe.
 *
 * See https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
 */
open class SingletonHolderSingleArg<out T, in A>(creator: (A) -> T) {
  private var creator: ((A) -> T)? = creator
  @Volatile
  private var instance: T? = null

  @Synchronized
  fun getInstance(arg: A): T {
    val i = instance
    if (i != null) {
      return i
    }

    //return synchronized(this) {
    val i2 = instance
    return if (i2 != null) {
      i2
    } else {
      val created = creator!!(arg)
      instance = created
      created
    }
    //}
  }
}