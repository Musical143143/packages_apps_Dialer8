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

package com.android.dialer.speeddial;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import com.android.dialer.R;
import com.android.dialer.common.Assert;
import com.android.dialer.glass.GlassActionSheet;
import com.android.dialer.speeddial.database.SpeedDialEntry.Channel;
import com.android.dialer.speeddial.loader.SpeedDialUiItem;

/** Frosted-glass action card that presents contact options for a favourite contact. */
public class ContextMenu {

  private final GlassActionSheet sheet;

  /** Creates a new context menu and displays it. */
  public static ContextMenu show(
      Context context,
      View anchor,
      ContextMenuItemListener contextMenuListener,
      SpeedDialUiItem speedDialUiItem) {
    ContextMenu menu = new ContextMenu(context, anchor, contextMenuListener, speedDialUiItem);
    menu.sheet.show();
    return menu;
  }

  /** Hides the context menu. */
  public void hide() {
    sheet.dismiss();
  }

  private ContextMenu(
      @NonNull Context context,
      @NonNull View anchor,
      ContextMenuItemListener listener,
      SpeedDialUiItem speedDialUiItem) {
    Channel voiceChannel = speedDialUiItem.getDefaultVoiceChannel();
    Channel videoChannel = speedDialUiItem.getDefaultVideoChannel();

    sheet = new GlassActionSheet(context, anchor.getRootView());
    if (voiceChannel != null) {
      String secondaryInfo =
          TextUtils.isEmpty(voiceChannel.label())
              ? voiceChannel.number()
              : context.getString(
                  R.string.call_subject_type_and_number,
                  voiceChannel.label(),
                  voiceChannel.number());
      sheet.setTitle(secondaryInfo);
      sheet.addAction(
          context.getString(R.string.contact_menu_voice_call),
          false,
          () -> listener.placeCall(Assert.isNotNull(voiceChannel)));
      sheet.addAction(
          context.getString(R.string.contact_menu_message),
          false,
          () -> listener.openSmsConversation(voiceChannel.number()));
    }
    if (videoChannel != null) {
      sheet.addAction(
          context.getString(R.string.contact_menu_video_call),
          false,
          () -> listener.placeCall(Assert.isNotNull(videoChannel)));
    }
    sheet.addAction(
        context.getString(R.string.contact_menu_contact_info),
        false,
        () -> listener.openContactInfo(speedDialUiItem));
    sheet.addAction(
        context.getString(R.string.contact_menu_remove),
        true,
        () -> listener.removeFavoriteContact(speedDialUiItem));
  }

  /** Listener to report user clicks on menu items. */
  public interface ContextMenuItemListener {

    /** Called when the user selects "voice call" or "video call" option from the context menu. */
    void placeCall(Channel channel);

    /** Called when the user selects "send message" from the context menu. */
    void openSmsConversation(String number);

    /** Called when the user selects "remove" from the context menu. */
    void removeFavoriteContact(SpeedDialUiItem speedDialUiItem);

    /** Called when the user selects "contact info" from the context menu. */
    void openContactInfo(SpeedDialUiItem speedDialUiItem);
  }
}
