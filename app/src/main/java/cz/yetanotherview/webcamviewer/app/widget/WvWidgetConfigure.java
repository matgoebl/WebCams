package cz.yetanotherview.webcamviewer.app.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;
import cz.yetanotherview.webcamviewer.app.adapter.WidgetConfigureAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class WvWidgetConfigure extends Activity {

    private static final String PREFS_NAME
            = "cz.yetanotherview.webcamviewer.app_widgets";
    private static final String PREF_PREFIX_KEY = "widget_";

    private List<WebCam> allWebCams;
    private Context context;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public WvWidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);
        context = WvWidgetConfigure.this;

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        setContentView(R.layout.widget_configure);

        DatabaseHelper db = new DatabaseHelper(context);
        allWebCams = db.getAllWebCams(Utils.defaultSortOrder);
        db.closeDB();

        RecyclerView recList = (RecyclerView) findViewById(R.id.widget_selection_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        WidgetConfigureAdapter widgetConfigureAdapter = new WidgetConfigureAdapter(allWebCams);
        recList.setAdapter(widgetConfigureAdapter);

        widgetConfigureAdapter.setClickListener(new WidgetConfigureAdapter.ClickListener() {

            @Override
            public void onClick(View v, int position) {
                WebCam selWebCam = allWebCams.get(position);

                saveSelectedPref(context, mAppWidgetId, "name", selWebCam.getName());
                saveSelectedPref(context, mAppWidgetId, "url", selWebCam.getUrl());

                new WvWidgetProvider()
                        .onUpdate(context,
                                AppWidgetManager.getInstance(context),
                                new int[] { mAppWidgetId }
                        );

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
            }
        });
    }

    public static void saveSelectedPref(Context context, int appWidgetId, String key, String value) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + key + appWidgetId, value);
        prefs.apply();
    }

    public static String loadSelectedPref(Context context, int appWidgetId, String key) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String value = prefs.getString(PREF_PREFIX_KEY + key + appWidgetId, null);
        if (value != null) {
            return value;
        } else {
            return "";
        }
    }
}
