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
    private WebCamListener mOnAddListener;
    private MaterialDialog dialog;
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

        analyzingTitle = mActivity.getString(R.string.analyzing) + " (BETA)"; //ToDo

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

        liveStream = (RadioButton) dialog.findViewById(R.id.radioLiveStreamAdd);

        imageLinks = new ArrayList<>();
        analyzer = new Analyzer(mActivity, mCallback);

        return dialog;
    }

    private void openSecondDialogStill() {
        dialog = new MaterialDialog.Builder(mActivity)
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
                            if (mWebCamUrl.toLowerCase().contains("Http://".toLowerCase())) {
                                showProgress(true);
                                new ConnectionTester(mWebCamUrl, new ConnectionTesterListener() {
                                    @Override
                                    public void connectionStatus(boolean result) {
                                        if (result) {
                                            openAnalyzeDialog();
                                        } else {
                                            new ConnectionTester(Utils.GOOGLE, new ConnectionTesterListener() {
                                                @Override
                                                public void connectionStatus(boolean result) {
                                                    if (result) {
                                                        showAvailableButWrong();
                                                    } else {
                                                        showUnavailable();
                                                    }
                                                }
                                            }).execute();
                                        }
                                    }
                                }).execute();
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

        webcamUrlTitleAddStill = (TextView) dialog.findViewById(R.id.webcam_url_title_add_still);

        View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webcamUrlAddStill = (EditText) dialog.findViewById(R.id.webcam_url_add_still);
        webcamUrlAddStill.addTextChangedListener(new OnTextChange(positiveAction));

        stillImageDirectCheckBox = (CheckBox) dialog.findViewById(R.id.still_image_direct_checkBox);
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

    private void showAvailableButWrong() {
        dialog.dismiss();
        openSecondDialogStill();
        webcamUrlAddStill.setText(mWebCamUrl);
        showBadUrlDialog();
    }

    private void showUnavailable() {
        dialog.dismiss();
        new UnavailableDialog().show(mActivity.getFragmentManager(), "UnavailableDialog");
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
        dialog.dismiss();

        dialog = new MaterialDialog.Builder(mActivity)
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
                                    }
                                    @Override
                                    public void onNegative(MaterialDialog dialog) {
                                        dialog.dismiss();
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
        dialog.dismiss();
        dialog = new MaterialDialog.Builder(mActivity)
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
            dialog.setCancelable(false);
        }
        else dialog.setCancelable(true);
        progressText = dialog.getContentView();

        dialog.show();
    }

    private void manualCancel() {
        analyzer.stopTask();
        dialog.dismiss();
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
                    progressText.setText(message  + "\n" + mActivity.getString(R.string.completed_tasks) + i);
                    i++;
                }
            });
        }
        @Override
        public void onAnalyzingFailed(List<Link> links, String Url, int errorCode) {
            // TODO:
        }
        @Override
        public void onAnalyzingCompleted(List<Link> links, boolean fromComplete) {
            imageLinks = links;
            dialog.dismiss();
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
        new MaterialDialog.Builder(mActivity)
                .title(R.string.no_results)
                .content(R.string.try_deeper_analyzing)
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .iconRes(R.drawable.settings_about)
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
        dialog = new MaterialDialog.Builder(mActivity)
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

        dialog.show();
    }

    private void openSecondDialogStream() {
        dialog = new MaterialDialog.Builder(mActivity)
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

        View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webCamUrlAddStream = (EditText) dialog.findViewById(R.id.webcam_url_add_stream);
        webCamUrlAddStream.addTextChangedListener(new OnTextChange(positiveAction));

        webCamThumbUrlAddStream = (EditText) dialog.findViewById(R.id.webcam_thumb_url_add_stream);

        dialog.show();
    }

    private void openThirdDialog(final boolean still) {
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
            dialog.setIcon(R.drawable.dialog_still_image);
        }
        else dialog.setIcon(R.drawable.dialog_stream);

        View positiveAction = dialog.getActionButton(DialogAction.POSITIVE);
        positiveAction.setEnabled(false);

        webCamNameAdd = (EditText) dialog.findViewById(R.id.webcam_name_add);
        webCamNameAdd.addTextChangedListener(new OnTextChange(positiveAction));

        webCamLatitude = (EditText) dialog.findViewById(R.id.webcam_latitude_add);
        webCamLongitude = (EditText) dialog.findViewById(R.id.webcam_longitude_add);

        ImageView mWebCamCoordinatesMapSelector = (ImageView) dialog.findViewById(R.id.webcam_coordinates_map_selector_add);
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

    private void openFourthDialog(boolean still) {
        dialog = new MaterialDialog.Builder(mActivity)
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
            dialog.setIcon(R.drawable.dialog_still_image);
        }
        else dialog.setIcon(R.drawable.dialog_stream);

        db = new DatabaseHelper(mActivity);
        allCategories = db.getAllCategories();
        db.closeDB();

        webCamCategoryButton = (TextView) dialog.findViewById(R.id.webcam_category_button_add);
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

    private void openLastDialog() {
        dialog = new MaterialDialog.Builder(mActivity)
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
        WebCam webCam = new WebCam(
                liveStream.isChecked(),
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