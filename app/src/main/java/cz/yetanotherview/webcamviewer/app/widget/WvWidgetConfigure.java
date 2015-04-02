package cz.yetanotherview.webcamviewer.app.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import cz.yetanotherview.webcamviewer.app.R;

public class WvWidgetConfigure extends Activity {
    static final String TAG = "WvWidgetConfigure";

    private static final String PREFS_NAME
            = "cz.yetanotherview.webcamviewer.app.widget.WvWidgetConfigure";
    private static final String PREF_PREFIX_KEY = "widget_";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    //EditText mUsername, mPassword;

    public WvWidgetConfigure() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setContentView(R.layout.widget_configure);

        // Find the EditText
        //mUsername = (EditText)findViewById(R.id.user_name);
        //mPassword = (EditText)findViewById(R.id.pass_word);

        // Bind the action for the save button.
        findViewById(R.id.widget_save_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

//        mAppWidgetPrefix.setText(loadTitlePref(ExampleAppWidgetConfigure.this, mAppWidgetId));
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = WvWidgetConfigure.this;

            // When the button is clicked, save the string in our prefs and return that they
            // clicked OK.
            //String username = mUsername.getText().toString();
            //String password = mPassword.getText().toString();
            //Log.d("log_U", username);
            //Log.d("log_P", password);
            //saveTitlePref(context, mAppWidgetId, "username", username);
            //saveTitlePref(context, mAppWidgetId, "password", password);

            // Push widget update to surface with newly set prefix
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            //WvWidgetProvider.updateAppWidget(context, appWidgetManager,
            //        mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String key, String value) {
//        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
//        prefs.putString(PREF_PREFIX_KEY + "androtwitt" + key, value);
//        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId, String key) {
//        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
//        String value = prefs.getString(PREF_PREFIX_KEY + "androtwitt" + key, null);
//        if (value != null) {
//            return value;
//        } else {
//            return context.getString(R.string.appwidget_prefix_default);
//        }
        return key;
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
    }

    static void loadAllTitlePrefs(Context context, ArrayList<Integer> appWidgetIds,
                                  ArrayList<String> texts) {
    }
}
