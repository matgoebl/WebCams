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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cz.yetanotherview.webcamviewer.app.R;

public class SpinnerAdapter extends ArrayAdapter<String> {

    private Activity activity;
    private String[] objects;
    private int[] icons = {R.drawable.ic_action_device_usb,
            R.drawable.ic_action_description};

    public SpinnerAdapter(Activity activity, int textViewResourceId,
                           String[] objects) {
        super(activity, textViewResourceId, objects);
        this.activity = activity;
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, parent);
    }

    public View getCustomView(int position, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View row = inflater.inflate(R.layout.spinner_item, parent, false);
        TextView label = (TextView)row.findViewById(R.id.spinner_text);
        label.setText(objects[position]);

        ImageView icon = (ImageView)row.findViewById(R.id.spinner_icon);
        icon.setImageResource(icons[position]);

        return row;
    }
}
