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
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.nispok.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;
import cz.yetanotherview.webcamviewer.app.adapter.SpinnerAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class ExportDialog extends DialogFragment {

    private View positiveAction;

    private String inputName;
    private EditText input;
    private List<WebCam> allWebCams;
    private MaterialDialog dialog;

    private static final int CREATE_REQUEST_CODE = 40;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());

        allWebCams = db.getAllWebCams("id ASC");
        db.closeDB();


        if (allWebCams.size() != 0) {

            dialog = new MaterialDialog.Builder(getActivity())
                    .title(R.string.pref_backup)
                    .customView(R.layout.export_dialog, true)
                    .positiveText(android.R.string.ok)
                    .negativeText(android.R.string.cancel)
                    .autoDismiss(false) //ToDo...!!!
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            //inputName = input.getText().toString().trim();
                            //exportJson(inputName);
                            dialog.dismiss();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            dialog.dismiss();
                        }
                    }).build();

            input = (EditText) dialog.getCustomView().findViewById(R.id.input_name);
            input.requestFocus();
            input.setText(Utils.getCustomDateString("yyyy-MM-dd_HH-mm"));


            String[] objects;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                objects = getResources().getStringArray(R.array.backup_values);
            }
            else objects = getResources().getStringArray(R.array.backup_values_pre_kk);

            Spinner spinner = (Spinner) dialog.getCustomView().findViewById(R.id.backup_spinner);
            spinner.setAdapter(new SpinnerAdapter(getActivity(), R.layout.spinner_item,objects));

            positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    positiveAction.setEnabled(s.toString().trim().length() > 0);
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
        else dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.nothing_to_export)
                .content(R.string.nothing_to_export_summary)
                .positiveText(android.R.string.ok)
                .build();

        return dialog;
    }

    private void exportJson(String fileName) {

        File exportDirectory = new File(Utils.folderWCVPath);

        if (!exportDirectory.exists()) {
            exportDirectory.mkdir();
        }

        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canWrite()) {
                Gson gson = new Gson();
                String json = gson.toJson(allWebCams);

                FileWriter writer = new FileWriter(Utils.folderWCVPath + fileName + Utils.extension);
                writer.write(json);
                writer.close();

                Snackbar.with(getActivity().getApplicationContext())
                        .text(R.string.export_done)
                        .actionLabel(R.string.dismiss)
                        .actionColor(getResources().getColor(R.color.yellow))
                        .show(getActivity());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Snackbar.with(getActivity().getApplicationContext())
                    .text(R.string.export_failed)
                    .actionLabel(R.string.dismiss)
                    .actionColor(getResources().getColor(R.color.yellow))
                    .show(getActivity());
        }
    }

    // ToDo: !!!! Finish implementation and make visible only on KitKat UP!
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void newFile() {


        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_TITLE, "Example.wcv");

        startActivityForResult(intent, CREATE_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == CREATE_REQUEST_CODE)
            {
                if (resultData != null) {
                    Log.d("","CREATE_REQUEST_CODE");
                    Uri currentUri = resultData.getData();
                    writeFileContent(currentUri);
                }
            }
        }
    }

        private void writeFileContent(Uri uri) {
            try{
                ParcelFileDescriptor pfd =
                        getActivity().getContentResolver().
                                openFileDescriptor(uri, "w");

                FileOutputStream fileOutputStream =
                        new FileOutputStream(pfd.getFileDescriptor());

                Gson gson = new Gson();
                String json = gson.toJson(allWebCams);

                fileOutputStream.write(json.getBytes());

                fileOutputStream.close();
                pfd.close();
                dialog.dismiss();
            } catch (IOException e) {
                e.printStackTrace();
        }
    }
}
