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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.SaveDialog;
import cz.yetanotherview.webcamviewer.app.actions.ShareDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.NoCoordinatesDialog;
import cz.yetanotherview.webcamviewer.app.adapter.WebCamAdapter;
import cz.yetanotherview.webcamviewer.app.fullscreen.FullScreenActivity;
import cz.yetanotherview.webcamviewer.app.fullscreen.LiveStreamActivity;
import cz.yetanotherview.webcamviewer.app.helper.SendToInbox;
import cz.yetanotherview.webcamviewer.app.helper.URLFetchTask;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.MarkerCluster;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class MapAppBarFragment extends BaseFragment implements OnMapReadyCallback {

    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private View mTintView;
    private WebCamAdapter mAdapter;
    private int mOrientation, autoRefreshInterval, mPosition;
    private List<WebCam> webCams;
    private String mStringSignature;
    private boolean imagesOnOff, hwAcceleration, autoRefresh, screenAlwaysOn;
    private WebCam webCam;
    private MaterialDialog materialDialog;
    private Toolbar mToolbar;

    URLFetchTask mTask;

    //MapView mMapView;

    private Drawable selectedMarker, markerNotSelected;

    //List<Marker> markers;


    //private AppBarLayout appBarLayout;

    private GoogleMap map;

    public static MapAppBarFragment newInstance() {
        return new MapAppBarFragment();
    }

    public MapAppBarFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        initData();
        mStringSignature = UUID.randomUUID().toString();
    }


    private void initData() {
        webCams = new ArrayList<>();
        mTask = new URLFetchTask(this, getActivity());
        mTask.showProgress(true);
        mTask.execute(-1);
    }

    private void setList() {



//        mMapView = (MapView) getActivity().findViewById(R.id.mapView);
//        LatLng latLng = new LatLng(knownLocation.getLatitude(), knownLocation.getLongitude());
//        mMapView.setCenter(latLng);
//        if (knownLocation.isNotDetected()) {
//            mMapView.setZoom(3);
//        } else mMapView.setZoom(8);
//        mMapView.setDiskCacheEnabled(false);
//
//        ImageButton zoomIn = (ImageButton) getActivity().findViewById(R.id.zoomIn);
//        zoomIn.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mMapView.getController().zoomIn();
//            }
//        });
//
//        ImageButton zoomOut = (ImageButton) getActivity().findViewById(R.id.zoomOut);
//        zoomOut.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                mMapView.getController().zoomOut();
//            }
//        });

        //selMarkers = new LinkedList<>();


    }


    @Override
    public void populateResult(List<WebCam> webCams) {
        this.webCams = webCams;

        // Declare a variable for the cluster manager.
        ClusterManager<MarkerCluster> mClusterManager;

        KnownLocation knownLocation = Utils.getLastKnownLocation(getActivity());

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(knownLocation.getLatitude(), knownLocation.getLongitude()), 10));
        map.getUiSettings().setZoomControlsEnabled(true);

        // Initialize the manager with the context and the map.
        // (Activity extends context, so we can pass 'this' in the constructor.)
        mClusterManager = new ClusterManager<>(getContext(), map);
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerCluster>() {
            @Override
                public boolean onClusterItemClick(MarkerCluster markerCluster) {
                //ToDO
                return false;
            }
        });

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

        // Add ten cluster items in close proximity, for purposes of this example.
        for (WebCam webCam : webCams) {
            String url;
            if (webCam.isStream()) {
                url = webCam.getThumbUrl();
            }
            else url = webCam.getUrl();
            MarkerCluster markerCluster = new MarkerCluster(webCam.getLatitude(), webCam.getLongitude(), webCam.getName(), url);
            mClusterManager.addItem(markerCluster);
        }
    }

    //ToDo ???
    protected boolean isTaskRunning(URLFetchTask task) {
        if (task==null ) {
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
        return R.layout.map_app_bar_fragment;
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
}
