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

package cz.yetanotherview.webcamviewer.app.drawer;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.AddCategoryDialog;
import cz.yetanotherview.webcamviewer.app.actions.DeleteCategoryDialog;
import cz.yetanotherview.webcamviewer.app.actions.EditCategoryDialog;
import cz.yetanotherview.webcamviewer.app.adapter.NavigationDrawerAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.Category;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements NavigationDrawerCallbacks, View.OnClickListener {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_SELECTED_NAME = "selected_navigation_drawer_name";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private List<Category> navigationItems;
    private NavigationDrawerAdapter mDrawerAdapter;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private int mClickedPosition;
    private long mClickedCategoryId;
    private String mCurrentSelectedName;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private DatabaseHelper db;
    private String allWebCamsString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        db = new DatabaseHelper(getActivity().getApplicationContext());
        allWebCamsString = getString(R.string.all_webcams);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mCurrentSelectedName = savedInstanceState.getString(STATE_SELECTED_NAME);
            mFromSavedInstanceState = true;
        }
        else mCurrentSelectedName = allWebCamsString;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        RecyclerView mDrawerList = (RecyclerView) view.findViewById(R.id.drawerList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDrawerList.setLayoutManager(layoutManager);
        mDrawerList.setHasFixedSize(true);

        navigationItems = getCategories();
        mDrawerAdapter = new NavigationDrawerAdapter(navigationItems);
        mDrawerAdapter.setNavigationDrawerCallbacks(this);
        mDrawerList.setAdapter(mDrawerAdapter);
        selectItem(mCurrentSelectedPosition, true);

        mDrawerAdapter.setClickListener(new NavigationDrawerAdapter.ClickListener() {
            @Override
            public void onClick(View v, int position, long categoryId) {
                mClickedPosition = position;
                mClickedCategoryId = categoryId;
                showMenuPopup(v);
            }
        });

        RelativeLayout addCategoryLayout = (RelativeLayout) view.findViewById(R.id.add_category_layout);
        addCategoryLayout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position, long categoryId) {
        selectItem(position, true);
    }

    public List<Category> getCategories() {

        List<Category> allCategories = db.getAllCategories();
        for (Category category : allCategories) {
            category.setCount(db.getCategoryItemsCount(category.getId()));
        }

        Category allWebCamsCategory = new Category();
        allWebCamsCategory.setId(-1);
        allWebCamsCategory.setCategoryIcon("@drawable/icon_all");
        allWebCamsCategory.setCategoryName(allWebCamsString);
        allWebCamsCategory.setCount(db.getWebCamCount());
        db.closeDB();

        List<Category> items = new ArrayList<>();
        items.add(allWebCamsCategory);
        items.addAll(allCategories);
        return items;
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     * @param toolbar      The Toolbar of the activity.
     */
    public void setup(int fragmentId, DrawerLayout drawerLayout, Toolbar toolbar) {
        mFragmentContainerView = (View) getActivity().findViewById(fragmentId).getParent();
        mDrawerLayout = drawerLayout;
        mToolbar = toolbar;

        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.primary_dark));

        mActionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) return;

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) return;
                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mActionBarDrawerToggle.syncState();
                if (mCurrentSelectedPosition == 0) {
                    mToolbar.setTitle(getString(R.string.app_name));
                } else mToolbar.setTitle(mCurrentSelectedName);
            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    private void selectItem(int position, boolean closeDrawer) {
        Category category = (Category) mDrawerAdapter.getItem(position);
        mCurrentSelectedPosition = position;
        mCurrentSelectedName = category.getCategoryName();
        if (mDrawerLayout != null) {
            if (mCurrentSelectedPosition == 0) {
                mToolbar.setTitle(getString(R.string.app_name));
            }
            else mToolbar.setTitle(category.getCategoryName());
            if (closeDrawer) {
                mDrawerLayout.closeDrawer(mFragmentContainerView);
            }
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position, category.getId());
        }
        mDrawerAdapter.selectPosition(position);
    }

    public void reloadData() {
        navigationItems = getCategories();
        mDrawerAdapter.swapData(navigationItems);
    }

    public void editData(int position, Category category) {
        mDrawerAdapter.modifyItem(position, category);
    }

    public void deleteData(int position) {
        mDrawerAdapter.removeItem(position, db.getWebCamCount());
        selectItem(0, false);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mFragmentContainerView);
    }

    public void closeDrawer() {
        mDrawerLayout.closeDrawer(mFragmentContainerView);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_category_layout:
                new AddCategoryDialog().show(getFragmentManager(), "AddCategoryDialog");
                break;
        }
    }

    private void showMenuPopup(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        popup.getMenuInflater().inflate(R.menu.edit_delete_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        showEditCategoryDialog();
                        return true;
                    case R.id.menu_delete:
                        showDeleteCategoryDialog();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    private void showEditCategoryDialog() {
        DialogFragment dialogFragment = new EditCategoryDialog();

        Bundle bundle = new Bundle();
        bundle.putInt("position", mClickedPosition);
        bundle.putLong("categoryId", mClickedCategoryId);
        dialogFragment.setArguments(bundle);

        dialogFragment.show(getFragmentManager(), "EditCategoryDialog");
    }

    private void showDeleteCategoryDialog() {
        DialogFragment dialogFragment = new DeleteCategoryDialog();

        Bundle bundle = new Bundle();
        bundle.putInt("position", mClickedPosition);
        bundle.putLong("categoryId", mClickedCategoryId);
        dialogFragment.setArguments(bundle);

        dialogFragment.show(getFragmentManager(), "DeleteCategoryDialog");
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putString(STATE_SELECTED_NAME, mCurrentSelectedName);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    public View getGoogleDrawer() {
        return mFragmentContainerView.findViewById(R.id.googleDrawer);
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    public ActionBarDrawerToggle getActionBarDrawerToggle() {
        return mActionBarDrawerToggle;
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }
}
