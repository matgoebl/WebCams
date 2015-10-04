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
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.PagerAdapter;

public class TabHolderFragment extends BaseFragment {

    View root;

    TabLayout mTabLayout;
    ViewPager mViewPager;
    ImageView mToolbarImage;

    PagerAdapter adapter;

    public TabHolderFragment() {}

    public static TabHolderFragment newInstance() {
        return new TabHolderFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mToolbar.inflateMenu(R.menu.menu_others);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root =  super.onCreateView(inflater, container, savedInstanceState);
        setup();
        return root;
    }

    private void setup() {

        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) root.findViewById(R.id.collapsing_toolbar_layout);
        collapsingToolbarLayout.setTitleEnabled(false);

        mToolbarImage = (ImageView) root.findViewById(R.id.toolbar_image);

        //You could use the normal supportFragmentManger if you like
        //PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), getActivity());
        mViewPager = (ViewPager) root.findViewById(R.id.view_pager);
        setupViewPager();
        //mViewPager.setAdapter(pagerAdapter);
        mTabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mViewPager);//this is the new nice thing ;D

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                Glide.with(getActivity()).load(adapter.getPageImage(tab.getPosition())).centerCrop().into(mToolbarImage);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void setupViewPager() {
        adapter = new PagerAdapter(getChildFragmentManager());
        adapter.addFrag(new TabFragment(0), getString(R.string.airports), R.drawable.image_airports);
        adapter.addFrag(new TabFragment(1), getString(R.string.animals), R.drawable.image_animals);
        adapter.addFrag(new TabFragment(2), getString(R.string.beaches), R.drawable.image_beaches);
        adapter.addFrag(new TabFragment(3), getString(R.string.bridges), R.drawable.image_bridges);
        adapter.addFrag(new TabFragment(4), getString(R.string.buildings), R.drawable.image_buildings);
        adapter.addFrag(new TabFragment(5), getString(R.string.castles), R.drawable.image_castles);
        adapter.addFrag(new TabFragment(6), getString(R.string.cities), R.drawable.image_cities);
        adapter.addFrag(new TabFragment(7), getString(R.string.constructions), R.drawable.image_constructions);
        adapter.addFrag(new TabFragment(31), getString(R.string.fun_parks), R.drawable.image_fun_parks);
        adapter.addFrag(new TabFragment(29), getString(R.string.golf_resorts), R.drawable.image_golf_resorts);
        adapter.addFrag(new TabFragment(8), getString(R.string.harbours), R.drawable.image_harbours);
        adapter.addFrag(new TabFragment(9), getString(R.string.churches), R.drawable.image_churches);
        adapter.addFrag(new TabFragment(10), getString(R.string.indoors), R.drawable.image_indoors);
        adapter.addFrag(new TabFragment(11), getString(R.string.lakes), R.drawable.image_lakes);
        adapter.addFrag(new TabFragment(12), getString(R.string.landscapes), R.drawable.image_landscapes);
        adapter.addFrag(new TabFragment(13), getString(R.string.market_square), R.drawable.image_market_square);
        adapter.addFrag(new TabFragment(14), getString(R.string.mountains), R.drawable.image_mountains);
        adapter.addFrag(new TabFragment(15), getString(R.string.others), R.drawable.image_others);
        adapter.addFrag(new TabFragment(16), getString(R.string.parks), R.drawable.image_parks);
        adapter.addFrag(new TabFragment(17), getString(R.string.pools), R.drawable.image_pools);
        adapter.addFrag(new TabFragment(18), getString(R.string.radio_studios), R.drawable.image_radio_studios);
        adapter.addFrag(new TabFragment(19), getString(R.string.railways), R.drawable.image_railways);
        adapter.addFrag(new TabFragment(20), getString(R.string.rivers), R.drawable.image_rivers);
        adapter.addFrag(new TabFragment(30), getString(R.string.rocks), R.drawable.image_rocks);
        adapter.addFrag(new TabFragment(24), getString(R.string.ships), R.drawable.image_ships);
        adapter.addFrag(new TabFragment(21), getString(R.string.ski_resorts), R.drawable.image_ski_resorts);
        adapter.addFrag(new TabFragment(25), getString(R.string.skies), R.drawable.image_skies);
        adapter.addFrag(new TabFragment(26), getString(R.string.stations), R.drawable.image_stations);
        adapter.addFrag(new TabFragment(27), getString(R.string.streets), R.drawable.image_streets);
        adapter.addFrag(new TabFragment(28), getString(R.string.surfs), R.drawable.image_surfs);
        adapter.addFrag(new TabFragment(22), getString(R.string.traffic_cameras), R.drawable.image_traffic_cameras);
        adapter.addFrag(new TabFragment(23), getString(R.string.universities), R.drawable.image_universities);
        mViewPager.setAdapter(adapter);
    }

    @Override
    public boolean hasCustomToolbar() {
        return true;
    }

    @Override
    protected int getLayout() {
        return R.layout.tab_app_bar_fragment_holder;
    }
}
