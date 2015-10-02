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

package cz.yetanotherview.webcamviewer.app.fragments.tabs;

import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.PagerAdapter;
import cz.yetanotherview.webcamviewer.app.fragments.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TabHolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TabHolderFragment extends BaseFragment {

    View root;

    TabLayout mTabLayout;
    ViewPager mViewPager;
    ImageView mToolbarImage;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TabFragment.
     */
    public static TabHolderFragment newInstance() {
        return new TabHolderFragment();
    }

    public TabHolderFragment() {
        // Required empty public constructor
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

                int image = R.drawable.image_all;
                switch (tab.getPosition()) {
                    case 0: image = R.drawable.image_airports; break;
                    case 1: image = R.drawable.image_animals; break;
                    case 2: image = R.drawable.image_beaches; break;
                    case 3: image = R.drawable.image_all; break; //ToDo
                    case 4: image = R.drawable.image_bridges; break;
                    case 5: image = R.drawable.image_buildings; break;
                    case 6: image = R.drawable.image_castles; break;
                    case 7: image = R.drawable.image_cities; break;
                }

                Glide.with(getActivity()).load(image).centerCrop().into(mToolbarImage);
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
        PagerAdapter adapter = new PagerAdapter(getChildFragmentManager());
        adapter.addFrag(new TabFragment(0), getString(R.string.airports));
        adapter.addFrag(new TabFragment(1), getString(R.string.animals));
        adapter.addFrag(new TabFragment(2), getString(R.string.beaches));
        adapter.addFrag(new TabFragment(24), getString(R.string.boats));
        adapter.addFrag(new TabFragment(3), getString(R.string.bridges));
        adapter.addFrag(new TabFragment(4), getString(R.string.buildings));
        adapter.addFrag(new TabFragment(5), getString(R.string.castles));
        adapter.addFrag(new TabFragment(6), getString(R.string.cities));
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
