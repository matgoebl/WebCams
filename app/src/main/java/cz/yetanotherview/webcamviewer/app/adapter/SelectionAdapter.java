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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.ArrayRes;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cz.yetanotherview.webcamviewer.app.R;

public class SelectionAdapter extends BaseAdapter {

    private final Context context;
    private final CharSequence[] mItems;
    private final int[] mIcons;

    public SelectionAdapter(Context context, @ArrayRes int arrayResId, int[] icons) {
        this(context, context.getResources().getTextArray(arrayResId), icons);
    }

    private SelectionAdapter(Context context, CharSequence[] items, int[] icons) {
        this.context = context;
        this.mItems = items;
        this.mIcons = icons;
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public CharSequence getItem(int position) {
        return mItems[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = View.inflate(context, R.layout.type_list_item, null);
        ((ImageView) view.findViewById(R.id.typeIcon)).setImageResource(mIcons[position]);
        ((TextView) view.findViewById(R.id.typeName)).setText(mItems[position]);

        return view;
    }
}

