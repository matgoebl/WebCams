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
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.LinkAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.ListImagesLinks;
import cz.yetanotherview.webcamviewer.app.helper.OnTextChange;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.helper.YouTubeIntent;
import cz.yetanotherview.webcamviewer.app.listener.WebCamListener;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.Link;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

/**
 * Add dialog fragment
 */
public class AddDialog extends DialogFragment implements CategoryDialog.Callback, CoordinatesChooserDialog.Callback {

    private Activity mActivity;
    private String mWebCamName, mWebCamUrl, mWebCamThumbUrl;
    private EditText webCamUrlAddStream, webCamThumbUrlAddStream, webCamNameAdd, webCamLatitude, webCamLongitude, webcamUrlAddStill;
    private double mLatitude, mLongitude;
    private boolean mEmpty;
    private WebCam webCam;
    private WebCamListener mOnAddListener;
    private MaterialDialog dialog;
    private TextView webcamUrlTitleAddStill, webCamCategoryButton;
    private StringBuilder selectedCategoriesNames;
    private List<Integer> category_ids;
    private List<Category> allCategories;
    private List<Link> links;
    private CheckBox stillImageDirectCheckBox, shareCheckBox;
    private DatabaseHelper db;

    private RadioButton liveStream;

    public AddDialog() {
    }

    public static AddDialog newInstance(WebCamListener listener) {
        AddDialog frag = new AddDialog();
        frag.setOnAddListener(listener);
        return frag;
    }

    private void setOnAddListener(WebCamListener onAddListener) {
        mOnAddListener = onAddListener;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_first, true)
                .positiveText(R.string.next)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (!liveStream.isChecked()) {
                            openSecondDialogStill();
                        } else openSecondDialogStream();
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        new YouTubeIntent(mActivity, Utils.HELP_MANUALLY_ADDING).open();
                    }
                })
                .autoDismiss(false)
                .build();

        liveStream = (RadioButton) dialog.getCustomView().findViewById(R.id.radioLiveStreamAdd);

        return dialog;
    }

    private void openSecondDialogStill() {
        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_second_still, true)
                .positiveText(R.string.analyze)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mWebCamUrl = webcamUrlAddStill.getText().toString().trim();
                        if (stillImageDirectCheckBox.isChecked()) {
                            openThirdDialog();
                        } else {
                            openAnalyzeDialog();
                        }
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        new YouTubeIntent(mActivity, Utils.HELP_MANUALLY_ADDING).open();
                    }
                })
                .cancelable(false)
                .autoDismiss(false)
                .build();

        webcamUrlTitleAddStill = (TextView) dialog.getCustomView().findViewById(R.id.webcam_url_title_add_still);

        View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webcamUrlAddStill = (EditText) dialog.getCustomView().findViewById(R.id.webcam_url_add_still);
        webcamUrlAddStill.addTextChangedListener(new OnTextChange(positiveAction));

        stillImageDirectCheckBox = (CheckBox) dialog.getCustomView().findViewById(R.id.still_image_direct_checkBox);
        stillImageDirectCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    webcamUrlTitleAddStill.setText(R.string.enter_direct_url);
                    webcamUrlAddStill.setHint(R.string.hint_webcam_url);
                    dialog.setActionButton(DialogAction.POSITIVE, R.string.next);
                } else {
                    webcamUrlTitleAddStill.setText(R.string.enter_page_for_analyzing);
                    webcamUrlAddStill.setHint(R.string.hint_webcam_analyze);
                    dialog.setActionButton(DialogAction.POSITIVE, R.string.analyze);
                }
            }
        });

        dialog.show();
    }

    private void openAnalyzeDialog() {
        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.analyzing)
                .content(R.string.please_wait)
                .progress(true, 0)
                .cancelable(false)
                .show();

        new runAnalyzeBackgroundTask().execute();

    }

    private class runAnalyzeBackgroundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                links = ListImagesLinks.main(mWebCamUrl);
            } catch (IOException e) {
                links = new ArrayList<>();
                //ToDo:
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            openResultDialog(); // ToDO:
        }
    }

    private void openResultDialog() {
        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.select_webcam)
                .adapter(new LinkAdapter(mActivity, links),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                Link link = links.get(which);
                                mWebCamUrl = link.getUrl();
                                openThirdDialog();
                                dialog.dismiss();
                            }
                        })
                .build();

        dialog.show();
    }

    private void openSecondDialogStream() {
        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_second_stream, true)
                .positiveText(R.string.next)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mWebCamUrl = webCamUrlAddStream.getText().toString().trim();
                        mWebCamThumbUrl = webCamThumbUrlAddStream.getText().toString().trim();
                        openThirdDialog();
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        new YouTubeIntent(mActivity, Utils.HELP_MANUALLY_ADDING).open();
                    }
                })
                .cancelable(false)
                .autoDismiss(false)
                .build();

        View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webCamUrlAddStream = (EditText) dialog.getCustomView().findViewById(R.id.webcam_url_add_stream);

        webCamThumbUrlAddStream = (EditText) dialog.getCustomView().findViewById(R.id.webcam_thumb_url_add_stream);
        webCamThumbUrlAddStream.addTextChangedListener(new OnTextChange(positiveAction));

        dialog.show();
    }

    private void openThirdDialog() {
        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_third, true)
                .positiveText(R.string.next)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mWebCamName = webCamNameAdd.getText().toString().trim();
                        getAndCheckLatLong();
                        openFourthDialog();
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        new YouTubeIntent(mActivity, Utils.HELP_MANUALLY_ADDING).open();
                    }
                })
                .cancelable(false)
                .autoDismiss(false)
                .build();

        View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webCamNameAdd = (EditText) dialog.getCustomView().findViewById(R.id.webcam_name_add);
        webCamNameAdd.addTextChangedListener(new OnTextChange(positiveAction));

        webCamLatitude = (EditText) dialog.getCustomView().findViewById(R.id.webcam_latitude_add);
        webCamLongitude = (EditText) dialog.getCustomView().findViewById(R.id.webcam_longitude_add);

        ImageView mWebCamCoordinatesMapSelector = (ImageView) dialog.getCustomView().findViewById(R.id.webcam_coordinates_map_selector_add);
        mWebCamCoordinatesMapSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = mActivity.getFragmentManager();
                DialogFragment dialogFragment = new CoordinatesChooserDialog();

                Bundle args = new Bundle();
                getAndCheckLatLong();
                args.putBoolean("empty", mEmpty);
                args.putDouble("latitude", mLatitude);
                args.putDouble("longitude", mLongitude);
                dialogFragment.setArguments(args);

                dialogFragment.setTargetFragment(AddDialog.this, 0);
                dialogFragment.show(fm, "CoordinatesChooserDialog");
            }
        });

        dialog.show();
    }

    private void openFourthDialog() {
        dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_fourth, true)
                .positiveText(R.string.dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        boolean shareIsChecked = false; //ToDo!!!
                        getAndCheckLatLong();

                        webCam = new WebCam(
                                liveStream.isChecked(),
                                mWebCamName,
                                mWebCamUrl,
                                mWebCamThumbUrl,
                                0, 0, mLatitude, mLongitude, new Date());

//                        if (shareCheckBox.isChecked()) { //ToDo!!!
//                            shareIsChecked = true;
//                        }

                        if (mOnAddListener != null) {
                            mOnAddListener.webCamAdded(webCam, category_ids, shareIsChecked); //ToDo!!!
                        }
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        new YouTubeIntent(mActivity, Utils.HELP_MANUALLY_ADDING).open();
                    }
                })
                .cancelable(false)
                .autoDismiss(false)
                .build();

        db = new DatabaseHelper(mActivity);
        allCategories = db.getAllCategories();
        db.closeDB();

        webCamCategoryButton = (TextView) dialog.getCustomView().findViewById(R.id.webcam_category_button_add);
        webCamCategoryButton.setText(R.string.select_categories);

        webCamCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = mActivity.getFragmentManager();
                DialogFragment dialogFragment = new CategoryDialog();
                Bundle args = new Bundle();
                args.putIntegerArrayList("category_ids", (ArrayList<Integer>) category_ids);
                dialogFragment.setArguments(args);
                dialogFragment.setTargetFragment(AddDialog.this, 0);
                dialogFragment.show(fm, "CategoryDialog");
            }
        });

        dialog.show();
    }

    private void getAndCheckLatLong() {
        mEmpty = false;
        String latitudeStr = webCamLatitude.getText().toString();
        if (latitudeStr.isEmpty()) {
            mEmpty = true;
            mLatitude = 0.0;
        }
        else mLatitude = Double.parseDouble(latitudeStr);

        String longitudeStr = webCamLongitude.getText().toString();
        if (longitudeStr.isEmpty()) {
            mEmpty = true;
            mLongitude = 0.0;
        }
        else mLongitude = Double.parseDouble(longitudeStr);
    }

    @Override
    public void onCategorySave(List<Integer> new_category_ids) {
        proceedAssigned(new_category_ids);
        setText();
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

    private void proceedAssigned(List<Integer> new_webCam_category_ids) {
        allCategories = db.getAllCategories();
        db.closeDB();
        selectedCategoriesNames = new StringBuilder();
        for (Category category : allCategories) {
            if (new_webCam_category_ids.contains(category.getId())) {
                if (selectedCategoriesNames.length() > 0) {
                    selectedCategoriesNames.append(", ");
                }
                selectedCategoriesNames.append(category.getCategoryName());
            }
        }
        category_ids = new_webCam_category_ids;
    }

    @Override
    public void onCoordinatesSave(String latitude, String longitude) {
        webCamLatitude.setText(latitude);
        webCamLongitude.setText(longitude);
    }
}