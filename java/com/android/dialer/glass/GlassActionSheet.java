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

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.android.dialer.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Frosted-glass action card presented from the bottom of the screen. The background behind the
 * card is blurred on Android 12+ and the card itself uses the light/dark glass theme tokens, so it
 * follows the system night mode without extra code.
 */
public final class GlassActionSheet {

  private static final float BLUR_RADIUS_PX = 24f;

  private final Context context;
  @Nullable private final View blurTarget;
  private final List<Action> actions = new ArrayList<>();
  @Nullable private CharSequence title;
  @Nullable private Dialog dialog;

  public GlassActionSheet(Context context, @Nullable View blurTarget) {
    this.context = context;
    this.blurTarget = blurTarget;
  }

  public GlassActionSheet setTitle(@Nullable CharSequence title) {
    this.title = title;
    return this;
  }

  public GlassActionSheet addAction(CharSequence label, boolean destructive, Runnable onClick) {
    actions.add(new Action(label, destructive, onClick));
    return this;
  }

  public void show() {
    dialog = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
    Window window = dialog.getWindow();
    if (window == null) {
      return;
    }
    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT);
    window.setGravity(Gravity.BOTTOM);
    window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    window.setDimAmount(0.25f);
    window.setWindowAnimations(R.style.GlassSheetAnimation);
    dialog.setContentView(buildContent());
    dialog.setOnDismissListener(
        new android.content.DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(android.content.DialogInterface d) {
            clearBlur();
          }
        });
    applyBlur();
    dialog.show();
  }

  public void dismiss() {
    if (dialog != null) {
      dialog.dismiss();
    }
  }

  private View buildContent() {
    int pad = dp(16);
    LinearLayout root = new LinearLayout(context);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setPadding(pad, pad, pad, pad);

    LinearLayout card = new LinearLayout(context);
    card.setOrientation(LinearLayout.VERTICAL);
    card.setBackgroundResource(R.drawable.glass_card);
    card.setPadding(0, dp(8), 0, dp(8));

    if (!TextUtils.isEmpty(title)) {
      TextView titleView = new TextView(context);
      titleView.setText(title);
      titleView.setSingleLine(true);
      titleView.setEllipsize(TextUtils.TruncateAt.END);
      titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
      titleView.setTextColor(context.getColor(R.color.glass_text_secondary));
      titleView.setPadding(dp(20), dp(12), dp(20), dp(10));
      card.addView(titleView, new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    for (final Action action : actions) {
      TextView row = new TextView(context);
      row.setText(action.label);
      row.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
      row.setTextColor(context.getColor(
          action.destructive ? R.color.glass_destructive : R.color.glass_text_primary));
      row.setPadding(dp(20), dp(15), dp(20), dp(15));
      row.setBackground(rippleBackground());
      row.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              dismiss();
              action.onClick.run();
            }
          });
      card.addView(row, new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    root.addView(card, new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    return root;
  }

  private Drawable rippleBackground() {
    TypedArray ta =
        context.obtainStyledAttributes(new int[] {android.R.attr.selectableItemBackground});
    Drawable drawable = ta.getDrawable(0);
    ta.recycle();
    return drawable;
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private void applyBlur() {
    if (blurTarget != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      blurTarget.setRenderEffect(
          RenderEffect.createBlurEffect(BLUR_RADIUS_PX, BLUR_RADIUS_PX, Shader.TileMode.CLAMP));
    }
  }

  @RequiresApi(Build.VERSION_CODES.S)
  private void clearBlur() {
    if (blurTarget != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      blurTarget.setRenderEffect(null);
    }
  }

  private int dp(int value) {
    return Math.round(
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()));
  }

  private static final class Action {
    final CharSequence label;
    final boolean destructive;
    final Runnable onClick;

    Action(CharSequence label, boolean destructive, Runnable onClick) {
      this.label = label;
      this.destructive = destructive;
      this.onClick = onClick;
    }
  }
}
