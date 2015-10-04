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

package cz.yetanotherview.webcamviewer.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.yetanotherview.webcamviewer.app.actions.SelectionDialog;
import cz.yetanotherview.webcamviewer.app.actions.SuggestionDialog;
import cz.yetanotherview.webcamviewer.app.actions.WelcomeDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.LocationWarningDialog;
import cz.yetanotherview.webcamviewer.app.adapter.ManualSelectionAdapter;
import cz.yetanotherview.webcamviewer.app.fragments.BaseFragment;
import cz.yetanotherview.webcamviewer.app.fragments.MapAppBarFragment;
import cz.yetanotherview.webcamviewer.app.fragments.SearchAppBarFragment;
import cz.yetanotherview.webcamviewer.app.fragments.StandardAppBarFragment;
import cz.yetanotherview.webcamviewer.app.fragments.StandardLocalAppBarFragment;
import cz.yetanotherview.webcamviewer.app.fragments.TabHolderFragment;
import cz.yetanotherview.webcamviewer.app.help.HelpActivity;
import cz.yetanotherview.webcamviewer.app.helper.ClearImageCache;
import cz.yetanotherview.webcamviewer.app.helper.OnFilterTextChange;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.settings.SettingsActivity;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.WebCam;
import cz.yetanotherview.webcamviewer.app.util.Navigator;

public class MainActivity extends AppCompatActivity { //implements WebCamListener, JsonFetcherDialog.ReloadInterface

    private DatabaseHelper db;
    private WebCam webCam, webCamToDelete;
    private List<Integer> webCamToDelete_category_ids;
    private List<WebCam> reallyAllWebCams;
    private ImageView toolbarImage;
    private ManualSelectionAdapter manualSelectionAdapter;
    private int numberOfColumns, selectedCategory, autoRefreshInterval,
            webCamToDeletePosition, selectedCategoryId, latestCategoryPos;
    private boolean firstRun, autoRefresh, autoRefreshFullScreenOnly, hwAcceleration,
            screenAlwaysOn, imagesOnOff, latestCategory;
    private String sortOrder;
    private FloatingActionButton floatingActionButtonNative;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbar;

    private MaterialDialog materialDialog, indeterminateProgress;
    private MenuItem searchItem;
    private SearchView searchView;
    //private NavigationDrawerFragment mNavigationDrawerFragment;

    private static Navigator mNavigator;

    //private Drawer mDrawer = null;
    private DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // loading saved preferences
//        loadPref();

        // Inflating main layout
        setContentView(R.layout.activity_main);

        //ButterKnife.bind(this);

//        // Auto Refreshing
//        if (autoRefresh && !autoRefreshFullScreenOnly) {
//            autoRefreshTimer(autoRefreshInterval);
//        }
//
//        // Screen Always on
//        if (screenAlwaysOn){
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
//        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Init DatabaseHelper for late use
        db = new DatabaseHelper(getApplicationContext());




        // Other core init
        setupToolbar();
        setupNavDrawer();
        initNavigator();

        //ToDo
        setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.latest_webcams, getString(R.string.latest_webcams));

        //loadLastSelectedCategory();
        //initRecyclerView();
        //initFab();
        //initFirstRun();
        //initReceivedIntent();

    }

    @Override
    public void onPause() {
        super.onPause();
//        if (materialDialog != null) {
//            materialDialog.dismiss();
//        }
//        if (latestCategory) {
//            latestCategoryPos = selectedCategory;
//            saveToPref();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //reInitializeRecyclerViewAdapter();
        //reInitializeDrawerListAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.deleteTmpCache();
        new ClearImageCache(this).execute();
    }

    @Override
    public void finish() {
        mNavigator = null;
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setupToolbar() {
        //mToolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(mToolbar);
        //collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        //toolbarImage = (ImageView) findViewById(R.id.toolbar_image);
        //appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
    }

    public ImageView getToolbarImage() {
        return toolbarImage;
    }

    private void setupNavDrawer() {

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                menuItem.setChecked(true);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                String title = menuItem.getTitle().toString();
                switch (menuItem.getItemId()) {
                    case R.id.latest_webcams:
                        setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.latest_webcams, title);
                        return true;
                    case R.id.popular_webcams:
                        setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.popular_webcams, title);
                        return true;
                    case R.id.nearby_webcams:
                        setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.nearby_webcams, title);
                        return true;
                    case R.id.selecting_by_name:
                        setNewRootFragment(SearchAppBarFragment.newInstance(), R.id.selecting_by_name, title);
                        return true;
                    case R.id.selecting_by_country:
                        //setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.selecting_by_country, title);
                        return true;
                    case R.id.selecting_by_type:
                        setNewRootFragment(TabHolderFragment.newInstance(), R.id.selecting_by_type, title);
                        return true;
                    case R.id.live_streams:
                        setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.live_streams, title);
                        return true;
                    case R.id.selecting_from_map:
                        setNewRootFragment(MapAppBarFragment.newInstance(), R.id.selecting_from_map, title);
                        return true;
                    case R.id.favorites_webcams:
                        //setNewRootFragment(StandardAppBarFragment.newInstance(), R.id.favorites_webcams, title);
                        return true;
                    case R.id.all_local_webcams:
                        setNewRootFragment(StandardLocalAppBarFragment.newInstance(), R.id.all_local_webcams, title);
                        return true;
                    case R.id.action_settings:
                        openSettings();
                        return true;
                    case R.id.action_menu_help:
                        openHelp();
                        return true;
                    default:
                        return true;
                }
            }
        });
    }

    private void initNavigator() {
        if(mNavigator != null) return;
        mNavigator = new Navigator(getSupportFragmentManager(), R.id.container);
    }

    public void openDrawer(){
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    private void setNewRootFragment(BaseFragment fragment, int id, String title) {
        mNavigator.setRootFragment(fragment, id, title);
    }

    private void loadLastSelectedCategory() {
        if (latestCategory) {
            //mNavigationDrawerFragment.selectItem(latestCategoryPos, false);
        }
    }

    private void initFab() {

//        floatingActionButtonNative = (FloatingActionButton) findViewById(R.id.small_floating_action_button);
//        floatingActionButtonNative.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                assignSelectedWebCamsToCategory();
//            }
//        });
    }

    private void initFirstRun() {
        if (firstRun){
            showWelcomeDialog();
            //mNavigationDrawerFragment.openDrawer();
            firstRun = false;
            saveToPref();
        }
    }

    private void initReceivedIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                //handleReceivedUrl(intent);
            }
        }
    }

//    private void handleReceivedUrl(Intent intent) {
//        String sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
//        if (sharedUrl != null) {
//            DialogFragment dialogFragment = AddDialog.newInstance(this);
//
//            Bundle bundle = new Bundle();
//            bundle.putString("sharedUrl", sharedUrl);
//            dialogFragment.setArguments(bundle);
//
//            dialogFragment.show(getFragmentManager(), "AddDialog");
//        }
//    }

//    @Override
//    public void onNavigationDrawerItemSelected(int position, int categoryId) {
//        selectedCategory = position;
//        selectedCategoryId = categoryId;
//        if (mAdapter != null) {
//            reInitializeRecyclerViewAdapter();
//            floatingActionMenu.showMenuButton(true);
//            if (mAdapter.getItemCount() != 0) {
//                appBarLayout.setExpanded(true, false);
//            }
//        }
//    }

//    private void reInitializeRecyclerViewAdapter() {
//        if (db.getWebCamCount() != 0) {
//            if (selectedCategory == 0) {
//                allWebCams = db.getAllWebCams(sortOrder);
//                mAdapter.swapData(allWebCams);
//            }
//            else {
//                allWebCams = db.getAllWebCamsByCategory(selectedCategoryId, sortOrder);
//                mAdapter.swapData(allWebCams);
//            }
//            saveToPref();
//        }
//        db.closeDB();
//    }

    private void reInitializeDrawerListAdapter() {
        //mNavigationDrawerFragment.reloadData();
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    private void openHelp() {
        Intent helpIntent = new Intent(this, HelpActivity.class);
        startActivity(helpIntent);
    }

    private void showSortDialog() {

        int whatMarkToCheck = 0;
        if (sortOrder.contains("position")) {whatMarkToCheck = 0;}
        else if (sortOrder.contains(" ) ASC")) {whatMarkToCheck = 1;}
        else if (sortOrder.contains(" ) DESC")) {whatMarkToCheck = 2;}
        else if (sortOrder.contains("created_at ASC")) {whatMarkToCheck = 3;}
        else if (sortOrder.contains("created_at DESC")) {whatMarkToCheck = 4;}
        else if (sortOrder.contains("UNICODE ASC")) {whatMarkToCheck = 5;}
        else if (sortOrder.contains("UNICODE DESC")) {whatMarkToCheck = 6;}

        materialDialog = new MaterialDialog.Builder(this)
                .title(R.string.action_sort)
                .items(R.array.sort_values)
                .itemsCallbackSingleChoice(whatMarkToCheck, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {

                        KnownLocation knownLocation;
                        Double fudge;
                        switch (which) {
                            case 0:
                                sortOrder = "position";
                                break;
                            case 1:
                                knownLocation = Utils.getLastKnownLocation(MainActivity.this);
                                fudge = Math.pow(Math.cos(Math.toRadians(knownLocation.getLatitude())), 2);

                                sortOrder = "((" + knownLocation.getLatitude() + " - latitude) * (" +
                                        knownLocation.getLatitude() + " - latitude) + (" + knownLocation.getLongitude() +
                                        " - longitude) * (" + knownLocation.getLongitude() + " - longitude) * " + fudge + " ) ASC";
                                if (knownLocation.isNotDetected()) {
                                    new LocationWarningDialog().show(getFragmentManager(), "LocationWarningDialog");
                                }
                                break;
                            case 2:
                                knownLocation = Utils.getLastKnownLocation(MainActivity.this);
                                fudge = Math.pow(Math.cos(Math.toRadians(knownLocation.getLatitude())), 2);

                                sortOrder = "((" + knownLocation.getLatitude() + " - latitude) * (" +
                                        knownLocation.getLatitude() + " - latitude) + (" + knownLocation.getLongitude() +
                                        " - longitude) * (" + knownLocation.getLongitude() + " - longitude) * " + fudge + " ) DESC";
                                if (knownLocation.isNotDetected()) {
                                    new LocationWarningDialog().show(getFragmentManager(), "LocationWarningDialog");
                                }
                                break;
                            case 3:
                                sortOrder = "created_at ASC";
                                break;
                            case 4:
                                sortOrder = "created_at DESC";
                                break;
                            case 5:
                                sortOrder = "webcam_name COLLATE UNICODE ASC";
                                break;
                            case 6:
                                sortOrder = "webcam_name COLLATE UNICODE DESC";
                                break;
                            default:
                                break;
                        }
                        //reInitializeRecyclerViewAdapter();
                        saveToPref();

                        return true;
                    }
                })
                .negativeText(R.string.close)
                .show();
    }

    private void showWelcomeDialog() {
        new WelcomeDialog().show(getFragmentManager(), "WelcomeDialog");
    }

    private void openAfterDelay(final int which) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (which) {
                    case 0:
                        new SelectionDialog().show(getFragmentManager(), "SelectionDialog");
                        break;
                    case 1:
                        //AddDialog.newInstance(MainActivity.this).show(getFragmentManager(), "AddDialog");
                        break;
                    case 2:
                        new SuggestionDialog().show(getFragmentManager(), "SuggestionDialog");
                        break;
                    default:
                        break;
                }
            }
        }, 50);
    }

//    @Override
//    public void webCamAdded(WebCam wc, List<Integer> category_ids, boolean share) {
//        if (category_ids != null) {
//            wc.setId(db.createWebCam(wc, category_ids));
//        }
//        else wc.setId(db.createWebCam(wc, null));
//        db.closeDB();
//
//        mAdapter.addItem(mAdapter.getItemCount(), wc);
//        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
//
//        reInitializeDrawerListAdapter();
//
//        if (share) {
//            new SendToInbox().sendToInboxWebCam(this, wc, false, -1);
//        }
//        else saveDone();
//    }
//
//    @Override
//    public void webCamEdited(int position, WebCam wc, List<Integer> category_ids) {
//        if (category_ids != null) {
//            db.updateWebCam(wc, category_ids);
//        } else db.updateWebCam(wc, null);
//        db.closeDB();
//
//        mAdapter.modifyItem(position, wc);
//        reInitializeDrawerListAdapter();
//
//        saveDone();
//    }
//
//    @Override
//    public void webCamDeleted(final WebCam wc, final int position) {
//        webCamToDelete = wc;
//        webCamToDelete_category_ids = db.getWebCamCategoriesIds(webCamToDelete.getId());
//        webCamToDeletePosition = position;
//
//        if (mAdapter != null && mAdapter.getItemCount() > 0) {
//            mAdapter.removeItem(mAdapter.getItemAt(webCamToDeletePosition));
//
//            db.deleteWebCam(webCamToDelete.getId());
//            db.closeDB();
//            reInitializeDrawerListAdapter();
//        }
//
//        reInitializeDrawerListAdapter();
//
//        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.undo,
//                Snackbar.LENGTH_LONG)
//                .setAction(R.string.undo, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        mAdapter.addItem(webCamToDeletePosition, webCamToDelete);
//                        db.undoDeleteWebCam(webCamToDelete, webCamToDelete_category_ids);
//                        db.closeDB();
//                        reInitializeDrawerListAdapter();
//                        //ToDo: Only temporary solution until FloatingActionMenuBehavior don't work correctly
//                        floatingActionMenu.showMenuButton(true);
//                    }
//                })
//                .show();
//        temporaryHideFab(false);
//    }

//    @Override
//    public void invokeReload() {
//        reInitializeRecyclerViewAdapter();
//        reInitializeDrawerListAdapter();
//        //mNavigationDrawerFragment.openDrawer();
//        //mNavigationDrawerFragment.selectPosition();
//    }

    private void assignSelectedWebCamsToCategory() {
        reallyAllWebCams = db.getAllWebCams(sortOrder);
        if (reallyAllWebCams.size() > 0) {
            if (selectedCategory != 0) {
                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(db.getCategory(selectedCategoryId).getCategoryName())
                        .customView(R.layout.manual_selection_dialog, false)
                        .positiveText(R.string.assign_selected)
                        .iconRes(R.drawable.edit)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                showIndeterminateProgress();
                                new assignSelectedWebCamsToCategoryBackgroundTask().execute();
                            }
                        })
                        .build();

                ListView assignSelectionList = (ListView) dialog.findViewById(R.id.filtered_list_view);
                assignSelectionList.setEmptyView(dialog.findViewById(R.id.empty_info_text));
                manualSelectionAdapter = new ManualSelectionAdapter(this, reallyAllWebCams);
                manualSelectionAdapter.setSelected(db.getAllWebCamsByCategory(selectedCategoryId, sortOrder));
                assignSelectionList.setAdapter(manualSelectionAdapter);

                EditText filterBox = (EditText) dialog.findViewById(R.id.ms_filter);
                filterBox.setHint(R.string.enter_name);
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

                dialog.show();
            };

        } else listIsEmpty();
        db.closeDB();
    }



    private class assignSelectedWebCamsToCategoryBackgroundTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            for (WebCam webCam : reallyAllWebCams) {
                List<Integer> webCamCategoriesIds = db.getWebCamCategoriesIds(webCam.getId());
                if (webCam.isSelected()) {
                    if (!webCamCategoriesIds.contains(selectedCategoryId)) {
                        webCamCategoriesIds.add(selectedCategoryId);
                    }
                }
                else {
                    if (webCamCategoriesIds.contains(selectedCategoryId)) {
                        webCamCategoriesIds.remove((Integer)selectedCategoryId);
                    }
                }
                db.updateWebCam(webCam, webCamCategoriesIds);
            }
            db.closeDB();
            this.publishProgress();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            //reInitializeRecyclerViewAdapter();
            reInitializeDrawerListAdapter();
            indeterminateProgress.dismiss();
            saveDone();
        }
    }

    private void showIndeterminateProgress() {
        indeterminateProgress = new MaterialDialog.Builder(this)
                .content(R.string.please_wait)
                .progress(true, 0)
                .show();
    }


    private void saveDone() {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.dialog_positive_toast_message,
                Snackbar.LENGTH_SHORT).show();
    }

    private void listIsEmpty() {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.list_is_empty,
                Snackbar.LENGTH_SHORT).show();
    }

//    private void refresh() {
//        if (mAdapter.getItemCount() != 0) {
//            mStringSignature = UUID.randomUUID().toString();
//            mAdapter.refreshViewImages(new StringSignature(mStringSignature));
//        }
//    }


    private void autoRefreshTimer(int interval) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            //refresh();
                        } catch (Exception ignored) {}
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, interval);
    }

    private void loadPref(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        firstRun = preferences.getBoolean("pref_first_run", true);
        numberOfColumns = preferences.getInt("number_of_columns", 1);
        imagesOnOff = preferences.getBoolean("pref_images_on_off", true);
        sortOrder = preferences.getString("pref_sort_order", Utils.defaultSortOrder);
        autoRefresh = preferences.getBoolean("pref_auto_refresh", false);
        autoRefreshInterval = preferences.getInt("pref_auto_refresh_interval", 30000);
        autoRefreshFullScreenOnly = preferences.getBoolean("pref_auto_refresh_fullscreen", false);
        hwAcceleration = preferences.getBoolean("pref_screen_hw_acceleration", true);
        screenAlwaysOn = preferences.getBoolean("pref_screen_always_on", false);
        latestCategory = preferences.getBoolean("pref_last_category", false);
        latestCategoryPos = preferences.getInt("pref_last_category_pos", 0);
    }

    private void saveToPref(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("pref_first_run", firstRun);
        editor.putInt("number_of_columns", numberOfColumns);
        editor.putBoolean("pref_images_on_off", imagesOnOff);
        editor.putString("pref_sort_order", sortOrder);
        editor.putInt("pref_last_category_pos", latestCategoryPos);
        editor.apply();
    }
}
