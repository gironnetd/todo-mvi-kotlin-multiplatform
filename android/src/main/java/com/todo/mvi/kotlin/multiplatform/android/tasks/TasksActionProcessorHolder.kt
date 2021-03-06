package com.todo.mvi.kotlin.multiplatform.android.tasks

import com.badoo.reaktive.rxjavainterop.asRxJava2Completable
import com.badoo.reaktive.rxjavainterop.asRxJava2Single
import com.todo.mvi.kotlin.multiplatform.data.source.TasksRepository
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviAction
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviResult
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviViewModel
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksAction.ActivateTaskAction
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksAction.ClearCompletedTasksAction
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksAction.CompleteTaskAction
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksAction.LoadTasksAction
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksResult.ActivateTaskResult
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksResult.ClearCompletedTasksResult
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksResult.CompleteTaskResult
import com.todo.mvi.kotlin.multiplatform.android.tasks.TasksResult.LoadTasksResult
import com.todo.mvi.kotlin.multiplatform.android.util.pairWithDelay
import com.todo.mvi.kotlin.multiplatform.android.util.schedulers.BaseSchedulerProvider
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.Function

/**
 * Contains and executes the business logic for all emitted [MviAction]
 * and returns one unique [Observable] of [MviResult].
 *
 *
 * This could have been included inside the [MviViewModel]
 * but was separated to ease maintenance, as the [MviViewModel] was getting too big.
 */
class TasksActionProcessorHolder(
    private val tasksRepository: TasksRepository,
    private val schedulerProvider: BaseSchedulerProvider
) {

  private val loadTasksProcessor =
      ObservableTransformer<LoadTasksAction, LoadTasksResult> { actions ->
        actions.flatMap { action ->
          tasksRepository.getTasks(action.forceUpdate).asRxJava2Single()
              // Transform the Single to an Observable to allow emission of multiple
              // events down the stream (e.g. the InFlight event)
              .toObservable()
              // Wrap returned data into an immutable object
              .map { tasks -> LoadTasksResult.Success(tasks, action.filterType) }
              .cast(LoadTasksResult::class.java)
              // Wrap any error into an immutable object and pass it down the stream
              // without crashing.
              // Because errors are data and hence, should just be part of the stream.
              .onErrorReturn(LoadTasksResult::Failure)
              .subscribeOn(schedulerProvider.io())
              .observeOn(schedulerProvider.ui())
              // Emit an InFlight event to notify the subscribers (e.g. the UI) we are
              // doing work and waiting on a response.
              // We emit it after observing on the UI thread to allow the event to be emitted
              // on the current frame and avoid jank.
              .startWith(TasksResult.LoadTasksResult.InFlight)
        }
      }

  private val activateTaskProcessor =
      ObservableTransformer<ActivateTaskAction, ActivateTaskResult> { actions ->
        actions.flatMap { action ->
          tasksRepository.activateTask(action.task).asRxJava2Completable()
              .andThen(tasksRepository.getTasks().asRxJava2Single())
              // Transform the Single to an Observable to allow emission of multiple
              // events down the stream (e.g. the InFlight event)
              .toObservable()
              .flatMap { tasks ->
                // Emit two events to allow the UI notification to be hidden after
                // some delay
                pairWithDelay(
                    TasksResult.ActivateTaskResult.Success(tasks),
                    TasksResult.ActivateTaskResult.HideUiNotification)
              }
              // Wrap any error into an immutable object and pass it down the stream
              // without crashing.
              // Because errors are data and hence, should just be part of the stream.
              .onErrorReturn(ActivateTaskResult::Failure)
              .subscribeOn(schedulerProvider.io())
              .observeOn(schedulerProvider.ui())
              // Emit an InFlight event to notify the subscribers (e.g. the UI) we are
              // doing work and waiting on a response.
              // We emit it after observing on the UI thread to allow the event to be emitted
              // on the current frame and avoid jank.
              .startWith(TasksResult.ActivateTaskResult.InFlight)
        }
      }

  private val completeTaskProcessor =
      ObservableTransformer<CompleteTaskAction, CompleteTaskResult> { actions ->
        actions.flatMap { action ->
          tasksRepository.completeTask(action.task).asRxJava2Completable()
              .andThen(tasksRepository.getTasks().asRxJava2Single())
              // Transform the Single to an Observable to allow emission of multiple
              // events down the stream (e.g. the InFlight event)
              .toObservable()
              .flatMap { tasks ->
                // Emit two events to allow the UI notification to be hidden after
                // some delay
                pairWithDelay(
                    TasksResult.CompleteTaskResult.Success(tasks),
                    TasksResult.CompleteTaskResult.HideUiNotification)
              }
              // Wrap any error into an immutable object and pass it down the stream
              // without crashing.
              // Because errors are data and hence, should just be part of the stream.
              .onErrorReturn(CompleteTaskResult::Failure)
              .subscribeOn(schedulerProvider.io())
              .observeOn(schedulerProvider.ui())
              // Emit an InFlight event to notify the subscribers (e.g. the UI) we are
              // doing work and waiting on a response.
              // We emit it after observing on the UI thread to allow the event to be emitted
              // on the current frame and avoid jank.
              .startWith(TasksResult.CompleteTaskResult.InFlight)
        }
      }

  private val clearCompletedTasksProcessor =
      ObservableTransformer<ClearCompletedTasksAction, ClearCompletedTasksResult> { actions ->
        actions.flatMap {
          tasksRepository.clearCompletedTasks().asRxJava2Completable()
              .andThen(tasksRepository.getTasks().asRxJava2Single())
              // Transform the Single to an Observable to allow emission of multiple
              // events down the stream (e.g. the InFlight event)
              .toObservable()
              .flatMap { tasks ->
                // Emit two events to allow the UI notification to be hidden after
                // some delay
                pairWithDelay(
                    ClearCompletedTasksResult.Success(tasks),
                    ClearCompletedTasksResult.HideUiNotification)
              }
              // Wrap any error into an immutable object and pass it down the stream
              // without crashing.
              // Because errors are data and hence, should just be part of the stream.
              .onErrorReturn(ClearCompletedTasksResult::Failure)
              .subscribeOn(schedulerProvider.io())
              .observeOn(schedulerProvider.ui())
              // Emit an InFlight event to notify the subscribers (e.g. the UI) we are
              // doing work and waiting on a response.
              // We emit it after observing on the UI thread to allow the event to be emitted
              // on the current frame and avoid jank.
              .startWith(TasksResult.ClearCompletedTasksResult.InFlight)
        }
      }

  /**
   * Splits the [Observable] to match each type of [MviAction] to
   * its corresponding business logic processor. Each processor takes a defined [MviAction],
   * returns a defined [MviResult]
   * The global actionProcessor then merges all [Observable] back to
   * one unique [Observable].
   *
   *
   * The splitting is done using [Observable.publish] which allows almost anything
   * on the passed [Observable] as long as one and only one [Observable] is returned.
   *
   *
   * An security layer is also added for unhandled [MviAction] to allow early crash
   * at runtime to easy the maintenance.
   */
  internal var actionProcessor =
      ObservableTransformer<TasksAction, TasksResult> { actions ->
        actions.publish { shared ->
          Observable.merge(
              // Match LoadTasksAction to loadTasksProcessor
              shared.ofType(TasksAction.LoadTasksAction::class.java).compose(loadTasksProcessor),
              // Match ActivateTaskAction to populateTaskProcessor
              shared.ofType(TasksAction.ActivateTaskAction::class.java)
                  .compose(activateTaskProcessor),
              // Match CompleteTaskAction to completeTaskProcessor
              shared.ofType(TasksAction.CompleteTaskAction::class.java)
                  .compose(completeTaskProcessor),
              // Match ClearCompletedTasksAction to clearCompletedTasksProcessor
              shared.ofType(TasksAction.ClearCompletedTasksAction::class.java)
                  .compose(clearCompletedTasksProcessor))
              .mergeWith(
                  // Error for not implemented actions
                  shared.filter { v ->
                    v !is TasksAction.LoadTasksAction
                        && v !is TasksAction.ActivateTaskAction
                        && v !is TasksAction.CompleteTaskAction
                        && v !is TasksAction.ClearCompletedTasksAction
                  }.flatMap { w ->
                    Observable.error<TasksResult>(
                        IllegalArgumentException("Unknown Action type: $w"))
                  }
              )
        }
      }
}
