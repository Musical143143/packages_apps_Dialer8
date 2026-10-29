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

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import androidx.annotation.Nullable;

/**
 * Animated RGB glow that runs around the edges of the incoming-call screen.
 *
 * <p>A sweep gradient in the RGB spectrum is drawn as a soft outer glow and a crisp inner core
 * along the screen edge, and the gradient rotates continuously. The view never takes touches, so
 * the answer controls underneath keep working. Animation is skipped when the system has animations
 * turned off, in which case the glow is shown static.
 */
public class EdgeGlowView extends View {

  private static final int[] RGB_COLORS = {
    Color.RED,
    Color.YELLOW,
    Color.GREEN,
    Color.CYAN,
    Color.BLUE,
    Color.MAGENTA,
    Color.RED
  };
  private static final long ROTATION_MS = 3200L;
  private static final long FADE_IN_MS = 500L;
  private static final float GLOW_WIDTH_DP = 22f;
  private static final float CORE_WIDTH_DP = 3f;
  private static final float INSET_DP = 2f;
  private static final float CORNER_DP = 36f;
  private static final int GLOW_ALPHA = 110;

  private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private final RectF rect = new RectF();
  private final Matrix matrix = new Matrix();
  @Nullable private SweepGradient gradient;
  @Nullable private ValueAnimator rotation;
  private float angle;

  public EdgeGlowView(Context context) {
    this(context, null);
  }

  public EdgeGlowView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setClickable(false);
    setFocusable(false);
    setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);

    glowPaint.setStyle(Paint.Style.STROKE);
    glowPaint.setStrokeWidth(dp(GLOW_WIDTH_DP));
    glowPaint.setAlpha(GLOW_ALPHA);

    corePaint.setStyle(Paint.Style.STROKE);
    corePaint.setStrokeWidth(dp(CORE_WIDTH_DP));

    setAlpha(0f);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    float inset = dp(INSET_DP);
    rect.set(inset, inset, w - inset, h - inset);
    gradient = new SweepGradient(w / 2f, h / 2f, RGB_COLORS, null);
    glowPaint.setShader(gradient);
    corePaint.setShader(gradient);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (rotation == null) {
      rotation = ValueAnimator.ofFloat(0f, 360f);
      rotation.setDuration(ROTATION_MS);
      rotation.setRepeatCount(ValueAnimator.INFINITE);
      rotation.setInterpolator(new LinearInterpolator());
      rotation.addUpdateListener(
          animation -> {
            angle = (float) animation.getAnimatedValue();
            invalidate();
          });
    }
    if (ValueAnimator.areAnimatorsEnabled() && !rotation.isStarted()) {
      rotation.start();
    }
    animate().alpha(1f).setDuration(FADE_IN_MS).start();
  }

  @Override
  protected void onDetachedFromWindow() {
    if (rotation != null) {
      rotation.cancel();
    }
    super.onDetachedFromWindow();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (gradient == null) {
      return;
    }
    matrix.setRotate(angle, getWidth() / 2f, getHeight() / 2f);
    gradient.setLocalMatrix(matrix);
    float radius = dp(CORNER_DP);
    canvas.drawRoundRect(rect, radius, radius, glowPaint);
    canvas.drawRoundRect(rect, radius, radius, corePaint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // Never consume touches: the answer controls underneath must keep receiving them.
    return false;
  }

  private float dp(float value) {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
  }
}
