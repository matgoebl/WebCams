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
import java.net.URL;
import java.net.URLConnection;

import cz.yetanotherview.webcamviewer.app.listener.ConnectionTesterListener;

public class ImageTester extends AsyncTask<Void, Void, Boolean> {

    private String url;
    private ConnectionTesterListener mListener;

    public ImageTester(String url, ConnectionTesterListener mListener) {
        this.url = url;
        this.mListener  = mListener;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            URLConnection connection = new URL(url).openConnection();
            String contentType = connection.getHeaderField("Content-Type");
            return contentType != null && contentType.startsWith("image/");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mListener != null)
            mListener.connectionStatus(result);
    }
}