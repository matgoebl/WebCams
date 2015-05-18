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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.CategorySelectionAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.OnTextChange;
import cz.yetanotherview.webcamviewer.app.listener.WebCamListener;
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
    private TextView mWebCamThumbUrlTitle, webCamCategoryButton;
    private CategorySelectionAdapter categorySelectionAdapter;
    private DatabaseHelper db;
    private StringBuilder selectedCategoriesNames;
    private List<Category> allCategories;
    private List<Long> category_ids;
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

        db = new DatabaseHelper(getActivity());
        webCam = db.getWebCam(id);
        allCategories = db.getAllCategories();
        List<Long> webCam_category_ids = db.getWebCamCategoriesIds(webCam.getId());
        selectedCategoriesNames = new StringBuilder();
        for (Category category : allCategories) {
            if (webCam_category_ids.contains(category.getId())) {
                category.setSelected(true);

                if (selectedCategoriesNames.length() > 0) {
                    selectedCategoriesNames.append(", ");
                }
                selectedCategoriesNames.append(category.getCategoryName());
            }
        }
        db.closeDB();
        category_ids = webCam_category_ids;

        pos = webCam.getPosition();
        status = webCam.getStatus();

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.action_edit)
                .customView(R.layout.edit_webcam_dialog, true)
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

        webCamCategoryButton = (TextView) dialog.getCustomView().findViewById(R.id.webcam_category_button);

        if (webCam_category_ids.size() == 0) {
            webCamCategoryButton.setText(R.string.select_categories);
        }
        else {
            webCamCategoryButton.setText(selectedCategoriesNames);
        }
        webCamCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.webcam_category)
                        .customView(R.layout.category_selection_dialog, false)
                        .positiveText(R.string.dialog_positive_text)
                        .negativeText(android.R.string.cancel)
                        .neutralText(R.string.action_new)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                selectedCategoriesNames = new StringBuilder();
                                category_ids = new ArrayList<>();
                                for (Category category : allCategories) {
                                    if (category.isSelected()) {
                                        category_ids.add(category.getId());

                                        if (selectedCategoriesNames.length() > 0) {
                                            selectedCategoriesNames.append(", ");
                                        }
                                        selectedCategoriesNames.append(category.getCategoryName());
                                    }
                                }
                                if (category_ids.size() == 0) {
                                    webCamCategoryButton.setText(R.string.select_categories);
                                }
                                else {
                                    webCamCategoryButton.setText(selectedCategoriesNames);
                                }

                                positiveAction.setEnabled(true);
                                dialog.dismiss();
                            }
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                new AddCategoryDialog().show(getFragmentManager(), "AddCategoryDialog");
                            }
                        })
                        .autoDismiss(false)
                        .build();

                ListView categorySelectionList = (ListView) dialog.getCustomView().findViewById(R.id.category_list_view);
                categorySelectionList.setEmptyView(dialog.getCustomView().findViewById(R.id.no_categories));
                categorySelectionAdapter = new CategorySelectionAdapter(getActivity(), allCategories);
                categorySelectionList.setAdapter(categorySelectionAdapter);

                dialog.show();
            }

        });

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

    public void addCategoryInAdapter(Category category) {
        categorySelectionAdapter.addItem(categorySelectionAdapter.getCount(), category);
    }

    public void editCategoryInAdapter(int position, Category category) {
        categorySelectionAdapter.modifyItem(position, category);
    }

    public void deleteCategoryInAdapter(int position) {
        categorySelectionAdapter.removeItem(position);
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
