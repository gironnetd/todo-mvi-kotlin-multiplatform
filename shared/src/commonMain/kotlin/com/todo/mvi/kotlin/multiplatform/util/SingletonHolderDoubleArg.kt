package com.todo.mvi.kotlin.multiplatform.util

import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile

/**
 * Used to allow Singleton with arguments in Kotlin while keeping the code efficient and safe.
 *
 * See https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
 */
open class SingletonHolderDoubleArg<out T, in A, in B>(creator: (A, B) -> T) {
  private var creator: ((A, B) -> T)? = creator
  @Volatile
  private var instance: T? = null

  @Synchronized
  fun getInstance(arg1: A, arg2: B): T {
    val i = instance
    if (i != null) {
      return i
    }

    //return synchronized(this) {
    val i2 = instance
    return if (i2 != null) {
      i2
    } else {
      val created = creator!!(arg1, arg2)
      instance = created
      created
    }
    //}
  }

  /**
   * Used to force [SingletonHolderDoubleArg.getInstance] to create a new instance next time it's called.
   * Used in tests.
   */
  fun clearInstance() {
    instance = null
  }
}