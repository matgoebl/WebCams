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
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.OnTextChange;
import cz.yetanotherview.webcamviewer.app.listener.WebCamListener;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class EditDialog extends DialogFragment implements View.OnClickListener, CategoryDialog.Callback, CoordinatesChooserDialog.Callback {

    private DatabaseHelper db;
    private EditText mWebCamName, mWebCamUrl, mWebCamThumbUrl, mWebCamLatitude, mWebCamLongitude;
    private Double mLatitude, mLongitude;
    private boolean mEmpty;
    private WebCam webCam;
    private WebCamListener mOnAddListener;
    private View positiveAction;
    private TextView mWebCamThumbUrlTitle, webCamCategoryButton;
    private StringBuilder selectedCategoriesNames;
    private List<Integer> category_ids;
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
        proceedAssigned(getIdsFromDb());

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
                        getAndCheckLatLong();
                        webCam.setLatitude(mLatitude);
                        webCam.setLongitude(mLongitude);

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

        RadioButton stillImage = (RadioButton) dialog.findViewById(R.id.radioStillImage);
        liveStream = (RadioButton) dialog.findViewById(R.id.radioLiveStream);

        mWebCamName = (EditText) dialog.findViewById(R.id.webcam_name);
        mWebCamName.setText(webCam.getName());
        mWebCamName.requestFocus();

        TextView mWebCamUrlTitle = (TextView) dialog.findViewById(R.id.webcam_url_title);
        mWebCamUrl = (EditText) dialog.findViewById(R.id.webcam_url);
        mWebCamUrl.setText(webCam.getUrl());

        mWebCamThumbUrlTitle = (TextView) dialog.findViewById(R.id.webcam_thumb_url_title);
        mWebCamThumbUrl = (EditText) dialog.findViewById(R.id.webcam_thumb_url);
        mWebCamThumbUrl.setText(webCam.getThumbUrl());

        mWebCamLatitude = (EditText) dialog.findViewById(R.id.webcam_latitude);
        mWebCamLatitude.setText(String.valueOf(webCam.getLatitude()));

        mWebCamLongitude = (EditText) dialog.findViewById(R.id.webcam_longitude);
        mWebCamLongitude.setText(String.valueOf(webCam.getLongitude()));

        ImageView mWebCamCoordinatesMapSelector = (ImageView) dialog.findViewById(R.id.webcam_coordinates_map_selector);
        mWebCamCoordinatesMapSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getFragmentManager();
                DialogFragment dialogFragment = new CoordinatesChooserDialog();

                Bundle args = new Bundle();
                getAndCheckLatLong();
                args.putBoolean("empty", mEmpty);
                args.putDouble("latitude", mLatitude);
                args.putDouble("longitude", mLongitude);
                dialogFragment.setArguments(args);

                dialogFragment.setTargetFragment(EditDialog.this, 0);
                dialogFragment.show(fm, "CoordinatesChooserDialog");
            }
        });

        webCamCategoryButton = (TextView) dialog.findViewById(R.id.webcam_category_button);
        if (getIdsFromDb().size() == 0) {
            webCamCategoryButton.setText(R.string.select_categories);
        }
        else {
            webCamCategoryButton.setText(selectedCategoriesNames);
        }
        webCamCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                DialogFragment dialogFragment = new CategoryDialog();

                Bundle args = new Bundle();
                args.putIntegerArrayList("category_ids", (ArrayList<Integer>) category_ids);
                dialogFragment.setArguments(args);

                dialogFragment.setTargetFragment(EditDialog.this, 0);
                dialogFragment.show(fm, "CategoryDialog");
            }
        });

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        if (webCam.isStream()) {
            liveStream.setChecked(true);
            mWebCamThumbUrlTitle.setVisibility(View.VISIBLE);
            mWebCamThumbUrl.setVisibility(View.VISIBLE);
        }
        else stillImage.setChecked(true);

        if (webCam.getUniId() != 0) {
            if (webCam.isStream()) {
                stillImage.setEnabled(false);
            }
            else liveStream.setEnabled(false);
            mWebCamUrlTitle.setVisibility(View.GONE);
            mWebCamUrl.setVisibility(View.GONE);
            mWebCamThumbUrlTitle.setVisibility(View.GONE);
            mWebCamThumbUrl.setVisibility(View.GONE);
        }
        else {
            stillImage.setOnClickListener(this);
            liveStream.setOnClickListener(this);
        }

        mWebCamName.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamUrl.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamThumbUrl.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamLatitude.addTextChangedListener(new OnTextChange(positiveAction));
        mWebCamLongitude.addTextChangedListener(new OnTextChange(positiveAction));

        positiveAction.setEnabled(false);

        return dialog;
    }

    private List<Integer> getIdsFromDb() {
        List<Integer> webCam_category_ids = db.getWebCamCategoriesIds(webCam.getId());
        db.closeDB();
        return webCam_category_ids;
    }

    private void proceedAssigned(List<Integer> new_webCam_category_ids) {
        List<Category> allCategories = db.getAllCategories();
        db.closeDB();
        selectedCategoriesNames = new StringBuilder();
        for (Category category : allCategories) {
            if (new_webCam_category_ids.contains(category.getId())) {
                category.setSelected(true);

                if (selectedCategoriesNames.length() > 0) {
                    selectedCategoriesNames.append(", ");
                }
                selectedCategoriesNames.append(category.getCategoryName());
            }
        }
        category_ids = new_webCam_category_ids;
    }

    private void getAndCheckLatLong() {
        mEmpty = false;
        String latitudeStr = mWebCamLatitude.getText().toString();
        if (latitudeStr.isEmpty()) {
            mEmpty = true;
            mLatitude = 0.0;
        }
        else mLatitude = Double.parseDouble(latitudeStr);

        String longitudeStr = mWebCamLongitude.getText().toString();
        if (longitudeStr.isEmpty()) {
            mEmpty = true;
            mLongitude = 0.0;
        }
        else mLongitude = Double.parseDouble(longitudeStr);

        if (mLatitude + mLongitude == 0) {
            mEmpty = true;
        }
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

    @Override
    public void onCategorySave(List<Integer> new_category_ids) {
        proceedAssigned(new_category_ids);
        setText();
        positiveAction.setEnabled(true);
    }

    @Override
    public void onUpdate(List<Integer> new_category_ids) {
        proceedAssigned(new_category_ids);
        setText();
    }

    private void setText() {
        if (category_ids.size() == 0) {
            webCamCategoryButton.setText(R.string.select_categories);
        }
        else {
            webCamCategoryButton.setText(selectedCategoriesNames);
        }
    }

    @Override
    public void onCoordinatesSave(String latitude, String longitude) {
        mWebCamLatitude.setText(latitude);
        mWebCamLongitude.setText(longitude);
    }
}
