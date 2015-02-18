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
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.WebCamListener;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

/**
 * Edit dialog fragment
 */
public class EditDialog extends DialogFragment {

    private EditText mWebCamName;
    private EditText mWebCamUrl;
    private WebCam webCam;
    private WebCamListener mOnAddListener;
    private View positiveAction;

    private List<Category> allCategories;
    private Category category;

    private CheckBox shareCheckBox;
    private Button reportButton;

    private Button webCamCategoryButton;
    private String[] items;
    private long[] category_ids;
    private long[] webCam_category_ids;
    private Integer[] checked;

    private long id;
    private int pos;
    private int status;

    private int position;

    public EditDialog() {
    }

    public static EditDialog newInstance(WebCamListener listener) {
        EditDialog frag = new EditDialog();
        frag.setOnAddListener(listener);
        return frag;
    }

    private void setOnAddListener(WebCamListener onAddListener) {
        mOnAddListener = onAddListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        id = bundle.getLong("id", 0);
        position = bundle.getInt("position", 0);

        DatabaseHelper db = new DatabaseHelper(getActivity());
        webCam = db.getWebCam(id);
        allCategories = db.getAllCategories();
        webCam_category_ids = db.getWebCamCategoriesIds(webCam.getId());
        category_ids = webCam_category_ids;
        db.closeDB();

        long uniId = webCam.getUniId();
        pos = webCam.getPosition();
        status = webCam.getStatus();

        long[] ids = new long[allCategories.size()];
        items = new String[allCategories.size()];
        int count = 0;
        for (Category category : allCategories) {
            ids [count] = category.getId();
            items[count] = category.getcategoryName();
            count++;
        }

        checked = new Integer[webCam_category_ids.length];
        StringBuilder checkedNames = new StringBuilder();
        int count2 = 0;
        for (int i=0; i < ids.length; i++) {
            for (long webCam_category_id : webCam_category_ids) {
                if (ids[i] == webCam_category_id) {
                    checkedNames.append("[");
                    checkedNames.append(items[i]);
                    checkedNames.append("] ");

                    checked[count2] = i;
                    count2++;
                }
            }
        }

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.action_edit)
                .customView(R.layout.add_edit_dialog, true)
                .positiveText(R.string.dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.action_delete)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        boolean shareIsChecked = false;

                        webCam.setName(mWebCamName.getText().toString().trim());
                        webCam.setUrl(mWebCamUrl.getText().toString().trim());
                        webCam.setPosition(pos);
                        webCam.setStatus(status);

                        if (shareCheckBox.isChecked()) {
                            shareIsChecked = true;
                        }

                        if (mOnAddListener != null) {
                            mOnAddListener.webCamEdited(position, webCam, category_ids, shareIsChecked);
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        if (mOnAddListener != null)
                            mOnAddListener.webCamDeleted(id, position);
                    }
                }).build();

        shareCheckBox = (CheckBox) dialog.getCustomView().findViewById(R.id.shareCheckBox);
        reportButton = (Button) dialog.getCustomView().findViewById(R.id.reportButton);
        if (uniId != 0) {
            shareCheckBox.setVisibility(View.GONE);
            reportButton.setVisibility(View.VISIBLE);
        }
        else {
            shareCheckBox.setVisibility(View.VISIBLE);
        }

        mWebCamName = (EditText) dialog.getCustomView().findViewById(R.id.webcam_name);
        mWebCamName.setText(webCam.getName());
        mWebCamName.requestFocus();

        mWebCamUrl = (EditText) dialog.getCustomView().findViewById(R.id.webcam_url);
        mWebCamUrl.setText(webCam.getUrl());

        webCamCategoryButton = (Button) dialog.getCustomView().findViewById(R.id.webcam_category_button);
        if (allCategories.size() == 0 ) {
            webCamCategoryButton.setText(R.string.category_array_empty);
        }
        else {
            if (webCam_category_ids.length == 0) {
                webCamCategoryButton.setText(R.string.category_array_choose);
            }
            else webCamCategoryButton.setText(checkedNames);

            webCamCategoryButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.webcam_category)
                            .autoDismiss(false)
                            .items(items)
                            .itemsCallbackMultiChoice(checked, new MaterialDialog.ListCallbackMulti() {
                                @Override
                                public void onSelection(MaterialDialog multiDialog, Integer[] which, CharSequence[] text) {
                                }
                            })
                            .positiveText(R.string.choose)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog multiDialog) {
                                    Integer[] which = multiDialog.getSelectedIndices();

                                    if (which != null && which.length != 0) {
                                        StringBuilder str = new StringBuilder();

                                        category_ids = new long[which.length];
                                        int count = 0;

                                        for (Integer aWhich : which) {
                                            category = allCategories.get(aWhich);

                                            category_ids[count] = category.getId();
                                            count++;

                                            str.append("[");
                                            str.append(category.getcategoryName());
                                            str.append("] ");
                                        }
                                        webCamCategoryButton.setText(str);
                                    } else {
                                        category_ids = null;
                                        webCamCategoryButton.setText(R.string.category_array_choose);
                                    }
                                    checked = which;
                                    positiveAction.setEnabled(true);

                                    multiDialog.dismiss();
                                }
                            })
                            .show();
                }

            });
        }

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        mWebCamName.addTextChangedListener(new TextWatcher() {
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
        mWebCamUrl.addTextChangedListener(new TextWatcher() {
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
        shareCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
               if (isChecked) {
                   positiveAction.setEnabled(true);
               }
               else positiveAction.setEnabled(false);
           }
        });
        reportButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", "cz840311@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Something is wrong");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "WebCam UniID: " + webCam.getUniId()
                        + "\n" + "WebCam Name: " + webCam.getName());

                List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities(emailIntent, 0);
                if(list.isEmpty()) {
                    noEmailClientsFound();
                }
                else {
                    try {
                        startActivity(Intent.createChooser(emailIntent, getString(R.string.send_via_email)));
                    } catch (android.content.ActivityNotFoundException ex) {
                        noEmailClientsFound();
                    }
                }
            }

        });

        positiveAction.setEnabled(false);

        return dialog;
    }

    private void noEmailClientsFound() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.oops)
                .content(getString(R.string.no_email_clients_installed))
                .positiveText(android.R.string.ok)
                .show();
    }
}
