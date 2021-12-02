/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("SynchronizationUtils")

package androidx.test.espresso.device.action

import android.app.Activity
import android.content.pm.ActivityInfo.CONFIG_ORIENTATION
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage

/** Collection of utility methods for synchronizing device actions. */

/**
 * Detects if orientation changes are handled by the provided Activity using ActivityInfo.
 *
 * @param activity the Activity to check
 * @return whether or not the Activity handles configuration changes itself
 */
fun areOrientationChangesHandledByActivity(activity: Activity): Boolean {
  val activityInfo = activity.getPackageManager().getActivityInfo(activity.getComponentName(), 0)
  return (activityInfo.configChanges and CONFIG_ORIENTATION) != 0
}

/** Returns the first Activity found in the RESUMED stage, or null if none are found. */
fun getResumedActivityOrNull(): Activity? {
  var activity: Activity? = null
  InstrumentationRegistry.getInstrumentation().runOnMainSync {
    run {
      activity =
        ActivityLifecycleMonitorRegistry.getInstance()
          .getActivitiesInStage(Stage.RESUMED)
          .elementAtOrNull(0)
    }
  }
  return activity
}
