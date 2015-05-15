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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.drawer.NavigationDrawerCallbacks;
import cz.yetanotherview.webcamviewer.app.model.Category;

public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.ViewHolder> {

    private Context context;
    private List<Category> mData;
    private NavigationDrawerCallbacks mNavigationDrawerCallbacks;
    private View mSelectedView;
    private int mSelectedPosition;
    private ClickListener clickListener;

    public NavigationDrawerAdapter(List<Category> data) {
        mData = data;
    }

    public void setNavigationDrawerCallbacks(NavigationDrawerCallbacks navigationDrawerCallbacks) {
        mNavigationDrawerCallbacks = navigationDrawerCallbacks;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        }
        else return 1;
    }

    @Override
    public NavigationDrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int index) {
        context = viewGroup.getContext();
        View v;
        if (index == 0) {
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_list_item_zero, viewGroup, false);
        }
        else v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.drawer_list_item, viewGroup, false);
        final ViewHolder viewHolder = new ViewHolder(v);
        viewHolder.itemView.setClickable(true);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       if (mSelectedView != null) {
                                                           mSelectedView.setSelected(false);
                                                       }
                                                       mSelectedPosition = viewHolder.getAdapterPosition();
                                                       v.setSelected(true);
                                                       mSelectedView = v;
                                                       if (mNavigationDrawerCallbacks != null)
                                                           mNavigationDrawerCallbacks.onNavigationDrawerItemSelected(viewHolder.getAdapterPosition(), 0);
                                                   }
                                               }
        );
        viewHolder.itemView.setBackgroundResource(R.drawable.row_selector);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NavigationDrawerAdapter.ViewHolder viewHolder, int i) {
        int imageResource;
        if (mData.get(i).getCategoryIcon() != null) {
            imageResource = context.getResources().getIdentifier(mData.get(i).getCategoryIcon(), null,
                    context.getPackageName());
        }
        else imageResource = R.drawable.icon_unknown;
        viewHolder.categoryIcon.setImageResource(imageResource);
        viewHolder.categoryName.setText(mData.get(i).getCategoryName());
        viewHolder.categoryCount.setText("(" + mData.get(i).getCountAsString() + ")");

        if (mSelectedPosition == i) {
            if (mSelectedView != null) {
                mSelectedView.setSelected(false);
            }
            mSelectedPosition = i;
            mSelectedView = viewHolder.itemView;
            mSelectedView.setSelected(true);
        }
    }

    public void selectPosition(int position) {
        mSelectedPosition = position;
        notifyItemChanged(position);
    }

    public void swapData(List<Category> categoryItems) {
        this.mData = categoryItems;
        notifyDataSetChanged();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    private long getCategoryIdFromPosition(int position) {
        return mData.get(position).getId();
    }

    public void modifyItem(int position, Category category) {
        mData.set(position, category);
        notifyItemChanged(position);
    }

    public void removeItem(int position, int allCount) {
        mData.remove(position);
        notifyItemRemoved(position);
        mData.get(0).setCount(allCount);
        notifyItemChanged(0);
    }

    @Override
    public int getItemCount() {
        return mData != null ? mData.size() : 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView categoryIcon;
        public TextView categoryName;
        public TextView categoryCount;
        public ImageView categoryOptions;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryIcon = (ImageView) itemView.findViewById(R.id.categoryIcon);
            categoryName = (TextView) itemView.findViewById(R.id.categoryName);
            categoryCount = (TextView) itemView.findViewById(R.id.categoryCount);
            categoryOptions = (ImageView) itemView.findViewById(R.id.categoryOptions);

            if (categoryOptions != null) {
                categoryOptions.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition(), getCategoryIdFromPosition(getAdapterPosition()));
        }
    }

    public interface ClickListener {
        void onClick(View v, int position, long categoryId);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}