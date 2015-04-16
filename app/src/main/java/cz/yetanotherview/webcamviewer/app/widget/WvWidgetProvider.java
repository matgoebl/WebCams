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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.*;

import java.util.*;

import cz.yetanotherview.webcamviewer.app.R;

public class WvWidgetProvider extends AppWidgetProvider {

    public static final String WIDGET_BUTTON = "cz.yetanotherview.webcamviewer.app.widget.WIDGET_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            int textColor = WvWidgetConfigure.loadSelectedColor(context, appWidgetId, "textColor");
            int backgroundColor = WvWidgetConfigure.loadSelectedColor(context, appWidgetId, "backgroundColor");
            String name = WvWidgetConfigure.loadSelectedPref(context, appWidgetId, "name");
            if(name == null) {
                continue;
            }

            RemoteViews remoteViews =  new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            remoteViews.setOnClickPendingIntent(R.id.sync_button, createRefresh(context, appWidgetId));
            remoteViews.setTextViewText(R.id.wTitle, name);
            if (textColor != 0) {
                remoteViews.setTextColor(R.id.wTitle, textColor);
                remoteViews.setInt(R.id.sync_button, "setColorFilter", textColor);
            }
            if (backgroundColor != 0) {
                remoteViews.setInt(R.id.background_layout, "setColorFilter", backgroundColor);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    remoteViews.setInt(R.id.background_layout, "setImageAlpha",
                            230);
                } else {
                    remoteViews.setInt(R.id.background_layout, "setAlpha",
                            230);
                }
            }
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

            loadImage(context, remoteViews, appWidgetId);
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

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
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

    private void loadImage(Context context, RemoteViews remoteViews, final int appWidgetId) {
        String url = WvWidgetConfigure.loadSelectedPref(context, appWidgetId, "url");

        AppWidgetTarget target = TARGETS.get(appWidgetId);
        if (target == null) {
            target = new AppWidgetTarget(context, remoteViews, R.id.wImage,
                    400, 400, new int[] {appWidgetId});
            TARGETS.put(appWidgetId, target);
        }

        Glide.with(context)
                .load(url)
                .asBitmap()
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override public boolean onException(
                            Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        TARGETS.remove(appWidgetId);
                        return false;
                    }
                    @Override public boolean onResourceReady(
                            Bitmap resource, String model, Target<Bitmap> target,
                            boolean isFromMemoryCache, boolean isFirstResource) {
                        TARGETS.remove(appWidgetId);
                        return false;
                    }
                })
                .into(target);
    }
}
