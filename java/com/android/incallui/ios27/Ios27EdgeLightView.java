package com.android.incallui.ios27;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public class Ios27EdgeLightView extends View {
    private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private ValueAnimator animator;

    public Ios27EdgeLightView(Context context) {
        super(context);
        init();
    }

    public Ios27EdgeLightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        edgePaint.setStyle(Paint.Style.STROKE);
        edgePaint.setStrokeWidth(10f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        animator = ValueAnimator.ofFloat(0f, 360f);
        animator.setDuration(2600L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> postInvalidateOnAnimation());
        animator.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator == null) {
            super.onDetachedFromWindow();
            return;
        }
        animator.cancel();
        animator = null;
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            edgePaint.setShader(new SweepGradient(
                    w / 2f, h / 2f,
                    new int[]{0xFF06B6D4, 0xFF8B5CF6, 0xFFEC4899, 0xFFF59E0B, 0xFF10B981, 0xFF06B6D4},
                    null));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = edgePaint.getStrokeWidth();
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawRoundRect(rect, 64f, 64f, edgePaint);
    }
}
