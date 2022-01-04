package com.todo.mvi.kotlin.multiplatform.util.schedulers

import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.scheduler.computationScheduler
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.scheduler.mainScheduler

/**
 * Provides different types of schedulers.
 */
object SchedulerProvider : BaseSchedulerProvider {
  override fun computation(): Scheduler = computationScheduler

  override fun io(): Scheduler = ioScheduler

  override fun ui(): Scheduler = mainScheduler
}
