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

package androidx.test.espresso.device.action

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.test.espresso.device.context.ActionContext
import androidx.test.espresso.device.controller.DeviceController
import androidx.test.runner.lifecycle.ActivityLifecycleCallback
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.util.concurrent.CountDownLatch

/** Action to set the test device to the provided screen orientation. */
internal class ScreenOrientationAction(val screenOrientation: ScreenOrientation) : DeviceAction {
  companion object {
    private val TAG = "ScreenOrientationAction"
  }

  /**
   * Performs a screen rotation to the provided orientation.
   *
   * Checks if the test device is already in the requested orientation. If it is not, performs a
   * DeviceController call to rotate the device. To synchronize this action, if an Activity is in
   * the RESUMED stage and does not handle configuration changes, we wait for the Activity to be in
   * the PAUSED stage. If there is not an Activity in the RESUMED stage or if the Activity found
   * handles configuration changes and therefore will not be recreated, we wait for Application's
   * orientation to be updated to the requested orientation.
   *
   * When called from a Robolectric test, performs a DeviceController call to rotate the device and
   * then returns.
   *
   * @param context the ActionContext containing the context for this application and test app.
   * @param deviceController the controller to use to interact with the device.
   */
  override fun perform(context: ActionContext, deviceController: DeviceController) {
    var currentOrientation =
      context.applicationContext.getResources().getConfiguration().orientation
    val requestedOrientation =
      if (screenOrientation == ScreenOrientation.LANDSCAPE) Configuration.ORIENTATION_LANDSCAPE
      else Configuration.ORIENTATION_PORTRAIT
    if (currentOrientation == requestedOrientation) {
      if (Log.isLoggable(TAG, Log.DEBUG)) {
        Log.d(TAG, "Device screen is already in the requested orientation, no need to rotate.")
      }
      return
    }

    if (Build.FINGERPRINT.equals("robolectric")) {
      deviceController.setScreenOrientation(screenOrientation.orientation)
      return
    }

    var currentActivityName: String? = null
    var configChangesHandled: Boolean = false
    val currentActivity = getResumedActivityOrNull()
    if (currentActivity != null) {
      currentActivityName = currentActivity.getLocalClassName()
      configChangesHandled = areOrientationChangesHandledByActivity(currentActivity)
    }

    val latch: CountDownLatch = CountDownLatch(1)

    if (currentActivity == null || configChangesHandled) {
      if (currentActivity == null) {
        Log.d(TAG, "No activity was found in the RESUMED stage.")
      } else if (configChangesHandled) {
        Log.d(TAG, "The current activity handles configuration changes.")
      }
      context.applicationContext.registerComponentCallbacks(
        object : ComponentCallbacks {
          override fun onConfigurationChanged(newConfig: Configuration) {
            if (newConfig.orientation == requestedOrientation) {
              if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Application's orientation was set to the requested orientation.")
              }
              latch.countDown()
            }
          }
          override fun onLowMemory() {}
        }
      )
    } else {
      Log.d(
        TAG,
        "The current activity does not handle configuration changes and will be recreated when " +
          "its orientation changes."
      )
      ActivityLifecycleMonitorRegistry.getInstance()
        .addLifecycleCallback(
          object : ActivityLifecycleCallback {
            override fun onActivityLifecycleChanged(activity: Activity, stage: Stage) {
              if (activity.getLocalClassName() == currentActivityName && stage == Stage.PAUSED) {
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                  Log.d(TAG, "Test activity was paused.")
                }
                ActivityLifecycleMonitorRegistry.getInstance().removeLifecycleCallback(this)
                latch.countDown()
              }
            }
          }
        )
    }
    deviceController.setScreenOrientation(screenOrientation.orientation)
    latch.await()
  }
}
