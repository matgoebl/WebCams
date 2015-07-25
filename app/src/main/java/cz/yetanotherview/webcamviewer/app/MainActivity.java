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

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.signature.StringSignature;
import com.github.clans.fab.FloatingActionMenu;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.actions.AddDialog;
import cz.yetanotherview.webcamviewer.app.actions.EditDialog;
import cz.yetanotherview.webcamviewer.app.actions.JsonFetcherDialog;
import cz.yetanotherview.webcamviewer.app.actions.SaveDialog;
import cz.yetanotherview.webcamviewer.app.actions.SelectionDialog;
import cz.yetanotherview.webcamviewer.app.actions.ShareDialog;
import cz.yetanotherview.webcamviewer.app.actions.SuggestionDialog;
import cz.yetanotherview.webcamviewer.app.actions.WelcomeDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.LocationWarningDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.NoCoordinatesDialog;
import cz.yetanotherview.webcamviewer.app.adapter.ManualSelectionAdapter;
import cz.yetanotherview.webcamviewer.app.drawer.NavigationDrawerCallbacks;
import cz.yetanotherview.webcamviewer.app.drawer.NavigationDrawerFragment;
import cz.yetanotherview.webcamviewer.app.fullscreen.FullScreenActivity;
import cz.yetanotherview.webcamviewer.app.adapter.WebCamAdapter;
import cz.yetanotherview.webcamviewer.app.help.HelpActivity;
import cz.yetanotherview.webcamviewer.app.helper.ClearImageCache;
import cz.yetanotherview.webcamviewer.app.helper.ControllableAppBarLayout;
import cz.yetanotherview.webcamviewer.app.helper.EmptyRecyclerView;
import cz.yetanotherview.webcamviewer.app.helper.OnFilterTextChange;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.settings.SettingsActivity;
import cz.yetanotherview.webcamviewer.app.stream.LiveStreamActivity;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.ImmersiveMode;
import cz.yetanotherview.webcamviewer.app.helper.SendToInbox;
import cz.yetanotherview.webcamviewer.app.listener.WebCamListener;
import cz.yetanotherview.webcamviewer.app.model.KnownLocation;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class MainActivity extends AppCompatActivity implements NavigationDrawerCallbacks, WebCamListener,
        JsonFetcherDialog.ReloadInterface, AppBarLayout.OnOffsetChangedListener {

    private DatabaseHelper db;
    private WebCam webCam, webCamToDelete;
    private List<Integer> webCamToDelete_category_ids;
    private List<WebCam> allWebCams, reallyAllWebCams;
    private EmptyRecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mLayoutManager;
    private View mTintView;
    private ImageView toolbarImage;
    private WebCamAdapter mAdapter;
    private ManualSelectionAdapter manualSelectionAdapter;
    private int numberOfColumns, mOrientation, selectedCategory, autoRefreshInterval, mPosition,
            webCamToDeletePosition, selectedCategoryId;
    private boolean firstRun, fullScreen, autoRefresh, autoRefreshFullScreenOnly, screenAlwaysOn,
            imagesOnOff, simpleList;
    private String mStringSignature, sortOrder;
    private FloatingActionMenu floatingActionMenu;
    private android.support.design.widget.FloatingActionButton floatingActionButtonNative;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar mToolbar;
    private CollapsingToolbarLayout collapsingToolbar;
    private ControllableAppBarLayout controllableAppBarLayout;
    private MaterialDialog materialDialog, indeterminateProgress;
    private MenuItem searchItem;
    private SearchView searchView;
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // loading saved preferences
        loadPref();

        // Inflating main layout
        if (fullScreen) {
            setContentView(R.layout.activity_main_immersive);
        }
        else setContentView(R.layout.activity_main);

        // Auto Refreshing
        if (autoRefresh && !autoRefreshFullScreenOnly) {
            autoRefreshTimer(autoRefreshInterval);
        }

        // Go FullScreen only on KitKat and up
        goToFullScreen();

        // Screen Always on
        if (screenAlwaysOn){
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Init DatabaseHelper for late use
        db = new DatabaseHelper(getApplicationContext());

        // Get current orientation
        mOrientation = getResources().getConfiguration().orientation;

        // New signature
        mStringSignature = UUID.randomUUID().toString();

        // Other core init
        initToolbar();
        initDrawer();
        initRecyclerView();
        initFab();
        initPullToRefresh();
        initFirstRun();
        initReceivedIntent();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (materialDialog != null) {
            materialDialog.dismiss();
        }
        controllableAppBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        controllableAppBarLayout.addOnOffsetChangedListener(this);
        reInitializeRecyclerViewAdapter();
        reInitializeDrawerListAdapter();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        new ClearImageCache(this).execute();
    }

    private void goToFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && fullScreen) {
            new ImmersiveMode().goFullScreen(this);
        }
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        toolbarImage = (ImageView) findViewById(R.id.toolbar_image);
        controllableAppBarLayout = (ControllableAppBarLayout) findViewById(R.id.controllable_app_bar_layout);
    }

    private void initDrawer() {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.fragment_drawer);
        mNavigationDrawerFragment.setup(R.id.fragment_drawer, (DrawerLayout) findViewById(R.id.drawer_layout),
                mToolbar, collapsingToolbar, toolbarImage);
    }

    private void initRecyclerView() {
        int mLayoutId = 1;
        if (numberOfColumns == 1 && mOrientation == 1) {
            mLayoutId = 1;
        }
        else if(numberOfColumns == 1 && mOrientation == 2) {
            mLayoutId = 2;
        }
        else if(numberOfColumns == 2 && mOrientation == 1) {
            mLayoutId = 2;
        }
        else if(numberOfColumns == 2 && mOrientation == 2) {
            mLayoutId = 3;
        }
        mLayoutManager = new StaggeredGridLayoutManager(mLayoutId, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.mainList);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setEmptyView(findViewById(R.id.list_empty));

        mRecyclerView.setLayoutManager(mLayoutManager);
        if (selectedCategory == 0) {
            allWebCams = db.getAllWebCams(sortOrder);
        }
        else allWebCams = db.getAllWebCamsByCategory(selectedCategoryId, sortOrder);
        db.closeDB();

        mAdapter = new WebCamAdapter(this, allWebCams, mOrientation, mLayoutId,
                new StringSignature(mStringSignature), imagesOnOff, simpleList);
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

    private void initFab() {
        floatingActionMenu = (FloatingActionMenu) findViewById(R.id.floating_action_menu);
        floatingActionMenu.setClosedOnTouchOutside(true);

        com.github.clans.fab.FloatingActionButton floatingActionButtonImport =
                (com.github.clans.fab.FloatingActionButton) findViewById(R.id.floating_action_button_import);
        com.github.clans.fab.FloatingActionButton floatingActionButtonManual =
                (com.github.clans.fab.FloatingActionButton) findViewById(R.id.floating_action_button_manual);
        com.github.clans.fab.FloatingActionButton floatingActionButtonSuggestion =
                (com.github.clans.fab.FloatingActionButton) findViewById(R.id.floating_action_button_suggestion);

        floatingActionButtonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAfterDelay(0);
                hideAfterDelay();
            }
        });
        floatingActionButtonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAfterDelay(1);
                hideAfterDelay();
            }
        });
        floatingActionButtonSuggestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAfterDelay(2);
                hideAfterDelay();
            }
        });

        floatingActionButtonNative = (android.support.design.widget.FloatingActionButton) findViewById(R.id.small_floating_action_button);
        floatingActionButtonNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                assignSelectedWebCamsToCategory();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int scrollLimit = 4;
                boolean scrollLimitReached = Math.abs(dy) >= scrollLimit;
                if (scrollLimitReached) {
                    boolean scrollUp = dy >= 0;
                    if (scrollUp) {
                        floatingActionMenu.hideMenuButton(true);
                    } else floatingActionMenu.showMenuButton(true);
                }
            }
        });

        floatingActionMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean b) {
                if (b) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !fullScreen) {
                        getWindow().setStatusBarColor(getResources().getColor(R.color.black_transparent));
                    }
                    floatingActionButtonNative.setVisibility(View.INVISIBLE);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !fullScreen) {
                        getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
                    }
                    floatingActionButtonNative.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void initPullToRefresh() {
        // Pull To Refresh 1/2
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.swipe, R.color.yellow);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2500);
            }
        });
    }

    private void initFirstRun() {
        if (firstRun){
            showWelcomeDialog();
            mNavigationDrawerFragment.openDrawer();
            // Save the state
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
                handleReceivedUrl(intent);
            }
        }
    }

    private void handleReceivedUrl(Intent intent) {
        String sharedUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedUrl != null) {
            DialogFragment dialogFragment = AddDialog.newInstance(this);

            Bundle bundle = new Bundle();
            bundle.putString("sharedUrl", sharedUrl);
            dialogFragment.setArguments(bundle);

            dialogFragment.show(getFragmentManager(), "AddDialog");
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, int categoryId) {
        selectedCategory = position;
        selectedCategoryId = categoryId;
        if (mAdapter != null) {
            reInitializeRecyclerViewAdapter();
            floatingActionMenu.showMenuButton(true);
            controllableAppBarLayout.expandToolbar();
        }
    }

    private void reInitializeRecyclerViewAdapter() {
        if (db.getWebCamCount() != 0) {
            if (selectedCategory == 0) {
                allWebCams = db.getAllWebCams(sortOrder);
                mAdapter.swapData(allWebCams);
            }
            else {
                allWebCams = db.getAllWebCamsByCategory(selectedCategoryId, sortOrder);
                mAdapter.swapData(allWebCams);
            }
            saveToPref();
        }
        db.closeDB();
    }

    private void reInitializeDrawerListAdapter() {
        mNavigationDrawerFragment.reloadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                controllableAppBarLayout.collapseToolbar(true);
                collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(android.R.color.transparent));
                goToFullScreen();
                searchView.setIconified(false);
                searchView.requestFocus();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                controllableAppBarLayout.expandToolbar(true);
                collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.white));
                goToFullScreen();
                searchView.clearFocus();
                return true;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchItem.collapseActionView();
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.filter(newText);
                return true;
            }
        });

        MenuItem dashboard = menu.findItem(R.id.action_dashboard);
        if (numberOfColumns == 1) {
            dashboard.setIcon(R.drawable.ic_action_dashboard);
        }
        else dashboard.setIcon(R.drawable.ic_action_view_day);

        MenuItem imagesOnOffItem = menu.findItem(R.id.action_image_on_off);
        if (imagesOnOff) {
            imagesOnOffItem.setTitle(R.string.images_off);
            imagesOnOffItem.setIcon(R.drawable.ic_action_image_off);
        }
        else {
            imagesOnOffItem.setTitle(R.string.images_on);
            imagesOnOffItem.setIcon(R.drawable.ic_action_image_on);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_search:
                return super.onOptionsItemSelected(item);

            case R.id.action_refresh:
                refresh();
                break;

            case R.id.action_dashboard:
                if (numberOfColumns == 1) {
                    numberOfColumns = 2;
                    item.setIcon(R.drawable.ic_action_view_day);
                }
                else if (numberOfColumns == 2) {
                    numberOfColumns = 1;
                    item.setIcon(R.drawable.ic_action_dashboard);
                }
                initRecyclerView();
                saveToPref();
                break;

            case R.id.action_sort:
                showSortDialog();
                break;

            case R.id.action_image_on_off:
                if (imagesOnOff) {
                    imagesOnOff = false;
                    item.setTitle(R.string.images_on);
                    item.setIcon(R.drawable.ic_action_image_on);
                } else {
                    imagesOnOff = true;
                    item.setTitle(R.string.images_off);
                    item.setIcon(R.drawable.ic_action_image_off);
                }
                initRecyclerView();
                saveToPref();
                break;

            case R.id.action_settings:
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SettingsActivity.class);
                startActivityForResult(intent, 0);
                break;

            case R.id.menu_help:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSearchRequested() {
        MenuItemCompat.expandActionView(searchItem);
        searchView.requestFocus();
        return true;
    }

    @Override
    public void onOffsetChanged(AppBarLayout controllableAppBarLayout, int i) {
        if (i == 0) {
            swipeRefreshLayout.setEnabled(true);
        } else swipeRefreshLayout.setEnabled(false);
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
                                knownLocation = Utils.getLastKnownLocation(getApplicationContext());
                                fudge = Math.pow(Math.cos(Math.toRadians(knownLocation.getLatitude())), 2);

                                sortOrder = "((" + knownLocation.getLatitude() + " - latitude) * (" +
                                        knownLocation.getLatitude() + " - latitude) + (" + knownLocation.getLongitude() +
                                        " - longitude) * (" + knownLocation.getLongitude() + " - longitude) * " + fudge + " ) ASC";
                                if (knownLocation.isNotDetected()) {
                                    new LocationWarningDialog().show(getFragmentManager(), "LocationWarningDialog");
                                }
                                break;
                            case 2:
                                knownLocation = Utils.getLastKnownLocation(getApplicationContext());
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
                        reInitializeRecyclerViewAdapter();
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

    private void maximizeImageOrPlayStream(int position, boolean map, boolean fromEditClick) {
        webCam = (WebCam) mAdapter.getItem(position);

        if (webCam.isStream() && !map) {
            if (fromEditClick) {
                new MaterialDialog.Builder(MainActivity.this)
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
    }

    private void playStream() {
        Intent intent = new Intent(this, LiveStreamActivity.class);
        intent.putExtra("url", webCam.getUrl());
        intent.putExtra("name", webCam.getName());
        intent.putExtra("fullScreen", fullScreen);
        startActivity(intent);
    }

    private void maximizeImage(boolean map, boolean preview) {
        Intent intent = new Intent(this, FullScreenActivity.class);
        intent.putExtra("signature", mStringSignature);
        intent.putExtra("map", map);
        intent.putExtra("name", webCam.getName());
        String url = webCam.getUrl();
        if (preview) url = webCam.getThumbUrl();
        intent.putExtra("url", url);
        intent.putExtra("latitude", webCam.getLatitude());
        intent.putExtra("longitude", webCam.getLongitude());
        intent.putExtra("fullScreen", fullScreen);
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
            else new NoCoordinatesDialog().show(getFragmentManager(), "NoCoordinatesDialog");
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

        materialDialog = new MaterialDialog.Builder(this)
                .items(options_values)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Bundle bundle = new Bundle();
                        switch (which) {
                            case 0:
                                refreshSelected(position);
                                break;
                            case 1:
                                showEditDialog(position);
                                break;
                            case 2:
                                webCamDeleted(webCam, position);
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
                                saveDialog.show(getFragmentManager(), "SaveDialog");
                                break;
                            case 6:
                                ShareDialog shareDialog = new ShareDialog();
                                if (webCam.isStream()) {
                                    bundle.putString("url", webCam.getThumbUrl());
                                } else bundle.putString("url", webCam.getUrl());
                                shareDialog.setArguments(bundle);
                                shareDialog.show(getFragmentManager(), "ShareDialog");
                                break;
                            case 7:
                                maximizeImageOrPlayStream(position, true, true);
                                break;
                            case 8:
                                if (webCam.getUniId() != 0) {
                                    new MaterialDialog.Builder(MainActivity.this)
                                            .title(R.string.report_problem)
                                            .content(R.string.report_problem_summary)
                                            .positiveText(R.string.send)
                                            .negativeText(android.R.string.cancel)
                                            .iconRes(R.drawable.settings_about)
                                            .items(R.array.whats_wrong)
                                            .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                                @Override
                                                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                                    new SendToInbox().sendToInboxWebCam(MainActivity.this, webCam, true, which);
                                                    return true;
                                                }
                                            })
                                            .show();
                                } else new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.submit_as_suggestion)
                                        .content(R.string.community_list_summary)
                                        .positiveText(R.string.Yes)
                                        .negativeText(android.R.string.cancel)
                                        .iconRes(R.drawable.settings_about)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                new SendToInbox().sendToInboxWebCam(MainActivity.this, webCam, false, -1);
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

    private void openAfterDelay(final int which) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (which) {
                    case 0:
                        new SelectionDialog().show(getFragmentManager(), "SelectionDialog");
                        break;
                    case 1:
                        AddDialog.newInstance(MainActivity.this).show(getFragmentManager(), "AddDialog");
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

    private void hideAfterDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                floatingActionMenu.close(true);
            }
        }, 500);
    }

    private void showEditDialog(int position) {
        DialogFragment dialogFragment = EditDialog.newInstance(this);

        webCam = (WebCam) mAdapter.getItem(position);
        Bundle bundle = new Bundle();
        bundle.putLong("id", webCam.getId());
        bundle.putInt("position", position);
        dialogFragment.setArguments(bundle);

        dialogFragment.show(getFragmentManager(), "EditDialog");
    }

    @Override
    public void webCamAdded(WebCam wc, List<Integer> category_ids, boolean share) {
        if (category_ids != null) {
            wc.setId(db.createWebCam(wc, category_ids));
        }
        else wc.setId(db.createWebCam(wc, null));
        db.closeDB();

        mAdapter.addItem(mAdapter.getItemCount(), wc);
        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);

        reInitializeDrawerListAdapter();

        if (share) {
            new SendToInbox().sendToInboxWebCam(this, wc, false, -1);
        }
        else saveDone();
    }

    @Override
    public void webCamEdited(int position, WebCam wc, List<Integer> category_ids) {
        if (category_ids != null) {
            db.updateWebCam(wc, category_ids);
        } else db.updateWebCam(wc, null);
        db.closeDB();

        mAdapter.modifyItem(position, wc);
        reInitializeDrawerListAdapter();

        saveDone();
    }

    @Override
    public void webCamDeleted(final WebCam wc, final int position) {
        webCamToDelete = wc;
        webCamToDelete_category_ids = db.getWebCamCategoriesIds(webCamToDelete.getId());
        webCamToDeletePosition = position;

        if (mAdapter != null && mAdapter.getItemCount() > 0) {
            mAdapter.removeItem(mAdapter.getItemAt(webCamToDeletePosition));

            db.deleteWebCam(webCamToDelete.getId());
            db.closeDB();
            reInitializeDrawerListAdapter();
        }

        reInitializeDrawerListAdapter();

        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.undo,
                Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mAdapter.addItem(webCamToDeletePosition, webCamToDelete);
                        db.undoDeleteWebCam(webCamToDelete, webCamToDelete_category_ids);
                        db.closeDB();
                        reInitializeDrawerListAdapter();
                        //ToDo: Only until it's officially added to Clans/FloatingActionButton
                        floatingActionMenu.showMenuButton(true);
                    }
                })
                .show();
        temporaryHideFab(false);
    }

    @Override
    public void invokeReload() {
        reInitializeRecyclerViewAdapter();
        reInitializeDrawerListAdapter();
        mNavigationDrawerFragment.openDrawer();
        mNavigationDrawerFragment.selectPosition();
    }

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
            } else cannotBeEdited();

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

            reInitializeRecyclerViewAdapter();
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

    private void moveItem() {

        mToolbar.startActionMode(new ActionMode.Callback() {

            int pos = mPosition;
            View tempView = findViewById(R.id.tempView);

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.move_menu, menu);
                mode.setTitle(R.string.move);
                mTintView.setVisibility(View.VISIBLE);
                tempView.setVisibility(View.VISIBLE);
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
                        sortOrder = "position";
                        saveToPref();
                        showIndeterminateProgress();
                        new savePositionsToDB().execute();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            public void onDestroyActionMode(ActionMode mode) {
                mTintView.setVisibility(View.GONE);
                tempView.setVisibility(View.GONE);
            }
        });
    }

    private class savePositionsToDB extends AsyncTask<Long, Void, Void> {

        @Override
        protected Void doInBackground(Long... longs) {

            List<WebCam> newWebCams = mAdapter.getItems();
            int i = 0;
            for (WebCam mWebCam : newWebCams) {
                mWebCam.setPosition(i);
                db.updateWebCamPosition(mWebCam);
                i++;
            }
            db.closeDB();
            this.publishProgress();

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            indeterminateProgress.dismiss();
            saveDone();
        }
    }

    private void saveDone() {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.dialog_positive_toast_message,
                Snackbar.LENGTH_SHORT).show();
        temporaryHideFab(true);
    }

    private void listIsEmpty() {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.list_is_empty,
                Snackbar.LENGTH_SHORT).show();
        temporaryHideFab(true);
    }

    private void cannotBeEdited() {
        Snackbar.make(findViewById(R.id.coordinator_layout), R.string.this_category_cannot_be_edited,
                Snackbar.LENGTH_SHORT).show();
        temporaryHideFab(true);
    }

    //ToDo: Only until it's officially added to Clans/FloatingActionButton
    private void temporaryHideFab(boolean durationShort) {
        floatingActionMenu.hideMenuButton(true);

        int duration = 2000;
        if (!durationShort) duration = 3200;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                floatingActionMenu.showMenuButton(true);
            }
        }, duration);
    }

    private void refresh() {
        if (mAdapter.getItemCount() != 0) {
            mStringSignature = UUID.randomUUID().toString();
            mAdapter.refreshViewImages(new StringSignature(mStringSignature));
        }
    }

    private void refreshSelected(int position) {
        mStringSignature = UUID.randomUUID().toString();
        mAdapter.refreshSelectedImage(position, new StringSignature(mStringSignature));
    }

    private void autoRefreshTimer(int interval) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            refresh();
                        } catch (Exception ignored) {}
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, interval);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && fullScreen) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void loadPref(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        firstRun = preferences.getBoolean("pref_first_run", true);
        numberOfColumns = preferences.getInt("number_of_columns", 1);
        imagesOnOff = preferences.getBoolean("pref_images_on_off", true);
        sortOrder = preferences.getString("pref_sort_order", Utils.defaultSortOrder);
        fullScreen = preferences.getBoolean("pref_full_screen", false);
        autoRefresh = preferences.getBoolean("pref_auto_refresh", false);
        autoRefreshInterval = preferences.getInt("pref_auto_refresh_interval", 30000);
        autoRefreshFullScreenOnly = preferences.getBoolean("pref_auto_refresh_fullscreen", false);
        screenAlwaysOn = preferences.getBoolean("pref_screen_always_on", false);
        simpleList = preferences.getBoolean("pref_simple_list", false);
    }

    private void saveToPref(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("pref_first_run", firstRun);
        editor.putInt("number_of_columns", numberOfColumns);
        editor.putBoolean("pref_images_on_off", imagesOnOff);
        editor.putString("pref_sort_order", sortOrder);
        editor.apply();
    }
}
