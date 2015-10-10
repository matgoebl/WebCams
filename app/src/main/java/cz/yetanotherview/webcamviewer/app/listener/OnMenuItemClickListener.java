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

package cz.yetanotherview.webcamviewer.app.listener;

import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import cz.yetanotherview.webcamviewer.app.MainActivity;
import cz.yetanotherview.webcamviewer.app.R;

public class OnMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

    private final MainActivity mainActivity;

    public OnMenuItemClickListener(MainActivity activity) {
        this.mainActivity = activity;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mainActivity.openHelpOrSettings(0, 0);
        } else if (id == R.id.menu_help) {
            mainActivity.openHelpOrSettings(1, 0);
        }
        return false;

//        switch (item.getItemId()) {
//            case R.id.action_refresh:
//                //refresh();
//                break;
//
//            case R.id.action_dashboard:
//                if (numberOfColumns == 1) {
//                    numberOfColumns = 2;
//                    item.setIcon(R.drawable.ic_action_view_day);
//                }
//                else if (numberOfColumns == 2) {
//                    numberOfColumns = 1;
//                    item.setIcon(R.drawable.ic_action_dashboard);
//                }
//                //initRecyclerView();
//                //saveToPref();
//                break;
//
//            case R.id.action_sort:
//                //showSortDialog();
//                break;
//
//            default:
//                break;
//        }
    }
}
