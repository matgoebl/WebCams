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
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import cz.yetanotherview.webcamviewer.app.R;

public class CoordinatesChooserDialog extends DialogFragment {

    private Activity mActivity;
    private Callback mCallback;
    private MapView mMapView;
    private Marker marker;

    public interface Callback {
        void onCoordinatesSave(String latitude, String longitude);
    }

    public CoordinatesChooserDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.selecting_from_map)
                .customView(R.layout.maps_dialog_layout, false)
                .positiveText(R.string.dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (marker != null) {
                            LatLng latLng = marker.getPoint();
                            mCallback.onCoordinatesSave(String.valueOf(latLng.getLatitude()), String.valueOf(latLng.getLongitude()));
                        }
                    }
                })
                .build();

        mMapView = (MapView) dialog.getCustomView().findViewById(R.id.mapView);

        if (getArguments().getBoolean("empty")) {
            marker = null;
            mMapView.setZoom(2);
        } else {
            LatLng initPosition = new LatLng(getArguments().getDouble("latitude"), getArguments().getDouble("longitude"));
            addMarker(initPosition);
            mMapView.setZoom(16);
            mMapView.setCenter(initPosition);
        }

        ImageButton zoomIn = (ImageButton) dialog.getCustomView().findViewById(R.id.zoomIn);
        zoomIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMapView.getController().zoomIn();
            }
        });

        ImageButton zoomOut = (ImageButton) dialog.getCustomView().findViewById(R.id.zoomOut);
        zoomOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMapView.getController().zoomOut();
            }
        });

        mMapView.setDiskCacheEnabled(false);
        mMapView.setMapViewListener(new MapViewListener() {
            @Override
            public void onShowMarker(MapView pMapView, Marker pMarker) {}

            @Override
            public void onHideMarker(MapView pMapView, Marker pMarker) {}

            @Override
            public void onTapMarker(MapView pMapView, Marker pMarker) {}

            @Override
            public void onLongPressMarker(MapView pMapView, Marker pMarker) {}

            @Override
            public void onTapMap(MapView pMapView, ILatLng pPosition) {
                if (marker != null) {
                    mMapView.removeMarker(marker);
                }
                addMarker(new LatLng(pPosition.getLatitude(), pPosition.getLongitude()));
                Toast.makeText(mActivity, String.valueOf(pPosition.getLatitude()) + ", " +
                        String.valueOf(pPosition.getLongitude()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onLongPressMap(MapView pMapView, ILatLng pPosition) {}
        });

        return dialog;
    }

    private void addMarker(LatLng markerPosition) {
        marker = new Marker(mMapView, "", "", markerPosition);
        marker.setMarker(ResourcesCompat.getDrawable(getResources(), R.drawable.marker, null));
        mMapView.addMarker(marker);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mCallback = (Callback) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement CoordinatesChooserDialogListener");
        }
    }
}
