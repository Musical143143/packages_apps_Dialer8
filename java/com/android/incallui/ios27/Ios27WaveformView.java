package com.android.incallui.ios27;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class Ios27WaveformView extends View {
    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barRect = new RectF();
    private ValueAnimator animator;
    private float phase = 0f;
    private final int barCount = 42;

    public Ios27WaveformView(Context context) {
        super(context);
        init();
    }

    public Ios27WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Ios27WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        barPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startWaveAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopWaveAnimation();
        super.onDetachedFromWindow();
    }

    private void startWaveAnimation() {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
            animator.setDuration(1400L);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(a -> {
                phase = (float) a.getAnimatedValue();
                postInvalidateOnAnimation();
            });
            animator.start();
        }
    }

    private void stopWaveAnimation() {
        if (animator == null) {
            return;
        }
        animator.cancel();
        animator = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            barPaint.setShader(new LinearGradient(
                    0, 0, w, h,
                    new int[]{0xCC3B82F6, 0xCC8B5CF6, 0xCCEC4899, 0xCC06B6D4},
                    null,
                    Shader.TileMode.CLAMP));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        float totalSpacing = width * 0.45f;
        float barWidth = Math.max(4f, (width - totalSpacing) / barCount);
        float gap = totalSpacing / Math.max(1, barCount - 1);
        float centerY = height / 2f;

        for (int i = 0; i < barCount; i++) {
            float normalized = (float) i / barCount;
            float envelope = (float) Math.sin(normalized * Math.PI);
            float wave = (float) (0.35f + 0.65f * Math.abs(Math.sin(phase + i * 0.42f)));
            float barHeight = Math.max(barWidth, height * 0.85f * envelope * wave);

            float left = i * (barWidth + gap);
            float top = centerY - (barHeight / 2f);
            float right = left + barWidth;
            float bottom = centerY + (barHeight / 2f);

            barRect.set(left, top, right, bottom);
            canvas.drawRoundRect(barRect, barWidth / 2f, barWidth / 2f, barPaint);
        }
    }
}
