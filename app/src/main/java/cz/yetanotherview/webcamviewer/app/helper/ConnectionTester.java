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

import android.os.AsyncTask;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import cz.yetanotherview.webcamviewer.app.listener.ConnectionTesterListener;

import static junit.framework.Assert.assertTrue;

public class ConnectionTester extends AsyncTask<Void, Void, Boolean> {

    private String url;
    private ConnectionTesterListener mListener;
    private Integer[] statusCodes = { HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED,
            HttpURLConnection.HTTP_ACCEPTED, HttpURLConnection.HTTP_MOVED_PERM,
            HttpURLConnection.HTTP_MOVED_TEMP, HttpURLConnection.HTTP_NO_CONTENT};

    public ConnectionTester(String url, ConnectionTesterListener mListener) {
        this.url = url;
        this.mListener  = mListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        Boolean status;
        try {
            HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
            urlConn.connect();
            int responseCode = urlConn.getResponseCode();
            assertTrue(Arrays.asList(statusCodes).contains(responseCode));
            status = true;
        }
        catch (IOException e) {
            System.err.println("Error creating HTTP connection");
            status = false;
        }
        return status;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mListener != null)
            mListener.connectionStatus(result);
    }
}