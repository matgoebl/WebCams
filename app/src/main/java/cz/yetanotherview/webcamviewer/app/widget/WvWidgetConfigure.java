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

import android.appwidget.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.ColorChooserDialog;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.adapter.WidgetConfigureAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class WvWidgetConfigure extends AppCompatActivity implements ColorChooserDialog.ColorCallback {

    private static final String PREFS_NAME
            = "cz.yetanotherview.webcamviewer.app_widgets";
    private static final String PREF_PREFIX_KEY = "widget_";
    private static int preSelectedColorText;
    private static int preSelectedColorBackground;
    private GradientDrawable textColorBgShape, backgroundColorBgShape;

    private static Context context;

    private List<WebCam> allWebCams;
    private int mAppWidgetId;
    private Intent mResult;

    public WvWidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        context = this;

        mAppWidgetId = getIntent().getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        mResult = new Intent();
        mResult.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_CANCELED, mResult);

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        setContentView(R.layout.widget_configure);
        View mEmptyView = findViewById(R.id.empty_text);
        LinearLayout textColorButton = (LinearLayout) findViewById(R.id.text_color);
        LinearLayout backgroundColorButton = (LinearLayout) findViewById(R.id.background_color);

        View text_color_icon = findViewById(R.id.text_color_icon);
        preSelectedColorText = Utils.getColor(getResources(), R.color.white);
        textColorBgShape = (GradientDrawable) text_color_icon.getBackground();
        textColorBgShape.setColor(preSelectedColorText);

        View background_color_icon = findViewById(R.id.background_color_icon);
        preSelectedColorBackground = Utils.getColor(getResources(), R.color.widget_background);
        backgroundColorBgShape = (GradientDrawable) background_color_icon.getBackground();
        backgroundColorBgShape.setColor(preSelectedColorBackground);

        DatabaseHelper db = new DatabaseHelper(context);
        allWebCams = db.getAllWebCams(Utils.nameSortOrder);
        db.closeDB();

        RecyclerView recList = (RecyclerView) findViewById(R.id.widget_selection_list);
        recList.setHasFixedSize(true);
        recList.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        WidgetConfigureAdapter widgetConfigureAdapter = new WidgetConfigureAdapter(allWebCams);
        recList.setAdapter(widgetConfigureAdapter);

        if (widgetConfigureAdapter.getItemCount() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        widgetConfigureAdapter.setClickListener(new WidgetConfigureAdapter.ClickListener() {

            @Override
            public void onClick(View v, int position) {
                WebCam selWebCam = allWebCams.get(position);

                saveSelectedPref(context, mAppWidgetId, "name", selWebCam.getName());
                String url;
                if (selWebCam.isStream()) {
                    url = selWebCam.getThumbUrl();
                }
                else url = selWebCam.getUrl();
                saveSelectedPref(context, mAppWidgetId, "url", url);

                setResult(RESULT_OK, mResult);
                finish(); // finalize configuration
                forceUpdate();
            }

            private void forceUpdate() {
                AppWidgetProviderInfo info =
                        AppWidgetManager.getInstance(getApplicationContext()).getAppWidgetInfo(mAppWidgetId);

                Intent intent = new Intent();
                intent.setComponent(info.provider);
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {mAppWidgetId});
                sendBroadcast(intent);
            }
        });
        textColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorChooser(preSelectedColorText, true);
            }
        });
        backgroundColorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showColorChooser(preSelectedColorBackground, false);
            }
        });
    }

    private void showColorChooser(int preselected, Boolean fromTextButton) {
        new ColorChooserDialog.Builder(this, R.string.color_chooser)
                .accentMode(fromTextButton)
                .doneButton(R.string.done)
                .cancelButton(android.R.string.cancel)
                .backButton(R.string.back)
                .preselect(preselected)
                .show();
    }

    private static void saveSelectedPref(Context context, int appWidgetId, String key, String value) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + key + appWidgetId, value);
        prefs.apply();
    }

    private static void saveSelectedColor(Context context, int appWidgetId, String key, int color) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + key + appWidgetId, color);
        prefs.apply();
    }

    public static String loadSelectedPref(Context context, int appWidgetId, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String value = prefs.getString(PREF_PREFIX_KEY + key + appWidgetId, null);
        if (value != null) {
            return value;
        } else {
            return null;
        }
    }

    public static int loadSelectedColor(Context context, int appWidgetId, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        int value = prefs.getInt(PREF_PREFIX_KEY + key + appWidgetId, 0);
        if (value != 0) {
            return value;
        } else {
            return 0;
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {
        if (dialog.isAccentMode()) {
            preSelectedColorText = color;
            saveSelectedColor(context, mAppWidgetId, "textColor", color);
            textColorBgShape.setColor(color);
        } else {
            preSelectedColorBackground = color;
            saveSelectedColor(context, mAppWidgetId, "backgroundColor", color);
            backgroundColorBgShape.setColor(color);
        }
    }
}
