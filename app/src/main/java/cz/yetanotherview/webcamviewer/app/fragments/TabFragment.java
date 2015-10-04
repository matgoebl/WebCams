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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class TabFragment extends BaseFragment {

    private static final String ARG_START = "start_param";
    public static final int AMOUNT_OF_COLUMNS = 2;
    public static final int AMOUNT_OF_IMG_IN_VIEW = 5;

    int mStart, mOrientation;

    int id;

    DatabaseHelper db;

    private String mStringSignature, sortOrder;

    private StaggeredGridLayoutManager mLayoutManager;

    private WebCamAdapter mAdapter;

    private List<WebCam> webCams;
    URLFetchTask mTask;

    public TabFragment() {}

    @SuppressLint("ValidFragment")
    public TabFragment(int id) {
        this.id = id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStart = getArguments().getInt(ARG_START);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mOrientation = getResources().getConfiguration().orientation;

        sortOrder = Utils.defaultSortOrder; //ToDo
        initData();
        setupRecyclerView(view);
        return view;
    }

    private void initData() {

        webCams = new ArrayList<>();

        mTask = new URLFetchTask(this, null);
        mTask.showProgress(true);
        mTask.execute(R.id.selecting_by_type, id);
    }

    @Override
    public void populateResult(List<WebCam> webCams) {
        this.webCams = webCams;
        mAdapter.swapData(this.webCams);
    }

    private void setupRecyclerView(View view) {
        EmptyRecyclerView mRecyclerView = (EmptyRecyclerView) view.findViewById(R.id.simpleGrid);

        mStringSignature = UUID.randomUUID().toString();
        int mLayoutId = 2; //ToDo
        Boolean imagesOnOff = true;

        mLayoutManager = new StaggeredGridLayoutManager(mLayoutId, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setEmptyView(view.findViewById(R.id.list_empty));
        mRecyclerView.setAppBarLayout((AppBarLayout) view.findViewById(R.id.app_bar_layout));
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new WebCamAdapter(getContext(), webCams, mOrientation, mLayoutId,
                new StringSignature(mStringSignature), imagesOnOff);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(new WebCamClickListener(getActivity(), mAdapter, mStringSignature));
    }

    @Override
    protected int getLayout() {
        return R.layout.tab_app_bar_fragment;
    }

}
