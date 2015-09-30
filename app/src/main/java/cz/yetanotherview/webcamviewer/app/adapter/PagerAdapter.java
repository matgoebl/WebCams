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

package cz.yetanotherview.webcamviewer.app.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.fragments.tabs.TabFragment;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class PagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<WebCam> webCams;

    public PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(getOffset(position));
    }

    private int getOffset(int position){
        switch (position){
            case 0: return 0;
            case 1: return 5;
            case 2: return 10;
            case 3: return 15;
        }
        return 0;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "not_title_set";
        switch (position) {
            case 0:
                title = "pop_title";
                break;
            case 1:
                title = "indie_title";
                break;
            case 2:
                title = "rock_title";
                break;
            case 3:
                //title = mContext.getString(R.string.r8b_title);
                title = "r8b_title";
                break;
        }
        return title;
    }
}
