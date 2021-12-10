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

package androidx.test.espresso;

import androidx.test.espresso.util.EspressoOptional;

/**
 * An {@link EspressoException} which message can be truncated.
 *
 * <p>Types which implement this are also expected to extend {@link Throwable} or any of its
 * sub-types, as is typical of all Java exceptions.
 */
public interface TruncatedException {
  /**
   * Returns a clone of this exception with a modified exception message.
   *
   * <p>If the current message does not need to be changed, the same exception can be returned.
   *
   * @param maxMsgLen The maximum length of the view hierarchy to add to the exception message.
   * @param artifactFilename The filename of the optional artifact containing the full view
   *     hierarchy.
   */
  Throwable withTruncatedMessage(int maxMsgLen, EspressoOptional<String> artifactFilename);
}
