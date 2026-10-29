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

package com.android.incallui.answer.impl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;

/**
 * Wave gesture over the proximity sensor, used as an air gesture on the incoming-call screen.
 *
 * <p>A wave is a quick near-then-far pulse of the proximity sensor, which is what a hand moved over
 * the top of the screen produces. One wave accepts the call and two waves decline it. The first wave
 * only counts after the sensor has been clear for a while, so a phone that starts out covered, such
 * as one in a pocket, never triggers it. A long covered period is also not a wave.
 *
 * <p>The controller reports a live preview to its listener while it waits for a second wave, so the
 * screen can show what the gesture will do before it fires.
 */
public class AirGestureController implements SensorEventListener {

  /**
   * Whether a hand detector is available to confirm the gesture is from a hand. The proximity
   * sensor cannot tell a hand from a body part or an object, so the wave never accepts or declines
   * a call until hand detection is added and this is set to true.
   */
  public static final boolean HAND_DETECTION_AVAILABLE = false;

  /** What the gesture is currently about to do. */
  public enum Preview {
    NONE,
    HAND,
    ACCEPT,
    REJECT
  }

  /** Receives previews and the final decision. Called on the main thread. */
  public interface Listener {
    void onGesturePreview(Preview preview, float progress);

    void onAccept();

    void onReject();
  }

  private static final long MIN_PULSE_MS = 120L;
  private static final long MAX_PULSE_MS = 700L;
  private static final long CLEAR_BEFORE_WAVE_MS = 300L;
  private static final long WAVE_WINDOW_MS = 1200L;
  private static final long TICK_MS = 33L;

  private final SensorManager sensorManager;
  @Nullable private final Sensor proximity;
  private final Listener listener;
  private final Handler handler = new Handler(Looper.getMainLooper());

  private boolean running;
  private boolean near;
  private long nearSinceMs;
  private long clearSinceMs;
  private boolean firstWaveAllowed;
  private int waves;
  private long windowStartMs;

  private final Runnable tick =
      new Runnable() {
        @Override
        public void run() {
          if (!running || waves != 1) {
            return;
          }
          float progress = (System.currentTimeMillis() - windowStartMs) / (float) WAVE_WINDOW_MS;
          if (progress >= 1f) {
            decide(Preview.ACCEPT);
          } else {
            listener.onGesturePreview(Preview.ACCEPT, progress);
            handler.postDelayed(this, TICK_MS);
          }
        }
      };

  public AirGestureController(Context context, Listener listener) {
    this.sensorManager = context.getSystemService(SensorManager.class);
    this.proximity =
        sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
    this.listener = listener;
  }

  /** Whether the device has a proximity sensor that can drive the gesture. */
  public boolean isSupported() {
    return proximity != null;
  }

  public void start() {
    if (running || proximity == null || sensorManager == null) {
      return;
    }
    running = true;
    near = false;
    waves = 0;
    firstWaveAllowed = false;
    sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI, handler);
  }

  public void stop() {
    if (!running) {
      return;
    }
    running = false;
    handler.removeCallbacks(tick);
    if (sensorManager != null) {
      sensorManager.unregisterListener(this);
    }
  }

  @Override
  public void onSensorChanged(SensorEvent event) {
    if (!running || event.sensor.getType() != Sensor.TYPE_PROXIMITY) {
      return;
    }
    long now = System.currentTimeMillis();
    boolean isNear = event.values[0] < proximity.getMaximumRange();
    if (isNear == near) {
      return;
    }
    if (isNear) {
      // Clear-to-near edge: the start of a potential wave.
      near = true;
      nearSinceMs = now;
      firstWaveAllowed = waves > 0 || (clearSinceMs > 0 && now - clearSinceMs >= CLEAR_BEFORE_WAVE_MS);
      if (waves == 0) {
        listener.onGesturePreview(Preview.HAND, 0f);
      }
    } else {
      // Near-to-clear edge: the end of a pulse. Only a short pulse counts as a wave.
      near = false;
      clearSinceMs = now;
      long pulse = now - nearSinceMs;
      if (pulse >= MIN_PULSE_MS && pulse <= MAX_PULSE_MS && firstWaveAllowed) {
        registerWave(now);
      } else if (waves == 0) {
        listener.onGesturePreview(Preview.NONE, 0f);
      }
    }
  }

  @Override
  public void onAccuracyChanged(Sensor sensor, int accuracy) {}

  private void registerWave(long now) {
    waves++;
    if (waves == 1) {
      windowStartMs = now;
      handler.post(tick);
    } else {
      // A second wave inside the window declines the call straight away.
      decide(Preview.REJECT);
    }
  }

  private void decide(Preview preview) {
    handler.removeCallbacks(tick);
    stop();
    if (preview == Preview.REJECT) {
      listener.onGesturePreview(Preview.REJECT, 1f);
      listener.onReject();
    } else {
      listener.onGesturePreview(Preview.ACCEPT, 1f);
      listener.onAccept();
    }
  }
}
