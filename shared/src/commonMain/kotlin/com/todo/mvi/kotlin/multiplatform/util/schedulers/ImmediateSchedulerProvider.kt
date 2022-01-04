package com.todo.mvi.kotlin.multiplatform.util.schedulers

import com.badoo.reaktive.scheduler.Scheduler
import com.badoo.reaktive.scheduler.trampolineScheduler

/**
 * Implementation of the [BaseSchedulerProvider] making all [Scheduler]s immediate.
 */
class ImmediateSchedulerProvider : BaseSchedulerProvider {
  override fun computation(): Scheduler = trampolineScheduler

  override fun io(): Scheduler = trampolineScheduler

  override fun ui(): Scheduler = trampolineScheduler
}
