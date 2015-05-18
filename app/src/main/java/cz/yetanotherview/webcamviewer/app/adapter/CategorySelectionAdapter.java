/*
* ******************************************************************************
* Copyright (c) 2013-2014 Tomas Valenta.
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

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.DeleteCategoryDialog;
import cz.yetanotherview.webcamviewer.app.actions.EditCategoryDialog;
import cz.yetanotherview.webcamviewer.app.model.Category;

public class CategorySelectionAdapter extends BaseAdapter implements OnClickListener {

    private Activity activity;
    private List<Category> categoryList;

    public CategorySelectionAdapter(Activity activity, List<Category> categoryList) {
        super();
        this.activity = activity;
        this.categoryList = categoryList;
    }

    public class ViewHolder {
        CheckBox selCategoryCheckBox;
        ImageView selCategoryOptions;
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    public void addItem(int position, Category category) {
        categoryList.add(position, category);
        notifyDataSetChanged();
    }

    public void modifyItem(int position, Category category) {
        categoryList.set(position, category);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        categoryList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(activity).inflate(R.layout.category_selection_list_item, parent, false);
            holder = new ViewHolder();
            holder.selCategoryCheckBox = (CheckBox) convertView.findViewById(R.id.category_sel_checkbox);
            holder.selCategoryOptions = (ImageView) convertView.findViewById(R.id.categorySelectionOptions);
            convertView.setTag(holder);

            holder.selCategoryCheckBox.setOnClickListener(this);
            holder.selCategoryOptions.setOnClickListener(this);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Category category = categoryList.get(position);

        holder.selCategoryCheckBox.setChecked(category.isSelected());
        int imageResource;
        if (category.getCategoryIcon() != null) {
            imageResource = activity.getResources().getIdentifier(category.getCategoryIcon(), null,
                    activity.getPackageName());
        }
        else imageResource = R.drawable.icon_unknown;
        holder.selCategoryCheckBox.setCompoundDrawablesWithIntrinsicBounds(imageResource, 0, 0, 0);
        holder.selCategoryCheckBox.setText(category.getCategoryName());
        holder.selCategoryCheckBox.setTag(category);
        holder.selCategoryOptions.setTag(position);

        return convertView;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.category_sel_checkbox) {
            CheckBox cb = (CheckBox) view;
            Category category = (Category) cb.getTag();
            category.setSelected(cb.isChecked());
        }
        else {
            ImageView imageView = (ImageView) view;
            final int position = (int) imageView.getTag();
            final Category category = categoryList.get(position);

            PopupMenu popup = new PopupMenu(activity, view);
            popup.getMenuInflater().inflate(R.menu.edit_delete_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    DialogFragment dialogFragment;
                    Bundle bundle = new Bundle();
                    bundle.putInt("position", position);
                    bundle.putLong("categoryId", category.getId());

                    switch (item.getItemId()) {
                        case R.id.menu_edit:
                            dialogFragment = new EditCategoryDialog();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(activity.getFragmentManager(), "EditCategoryDialog");
                            break;

                        case R.id.menu_delete:
                            dialogFragment = new DeleteCategoryDialog();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(activity.getFragmentManager(), "DeleteCategoryDialog");
                            break;
                    }
                    return true;
                }
            });

            popup.show();
        }
    }
}
