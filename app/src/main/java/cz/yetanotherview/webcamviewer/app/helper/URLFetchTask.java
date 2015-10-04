/*
* ******************************************************************************
* Copyright (c) 2013-2015 Tomas Valenta.
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

package cz.yetanotherview.webcamviewer.app.helper;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.fragments.BaseFragment;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class URLFetchTask extends AsyncTask<Integer, Void, List<WebCam>> {

    private boolean showProgress;

    BaseFragment container;
    Activity activity;
    String query;

    public URLFetchTask(BaseFragment baseFragment, String query) {
        this.container = baseFragment;
        this.activity = baseFragment.getActivity();
        this.query = query;
    }

    @Override
    protected List<WebCam> doInBackground(Integer... params) {

        List<WebCam> allWebCams = new ArrayList<>();

        try {
            String action;
            int id = params[0];
            if (id == R.id.latest_webcams) {
                action = "3";
            } else if (id == R.id.popular_webcams) {
                action = "1";
            } else if (id == R.id.live_streams) {
                action = "2";
            } else action = "0";

            URL url;
            if (id == R.id.nearby_webcams) {
                KnownLocation knownLocation = Utils.getLastKnownLocation(activity);
                url = new URL(Utils.JSON_FILE_SNRSRKUBIIXK +
                        "?action=" + "4" +
                        "&id=" + activity.getApplicationContext().getPackageName() + //ToDo
                        "&latitude=" + knownLocation.getLatitude() +
                        "&longitude=" + knownLocation.getLongitude()
                );
            } else if (id == R.id.selecting_by_type) {
                url = new URL(Utils.JSON_FILE_SNRSRKUBIIXK +
                        "?action=" + "5" +
                        "&id=" + activity.getApplicationContext().getPackageName() + //ToDo
                        "&type=" + params[1]
                );
            } else if (id == R.id.selecting_by_name) {
                url = new URL(Utils.JSON_FILE_SNRSRKUBIIXK +
                        "?action=" + "6" +
                        "&id=" + activity.getApplicationContext().getPackageName() + //ToDo
                        "&query=" + query
                );
            } else url = new URL(Utils.JSON_FILE_SNRSRKUBIIXK + "?action=" + action + "&id=" + activity.getApplicationContext().getPackageName());

            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.connect();
            Assert.assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());

            InputStream content = new BufferedInputStream(urlConn.getInputStream());
            try {
                //Read the server response and attempt to parse it as JSON
                Reader reader = new InputStreamReader(content);

                Gson gson = new GsonBuilder().setDateFormat(Utils.dateTimeFormat).create();
                allWebCams = Arrays.asList(gson.fromJson(reader, WebCam[].class));
                content.close();


            } catch (Exception ex) {
                Log.e("TAG", "Failed to parse JSON due to: " + ex);
                this.publishProgress();
            }
        } catch (IOException e) {
            System.err.println("Error creating HTTP connection");
            this.publishProgress();
        }

        return allWebCams;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (showProgress) {
            container.showProgressBar();
        }
    }

    @Override
    protected void onPostExecute(List<WebCam> allWebCams) {
        super.onPostExecute(allWebCams);
        // The activity can be null if it is thrown out by Android while task is running!
        if(container!=null && container.getActivity()!=null) {
            container.populateResult(allWebCams);
            if (showProgress) {
                container.hideProgressBar();
            }
            this.container = null;
        }
    }

    public void showProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }
}