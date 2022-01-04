package com.todo.mvi.kotlin.multiplatform.util.schedulers

import com.badoo.reaktive.scheduler.Scheduler

/**
 * Allow providing different types of [Scheduler]s.
 */
interface BaseSchedulerProvider {
  fun computation(): Scheduler

  fun io(): Scheduler

  fun ui(): Scheduler
}
