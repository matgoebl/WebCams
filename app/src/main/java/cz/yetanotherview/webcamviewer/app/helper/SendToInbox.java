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

import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;

import junit.framework.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.simple.UnavailableDialog;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class SendToInbox {

    private WebCam webCam;
    private Boolean fromCommunityList;
    private Activity mActivity;
    private MaterialDialog indeterminateProgress;

    public void sendToInbox(Activity activity, WebCam webCam, Boolean fromCommunityList) {

        this.webCam = webCam;
        this.fromCommunityList = fromCommunityList;
        this.mActivity = activity;

        showIndeterminateProgress();
        new sendToInboxBackgroundTask().execute();
    }

    private class sendToInboxBackgroundTask extends AsyncTask<Void, Void, Void> {

        protected Void doInBackground(Void... voids) {

            try {
                String url = Utils.JSON_FILE_NPOEMOWQCPPO;
                HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
                urlConn.connect();
                Assert.assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());

                HashMap<String , String> postDataParams = new HashMap<>();
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
                Snackbar.with(mActivity)
                        .text(R.string.sent)
                        .actionLabel(R.string.dismiss)
                        .actionColor(mActivity.getResources().getColor(R.color.yellow))
                        .show(mActivity);
            }
        });
    }
}
