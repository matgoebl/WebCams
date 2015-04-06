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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import cz.yetanotherview.webcamviewer.app.R;

public class ListButtonAdapter extends ArrayAdapter<File> {

    File localFile;
    ArrayList<File> files;

    public ListButtonAdapter(Context context, ArrayList<File> files) {
        super(context, 0, files);
        this.files = files;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        localFile = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.import_dialog_item, parent, false);
        }
        TextView fileName = (TextView) convertView.findViewById(R.id.file_Name);
        String name = localFile.getName();
        fileName.setText(name.replaceFirst("[.][^.]+$", ""));

        ImageView fileButton = (ImageView) convertView.findViewById(R.id.file_Button);
        fileButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getContext(), v);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(getContext(), "You selected the action : " + item.getTitle(), Toast.LENGTH_SHORT).show();

//                        files.remove(localFile);
//                        localFile.delete();
//                        notifyDataSetChanged();
                        return true;
                    }
                });

                popup.show();
            }
        });

        return convertView;
    }
}
