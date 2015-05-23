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
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.adapter.CategorySelectionAdapter;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.Category;

public class CategoryDialog extends DialogFragment {

    private Activity mActivity;
    private Callback mCallback;
    private DatabaseHelper db;
    private CategorySelectionAdapter categorySelectionAdapter;
    private List<Integer> category_ids;
    private List<Category> allCategories;

    public interface Callback {
        void onCategorySave(List<Integer> category_ids);
        void onUpdate(List<Integer> category_ids);
    }

    public CategoryDialog() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        category_ids = getArguments().getIntegerArrayList("category_ids");
        db = new DatabaseHelper(mActivity);
        allCategories = db.getAllCategories();
        db.closeDB();
        if (category_ids != null) {
            for (Category category : allCategories) {
                if (category_ids.contains(category.getId())) {
                    category.setSelected(true);
                }
            }
        }

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.webcam_category)
                .customView(R.layout.category_selection_dialog, false)
                .positiveText(R.string.dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .neutralText(R.string.action_new)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        assignIds();
                        mCallback.onCategorySave(category_ids);
                        dialog.dismiss();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        new AddCategoryDialog().show(getFragmentManager(), "AddCategoryDialog");
                    }
                })
                .autoDismiss(false)
                .build();

        ListView categorySelectionList = (ListView) dialog.getCustomView().findViewById(R.id.category_list_view);
        categorySelectionList.setEmptyView(dialog.getCustomView().findViewById(R.id.no_categories));
        categorySelectionAdapter = new CategorySelectionAdapter(mActivity, allCategories);
        categorySelectionList.setAdapter(categorySelectionAdapter);

        return dialog;
    }

    private void assignIds() {
        category_ids = new ArrayList<>();
        for (Category category : allCategories) {
            if (category.isSelected()) {
                category_ids.add(category.getId());
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            mCallback = (Callback) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement CategoryDialogListener");
        }
    }

    public void addCategoryInAdapter(Category category) {
        categorySelectionAdapter.addItem(categorySelectionAdapter.getCount(), category);
    }

    public void editCategoryInAdapter(int position, Category category) {
        assignIds();
        mCallback.onUpdate(category_ids);
        categorySelectionAdapter.modifyItem(position, category);
    }

    public void deleteCategoryInAdapter(int position) {
        allCategories = db.getAllCategories();
        assignIds();
        mCallback.onUpdate(category_ids);
        categorySelectionAdapter.removeItem(position);
    }
}
