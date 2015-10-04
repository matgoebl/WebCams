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

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.WebCamAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.EmptyRecyclerView;
import cz.yetanotherview.webcamviewer.app.helper.URLFetchTask;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.listener.WebCamClickListener;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class StandardLocalAppBarFragment extends BaseFragment {

    private StaggeredGridLayoutManager mLayoutManager;
    private EmptyRecyclerView mRecyclerView;
    private View mTintView;
    private WebCamAdapter mAdapter;
    private int id, mOrientation, autoRefreshInterval, mPosition;
    private List<WebCam> webCams;
    private String mStringSignature;
    private boolean imagesOnOff, autoRefresh;


    private DatabaseHelper db;

    public static StandardLocalAppBarFragment newInstance() {
        return new StandardLocalAppBarFragment();
    }

    public StandardLocalAppBarFragment() {}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar.inflateMenu(R.menu.menu_others);
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
        mAdapter.setClickListener(new WebCamClickListener(getActivity(), mAdapter, mStringSignature));
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

    @Override
    public boolean hasCustomToolbar() {
        return true;
    }

    @Override
    protected int getLayout() {
        return R.layout.standard_local_app_bar_fragment;
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
