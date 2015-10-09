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
import android.os.Handler;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.ThumbnailDialog;
import cz.yetanotherview.webcamviewer.app.helper.DataFetcher;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.MarkerCluster;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class MapAppBarFragment extends BaseFragment implements OnMapReadyCallback, ClusterManager.OnClusterItemClickListener<MarkerCluster> {


    DataFetcher mTask;
    private GoogleMap map;

    public static MapAppBarFragment newInstance() {
        return new MapAppBarFragment();
    }

    public MapAppBarFragment() {}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mToolbar.inflateMenu(R.menu.menu_others);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        initData();
        //mStringSignature = UUID.randomUUID().toString();
    }

    private void initData() {
        mTask = new DataFetcher(this, null);
        mTask.showProgress(true);
        mTask.execute(-1);
    }

    @Override
    public void populateResult(List<WebCam> webCams) {
        ClusterManager<MarkerCluster> mClusterManager;

        KnownLocation knownLocation = Utils.getLastKnownLocation(getActivity());

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(knownLocation.getLatitude(), knownLocation.getLongitude()), 10));
        map.getUiSettings().setZoomControlsEnabled(true);

        mClusterManager = new ClusterManager<>(getContext(), map);
        mClusterManager.setOnClusterItemClickListener(this);

        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

        for (WebCam webCam : webCams) {
            String url;
            if (webCam.isStream()) {
                url = webCam.getThumbUrl();
            }
            else url = webCam.getUrl();
            MarkerCluster markerCluster = new MarkerCluster(webCam.getLatitude(), webCam.getLongitude(), webCam.getName(), webCam.getTags(), url);
            mClusterManager.addItem(markerCluster);
        }
    }

    @Override
    public boolean onClusterItemClick(final MarkerCluster markerCluster) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ThumbnailDialog thumbnailDialog = new ThumbnailDialog();
                Bundle bundle = new Bundle();
                bundle.putString("title", markerCluster.getTitle());
                bundle.putString("tags", markerCluster.getTags());
                bundle.putString("url", markerCluster.getUrl());
                thumbnailDialog.setArguments(bundle);
                thumbnailDialog.show(getActivity().getFragmentManager(), "ThumbnailDialog");
            }
        }, 400);
        return false;
    }

    @Override
    public boolean hasCustomToolbar() {
        return true;
    }

    @Override
    protected int getLayout() {
        return R.layout.map_app_bar_fragment;
    }
}
