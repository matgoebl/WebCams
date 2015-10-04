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
import android.support.annotation.LayoutRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.MainActivity;
import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.listener.OnMenuItemClickListener;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public abstract class BaseFragment extends Fragment {

    Toolbar mToolbar;
    ImageView mToolbarImage;
    CollapsingToolbarLayout collapsingToolbar;
    AppBarLayout appBarLayout;

    public MainActivity getDrawerActivity(){
        return ((MainActivity) super.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getLayout(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setToolbarImage();
    }

    protected void setToolbarImage() {
        if(!hasCustomToolbar()) return;
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        mToolbarImage = (ImageView) getActivity().findViewById(R.id.toolbar_image);
        collapsingToolbar = (CollapsingToolbarLayout) getActivity().findViewById(R.id.collapsing_toolbar_layout);
        mToolbar.setTitle(getArguments().getString("title"));
        //collapsingToolbar.setTitle(getArguments().getString("title"));
        appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.app_bar_layout);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDrawerActivity().openDrawer();
            }
        });
        Glide.with(getActivity()).load(selectCategoryImage()).centerCrop().into(mToolbarImage);
        mToolbar.setOnMenuItemClickListener(new OnMenuItemClickListener());
    }

    private int selectCategoryImage() {
        switch (getArguments().getInt("id")) {
            case R.id.latest_webcams:
                return R.drawable.image_all;
            case R.id.popular_webcams:
                return R.drawable.image_popular;
            case R.id.nearby_webcams:
                return R.drawable.image_nearby;
            case R.id.selecting_by_name:
                return R.drawable.image_manual; //ToDo !! Something darker on top
            case R.id.selecting_by_country:
                return R.drawable.image_country;
            case R.id.selecting_by_type:
                return R.drawable.image_airports;
            case R.id.live_streams:
                return R.drawable.image_live_streams;
            case R.id.selecting_from_map:
                appBarLayout.setExpanded(false, true);
                return R.drawable.image_map;
            case R.id.favorites_webcams:
                return R.drawable.image_favorites;
            case R.id.all_local_webcams:
                return R.drawable.image_imported;
            default:
                return R.drawable.image_all;
        }
    }

    public void showProgressBar() {
        RelativeLayout progress = (RelativeLayout) getActivity().findViewById(R.id.progressBarFetch);
        progress.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        RelativeLayout progress = (RelativeLayout) getActivity().findViewById(R.id.progressBarFetch);
        progress.setVisibility(View.GONE);
    }

    public void populateResult(List<WebCam> webCams) {
    }

    public boolean hasCustomToolbar(){
        return false;
    }

    protected abstract  @LayoutRes
    int getLayout();
}
