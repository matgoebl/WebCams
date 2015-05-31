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
import android.app.backup.BackupManager;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import junit.framework.Assert;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.actions.simple.LocationWarningDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.NothingSelectedDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.ReportDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.UnavailableDialog;
import cz.yetanotherview.webcamviewer.app.adapter.CountryAdapter;
import cz.yetanotherview.webcamviewer.app.adapter.ManualSelectionAdapter;
import cz.yetanotherview.webcamviewer.app.adapter.TypeAdapter;
import cz.yetanotherview.webcamviewer.app.helper.CountryNameComparator;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.OnFilterTextChange;
import cz.yetanotherview.webcamviewer.app.helper.TypeNameComparator;
import cz.yetanotherview.webcamviewer.app.helper.WebCamNameComparator;
import cz.yetanotherview.webcamviewer.app.listener.SeekBarChangeListener;
import cz.yetanotherview.webcamviewer.app.model.Category;
import cz.yetanotherview.webcamviewer.app.model.Country;
import cz.yetanotherview.webcamviewer.app.model.Icons;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.Type;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class JsonFetcherDialog extends DialogFragment {

    // Object for intrinsic lock
    public static final Object sDataLock = new Object();
    private static final String TAG = "JsonFetcher";

    private Activity mActivity;
    private DatabaseHelper db;
    private List<WebCam> importWebCams, allWebCams;
    private List<Country> countryList;
    private List<Type> typeList;
    private List<Marker> markers, selMarkers;
    private MaterialDialog initDialog, progressDialog;
    private int selection, newWebCams, duplicityWebCams, updatedWebCams, maxProgressValue, seekBarProgress,
            seekBarCorrection;
    private boolean lastFetchNewWebCams = false;
    private float selectedDistance;
    private long lastFetchLatest;
    private String importProgress, units, countryCode, countryName;
    private EditText filterBox;
    private ManualSelectionAdapter manualSelectionAdapter;
    private ReloadInterface mListener;
    private KnownLocation knownLocation;
    private SeekBar seekBar;
    private TextView seekBarText;
    private MapView mMapView;
    private Drawable selectedMarker, markerNotSelected;
    private BackupManager backupManager;
    private SharedPreferences preferences;
    private Type type;

    public interface ReloadInterface {
        void invokeReload();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mListener = (ReloadInterface) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(mActivity);
        allWebCams = db.getAllWebCams(Utils.defaultSortOrder);

        Bundle bundle = this.getArguments();
        selection = bundle.getInt("selection", 0);
        String plsWait = getString(R.string.please_wait);
        importProgress = getString(R.string.import_progress) + " " + plsWait;

        initDialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.importing_from_server)
                .content(plsWait)
                .progress(true, 0)
                .build();

        backupManager = new BackupManager(mActivity);
        preferences = PreferenceManager.getDefaultSharedPreferences(mActivity);

        WebCamsFromJsonFetcher fetcher = new WebCamsFromJsonFetcher();
        fetcher.execute();

        return initDialog;
    }

    private class WebCamsFromJsonFetcher extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            try {

                URL url;
                switch (selection) {
                    case 0:
                        url = new URL(Utils.JSON_FILE_URL_POPULAR);
                        break;
                    case 6:
                        url = new URL(Utils.JSON_FILE_URL_LIVE_STREAMS);
                        break;
                    case 8:
                        url = new URL(Utils.JSON_FILE_URL_LATEST);
                        break;
                    default:
                        url = new URL(Utils.JSON_FILE_URL_ALL);
                        break;
                }

                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                InputStream content = new BufferedInputStream(urlConn.getInputStream());

                urlConn.connect();
                Assert.assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());

                try {
                    //Read the server response and attempt to parse it as JSON
                    Reader reader = new InputStreamReader(content);

                    Gson gson = new GsonBuilder().setDateFormat(Utils.dateTimeFormat).create();
                    importWebCams = Arrays.asList(gson.fromJson(reader, WebCam[].class));
                    content.close();

                    // Swap dialogs
                    maxProgressValue = importWebCams.size();
                    if (selection == 0 || selection == 6 || selection == 8) {
                        swapProgressDialog();
                    }

                    // Handle WebCams importing task
                    newWebCams = 0;
                    duplicityWebCams = 0;
                    updatedWebCams = 0;

                        if (selection == 0) {
                            lastFetchLatest = preferences.getLong("pref_last_fetch_popular", 0);
                            proceed(new Category("@drawable/icon_popular",
                                            getString(R.string.popular) + " " + Utils.getDateString()));
                        }
                        else if (selection == 1) {
                            knownLocation = Utils.getLastKnownLocation(mActivity);
                            handleNearSelection();
                        }
                        else if (selection == 2) {
                            Collections.sort(importWebCams, new WebCamNameComparator());
                            handleManualSelection();
                        }
                        else if (selection == 3) {
                            List<String> tempList = new ArrayList<>();
                            List<String> listAllCountries = new ArrayList<>();
                            countryList = new ArrayList<>();
                            for (WebCam webCam : importWebCams) {

                                String countryCode = webCam.getCountry();
                                listAllCountries.add(countryCode);
                                if (!tempList.contains(countryCode)) {
                                    tempList.add(countryCode);

                                    Country country = new Country();
                                    country.setCountryCode(countryCode);
                                    country.setCountryName(new Locale("", countryCode).getDisplayCountry());
                                    String drawable =  countryCode.toLowerCase();
                                    country.setIcon(Utils.getResId(drawable, R.drawable.class));

                                    countryList.add(country);
                                }
                            }

                            Collections.sort(countryList, new CountryNameComparator());
                            Collections.sort(listAllCountries);

                            for (Country country : countryList) {
                                int occurrences = Collections.frequency(listAllCountries, country.getCountryCode());
                                country.setCount(occurrences);
                            }
                            handleCountrySelection();
                        }
                        else if (selection == 4) {
                            typeList = new ArrayList<>();
                            List<Integer> countList = new ArrayList<>();
                            List<String> listAllTypes = Arrays.asList(getResources().getStringArray(R.array.types));
                            for (String typeName : listAllTypes) {
                                Type type = new Type();
                                Icons icons = new Icons();
                                type.setId(listAllTypes.indexOf(typeName));
                                type.setIconName(icons.getIconName(listAllTypes.indexOf(typeName)));
                                type.setTypeName(typeName);
                                type.setIcon(icons.getIconId(listAllTypes.indexOf(typeName)));

                                typeList.add(type);
                            }

                            for (WebCam webCam : importWebCams) {
                                countList.add(webCam.getStatus());
                            }

                            Collections.sort(typeList, new TypeNameComparator());
                            Collections.sort(countList);

                            for (Type type : typeList) {
                                int status = type.getId();
                                int occurrences = Collections.frequency(countList, status);
                                type.setCount(occurrences);
                            }
                            handleTypeSelection();
                        }
                        else if (selection == 5) {
                            knownLocation = Utils.getLastKnownLocation(mActivity);
                            selectedMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.marker, null);
                            markerNotSelected = ResourcesCompat.getDrawable(getResources(), R.drawable.marker_not_selected, null);

                            markers = new ArrayList<>();
                            for (WebCam webCam : importWebCams) {
                                LatLng latLng = new LatLng(webCam.getLatitude(), webCam.getLongitude());
                                Marker marker = new Marker(mMapView, webCam.getName(), String.valueOf(webCam.getLatitude()) +
                                        ", " + String.valueOf(webCam.getLongitude()), latLng);
                                marker.setMarker(markerNotSelected);
                                markers.add(marker);
                            }
                            handleMapSelection();
                        }
                        else if (selection == 6) {
                            proceed(new Category("@drawable/icon_live_streams",
                                    getString(R.string.live_streams) + " " + Utils.getDateString()));
                        }
                        else if (selection == 7) {
                            handleAll();
                        }
                        else if (selection == 8) {
                            lastFetchLatest = preferences.getLong("pref_last_fetch_latest", 0);
                            proceed(new Category("@drawable/icon_latest",
                                            getString(R.string.latest) + " " + Utils.getDateString()));
                        }
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to parse JSON due to: " + ex);
                }
            } catch (IOException e) {
                System.err.println("Error creating HTTP connection");

                initDialog.dismiss();
                this.publishProgress();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            new UnavailableDialog().show(mActivity.getFragmentManager(), "UnavailableDialog");
        }
    }

    private void swapProgressDialog() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                if (selection == 0 || selection == 6 || selection == 7 || selection == 8) {
                    initDialog.dismiss();
                }
                progressDialog = new MaterialDialog.Builder(mActivity)
                        .title(R.string.importing_from_server)
                        .content(importProgress)
                        .progress(false, maxProgressValue, false)
                        .cancelable(false)
                        .show();
            }
        });
    }

    private void progressUpdate() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                progressDialog.incrementProgress(1);
            }
        });
    }

    private void handleNearSelection() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                if (knownLocation.isNotDetected()) {
                    initDialog.dismiss();
                    new LocationWarningDialog().show(getFragmentManager(), "LocationWarningDialog");
                }
                else {
                    MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                            .title(R.string.select_radius)
                            .customView(R.layout.seekbar_dialog, false)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    selectedDistance = (seekBar.getProgress() + seekBarCorrection) * 1000;
                                    new backgroundTask().execute();
                                    swapProgressDialog();
                                }
                            })
                            .positiveText(android.R.string.ok)
                            .build();

                    seekBar = (SeekBar) dialog.findViewById(R.id.seekbar_seek);
                    seekBarText = (TextView) dialog.findViewById(R.id.seekbar_text);

                    units = " km";
                    String mLocale = getResources().getConfiguration().locale.getISO3Country();
                    if (mLocale.equalsIgnoreCase(Locale.US.getISO3Country())) {
                        units = " mi";
                    }

                    seekBarCorrection = 10;
                    seekBar.setMax(290);
                    seekBarProgress = 50;
                    seekBar.setProgress(seekBarProgress - seekBarCorrection);
                    seekBarText.setText((seekBar.getProgress() + seekBarCorrection) + units);

                    seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(seekBar, seekBarText,
                            seekBarCorrection, units));

                    initDialog.dismiss();
                    dialog.show();
                }
            }
        });
    }

    private void handleManualSelection() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                        .title(R.string.selecting_by_name)
                        .customView(R.layout.manual_selection_dialog, false)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                new backgroundTask().execute();
                                swapProgressDialog();
                            }

                        })
                        .positiveText(R.string.import_selected)
                        .build();

                ListView manualSelectionList = (ListView) dialog.findViewById(R.id.filtered_list_view);
                manualSelectionList.setEmptyView(dialog.findViewById(R.id.empty_info_text));
                manualSelectionAdapter = new ManualSelectionAdapter(mActivity, importWebCams);
                manualSelectionList.setAdapter(manualSelectionAdapter);

                filterBox = (EditText) dialog.findViewById(R.id.ms_filter);
                filterBox.addTextChangedListener(new OnFilterTextChange(manualSelectionAdapter));

                CheckBox chkAll = (CheckBox) dialog.findViewById(R.id.chkAll);
                chkAll.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        CheckBox chk = (CheckBox) v;
                        if (chk.isChecked()) {
                            manualSelectionAdapter.setAllChecked();
                        } else manualSelectionAdapter.setAllUnChecked();
                    }
                });

                initDialog.dismiss();
                dialog.show();
            }
        });
    }

    private void handleCountrySelection() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                        .title(R.string.countries)
                        .adapter(new CountryAdapter(mActivity, countryList),
                                new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        Country country = countryList.get(which);
                                        countryCode = country.getCountryCode();
                                        countryName = country.getCountryName();
                                        new backgroundTask().execute();
                                        dialog.dismiss();
                                        swapProgressDialog();
                                    }
                                })
                        .build();

                initDialog.dismiss();
                dialog.show();
            }
        });
    }

    private void handleTypeSelection() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                        .title(R.string.types)
                        .adapter(new TypeAdapter(mActivity, typeList),
                                new MaterialDialog.ListCallback() {
                                    @Override
                                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        type = typeList.get(which);
                                        new backgroundTask().execute();
                                        dialog.dismiss();
                                        swapProgressDialog();
                                    }
                                })
                        .build();

                initDialog.dismiss();
                dialog.show();
            }
        });
    }

    private void handleMapSelection() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                        .title(R.string.selecting_from_map)
                        .customView(R.layout.maps_dialog_layout, false)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                new backgroundTask().execute();
                                swapProgressDialog();
                            }

                        })
                        .positiveText(R.string.import_selected)
                        .build();

                mMapView = (MapView) dialog.findViewById(R.id.mapView);
                LatLng latLng = new LatLng(knownLocation.getLatitude(), knownLocation.getLongitude());
                mMapView.setCenter(latLng);
                if (knownLocation.isNotDetected()) {
                    mMapView.setZoom(3);
                } else mMapView.setZoom(8);

                for (Marker marker : markers) {
                    mMapView.addMarker(marker);
                }

                mMapView.setDiskCacheEnabled(false);

                ImageButton zoomIn = (ImageButton) dialog.findViewById(R.id.zoomIn);
                zoomIn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mMapView.getController().zoomIn();
                    }
                });

                ImageButton zoomOut = (ImageButton) dialog.findViewById(R.id.zoomOut);
                zoomOut.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        mMapView.getController().zoomOut();
                    }
                });

                selMarkers = new ArrayList<>();
                mMapView.addItemizedOverlay(new ItemizedIconOverlay(mActivity, markers,
                        new ItemizedIconOverlay.OnItemGestureListener<Marker>() {

                            @Override
                            public boolean onItemSingleTapUp(int i, Marker marker) {
                                InfoWindow tooltip = marker.getToolTip(mMapView);

                                if (selMarkers.size() != 0) {
                                    if (selMarkers.contains(marker)) {
                                        //marker.closeToolTip();
                                        marker.setMarker(markerNotSelected);
                                        selMarkers.remove(marker);
                                    } else {
                                        selMarkers.add(marker);
                                        marker.setMarker(selectedMarker);
                                        marker.showBubble(tooltip, mMapView, true);
                                    }
                                } else {
                                    selMarkers.add(marker);
                                    marker.setMarker(selectedMarker);
                                    marker.showBubble(tooltip, mMapView, true);
                                }

                                return true;
                            }

                            @Override
                            public boolean onItemLongPress(int i, Marker marker) {
                                return true;
                            }
                        }));

                initDialog.dismiss();
                dialog.show();

                if (knownLocation.isNotDetected()) {
                    new LocationWarningDialog().show(getFragmentManager(), "LocationWarningDialog");
                }
            }
        });
    }

    private void handleAll() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                int count = importWebCams.size();
                String content = getString(R.string.all_webcams_confirmation_part1) + " " + count + " " +
                        getString(R.string.all_webcams_confirmation_part2) + " " + getString(R.string.are_you_sure);
                new MaterialDialog.Builder(mActivity)
                        .title(R.string.all_webcams)
                        .content(content)
                        .positiveText(R.string.Yes)
                        .negativeText(android.R.string.cancel)
                        .iconRes(R.drawable.warning)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                new backgroundTask().execute();
                                swapProgressDialog();
                            }
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                initDialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }

    private class backgroundTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            switch (selection) {
                case 1:
                    proceed(new Category("@drawable/icon_nearby",
                            mActivity.getString(R.string.nearby) + " " + Utils.getDateString()));
                    break;
                case 2:
                    proceed(new Category("@drawable/icon_selected",
                            mActivity.getString(R.string.selected) + " " + Utils.getDateString()));
                    break;
                case 3:
                    proceed(new Category("@drawable/icon_country",
                            countryName + " " + Utils.getDateString()));
                    break;
                case 4:
                    proceed(new Category("@drawable/icon_" + type.getIconName(), type.getTypeName() +
                            " " + Utils.getDateString()));
                    break;
                case 5:
                    List<WebCam> tempList = new ArrayList<>();
                    for (Marker marker : markers) {
                        if (selMarkers.contains(marker)) {
                            WebCam webCam = importWebCams.get(markers.indexOf(marker));
                            tempList.add(webCam);
                        }
                    }
                    importWebCams = tempList;

                    proceed(new Category("@drawable/icon_map", mActivity.getString(R.string.from_map) + " " +
                            Utils.getDateString()));
                    break;
                case 7:
                    proceed(new Category("@drawable/icon_all_imported",
                            getString(R.string.all) + " " + Utils.getDateString()));
                    break;
            }

            return null;
        }
    }

    private void proceed(Category category) {

        synchronized (sDataLock) {
            List<Category> categoriesFromDb = db.getAllCategories();
            int newCategory = db.createCategory(category);
            for (WebCam webCam : importWebCams) {

                boolean condition;
                long differenceBetweenLastFetch;
                switch (selection) {
                    case 0:
                        differenceBetweenLastFetch = lastFetchLatest - webCam.getDateModifiedMillisecond();
                        condition = differenceBetweenLastFetch < 0;
                        break;
                    case 1:
                        float[] distance = new float[1];
                        Location.distanceBetween(webCam.getLatitude(), webCam.getLongitude(),
                                knownLocation.getLatitude(), knownLocation.getLongitude(), distance);
                        condition = distance[0] < selectedDistance;
                        break;
                    case 2:
                        condition = webCam.isSelected();
                        break;
                    case 3:
                        condition = webCam.getCountry().equals(countryCode);
                        break;
                    case 4:
                        condition = webCam.getStatus() == type.getId();
                        break;
                    case 8:
                        differenceBetweenLastFetch = lastFetchLatest - webCam.getDateModifiedMillisecond();
                        condition = differenceBetweenLastFetch < 0;
                        break;
                    default:
                        // case: 5,6,7
                        condition = true;
                        break;
                }

                if (condition) {
                    lastFetchNewWebCams = true;
                    if (allWebCams.size() != 0) {
                        boolean found = false;
                        for (WebCam allWebCam : allWebCams) {
                            if (webCam.getUniId() == allWebCam.getUniId()) {
                                if (webCam.getDateModifiedMillisecond() == allWebCam.getDateModifiedFromDb()) {
                                    db.createWebCamCategory(allWebCam.getId(), newCategory);
                                    duplicityWebCams++;
                                }
                                else {
                                    db.updateWebCamFromJson(allWebCam, webCam, newCategory);
                                    updatedWebCams++;
                                }
                                found = true;
                            }
                        }
                        if (!found) {
                            db.createWebCam(webCam, Collections.singletonList(newCategory));
                            newWebCams++;
                        }
                    }
                    else {
                        db.createWebCam(webCam, Collections.singletonList(newCategory));
                        newWebCams++;
                    }
                }
                progressUpdate();
            }

            if (!lastFetchNewWebCams) {
                db.deleteCategory(newCategory, false);
            }
            else if (selection == 0 || selection == 8) {

                String compare;
                if (selection == 0) {
                    compare = mActivity.getString(R.string.popular);
                }
                else compare = mActivity.getString(R.string.latest);
                for (Category categoryFromDb : categoriesFromDb) {
                    if (categoryFromDb.getCategoryName().contains(compare)){
                        db.deleteCategory(categoryFromDb.getId(), false);
                    }
                }

                SharedPreferences.Editor editor = preferences.edit();
                if (selection == 0) {
                    editor.putLong("pref_last_fetch_popular", Utils.getDate());
                }
                else editor.putLong("pref_last_fetch_latest", Utils.getDate());
                editor.apply();
            }

            showResult();
        }
        db.closeDB();
        backupManager.dataChanged();
    }

    private void showResult() {

        mActivity.runOnUiThread(new Runnable() {
            public void run() {

                progressDialog.dismiss();
                if (lastFetchNewWebCams) {
                    mListener = (ReloadInterface) mActivity;
                    mListener.invokeReload();
                    showReportDialog();
                }
                else {
                    if (selection == 1) {
                        noNearbyWebCamsDialog();
                    }
                    else if (selection == 2 || selection == 5) {
                        new NothingSelectedDialog().show(mActivity.getFragmentManager(), "NothingSelectedDialog");
                    }
                    else noNewWebCamsDialog();
                }
            }
        });
    }

    private void showReportDialog() {
        DialogFragment reportDialog = new ReportDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("newWebCams", newWebCams);
        bundle.putInt("duplicityWebCams", duplicityWebCams);
        bundle.putInt("updatedWebCams", updatedWebCams);
        reportDialog.setArguments(bundle);
        reportDialog.show(mActivity.getFragmentManager(), "ReportDialog");
    }

    private void noNearbyWebCamsDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.no_nearby_webcams)
                .content(R.string.no_nearby_webcams_summary)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.warning)
                .show();
    }

    private void noNewWebCamsDialog() {
        new MaterialDialog.Builder(mActivity)
                .title(R.string.no_new_webcams)
                .content(R.string.no_new_webcams_summary)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.settings_about)
                .show();
    }
}
