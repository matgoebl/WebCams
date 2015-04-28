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
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.OnTextChange;
import cz.yetanotherview.webcamviewer.app.helper.WebCamListener;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

/**
 * Edit dialog fragment
 */
public class EditDialog extends DialogFragment implements View.OnClickListener {

    private EditText mWebCamName, mWebCamUrl, mWebCamThumbUrl, mWebCamLatitude, mWebCamLongitude;
    private WebCam webCam;
    private WebCamListener mOnAddListener;
    private View positiveAction;
    private TextView mWebCamThumbUrlTitle;

    private List<Category> allCategories;
    private Category category;

    private Button webCamCategoryButton;
    private String[] items;
    private long[] category_ids;
    private Integer[] checked;
    private RadioButton liveStream;

    private int pos, status, position;

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
        long id = bundle.getLong("id", 0);
        position = bundle.getInt("position", 0);

        DatabaseHelper db = new DatabaseHelper(getActivity());
        webCam = db.getWebCam(id);
        allCategories = db.getAllCategories();
        long[] webCam_category_ids = db.getWebCamCategoriesIds(webCam.getId());
        category_ids = webCam_category_ids;
        db.closeDB();

        pos = webCam.getPosition();
        status = webCam.getStatus();

        long[] ids = new long[allCategories.size()];
        items = new String[allCategories.size()];
        int count = 0;
        for (Category category : allCategories) {
            ids [count] = category.getId();
            items[count] = category.getCategoryName();
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
                        webCam.setIsStream(liveStream.isChecked());
                        webCam.setName(mWebCamName.getText().toString().trim());
                        webCam.setUrl(mWebCamUrl.getText().toString().trim());
                        if (liveStream.isChecked()) {
                            webCam.setThumbUrl(mWebCamThumbUrl.getText().toString().trim());
                        }
                        else webCam.setThumbUrl(webCam.getThumbUrl());
                        webCam.setPosition(pos);
                        webCam.setStatus(status);
                        webCam.setLatitude(Double.parseDouble(mWebCamLatitude.getText().toString().trim()));
                        webCam.setLongitude(Double.parseDouble(mWebCamLongitude.getText().toString().trim()));

                        if (mOnAddListener != null) {
                            mOnAddListener.webCamEdited(position, webCam, category_ids);
                        }
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        if (mOnAddListener != null)
                            mOnAddListener.webCamDeleted(webCam, position);
                    }
                }).build();

        RadioButton stillImage = (RadioButton) dialog.getCustomView().findViewById(R.id.radioStillImage);
        liveStream = (RadioButton) dialog.getCustomView().findViewById(R.id.radioLiveStream);

        mWebCamName = (EditText) dialog.getCustomView().findViewById(R.id.webcam_name);
        mWebCamName.setText(webCam.getName());
        mWebCamName.requestFocus();

        mWebCamUrl = (EditText) dialog.getCustomView().findViewById(R.id.webcam_url);
        mWebCamUrl.setText(webCam.getUrl());

        mWebCamThumbUrlTitle = (TextView) dialog.getCustomView().findViewById(R.id.webcam_thumb_url_title);
        mWebCamThumbUrl = (EditText) dialog.getCustomView().findViewById(R.id.webcam_thumb_url);
        mWebCamThumbUrl.setText(webCam.getThumbUrl());

        mWebCamLatitude = (EditText) dialog.getCustomView().findViewById(R.id.webcam_latitude);
        mWebCamLatitude.setText(String.valueOf(webCam.getLatitude()));

        mWebCamLongitude = (EditText) dialog.getCustomView().findViewById(R.id.webcam_longitude);
        mWebCamLongitude.setText(String.valueOf(webCam.getLongitude()));

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
                            .items(items)
                            .itemsCallbackMultiChoice(checked, new MaterialDialog.ListCallbackMultiChoice() {
                                @Override
                                public boolean onSelection(MaterialDialog multiDialog, Integer[] which, CharSequence[] text) {

                                    if (which != null && which.length != 0) {
                                        StringBuilder str = new StringBuilder();

                                        category_ids = new long[which.length];
                                        int count = 0;

                                        for (Integer aWhich : which) {
                                            category = allCategories.get(aWhich);

                                            category_ids[count] = category.getId();
                                            count++;

                                            str.append("[");
                                            str.append(category.getCategoryName());
                                            str.append("] ");
                                        }
                                        webCamCategoryButton.setText(str);
                                    } else {
                                        category_ids = null;
                                        webCamCategoryButton.setText(R.string.category_array_choose);
                                    }
                                    checked = which;
                                    positiveAction.setEnabled(true);

                                    return true;
                                }
                            })
                            .positiveText(R.string.choose)
                            .show();
                }

            });
        }

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        if (webCam.isStream()) {
            liveStream.setChecked(true);
            mWebCamThumbUrlTitle.setVisibility(View.VISIBLE);
            mWebCamThumbUrl.setVisibility(View.VISIBLE);
        }
        else stillImage.setChecked(true);

        stillImage.setOnClickListener(this);
        liveStream.setOnClickListener(this);
        mWebCamName.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamUrl.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamThumbUrl.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamLatitude.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamLongitude.addTextChangedListener(new OnTextChange(positiveAction));

        positiveAction.setEnabled(false);

        return dialog;
    }

    @Override
    public void onClick(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.radioStillImage:
                if (checked) {
                    positiveAction.setEnabled(true);
                    mWebCamThumbUrlTitle.setVisibility(View.GONE);
                    mWebCamThumbUrl.setVisibility(View.GONE);
                }
                break;
            case R.id.radioLiveStream:
                if (checked) {
                    if (mWebCamThumbUrl.getText().toString().trim().length() != 0) {
                        positiveAction.setEnabled(true);
                    } else positiveAction.setEnabled(false);
                    mWebCamThumbUrlTitle.setVisibility(View.VISIBLE);
                    mWebCamThumbUrl.setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}
