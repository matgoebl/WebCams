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
import android.support.design.widget.Snackbar;

import com.afollestad.materialdialogs.MaterialDialog;

import junit.framework.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.simple.UnavailableDialog;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class SendToInbox {

    private double latitude, longitude;
    private WebCam webCam;
    private Boolean fromCommunityList;
    private Activity mActivity;
    private MaterialDialog indeterminateProgress;

    public void sendToInboxWebCam(Activity activity, WebCam webCam, Boolean fromCommunityList) {

        this.webCam = webCam;
        this.fromCommunityList = fromCommunityList;
        this.mActivity = activity;

        showIndeterminateProgress();
        new sendToInboxBackgroundTask().execute(0);
    }

    public void sendToInboxLocation(Activity activity, double latitude, double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.mActivity = activity;

        showIndeterminateProgress();
        new sendToInboxBackgroundTask().execute(1);
    }

    private class sendToInboxBackgroundTask extends AsyncTask<Integer, Void, Void> {

        protected Void doInBackground(Integer... params) {

            try {
                String url = "";
                switch (params[0]) {
                    case 0:
                        url = Utils.JSON_FILE_NPOEMOWQCPPO;
                        break;
                    case 1:
                        url = Utils.JSON_FILE_JDWUFOYXLOYY;
                        break;
                }
                HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
                urlConn.connect();
                Assert.assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());

                HashMap<String , String> postDataParams = new HashMap<>();

                switch (params[0]) {
                    case 0:
                        if (fromCommunityList) {
                            postDataParams.put("subject", "Something is wrong");
                            postDataParams.put("webCamUniId", String.valueOf(webCam.getUniId()));
                        }
                        else {
                            postDataParams.put("subject", "New WebCam for approval");
                            postDataParams.put("webCamUniId", "none");
                        }
                        postDataParams.put("isStream", String.valueOf(webCam.isStream()));
                        postDataParams.put("webCamName", webCam.getName());
                        postDataParams.put("webCamUrl", webCam.getUrl());
                        if (webCam.isStream()) {
                            postDataParams.put("webCamThumbUrl", webCam.getThumbUrl());
                        }
                        postDataParams.put("webCamLatitude", String.valueOf(webCam.getLatitude()));
                        postDataParams.put("webCamLongitude", String.valueOf(webCam.getLongitude()));
                        break;
                    case 1:
                        postDataParams.put("subject", "No nearby WebCams");
                        postDataParams.put("latitude", String.valueOf(latitude));
                        postDataParams.put("longitude", String.valueOf(longitude));
                        break;
                }

                new PerformPostCall().performPostCall(url, postDataParams);
                sent();
            }
            catch (IOException e) {
                System.err.println("Error creating HTTP connection");
                this.publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... args) {
            super.onProgressUpdate(args);

            indeterminateProgress.dismiss();
            new UnavailableDialog().show(mActivity.getFragmentManager(), "UnavailableDialog");
        }
    }

    private void showIndeterminateProgress() {
        indeterminateProgress = new MaterialDialog.Builder(mActivity)
                .content(R.string.please_wait)
                .progress(true, 0)
                .show();
    }

    private void sent() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                indeterminateProgress.dismiss();
                Snackbar.make(mActivity.findViewById(R.id.coordinator_layout), R.string.sent,
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
