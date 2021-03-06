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

package com.todo.mvi.kotlin.multiplatform.android.addedittask

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.todo.mvi.kotlin.multiplatform.android.R
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviIntent
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviView
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviViewModel
import com.todo.mvi.kotlin.multiplatform.android.mvibase.MviViewState
import com.todo.mvi.kotlin.multiplatform.android.util.ToDoViewModelFactory
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Main UI for the add task screen. Users can enter a task title and description.
 */
class AddEditTaskFragment : Fragment(), MviView<AddEditTaskIntent, AddEditTaskViewState> {
  private lateinit var title: TextView
  private lateinit var description: TextView
  private lateinit var fab: FloatingActionButton
  // Used to manage the data flow lifecycle and avoid memory leak.
  private val disposables = CompositeDisposable()
  /*private val viewModel: AddEditTaskViewModel by lazy(NONE) {
    ViewModelProviders
        .of(this, ToDoViewModelFactory.getInstance(context!!))
        .get(AddEditTaskViewModel::class.java)
  }*/
  private val viewModel: AddEditTaskViewModel by lazy(NONE) {
    ViewModelProviders
      .of(this, ToDoViewModelFactory.getInstance(context!!))
      .get(AddEditTaskViewModel::class.java)
  }

  private val argumentTaskId: String?
    get() = arguments?.getString(ARGUMENT_EDIT_TASK_ID)

  override fun onDestroy() {
    super.onDestroy()
    disposables.dispose()
  }

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.addtask_frag, container, false)
        .also {
          title = it.findViewById(R.id.add_task_title)
          description = it.findViewById(R.id.add_task_description)
          setHasOptionsMenu(true)
        }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    fab = activity!!.findViewById(R.id.fab_edit_task_done)
    fab.setImageResource(R.drawable.ic_done)

    bind()
  }

  /**
   * Connect the [MviView] with the [MviViewModel]
   * We subscribe to the [MviViewModel] before passing it the [MviView]'s [MviIntent]s.
   * If we were to pass [MviIntent]s to the [MviViewModel] before listening to it,
   * emitted [MviViewState]s could be lost
   */
  private fun bind() {
    // Subscribe to the ViewModel and call render for every emitted state
    disposables.add(viewModel.states().subscribe(this::render))
    // Pass the UI's intents to the ViewModel
    viewModel.processIntents(intents())
  }

  override fun intents(): Observable<AddEditTaskIntent> {
    return Observable.merge(initialIntent(), saveTaskIntent())
  }

  /**
   * The initial Intent the [MviView] emit to convey to the [MviViewModel]
   * that it is ready to receive data.
   * This initial Intent is also used to pass any parameters the [MviViewModel] might need
   * to render the initial [MviViewState] (e.g. the task id to load).
   */
  private fun initialIntent(): Observable<AddEditTaskIntent.InitialIntent> {
    return Observable.just(AddEditTaskIntent.InitialIntent(argumentTaskId))
  }

  private fun saveTaskIntent(): Observable<AddEditTaskIntent.SaveTask> {
    // Wrap the FAB click events into a SaveTaskIntent and set required information
    return RxView.clicks(fab).map {
      AddEditTaskIntent.SaveTask(argumentTaskId, title.text.toString(), description.text.toString())
    }
  }

  override fun render(state: AddEditTaskViewState) {
    if (state.isSaved) {
      showTasksList()
      return
    }
    if (state.isEmpty) {
      showEmptyTaskError()
    }
    if (state.title.isNotEmpty()) {
      setTitle(state.title)
    }
    if (state.description.isNotEmpty()) {
      setDescription(state.description)
    }
  }

  private fun showEmptyTaskError() {
    Snackbar.make(title, getString(R.string.empty_task_message), Snackbar.LENGTH_LONG).show()
  }

  private fun showTasksList() {
    activity!!.setResult(Activity.RESULT_OK)
    activity!!.finish()
  }

  private fun setTitle(title: String) {
    this.title.text = title
  }

  private fun setDescription(description: String) {
    this.description.text = description
  }

  companion object {
    const val ARGUMENT_EDIT_TASK_ID = "EDIT_TASK_ID"

    operator fun invoke(): AddEditTaskFragment = AddEditTaskFragment()
  }
}
