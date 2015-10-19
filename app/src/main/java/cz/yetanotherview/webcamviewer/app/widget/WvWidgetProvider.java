/*
* ******************************************************************************
* Copyright (c) 2013-2015 Róbert Papp - Tomas Valenta.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* *****************************************************************************
*/

package cz.yetanotherview.webcamviewer.app.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.*;

import java.util.*;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.MainActivity;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;

public class WvWidgetProvider extends AppWidgetProvider {

    private static final String WIDGET_BUTTON = "cz.yetanotherview.webcamviewer.app.widget.WIDGET_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        Log.d("WebCamWidget", "Update");
        for (int appWidgetId : appWidgetIds) {
            Log.d("WebCamWidget", "Update for id="+appWidgetId);

            int textColor = WvWidgetConfigure.loadSelectedColor(context, appWidgetId, "textColor");
            int backgroundColor = WvWidgetConfigure.loadSelectedColor(context, appWidgetId, "backgroundColor");
            String name = WvWidgetConfigure.loadSelectedPref(context, appWidgetId, "name");
            if(name == null) {
                continue;
            }

            RemoteViews remoteViews =  new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setOnClickPendingIntent(R.id.sync_button, createRefresh(context, appWidgetId));
            remoteViews.setOnClickPendingIntent(R.id.wImage, createMain(context, appWidgetId));
            remoteViews.setTextViewText(R.id.wTitle, name);
            if (textColor != 0) {
                remoteViews.setTextColor(R.id.wTitle, textColor);
                remoteViews.setTextColor(R.id.wTime, textColor);
                remoteViews.setTextColor(R.id.wRefreshInfoText, textColor);
                remoteViews.setInt(R.id.sync_button, "setColorFilter", textColor);
            }
            if (backgroundColor != 0) {
                remoteViews.setInt(R.id.background_layout, "setColorFilter", backgroundColor);
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

            loadImage(context, remoteViews, appWidgetId);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Boolean autoRefresh = preferences.getBoolean("pref_auto_refresh", false);
        Boolean autoRefreshFullScreenOnly = preferences.getBoolean("pref_auto_refresh_fullscreen", false);
        int autoRefreshInterval = preferences.getInt("pref_auto_refresh_interval", 30000);

        if (autoRefresh && !autoRefreshFullScreenOnly)
        {
            Intent intent = new Intent(context, WvWidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Log.d("WebCamWidget", "Schedule update in " + autoRefreshInterval + " seconds");
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC, System.currentTimeMillis() + autoRefreshInterval, pendingIntent);
        }

    }

    private PendingIntent createRefresh(Context context, int appWidgetId) {
        Intent intent = new Intent(context, WvWidgetProvider.class);
        intent.setAction(WIDGET_BUTTON);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        return PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createMain(Context context, int appWidgetId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, appWidgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        Log.d("WebCamWidget", "Received intent");
        if (WIDGET_BUTTON.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                   R.layout.widget_layout);

            loadImage(context, remoteViews, appWidgetId);
        }
        else {
            super.onReceive(context, intent);
        }
    }

    /**
     * Need to store the AppWidgetTargets for each appWidgetId
     * to be able to cancel a previous load when something new comes.
     * @see AppWidgetTarget
     */
    private static final Map<Integer, AppWidgetTarget> TARGETS = new HashMap<>();

    private void loadImage(final Context context, final RemoteViews remoteViews, final int appWidgetId) {
        String url = WvWidgetConfigure.loadSelectedPref(context, appWidgetId, "url");

        AppWidgetTarget target = TARGETS.get(appWidgetId);
        if (target == null) {
            target = new AppWidgetTarget(context, remoteViews, R.id.wImage,
                    500, 500, new int[] {appWidgetId});
            TARGETS.put(appWidgetId, target);
        }

        Glide.with(context)
                .load(HttpHeader.getUrl(url))
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(new RequestListener<GlideUrl, Bitmap>() {
                    @Override
                    public boolean onException(
                            Exception e, GlideUrl model, Target<Bitmap> target, boolean isFirstResource) {
                        TARGETS.remove(appWidgetId);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Bitmap resource, GlideUrl model, Target<Bitmap> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        TARGETS.remove(appWidgetId);
                        remoteViews.setTextViewText(R.id.wTime,
                                Utils.getCustomDateString(Utils.getPattern(context)));
                        return false;
                    }
                })
                .into(target);
    }
}
