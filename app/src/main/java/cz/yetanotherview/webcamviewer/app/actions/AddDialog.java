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
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.simple.UnavailableDialog;
import cz.yetanotherview.webcamviewer.app.adapter.LinkAdapter;
import cz.yetanotherview.webcamviewer.app.analyzer.AnalyzerCallback;
import cz.yetanotherview.webcamviewer.app.analyzer.Analyzer;
import cz.yetanotherview.webcamviewer.app.helper.ConnectionTester;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.ImageTester;
import cz.yetanotherview.webcamviewer.app.helper.OnTextChange;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.helper.YouTubeIntent;
import cz.yetanotherview.webcamviewer.app.listener.ConnectionTesterListener;
import cz.yetanotherview.webcamviewer.app.listener.WebCamListener;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.Link;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

/**
 * Add dialog fragment
 */
public class AddDialog extends DialogFragment implements CategoryDialog.Callback, CoordinatesChooserDialog.Callback {

    private Activity mActivity;
    private String mWebCamName, mWebCamUrl, mWebCamThumbUrl, analyzingTitle;
    private EditText webCamUrlAddStream, webCamThumbUrlAddStream, webCamNameAdd, webCamLatitude, webCamLongitude, webcamUrlAddStill;
    private double mLatitude, mLongitude;
    private boolean mEmpty;
    private int taskResult;
    private WebCamListener mOnAddListener;
    private MaterialDialog materialDialog, progressDialog;
    private TextView webcamUrlTitleAddStill, webCamCategoryButton, progressText;
    private StringBuilder selectedCategoriesNames;
    private List<Integer> category_ids;
    private List<Category> allCategories;
    private List<Link> imageLinks;
    private CheckBox stillImageDirectCheckBox;
    private DatabaseHelper db;
    private RadioButton liveStream;
    private Analyzer analyzer;

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

        Bundle bundle = this.getArguments();
        String sharedUrl;
        if (bundle != null) {
            sharedUrl = bundle.getString("sharedUrl", null);
        }
        else sharedUrl = null;

        analyzingTitle = mActivity.getString(R.string.analyzing) + " (BETA)";
        imageLinks = new ArrayList<>();
        analyzer = new Analyzer(mActivity, mCallback);

        if (sharedUrl == null) {
            materialDialog = new MaterialDialog.Builder(mActivity)
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

            liveStream = (RadioButton) materialDialog.findViewById(R.id.radioLiveStreamAdd);
        }
        else {
            mWebCamUrl = sharedUrl;
            materialDialog = new MaterialDialog.Builder(mActivity)
                    .content(R.string.please_wait)
                    .progress(true, 0)
                    .build();
            new SharedUrlAsyncTask().execute();
        }

        return materialDialog;
    }

    private class SharedUrlAsyncTask extends AsyncTask<Void, Void, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {

            if (Patterns.WEB_URL.matcher(mWebCamUrl).matches()) {
                checkIfUrlStartsWithHttp();
                if (ConnectionTester.isConnected(mActivity)) {
                    taskResult = 3;
                } else {
                    taskResult = 2;
                }
            } else {
                taskResult = 1;
            }
            return taskResult;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            switch (result) {
                case 1:
                    showBadUrlDialog();
                    materialDialog.dismiss();
                    break;
                case 2:
                    new UnavailableDialog().show(mActivity.getFragmentManager(), "UnavailableDialog");
                    materialDialog.dismiss();
                    break;
                case 3:
                    testImageAfterAsyncTask();
                    break;
            }
        }
    }

    private void testImageAfterAsyncTask() {
        new ImageTester(mWebCamUrl, new ConnectionTesterListener() {
            @Override
            public void connectionStatus(boolean result) {
                materialDialog.dismiss();
                if (result) {
                    openThirdDialog(true);
                } else {
                    openAnalyzeDialog();
                }
            }
        }).execute();
    }

    private void checkIfUrlStartsWithHttp() {
        String http = "http://";
        String https = "https://";
        if(!mWebCamUrl.startsWith(http) && !mWebCamUrl.startsWith(https)){
            mWebCamUrl = http + mWebCamUrl;
        }
    }

    private void openSecondDialogStill() {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_second_still, true)
                .positiveText(R.string.analyze)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .iconRes(R.drawable.dialog_still_image)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mWebCamUrl = webcamUrlAddStill.getText().toString().trim();
                        if (stillImageDirectCheckBox.isChecked()) {
                            openThirdDialog(true);
                            dialog.dismiss();
                        } else {
                            if (Patterns.WEB_URL.matcher(mWebCamUrl).matches()) {
                                showProgress(true);
                                checkIfUrlStartsWithHttp();
                                if (ConnectionTester.isConnected(mActivity)) {
                                    dialog.dismiss();
                                    new ImageTester(mWebCamUrl, new ConnectionTesterListener() {
                                        @Override
                                        public void connectionStatus(boolean result) {
                                            if (result) {
                                                openThirdDialog(true);
                                            } else {
                                                openAnalyzeDialog();
                                            }
                                            progressDialog.dismiss();
                                        }
                                    }).execute();
                                } else {
                                    new UnavailableDialog().show(mActivity.getFragmentManager(), "UnavailableDialog");
                                    progressDialog.dismiss();
                                }
                            } else showBadUrlDialog();
                        }
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

        webcamUrlTitleAddStill = (TextView) materialDialog.findViewById(R.id.webcam_url_title_add_still);

        View positiveAction = materialDialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webcamUrlAddStill = (EditText) materialDialog.findViewById(R.id.webcam_url_add_still);
        webcamUrlAddStill.addTextChangedListener(new OnTextChange(positiveAction));

        stillImageDirectCheckBox = (CheckBox) materialDialog.findViewById(R.id.still_image_direct_checkBox);
        stillImageDirectCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    webcamUrlTitleAddStill.setText(R.string.enter_direct_url);
                    webcamUrlAddStill.setHint(R.string.hint_webcam_url);
                    materialDialog.setActionButton(DialogAction.POSITIVE, R.string.next);
                } else {
                    webcamUrlTitleAddStill.setText(R.string.enter_page_for_analyzing);
                    webcamUrlAddStill.setHint(R.string.hint_webcam_analyze);
                    materialDialog.setActionButton(DialogAction.POSITIVE, R.string.analyze);
                }
            }
        });

        materialDialog.show();
    }

    private void showBadUrlDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.wrong_url)
                .content(R.string.url_is_not_valid)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.warning)
                .show();
    }

    private void openAnalyzeDialog() {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(analyzingTitle)
                .content(R.string.analyze_links_confirmation)
                .iconRes(R.drawable.settings_about)
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {

                        String noteMessage = "• " + mActivity.getString(R.string.analysis_time_info) + "\n"
                                + "• " + mActivity.getString(R.string.analyzer_doesnt_work);

                        new MaterialDialog.Builder(mActivity)
                                .title(R.string.notice)
                                .content(noteMessage)
                                .positiveText(android.R.string.ok)
                                .negativeText(R.string.back)
                                .iconRes(R.drawable.settings_about)
                                .callback(new MaterialDialog.ButtonCallback() {
                                    @Override
                                    public void onPositive(MaterialDialog dialog) {
                                        showProgress(false);
                                        analyzer.startTask(mWebCamUrl, true);
                                        materialDialog.dismiss();
                                    }
                                })
                                .show();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        showProgress(false);
                        analyzer.startTask(mWebCamUrl, false);
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .show();
    }

    private void showProgress(boolean beforeTest) {
        progressDialog = new MaterialDialog.Builder(mActivity)
                .title(analyzingTitle)
                .content(R.string.please_wait)
                .iconRes(R.drawable.dialog_still_image)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        manualCancel();
                    }
                })
                .progress(true, 0)
                .build();

        if (beforeTest) {
            progressDialog.setCancelable(false);
        }
        else progressDialog.setCancelable(true);
        progressText = progressDialog.getContentView();

        progressDialog.show();
    }

    private void manualCancel() {
        analyzer.stopTask();
        progressDialog.dismiss();
        openSecondDialogStill();
        webcamUrlAddStill.setText(mWebCamUrl);
        new MaterialDialog.Builder(mActivity)
                .title(R.string.aborted)
                .content(R.string.analyzing_canceled)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.warning)
                .show();
    }

    private AnalyzerCallback mCallback = new AnalyzerCallback() {

        int i = 0;
        @Override
        public void onAnalyzingUpdate(final String message) {
            progressText.post(new Runnable() {

                @Override
                public void run() {
                    progressText.setText(message + "\n" + mActivity.getString(R.string.completed_tasks) + i);
                    i++;
                }
            });
        }
        @Override
        public void onAnalyzingFailed(List<Link> links, String Url, int errorCode) {}
        @Override
        public void onAnalyzingCompleted(List<Link> links, boolean fromComplete) {
            if (links != null) {
                imageLinks = links;
            } else imageLinks = new ArrayList<>();
            progressDialog.dismiss();
            if (imageLinks.size() != 0) {
                openResultDialog();
            } else if (fromComplete) {
                openSecondDialogStill();
                webcamUrlAddStill.setText(mWebCamUrl);
                openNoResultsDialog();
            }
            else openTryAgainDialog();
        }
    };

    private void openTryAgainDialog() {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.no_results)
                .content(R.string.try_deeper_analyzing)
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .iconRes(R.drawable.warning)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        showProgress(false);
                        analyzer.startTask(mWebCamUrl, true);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        openSecondDialogStill();
                        webcamUrlAddStill.setText(mWebCamUrl);
                    }
                })
                .show();
    }

    private void openNoResultsDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.no_results)
                .content(R.string.url_try_again)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.settings_about)
                .show();
    }

    private void openResultDialog() {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.select_webcam)
                .iconRes(R.drawable.dialog_still_image)
                .adapter(new LinkAdapter(mActivity, imageLinks),
                        new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                Link link = imageLinks.get(which);
                                mWebCamUrl = link.getUrl();
                                openThirdDialog(true);
                                dialog.dismiss();
                            }
                        })
                .build();

        materialDialog.show();
    }

    private void openSecondDialogStream() {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_second_stream, true)
                .positiveText(R.string.next)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .iconRes(R.drawable.dialog_stream)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        mWebCamUrl = webCamUrlAddStream.getText().toString().trim();
                        mWebCamThumbUrl = webCamThumbUrlAddStream.getText().toString().trim();
                        openThirdDialog(false);
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

        View positiveAction = materialDialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webCamUrlAddStream = (EditText) materialDialog.findViewById(R.id.webcam_url_add_stream);
        webCamUrlAddStream.addTextChangedListener(new OnTextChange(positiveAction));

        webCamThumbUrlAddStream = (EditText) materialDialog.findViewById(R.id.webcam_thumb_url_add_stream);

        materialDialog.show();
    }

    private void openThirdDialog(final boolean still) {
        materialDialog = new MaterialDialog.Builder(mActivity)
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
                        openFourthDialog(still);
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

        if (still) {
            materialDialog.setIcon(R.drawable.dialog_still_image);
        }
        else materialDialog.setIcon(R.drawable.dialog_stream);

        View positiveAction = materialDialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webCamNameAdd = (EditText) materialDialog.findViewById(R.id.webcam_name_add);
        webCamNameAdd.addTextChangedListener(new OnTextChange(positiveAction));

        webCamLatitude = (EditText) materialDialog.findViewById(R.id.webcam_latitude_add);
        webCamLongitude = (EditText) materialDialog.findViewById(R.id.webcam_longitude_add);

        ImageView mWebCamCoordinatesMapSelector = (ImageView) materialDialog.findViewById(R.id.webcam_coordinates_map_selector_add);
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

        materialDialog.show();
    }

    private void openFourthDialog(boolean still) {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.input_dialog_title)
                .customView(R.layout.add_webcam_dialog_fourth, true)
                .positiveText(R.string.dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.how_to)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        openLastDialog();
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

        if (still) {
            materialDialog.setIcon(R.drawable.dialog_still_image);
        }
        else materialDialog.setIcon(R.drawable.dialog_stream);

        db = new DatabaseHelper(mActivity);
        allCategories = db.getAllCategories();
        db.closeDB();

        webCamCategoryButton = (TextView) materialDialog.findViewById(R.id.webcam_category_button_add);
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

        materialDialog.show();
    }

    private void openLastDialog() {
        materialDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.community_list)
                .content(R.string.community_list_summary)
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .iconRes(R.drawable.settings_about)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        buildWebCam(true);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        buildWebCam(false);
                    }
                })
                .cancelable(false)
                .show();
    }

    private void buildWebCam(boolean submit) {
        boolean isStream = liveStream != null && liveStream.isChecked();
        WebCam webCam = new WebCam(
                isStream,
                mWebCamName,
                mWebCamUrl,
                mWebCamThumbUrl,
                0, 0, mLatitude, mLongitude, new Date());

        if (mOnAddListener != null) {
            mOnAddListener.webCamAdded(webCam, category_ids, submit);
        }
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