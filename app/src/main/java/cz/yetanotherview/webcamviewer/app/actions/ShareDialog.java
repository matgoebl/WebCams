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

package cz.yetanotherview.webcamviewer.app.actions;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.actions.simple.UnavailableDialog;
import cz.yetanotherview.webcamviewer.app.helper.ConnectionTester;

public class ShareDialog extends DialogFragment {

    private Uri bmpUri;
    private String url;
    private MaterialDialog mProgressDialog;

    private static final String baseFolderPath = Utils.folderWCVPath;
    private static final String tmpFolderPath = Utils.folderWCVPathTmp;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        url = bundle.getString("url", "");

        mProgressDialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.please_wait)
                .progress(false, 100)
                .build();

        createFolders();
        if (ConnectionTester.isConnected(getActivity())) {
            new ShareImage().execute(true);
        } else {
            new ShareImage().execute(false);
        }

        return mProgressDialog;
    }

    private void createFolders() {
        File baseFolder = new File(baseFolderPath);
        File tmpFolder = new File(tmpFolderPath);
        if (!baseFolder.exists()) {
            if (!baseFolder.mkdir()) {
                Log.d("Error","Folder cannot be created.");
            }
        }
        if (!tmpFolder.exists()) {
            if (!tmpFolder.mkdir()) {
                Log.d("Error","Folder cannot be created.");
            }
        }
    }

    private class ShareImage extends AsyncTask <Boolean,Integer,Long> {

        @Override
        protected Long doInBackground(Boolean... booleans) {
            int count;
            if (booleans[0]) {
                try {
                    URL mUrl = new URL(url);
                    URLConnection connexion = mUrl.openConnection();
                    connexion.connect();
                    String targetFileName = "share_image_" + System.currentTimeMillis() + ".jpg";
                    int lengthOfFile = connexion.getContentLength();
                    InputStream input = new BufferedInputStream(mUrl.openStream());
                    OutputStream output = new FileOutputStream(tmpFolderPath + targetFileName);
                    byte data[] = new byte[1024];
                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress ((int)(total*100/lengthOfFile));
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    // Compress
                    File file =  new File(tmpFolderPath + targetFileName);
                    Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
                    FileOutputStream out = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 78, out);
                    out.close();

                    bmpUri = Uri.fromFile(file);

                    continueOnUiThread(true);
                } catch (Exception ignored) {}
            }
            else continueOnUiThread(false);
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setProgress(progress[0]);
        }
    }

    private void continueOnUiThread(final boolean connected) {

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                mProgressDialog.dismiss();

                if (connected) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/jpeg");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share)));
                }
                else new UnavailableDialog().show(getFragmentManager(), "UnavailableDialog");
            }
        });
    }
}
