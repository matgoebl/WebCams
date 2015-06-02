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

import com.afollestad.materialdialogs.MaterialDialog;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.drawer.NavigationDrawerFragment;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.Category;

public class DeleteCategoryDialog extends DialogFragment {

    private Category category;
    private Activity mActivity;
    private DatabaseHelper db;
    private int position;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        position = bundle.getInt("position", 0);
        int categoryId = bundle.getInt("categoryId", 0);

        db = new DatabaseHelper(mActivity);
        category = db.getCategory(categoryId);

        return new MaterialDialog.Builder(mActivity)
                .title(R.string.action_delete)
                .content(R.string.are_you_sure)
                .positiveText(R.string.Yes)
                .negativeText(android.R.string.cancel)
                .iconRes(R.drawable.warning)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        db.deleteCategory(category.getId(), false);
                        db.closeDB();
                        reloadHost();
                    }
                })
                .build();
    }

    private void reloadHost() {
        NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment)
                mActivity.getFragmentManager().findFragmentById(R.id.fragment_drawer);
        if (mNavigationDrawerFragment != null) {
            mNavigationDrawerFragment.deleteData(position);
        }
        CategoryDialog categoryDialog = (CategoryDialog) getFragmentManager().findFragmentByTag("CategoryDialog");
        if (categoryDialog != null) {
            categoryDialog.deleteCategoryInAdapter(position);
            if (mNavigationDrawerFragment != null) {
                mNavigationDrawerFragment.reloadData();
            }
        }
    }
}
