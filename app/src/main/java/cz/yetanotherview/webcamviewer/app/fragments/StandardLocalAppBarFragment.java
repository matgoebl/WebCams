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

package cz.yetanotherview.webcamviewer.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.SaveDialog;
import cz.yetanotherview.webcamviewer.app.actions.ShareDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.NoCoordinatesDialog;
import cz.yetanotherview.webcamviewer.app.adapter.WebCamAdapter;
import cz.yetanotherview.webcamviewer.app.fullscreen.FullScreenActivity;
import cz.yetanotherview.webcamviewer.app.fullscreen.LiveStreamActivity;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.EmptyRecyclerView;
import cz.yetanotherview.webcamviewer.app.helper.SendToInbox;
import cz.yetanotherview.webcamviewer.app.helper.URLFetchTask;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class StandardLocalAppBarFragment extends BaseFragment {

    private StaggeredGridLayoutManager mLayoutManager;
    private EmptyRecyclerView mRecyclerView;
    private View mTintView;
    private WebCamAdapter mAdapter;
    private int id, mOrientation, autoRefreshInterval, mPosition;
    private List<WebCam> webCams;
    private String mStringSignature;
    private boolean imagesOnOff, hwAcceleration, autoRefresh, screenAlwaysOn;
    private WebCam webCam;
    private MaterialDialog materialDialog;
    private Toolbar mToolbar;

    URLFetchTask mTask;

    private DatabaseHelper db;


    //private AppBarLayout appBarLayout;



    public static StandardLocalAppBarFragment newInstance() {
        return new StandardLocalAppBarFragment();
    }

    public StandardLocalAppBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setList();

        //ToDo ???
//        // Sync UI state to current fragment and task state
//        if(isTaskRunning(mTask)) {
//            showProgressBar();
//        }else {
//            hideProgressBar();
//        }
//        if(webCams!=null) {
//            populateResult(webCams);
//        }
    }

    private void setList() {
        mRecyclerView = (EmptyRecyclerView) getActivity().findViewById(R.id.emptyRecyclerViewList);

        // Get current orientation
        mOrientation = getResources().getConfiguration().orientation;


        // New signature
        mStringSignature = UUID.randomUUID().toString();

        initData();

        int mLayoutId = 1; //ToDo
        imagesOnOff = true;
//        if (numberOfColumns == 1 && mOrientation == 1) {
//            mLayoutId = 1;
//        }
//        else if(numberOfColumns == 1 && mOrientation == 2) {
//            mLayoutId = 2;
//        }
//        else if(numberOfColumns == 2 && mOrientation == 1) {
//            mLayoutId = 2;
//        }
//        else if(numberOfColumns == 2 && mOrientation == 2) {
//            mLayoutId = 3;
//        }

        mLayoutManager = new StaggeredGridLayoutManager(mLayoutId, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setEmptyView(getActivity().findViewById(R.id.list_empty));
        mRecyclerView.setAppBarLayout((AppBarLayout) getActivity().findViewById(R.id.app_bar_layout));
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new WebCamAdapter(getContext(), webCams, mOrientation, mLayoutId,
                new StringSignature(mStringSignature), imagesOnOff);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(new WebCamAdapter.ClickListener() {

            @Override
            public void onClick(View view, int position, boolean isEditClick, boolean isLongClick,
                                View tintView, View errorView) {
                if (isEditClick) {
                    mTintView = tintView;
                    showOptionsDialog(position);
                } else if (isLongClick) {
                    mTintView = tintView;
                    mPosition = position;
                    moveItem();
                } else {
                    webCam = (WebCam) mAdapter.getItem(position);
                    if (errorView.getVisibility() == View.VISIBLE && !webCam.isStream()) {
                        refreshSelected(position);
                    } else maximizeImageOrPlayStream(position, false, false);
                }
            }
        });
    }

    private void initData() {

        webCams = new ArrayList<>();

        if (db == null) {
            db = new DatabaseHelper(getActivity().getApplicationContext());
        }
        //if (selectedCategory == 0) {
        webCams = db.getAllWebCams(Utils.defaultSortOrder);
        //}
        //else webCams = db.getAllWebCamsByCategory(selectedCategoryId, sortOrder);
        db.closeDB();

    }


    @Override
    public void populateResult(List<WebCam> webCams) {
        this.webCams = webCams;
        mAdapter.swapData(this.webCams);
    }

    //ToDo ???
    protected boolean isTaskRunning(URLFetchTask task) {
        if(task==null ) {
            return false;
        } else if(task.getStatus() == URLFetchTask.Status.FINISHED){
            return false;
        } else {
            return true;
        }
    }

    private void refreshSelected(int position) {
        mStringSignature = UUID.randomUUID().toString();
        mAdapter.refreshSelectedImage(position, new StringSignature(mStringSignature));
    }

    @Override
    public boolean hasCustomToolbar() {
        return true;
    }

    @Override
    protected int getLayout() {
        return R.layout.standard_local_app_bar_fragment;
    }

    private void maximizeImageOrPlayStream(int position, boolean map, boolean fromEditClick) {
        webCam = (WebCam) mAdapter.getItem(position);

        if (webCam.isStream() && !map) {
            if (fromEditClick) {
                new MaterialDialog.Builder(getActivity())
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
        Intent intent = new Intent(getActivity(), LiveStreamActivity.class);
        intent.putExtra("url", webCam.getUrl());
        intent.putExtra("name", webCam.getName());
        intent.putExtra("hwAcceleration", hwAcceleration);
        startActivity(intent);
    }

    private void maximizeImage(boolean map, boolean preview) {
        Intent intent = new Intent(getActivity(), FullScreenActivity.class);
        intent.putExtra("signature", mStringSignature);
        intent.putExtra("map", map);
        intent.putExtra("name", webCam.getName());
        String url = webCam.getUrl();
        if (preview) url = webCam.getThumbUrl();
        intent.putExtra("url", url);
        intent.putExtra("latitude", webCam.getLatitude());
        intent.putExtra("longitude", webCam.getLongitude());
        intent.putExtra("autoRefresh", autoRefresh);
        intent.putExtra("interval", autoRefreshInterval);
        intent.putExtra("screenAlwaysOn", screenAlwaysOn);

        if (!map){
            startActivity(intent);
        }
        else {
            if (webCam.getLatitude() != 0 || webCam.getLongitude() != 0) {
                startActivity(intent);
            }
            else new NoCoordinatesDialog().show(getActivity().getFragmentManager(), "NoCoordinatesDialog");
        }
    }

    private void showOptionsDialog(final int position) {
        webCam = (WebCam) mAdapter.getItem(position);

        String[] options_values = getResources().getStringArray(R.array.opt_values);
        if (webCam.isStream()) {
            options_values[4] = getString(R.string.play_maximize);
        }
        if (webCam.getUniId() != 0) {
            options_values[8] = getString(R.string.report_problem);
        }
        else options_values[8] = getString(R.string.submit_as_suggestion);

        materialDialog = new MaterialDialog.Builder(getActivity())
                .items(options_values)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Bundle bundle = new Bundle();
                        switch (which) {
                            case 0:
                                //refreshSelected(position);
                                break;
                            case 1:
                                //showEditDialog(position);
                                break;
                            case 2:
                                //webCamDeleted(webCam, position);
                                break;
                            case 3:
                                mPosition = position;
                                moveItem();
                                break;
                            case 4:
                                maximizeImageOrPlayStream(position, false, true);
                                break;
                            case 5:
                                SaveDialog saveDialog = new SaveDialog();
                                bundle.putInt("from", 0);
                                bundle.putString("name", webCam.getName());
                                if (webCam.isStream()) {
                                    bundle.putString("url", webCam.getThumbUrl());
                                } else bundle.putString("url", webCam.getUrl());
                                saveDialog.setArguments(bundle);
                                saveDialog.show(getActivity().getFragmentManager(), "SaveDialog");
                                break;
                            case 6:
                                ShareDialog shareDialog = new ShareDialog();
                                if (webCam.isStream()) {
                                    bundle.putString("url", webCam.getThumbUrl());
                                } else bundle.putString("url", webCam.getUrl());
                                shareDialog.setArguments(bundle);
                                shareDialog.show(getActivity().getFragmentManager(), "ShareDialog");
                                break;
                            case 7:
                                maximizeImageOrPlayStream(position, true, true);
                                break;
                            case 8:
                                if (webCam.getUniId() != 0) {
                                    new MaterialDialog.Builder(getActivity())
                                            .title(R.string.report_problem)
                                            .content(R.string.report_problem_summary)
                                            .positiveText(R.string.send)
                                            .negativeText(android.R.string.cancel)
                                            .iconRes(R.drawable.settings_about)
                                            .items(R.array.whats_wrong)
                                            .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                                @Override
                                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                    new SendToInbox().sendToInboxWebCam(getActivity(), webCam, true, which);
                                                    return true;
                                                }
                                            })
                                            .show();
                                } else new MaterialDialog.Builder(getActivity())
                                        .title(R.string.submit_as_suggestion)
                                        .content(R.string.community_list_summary)
                                        .positiveText(R.string.Yes)
                                        .negativeText(android.R.string.cancel)
                                        .iconRes(R.drawable.settings_about)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                new SendToInbox().sendToInboxWebCam(getActivity(), webCam, false, -1);
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

//    private void showEditDialog(int position) {
//        DialogFragment dialogFragment = EditDialog.newInstance(this);
//
//        webCam = (WebCam) mAdapter.getItem(position);
//        Bundle bundle = new Bundle();
//        bundle.putLong("id", webCam.getId());
//        bundle.putInt("position", position);
//        dialogFragment.setArguments(bundle);
//
//        dialogFragment.show(getActivity().getFragmentManager(), "EditDialog");
//    }

    private void moveItem() {

        mToolbar.startActionMode(new ActionMode.Callback() {

            int pos = mPosition;
            //final View tempView = rootView.findViewById(R.id.tempView);

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.move_menu, menu);
                mode.setTitle(R.string.move);
                mTintView.setVisibility(View.VISIBLE);
                //tempView.setVisibility(View.VISIBLE);
                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.up:
                        mAdapter.moveItemUp(mAdapter.getItemAt(pos));
                        mLayoutManager.scrollToPosition(0);
                        if (pos > 0) {
                            pos = pos - 1;
                        }
                        mLayoutManager.scrollToPositionWithOffset(pos, 0);
                        return true;
                    case R.id.down:
                        mAdapter.moveItemDown(mAdapter.getItemAt(pos));
                        if (pos < (mAdapter.getItemCount() - 1)) {
                            pos = pos + 1;
                        }
                        mLayoutManager.scrollToPositionWithOffset(pos, 0);
                        return true;
                    case R.id.done:
//                        sortOrder = "position";
//                        //saveToPref();
//                        //showIndeterminateProgress();
//                        new savePositionsToDB().execute();
//                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            public void onDestroyActionMode(ActionMode mode) {
                mTintView.setVisibility(View.GONE);
                //tempView.setVisibility(View.GONE);
            }
        });
    }

//    @Override
//    public void webCamAdded(WebCam webCam, List<Integer> category_ids, boolean share) {
//
//    }
//
//    @Override
//    public void webCamEdited(int position, WebCam webCam, List<Integer> category_ids) {
//
//    }
//
//    @Override
//    public void webCamDeleted(WebCam webCam, int position) {
//
//    }

//    private class savePositionsToDB extends AsyncTask<Long, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Long... longs) {
//
//            List<WebCam> newWebCams = mAdapter.getItems();
//            int i = 0;
//            for (WebCam mWebCam : newWebCams) {
//                mWebCam.setPosition(i);
//                db.updateWebCamPosition(mWebCam);
//                i++;
//            }
//            db.closeDB();
//            this.publishProgress();
//
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//
//            //indeterminateProgress.dismiss();
//            //saveDone();
//        }
//    }
}
