/*
* ******************************************************************************
* Copyright (c) 2013-2014 Tomas Valenta.
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

package cz.yetanotherview.webcamviewer.app.actions;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class JsonFetcherDialog extends DialogFragment {

    // Object for intrinsic lock
    public static final Object sDataLock = new Object();
    private DatabaseHelper db;
    private List<WebCam> webCams;
    private ProgressDialog dialog;

    private int selection;
    private boolean noNewWebCams = true;

    private static final String TAG = "JsonFetcherDialog";
    private static final String SERVER_URL = "yetanotherview.cz";
    private static final String JSON_FILE_URL = "http://api." + SERVER_URL + "/webcams";
    private static final int latest = 14;

    private String country;
    private Activity mActivity;

    private ReloadInterface mListener;

    public static interface ReloadInterface {
        public void invokeReload();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mListener = (ReloadInterface) activity;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(mActivity);

        Bundle bundle = this.getArguments();
        selection = bundle.getInt("selection", 0);

        dialog = new ProgressDialog(mActivity, getTheme());
        dialog.setTitle(R.string.importing_from_server);
        dialog.setMessage(getString(R.string.please_wait));
        dialog.setIndeterminate(true);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        WebCamsFromJsonFetcher fetcher = new WebCamsFromJsonFetcher();
        fetcher.execute();

        return dialog;
    }

    private void handleWebCamList() {

        dialog.dismiss();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
                SharedPreferences.Editor editor = preferences.edit();
                long now = Utils.getDate();

                synchronized (sDataLock) {
                    if (selection == 0) {
                        long lastFetchPopular = preferences.getLong("pref_last_fetch_popular", 0);
                        long categoryPopular = db.createCategory(new Category(getString(R.string.popular) + " " + Utils.getDateString()));
                            for (WebCam webCam : webCams) {
                                long webCamDateAdded = webCam.getDateAdded().getTime();
                                long differenceBetweenLastFetch = lastFetchPopular - webCamDateAdded;

                                if (webCam.isPopular() && differenceBetweenLastFetch < 0) {
                                    db.createWebCam(webCam, new long[] {categoryPopular});
                                    noNewWebCams = false;
                                }
                            }
                        editor.putLong("pref_last_fetch_popular", now);
                        db.closeDB();
                        if (noNewWebCams) {
                            db.deleteCategory(categoryPopular, false);
                            noNewWebCamsDialog();
                        }
                        else mListener.invokeReload();
                    }
                    else if (selection == 1) {

                        List<String> list = new ArrayList<>();
                        for (WebCam webCam : webCams) {
                            String country = webCam.getCountry();
                            if (!list.contains(country))
                                list.add(country);
                        }
                        Collections.sort(list);
                        String[] items = list.toArray(new String[list.size()]);

                        country = getString(R.string.country);

                        new MaterialDialog.Builder(mActivity)
                                .title(R.string.countries)
                                .items(items)
                                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                                        long categoryCountry = db.createCategory(new Category(country + " " + Utils.getDateString()));
                                        for (WebCam webCam : webCams) {
                                            if (webCam.getCountry().equals(text)) {
                                                db.createWebCam(webCam, new long[] {categoryCountry});
                                            }
                                        }
                                        db.closeDB();
                                        mListener = (ReloadInterface) mActivity;
                                        mListener.invokeReload();
                                    }
                                })
                                .positiveText(R.string.choose)
                                .show();

                    }
                    else if (selection == 2) {
                        long lastFetchLatest = preferences.getLong("pref_last_fetch_latest", 0);
                        long categoryLatest = db.createCategory(new Category(getString(R.string.latest) + " " + Utils.getDateString()));
                            for (WebCam webCam : webCams) {
                                long webCamDateAdded = webCam.getDateAdded().getTime();
                                long differenceBetweenLastFetch = lastFetchLatest - webCamDateAdded;
                                int differenceBetweenDates = (int) ((now - webCamDateAdded)/86400000);

                                if (differenceBetweenDates < latest && differenceBetweenLastFetch < 0) {
                                    db.createWebCam(webCam, new long[] {categoryLatest});
                                    noNewWebCams = false;
                                }
                            }
                        editor.putLong("pref_last_fetch_latest", now);
                        db.closeDB();
                        if (noNewWebCams) {
                            db.deleteCategory(categoryLatest, false);
                            noNewWebCamsDialog();
                        }
                        else mListener.invokeReload();
                    }
                }
                BackupManager backupManager = new BackupManager(mActivity);
                backupManager.dataChanged();

                editor.apply();
            }
        });
    }

    private class WebCamsFromJsonFetcher extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            Runtime runtime = Runtime.getRuntime();
            Process proc;
            try {
                proc = runtime.exec("ping -c 1 " + SERVER_URL);
                proc.waitFor();
                int exit = proc.exitValue();
                if (exit == 0) {
                    try {
                        //Create an HTTP client
                        HttpClient client = new DefaultHttpClient();
                        HttpPost post = new HttpPost(JSON_FILE_URL);

                        //Perform the request and check the status code
                        HttpResponse response = client.execute(post);
                        StatusLine statusLine = response.getStatusLine();
                        if(statusLine.getStatusCode() == 200) {
                            HttpEntity entity = response.getEntity();
                            InputStream content = entity.getContent();

                            try {
                                //Read the server response and attempt to parse it as JSON
                                Reader reader = new InputStreamReader(content);

                                Gson gson = new GsonBuilder().setDateFormat("dd.MM.yyyy HH:mm:ss, zzzz").create();
                                webCams = Arrays.asList(gson.fromJson(reader, WebCam[].class));
                                content.close();

                                handleWebCamList();
                            } catch (Exception ex) {
                                Log.e(TAG, "Failed to parse JSON due to: " + ex);
                            }
                        } else {
                            Log.e(TAG, "Server responded with status code: " + statusLine.getStatusCode());
                        }
                    } catch(Exception ex) {
                        Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
                    }
                } else {
                    dialog.dismiss();
                    this.publishProgress();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            dialogUnavailable();
        }
    }

    private void noNewWebCamsDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.no_new_webcams)
                .content(R.string.no_new_webcams_summary)
                .positiveText(android.R.string.ok)
                .show();
    }

    private void dialogUnavailable() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.server_unavailable)
                .content(R.string.server_unavailable_summary)
                .positiveText(android.R.string.ok)
                .show();
    }
}
