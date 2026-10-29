/*
 * Copyright (C) 2026 The LineageOS Project
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

package com.android.dialer.glass;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

/** Reads the user's on/off choices for the frosted-glass visual effects. */
public final class GlassSettings {

  /** Preference key for the animated RGB edge glow on the incoming-call screen. */
  public static final String KEY_EDGE_GLOW_ENABLED = "glass_edge_glow_enabled";

  /** Preference key for the background blur behind frosted-glass cards. */
  public static final String KEY_BLUR_ENABLED = "glass_blur_enabled";

  private GlassSettings() {}

  public static boolean isEdgeGlowEnabled(Context context) {
    return prefs(context).getBoolean(KEY_EDGE_GLOW_ENABLED, true);
  }

  public static boolean isBlurEnabled(Context context) {
    return prefs(context).getBoolean(KEY_BLUR_ENABLED, true);
  }

  private static SharedPreferences prefs(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }
}
