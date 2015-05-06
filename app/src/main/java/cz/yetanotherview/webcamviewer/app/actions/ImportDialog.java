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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.backup.BackupManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.nispok.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;
import cz.yetanotherview.webcamviewer.app.adapter.ListButtonAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.DeleteAllWebCams;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class ImportDialog extends DialogFragment {

    // Object for intrinsic lock
    public static final Object sDataLock = new Object();

    private MaterialDialog importDialog;
    private File selectedFile;
    private InputStream inputStream;
    private ListButtonAdapter listButtonAdapter;

    private MaterialDialog progressDialog;
    private int maxProgressValue;

    private DatabaseHelper db;
    private Activity mActivity;

    private static final int OPEN_REQUEST_CODE = 41;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(mActivity);
        importDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.backups)
                .customView(R.layout.import_dialog, false)
                .iconRes(R.drawable.settings_restore)
                .build();

        listButtonAdapter = new ListButtonAdapter(mActivity, Utils.getFiles(Utils.folderWCVPath));
        ListView listView = (ListView) importDialog.getCustomView().findViewById(R.id.list_files);
        View empty = importDialog.getCustomView().findViewById(R.id.list_files_empty);
        listView.setEmptyView(empty);
        listView.setAdapter(listButtonAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> list, View view, int position, long id) {

                selectedFile = listButtonAdapter.getItem(position);
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.are_you_sure)
                        .content(R.string.restore_confirm)
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                importDialog.dismiss();
                                importDialog = new MaterialDialog.Builder(mActivity)
                                        .title(getString(R.string.pref_delete_all_webcams) + "?")
                                        .content(R.string.import_summary)
                                        .positiveText(R.string.Yes)
                                        .negativeText(R.string.No)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                new importJsonBackgroundTask().execute(true, true);
                                            }
                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                new importJsonBackgroundTask().execute(false, true);
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();
            }

        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            LinearLayout containerLayout = (LinearLayout) importDialog.getCustomView().findViewById(R.id.container_layout);
            containerLayout.setVisibility(View.VISIBLE);

            ArrayAdapter<String> itemsAdapter =
                    new ArrayAdapter<>(mActivity, R.layout.simple_list_item,
                            Arrays.asList(mActivity.getString(R.string.browse)));
            ListView fakeList = (ListView) importDialog.getCustomView().findViewById(R.id.fake_list);
            fakeList.setVisibility(View.VISIBLE);
            fakeList.setAdapter(itemsAdapter);

            fakeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> list, View view, int position, long id) {
                    openFile();
                }

            });
        }

        return importDialog;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == OPEN_REQUEST_CODE) {
                if (resultData != null) {
                    try {
                        inputStream = mActivity.getContentResolver().openInputStream(resultData.getData());

                        importDialog.dismiss();
                        importDialog = new MaterialDialog.Builder(mActivity)
                                .title(getString(R.string.pref_delete_all_webcams) + "?")
                                .content(R.string.import_summary)
                                .positiveText(R.string.Yes)
                                .negativeText(R.string.No)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        new importJsonBackgroundTask().execute(true, false);
                                    }
                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        new importJsonBackgroundTask().execute(false, false);
                                    }
                                })
                                .show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class importJsonBackgroundTask extends AsyncTask<Boolean, Integer, Long> {

        @Override
        protected Long doInBackground(Boolean... booleans) {

            if (booleans[0]) {
                DeleteAllWebCams.execute(mActivity.getApplicationContext());
            }

            try {
                Gson gson = new Gson();

                BufferedReader bufferedReader;
                if (booleans[1]) {
                    bufferedReader = new BufferedReader(
                            new FileReader(selectedFile));
                }
                else {
                    bufferedReader = new BufferedReader(new InputStreamReader(
                            inputStream));
                }

                List<WebCam> allWebCams = Arrays.asList(gson.fromJson(bufferedReader, WebCam[].class));
                bufferedReader.close();

                maxProgressValue = allWebCams.size();
                showProgressDialog();

                long categoryFromCurrentDate = db.createCategory(new Category("@drawable/icon_imported",
                        mActivity.getString(R.string.imported) + " " + Utils.getDateString()));
                synchronized (sDataLock) {
                    for(WebCam webCam : allWebCams) {
                        db.createWebCam(webCam, new long[] {categoryFromCurrentDate});
                        progressUpdate();
                    }
                }
                db.closeDB();
                BackupManager backupManager = new BackupManager(mActivity);
                backupManager.dataChanged();
                snackBarImportDone();

            } catch (IOException e) {
                e.printStackTrace();
                snackBarImportFailed();
            }

            return null;
        }
    }

    private void showProgressDialog() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                progressDialog = new MaterialDialog.Builder(mActivity)
                        .title(R.string.restore_progress)
                        .content(R.string.please_wait)
                        .progress(false, maxProgressValue)
                        .show();
            }
        });
    }

    private void progressUpdate() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                progressDialog.incrementProgress(1);
            }
        });
    }

    private void snackBarImportDone() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                progressDialog.dismiss();
                Snackbar.with(mActivity)
                        .text(R.string.import_done)
                        .actionLabel(R.string.dismiss)
                        .actionColor(mActivity.getResources().getColor(R.color.yellow))
                        .show(mActivity);
            }
        });
    }

    private void snackBarImportFailed() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                progressDialog.dismiss();
                Snackbar.with(mActivity)
                        .text(R.string.import_failed)
                        .actionLabel(R.string.dismiss)
                        .actionColor(mActivity.getResources().getColor(R.color.yellow))
                        .show(mActivity);
            }
        });
    }
}
