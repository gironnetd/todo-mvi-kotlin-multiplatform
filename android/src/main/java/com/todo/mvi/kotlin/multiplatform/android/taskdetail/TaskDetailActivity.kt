/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.todo.mvi.kotlin.multiplatform.android.taskdetail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.todo.mvi.kotlin.multiplatform.android.R
import com.todo.mvi.kotlin.multiplatform.android.util.addFragmentToActivity

/**
 * Displays task details screen.
 */
class TaskDetailActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.taskdetail_act)

    // Set up the toolbar.
    setSupportActionBar(findViewById(R.id.toolbar))
    supportActionBar?.run {
      setDisplayHomeAsUpEnabled(true)
      setDisplayShowHomeEnabled(true)
    }

    // Get the requested task id
    val taskId = intent.getStringExtra(EXTRA_TASK_ID)

    if (supportFragmentManager.findFragmentById(R.id.contentFrame) == null) {
      addFragmentToActivity(supportFragmentManager, TaskDetailFragment(taskId.toString()), R.id.contentFrame)
    }
  }

  override fun onSupportNavigateUp(): Boolean {
    onBackPressed()
    return true
  }

  companion object {
    const val EXTRA_TASK_ID = "TASK_ID"
  }
}
