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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.WebCamAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DataFetcher;
import cz.yetanotherview.webcamviewer.app.listener.WebCamClickListener;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class StandardAppBarFragment extends BaseFragment {

    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private View mTintView;
    private WebCamAdapter mAdapter;
    private int id, mOrientation, autoRefreshInterval, mPosition;
    private List<WebCam> webCams;
    private String mStringSignature, title;
    private boolean imagesOnOff, autoRefresh;

    DataFetcher mTask;

    public static StandardAppBarFragment newInstance() {
        return new StandardAppBarFragment();
    }

    public StandardAppBarFragment() {}

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
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recyclerViewList);

        // Get current orientation
        mOrientation = getResources().getConfiguration().orientation;


        // New signature
        mStringSignature = UUID.randomUUID().toString();

        id = getArguments().getInt("id");

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
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new WebCamAdapter(getContext(), webCams, mOrientation, mLayoutId,
                new StringSignature(mStringSignature), imagesOnOff);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(new WebCamClickListener(getActivity(), mAdapter, mStringSignature));
    }

    private void initData() {

        webCams = new ArrayList<>();

        mTask = new DataFetcher(this, null);
        mTask.showProgress(true);
        mTask.execute(id);

    }


    @Override
    public void populateResult(List<WebCam> webCams) {
        this.webCams = webCams;
        mAdapter.swapData(this.webCams);
    }

    //ToDo ???
    protected boolean isTaskRunning(DataFetcher task) {
        if (task==null ) {
            return false;
        } else if(task.getStatus() == DataFetcher.Status.FINISHED){
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
        return R.layout.standard_app_bar_fragment;
    }
}
