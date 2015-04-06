package cz.yetanotherview.webcamviewer.app.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;
import cz.yetanotherview.webcamviewer.app.adapter.WidgetConfigureAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class WvWidgetConfigure extends Activity {

    static final String TAG = "WvWidgetConfigure";

    private static final String PREFS_NAME
            = "cz.yetanotherview.webcamviewer.app.widget.WvWidgetConfigure";
    private static final String PREF_PREFIX_KEY = "widget_";

    private List<WebCam> allWebCams;
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    public WvWidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        setContentView(R.layout.widget_configure);

        DatabaseHelper db = new DatabaseHelper(this);
        allWebCams = db.getAllWebCams(Utils.defaultSortOrder);

        RecyclerView recList = (RecyclerView) findViewById(R.id.widget_selection_list);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        WidgetConfigureAdapter widgetConfigureAdapter = new WidgetConfigureAdapter(allWebCams);
        recList.setAdapter(widgetConfigureAdapter);

        widgetConfigureAdapter.setClickListener(new WidgetConfigureAdapter.ClickListener() {

            @Override
            public void onClick(View v, int position) {
                WebCam selWebCam = allWebCams.get(position);

//                Context context = WvWidgetConfigure.this;
//
//                // We need to broadcast an APPWIDGET_UPDATE to our appWidget
//                // so it will update the user name TextView.
//                AppWidgetManager appWidgetManager = AppWidgetManager
//                        .getInstance(context);
//                ComponentName thisAppWidget = new ComponentName(context
//                        .getPackageName(), WvWidgetProvider.class.getName());
//                Intent updateIntent = new Intent(context, WvWidgetProvider.class);
//                int[] appWidgetIds = appWidgetManager
//                        .getAppWidgetIds(thisAppWidget);
//                updateIntent
//                        .setAction("android.appwidget.action.APPWIDGET_UPDATE");
//                updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
//                        appWidgetIds);
//                context.sendBroadcast(updateIntent);
//                // Done with Configure, finish Activity.
//                finish();

            }
        });
    }
}
