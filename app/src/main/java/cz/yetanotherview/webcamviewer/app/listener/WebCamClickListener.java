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

package cz.yetanotherview.webcamviewer.app.listener;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.signature.StringSignature;

import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.SaveDialog;
import cz.yetanotherview.webcamviewer.app.actions.ShareDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.NoCoordinatesDialog;
import cz.yetanotherview.webcamviewer.app.adapter.WebCamAdapter;
import cz.yetanotherview.webcamviewer.app.fullscreen.FullScreenActivity;
import cz.yetanotherview.webcamviewer.app.fullscreen.LiveStreamActivity;
import cz.yetanotherview.webcamviewer.app.helper.SendToInbox;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class WebCamClickListener implements WebCamAdapter.ClickListener {

    private final Activity activity;
    private final WebCamAdapter adapter;
    private String stringSignature;
    private int position;
    private WebCam webCam;

    public WebCamClickListener(Activity activity, WebCamAdapter adapter, String stringSignature) {
        this.activity = activity;
        this.adapter = adapter;
        this.stringSignature = stringSignature; //ToDo
    }

    @Override
    public void onClick(View v, int position, boolean isEditClick, boolean isLongClick, View tintView, View errorView) {
        this.position = position;
        webCam = (WebCam) adapter.getItem(position);
        if (isEditClick) {
            //mTintView = tintView;
            showOptionsDialog();
        } else if (isLongClick) {
            //mTintView = tintView;
            //mPosition = position;
            //moveItem();
        } else {
            if (errorView.getVisibility() == View.VISIBLE && !webCam.isStream()) {
                refreshSelected();
            } else maximizeImageOrPlayStream(false, false);
        }
    }

    private void maximizeImageOrPlayStream(boolean map, boolean fromEditClick) {
        if (webCam.isStream() && !map) {
            if (fromEditClick) {
                new MaterialDialog.Builder(activity)
                        .items(R.array.play_maximize_values)
                        .itemsCallback(new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                if (which == 0) {
                                    playStream();
                                }
                                else maximizeImage(false, true);
                            }
                        })
                        .show();
            }
            else playStream();
        }
        else maximizeImage(map, false);

        // Workaround for cleaning the entered searchView text.
//        if (!searchView.isIconified()) {
//            searchView.setIconified(true);
//        }
    }

    private void playStream() {
        Intent intent = new Intent(activity, LiveStreamActivity.class);
        intent.putExtra("url", webCam.getUrl());
        intent.putExtra("name", webCam.getName());
        intent.putExtra("hwAcceleration",
                PreferenceManager.getDefaultSharedPreferences(activity).
                        getBoolean("pref_screen_hw_acceleration", true));
        activity.startActivity(intent);
    }

    private void maximizeImage(boolean map, boolean preview) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        Intent intent = new Intent(activity, FullScreenActivity.class);
        intent.putExtra("signature", stringSignature);
        intent.putExtra("map", map);
        intent.putExtra("name", webCam.getName());
        String url = webCam.getUrl();
        if (preview) url = webCam.getThumbUrl();
        intent.putExtra("url", url);
        intent.putExtra("latitude", webCam.getLatitude());
        intent.putExtra("longitude", webCam.getLongitude());
        intent.putExtra("autoRefresh", sharedPreferences.getBoolean("pref_auto_refresh", false));
        intent.putExtra("interval", sharedPreferences.getInt("pref_auto_refresh_interval", 30000));
        intent.putExtra("screenAlwaysOn", sharedPreferences.getBoolean("pref_screen_always_on", false));

        if (!map){
            activity.startActivity(intent);
        }
        else {
            if (webCam.getLatitude() != 0 || webCam.getLongitude() != 0) {
                activity.startActivity(intent);
            }
            else new NoCoordinatesDialog().show(activity.getFragmentManager(), "NoCoordinatesDialog");
        }
    }

    private void refreshSelected() {
        stringSignature = UUID.randomUUID().toString(); //ToDo!!
        adapter.refreshSelectedImage(position, new StringSignature(stringSignature));
    }

    private void showOptionsDialog() {

        String[] options_values = activity.getResources().getStringArray(R.array.opt_values);
        if (webCam.isStream()) {
            options_values[4] = activity.getString(R.string.play_maximize);
        }
        if (webCam.getUniId() != 0) {
            options_values[8] = activity.getString(R.string.report_problem);
        }
        else options_values[8] = activity.getString(R.string.submit_as_suggestion);

        new MaterialDialog.Builder(activity)
                .items(options_values)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Bundle bundle = new Bundle();
                        switch (which) {
                            case 0:
                                refreshSelected();
                                break;
                            case 1:
                                //showEditDialog();
                                break;
                            case 2:
                                //webCamDeleted(webCam);
                                break;
                            case 3:
                                //mPosition = position;
                                //moveItem();
                                break;
                            case 4:
                                maximizeImageOrPlayStream(false, true);
                                break;
                            case 5:
                                SaveDialog saveDialog = new SaveDialog();
                                bundle.putInt("from", 0);
                                bundle.putString("name", webCam.getName());
                                if (webCam.isStream()) {
                                    bundle.putString("url", webCam.getThumbUrl());
                                } else bundle.putString("url", webCam.getUrl());
                                saveDialog.setArguments(bundle);
                                saveDialog.show(activity.getFragmentManager(), "SaveDialog");
                                break;
                            case 6:
                                ShareDialog shareDialog = new ShareDialog();
                                if (webCam.isStream()) {
                                    bundle.putString("url", webCam.getThumbUrl());
                                } else bundle.putString("url", webCam.getUrl());
                                shareDialog.setArguments(bundle);
                                shareDialog.show(activity.getFragmentManager(), "ShareDialog");
                                break;
                            case 7:
                                maximizeImageOrPlayStream(true, true);
                                break;
                            case 8:
                                if (webCam.getUniId() != 0) {
                                    new MaterialDialog.Builder(activity)
                                            .title(R.string.report_problem)
                                            .content(R.string.report_problem_summary)
                                            .positiveText(R.string.send)
                                            .negativeText(android.R.string.cancel)
                                            .iconRes(R.drawable.settings_about)
                                            .items(R.array.whats_wrong)
                                            .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                                @Override
                                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                    new SendToInbox().sendToInboxWebCam(activity, webCam, true, which);
                                                    return true;
                                                }
                                            })
                                            .show();
                                } else new MaterialDialog.Builder(activity)
                                        .title(R.string.submit_as_suggestion)
                                        .content(R.string.community_list_summary)
                                        .positiveText(R.string.Yes)
                                        .negativeText(android.R.string.cancel)
                                        .iconRes(R.drawable.settings_about)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                new SendToInbox().sendToInboxWebCam(activity, webCam, false, -1);
                                            }
                                        })
                                        .show();
                                break;
                            default:
                                break;
                        }
                    }
                })
                .show();
    }

//    private void showEditDialog() {
//        DialogFragment dialogFragment = EditDialog.newInstance(this);
//
//        Bundle bundle = new Bundle();
//        bundle.putLong("id", webCam.getId());
//        bundle.putInt("position", position);
//        dialogFragment.setArguments(bundle);
//
//        dialogFragment.show(getActivity().getFragmentManager(), "EditDialog");
//    }

//    private void moveItem() {
//
//        mToolbar.startActionMode(new ActionMode.Callback() {
//
//            int pos = mPosition;
//            //final View tempView = rootView.findViewById(R.id.tempView);
//
//            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//                MenuInflater inflater = mode.getMenuInflater();
//                inflater.inflate(R.menu.move_menu, menu);
//                mode.setTitle(R.string.move);
//                mTintView.setVisibility(View.VISIBLE);
//                //tempView.setVisibility(View.VISIBLE);
//                return true;
//            }
//
//            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//                return false;
//            }
//
//            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//
//                switch (item.getItemId()) {
//                    case R.id.up:
//                        mAdapter.moveItemUp(mAdapter.getItemAt(pos));
//                        mLayoutManager.scrollToPosition(0);
//                        if (pos > 0) {
//                            pos = pos - 1;
//                        }
//                        mLayoutManager.scrollToPositionWithOffset(pos, 0);
//                        return true;
//                    case R.id.down:
//                        mAdapter.moveItemDown(mAdapter.getItemAt(pos));
//                        if (pos < (mAdapter.getItemCount() - 1)) {
//                            pos = pos + 1;
//                        }
//                        mLayoutManager.scrollToPositionWithOffset(pos, 0);
//                        return true;
//                    case R.id.done:
////                        sortOrder = "position";
////                        //saveToPref();
////                        //showIndeterminateProgress();
////                        new savePositionsToDB().execute();
////                        mode.finish();
//                        return true;
//                    default:
//                        return false;
//                }
//            }
//
//            public void onDestroyActionMode(ActionMode mode) {
//                mTintView.setVisibility(View.GONE);
//                //tempView.setVisibility(View.GONE);
//            }
//        });
//    }
}
