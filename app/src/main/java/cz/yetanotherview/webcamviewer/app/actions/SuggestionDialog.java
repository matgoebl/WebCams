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

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;

import junit.framework.Assert;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.actions.simple.UnavailableDialog;
import cz.yetanotherview.webcamviewer.app.helper.IsValidEmail;
import cz.yetanotherview.webcamviewer.app.helper.OnTextChange;
import cz.yetanotherview.webcamviewer.app.helper.PerformPostCall;

public class SuggestionDialog extends DialogFragment {

    private String inputSuggestion, inputEmail;
    private EditText mSuggestion, mEmail;
    private View positiveAction;

    private Activity mActivity;
    private MaterialDialog dialog, progressDialog;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.submit_suggestion)
                .customView(R.layout.suggestion_layout, false)
                .positiveText(R.string.send)
                .negativeText(android.R.string.cancel)
                .showListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.showSoftInput(mSuggestion, InputMethodManager.SHOW_IMPLICIT);
                    }
                })
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        inputSuggestion = mSuggestion.getText().toString().trim();
                        if (mEmail.isShown()) {
                            inputEmail = mEmail.getText().toString().trim();
                        } else inputEmail = "none";
                        showProgressDialog();
                        new createSuggestion().execute();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        mSuggestion = (EditText) dialog.findViewById(R.id.suggestion_input);
        mSuggestion.setHint(R.string.submit_suggestion_hint);
        mSuggestion.requestFocus();

        CheckBox suggestionCheckBox = (CheckBox) dialog.findViewById(R.id.suggestion_checkbox);
        suggestionCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEmail.setVisibility(View.VISIBLE);
                    if (mEmail.getText().toString().isEmpty()) {
                        positiveAction.setEnabled(false);
                    }
                    else positiveAction.setEnabled(true);
                    mEmail.addTextChangedListener(new IsValidEmail(positiveAction));
                } else {
                    mEmail.setVisibility(View.GONE);
                    if (!mSuggestion.getText().toString().isEmpty()) {
                        positiveAction.setEnabled(true);
                    }
                    else positiveAction.setEnabled(false);
                    mEmail.removeTextChangedListener(new IsValidEmail(positiveAction));
                }
            }
        });

        mEmail = (EditText) dialog.findViewById(R.id.suggestion_email);
        mEmail.setHint(R.string.email_hint);

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        mSuggestion.addTextChangedListener(new OnTextChange(positiveAction));
        positiveAction.setEnabled(false);

        return dialog;
    }

    private void showProgressDialog() {
        progressDialog = new MaterialDialog.Builder(mActivity)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();
    }

    private class createSuggestion extends AsyncTask<String, String, String> {

        protected String doInBackground(String... args) {

            String url = Utils.JSON_FILE_URL_SEND_SUGGESTION;
            String reason = "New Suggestion";

            try {
                HttpURLConnection urlConn = (HttpURLConnection) new URL(url).openConnection();
                urlConn.connect();
                Assert.assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());

                HashMap<String , String> postDataParams = new HashMap<>();
                postDataParams.put("subject", reason);
                postDataParams.put("suggestion", inputSuggestion);
                postDataParams.put("userEmail", inputEmail);
                new PerformPostCall().performPostCall(url, postDataParams);
                dialog.dismiss();
                progressDialog.dismiss();
                sent();
            }
            catch (IOException e) {
                System.err.println("Error creating HTTP connection");
                this.publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... args) {
            super.onProgressUpdate(args);
            progressDialog.dismiss();
            new UnavailableDialog().show(mActivity.getFragmentManager(), "UnavailableDialog");
        }
    }

    private void sent() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Snackbar.with(mActivity)
                        .text(R.string.sent)
                        .actionLabel(R.string.dismiss)
                        .actionColor(mActivity.getResources().getColor(R.color.yellow))
                        .show(mActivity);
            }
        });
    }
}
