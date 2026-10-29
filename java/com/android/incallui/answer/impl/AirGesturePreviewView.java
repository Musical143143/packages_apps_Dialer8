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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;

/**
 * Preview ring for the wave gesture. It fills as the accept window runs, shows a check mark for an
 * accept or a cross for a decline, and pulses while a hand is over the sensor.
 */
public class AirGesturePreviewView extends View {

  private static final int ACCEPT_COLOR = 0xFF34C759;
  private static final int REJECT_COLOR = 0xFFFF3B30;
  private static final int HAND_COLOR = 0x99FFFFFF;
  private static final float SIZE_DP = 120f;
  private static final float RING_DP = 6f;

  private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint glyphPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF ring = new RectF();
  private final Path glyph = new Path();

  private AirGestureController.Preview preview = AirGestureController.Preview.NONE;
  private float progress;

  public AirGesturePreviewView(Context context) {
    this(context, null);
  }

  public AirGesturePreviewView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setClickable(false);
    setFocusable(false);
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

    float ringWidth = dp(RING_DP);
    trackPaint.setStyle(Paint.Style.STROKE);
    trackPaint.setStrokeWidth(ringWidth);
    trackPaint.setColor(0x33FFFFFF);

    arcPaint.setStyle(Paint.Style.STROKE);
    arcPaint.setStrokeWidth(ringWidth);
    arcPaint.setStrokeCap(Paint.Cap.ROUND);

    glyphPaint.setStyle(Paint.Style.STROKE);
    glyphPaint.setStrokeWidth(dp(4f));
    glyphPaint.setStrokeCap(Paint.Cap.ROUND);
    glyphPaint.setStrokeJoin(Paint.Join.ROUND);

    setAlpha(0f);
  }

  /** Sets the preview state and fill progress (0 to 1), and fades the view in or out. */
  public void setPreview(AirGestureController.Preview newPreview, float newProgress) {
    preview = newPreview;
    progress = Math.max(0f, Math.min(1f, newProgress));
    float target = newPreview == AirGestureController.Preview.NONE ? 0f : 1f;
    if (getAlpha() != target) {
      animate().cancel();
      animate().alpha(target).setDuration(180L).start();
    }
    invalidate();
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    float half = dp(RING_DP) / 2f;
    ring.set(half, half, w - half, h - half);
    float cx = w / 2f;
    float cy = h / 2f;
    float arm = w * 0.18f;
    glyph.reset();
    // Check mark for accept.
    glyph.moveTo(cx - arm, cy);
    glyph.lineTo(cx - arm * 0.25f, cy + arm * 0.8f);
    glyph.lineTo(cx + arm, cy - arm * 0.7f);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (preview == AirGestureController.Preview.NONE) {
      return;
    }
    canvas.drawArc(ring, 0f, 360f, false, trackPaint);
    switch (preview) {
      case HAND:
        arcPaint.setColor(HAND_COLOR);
        canvas.drawArc(ring, -90f, 360f, false, arcPaint);
        break;
      case ACCEPT:
        arcPaint.setColor(ACCEPT_COLOR);
        canvas.drawArc(ring, -90f, 360f * progress, false, arcPaint);
        glyphPaint.setColor(ACCEPT_COLOR);
        if (progress >= 1f) {
          canvas.drawPath(glyph, glyphPaint);
        }
        break;
      case REJECT:
        arcPaint.setColor(REJECT_COLOR);
        canvas.drawArc(ring, -90f, 360f, false, arcPaint);
        glyphPaint.setColor(REJECT_COLOR);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float arm = getWidth() * 0.16f;
        canvas.drawLine(cx - arm, cy - arm, cx + arm, cy + arm, glyphPaint);
        canvas.drawLine(cx + arm, cy - arm, cx - arm, cy + arm, glyphPaint);
        break;
      default:
        break;
    }
  }

  /** Preferred size, so the parent can centre it. */
  public int preferredSizePx() {
    return Math.round(dp(SIZE_DP));
  }

  private float dp(float value) {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
  }
}
