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
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.fragments.BaseFragment;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class DataFetcher extends AsyncTask<Integer, Void, List<WebCam>> {

    private boolean showProgress;

    BaseFragment container;
    Activity activity;
    String query;

    public DataFetcher(BaseFragment baseFragment, String query) {
        this.container = baseFragment;
        this.activity = baseFragment.getActivity();
        this.query = query;
    }

    @Override
    protected List<WebCam> doInBackground(Integer... params) {

        List<WebCam> allWebCams = new ArrayList<>();

        try {
            String action = "0";
            String urlAdd = "";
            int id = params[0];
            if (id == R.id.latest_webcams) {
                action = "3";
            } else if (id == R.id.popular_webcams) {
                action = "1";
            } else if (id == R.id.live_streams) {
                action = "2";
            } else if (id == R.id.nearby_webcams) {
                KnownLocation knownLocation = Utils.getLastKnownLocation(activity);
                action = "4";
                urlAdd = "&latitude=" + knownLocation.getLatitude() +
                        "&longitude=" + knownLocation.getLongitude();
            } else if (id == R.id.selecting_by_type) {
                action = "5";
                urlAdd = "&type=" + params[1];
            } else if (id == R.id.selecting_by_name) {
                action = "6";
                urlAdd = "&query=" + query;
            }

            String urlString = Utils.JSON_FILE_A3L1Y8QFXHPG + "?action=" + action + "&id=" + activity.getApplicationContext().getPackageName() + urlAdd;
            URL url = new URL(urlString);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = activity.getApplicationContext().getResources().openRawResource(R.raw.cert);
            Certificate ca = cf.generateCertificate(caInput);
            caInput.close();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);

            HttpsURLConnection urlConn = (HttpsURLConnection) url.openConnection();
            urlConn.setHostnameVerifier(new HostNameVerifier());
            urlConn.setSSLSocketFactory(context.getSocketFactory());
            final String basicAuth = "Basic " + Base64.encodeToString(Utils.basicAuth.getBytes(), Base64.NO_WRAP);
            urlConn.setRequestProperty("Authorization", basicAuth);

            InputStream content = urlConn.getInputStream();
            try {
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
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            e.printStackTrace();
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