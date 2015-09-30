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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        setupTabTextColor();
        setupViewPager();
        return root;
    }

    private void setupTabTextColor() {
        mTabLayout = (TabLayout) root.findViewById(R.id.tab_layout);
    }

    private void setupViewPager() {
        //You could use the normal supportFragmentManger if you like
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), getActivity());
        mViewPager = (ViewPager) root.findViewById(R.id.view_pager);
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);//this is the new nice thing ;D
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
