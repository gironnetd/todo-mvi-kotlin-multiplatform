/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.todo.mvi.kotlin.multiplatform.android.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.todo.mvi.kotlin.multiplatform.android.R
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviIntent
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviView
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviViewModel
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviViewState
import com.todo.mvi.kotlin.multiplatform.android.util.ToDoViewModelFactory
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Main UI for the statistics screen.
 */
class StatisticsFragment : Fragment(), MviView<StatisticsIntent, StatisticsViewState> {
  private lateinit var statisticsTV: TextView
  // Used to manage the data flow lifecycle and avoid memory leak.
  private val disposables: CompositeDisposable = CompositeDisposable()
  private val viewModel: StatisticsViewModel by lazy(NONE) {
    ViewModelProviders
        .of(this, ToDoViewModelFactory.getInstance(context!!))
        .get(StatisticsViewModel::class.java)
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.statistics_frag, container, false)
        .also { statisticsTV = it.findViewById(R.id.statistics) }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    bind()
  }

  /**
   * Connect the [MviView] with the [MviViewModel].
   * We subscribe to the [MviViewModel] before passing it the [MviView]'s [MviIntent]s.
   * If we were to pass [MviIntent]s to the [MviViewModel] before listening to it,
   * emitted [MviViewState]s could be lost.
   */
  private fun bind() {
    // Subscribe to the ViewModel and call render for every emitted state
    disposables.add(
        viewModel.states().subscribe { this.render(it) }
    )
    // Pass the UI's intents to the ViewModel
    viewModel.processIntents(intents())
  }

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  override fun intents(): Observable<StatisticsIntent> = initialIntent()

  /**
   * The initial Intent the [MviView] emit to convey to the [MviViewModel]
   * that it is ready to receive data.
   * This initial Intent is also used to pass any parameters the [MviViewModel] might need
   * to render the initial [MviViewState] (e.g. the task id to load).
   */
  private fun initialIntent(): Observable<StatisticsIntent> {
    return Observable.just(StatisticsIntent.InitialIntent)
  }

  override fun render(state: StatisticsViewState) {
    if (state.isLoading) statisticsTV.text = getString(R.string.loading)
    if (state.error != null) {
      statisticsTV.text = resources.getString(R.string.statistics_error)
    }

    if (state.error == null && !state.isLoading) {
      showStatistics(state.activeCount, state.completedCount)
    }
  }

  private fun showStatistics(numberOfActiveTasks: Int, numberOfCompletedTasks: Int) {
    if (numberOfCompletedTasks == 0 && numberOfActiveTasks == 0) {
      statisticsTV.text = resources.getString(R.string.statistics_no_tasks)
    } else {
      val displayString = (resources.getString(R.string.statistics_active_tasks)
          + " "
          + numberOfActiveTasks
          + "\n"
          + resources.getString(R.string.statistics_completed_tasks)
          + " "
          + numberOfCompletedTasks)
      statisticsTV.text = displayString
    }
  }

  companion object {
    operator fun invoke(): StatisticsFragment = StatisticsFragment()
  }
}
