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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;

public class ListButtonAdapter extends ArrayAdapter<File> {

    private Activity context;
    private ArrayList<File> files;

    public ListButtonAdapter(Activity context, ArrayList<File> files) {
        super(context, 0, files);
        this.context = context;
        this.files = files;
    }

    static class ViewHolder {
        int position;
        protected TextView text;
        protected ImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.import_dialog_item, null);
            final ViewHolder viewHolder = new ViewHolder();

            viewHolder.text = (TextView) view.findViewById(R.id.file_Name);
            viewHolder.imageView = (ImageView) view.findViewById(R.id.file_Button);

            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(getContext(), v);
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.rename:

                                    String input_prefill = files.get(viewHolder.position).getName()
                                            .replaceFirst("[.][^.]+$", "");
                                    new MaterialDialog.Builder(context)
                                            .title(R.string.rename)
                                            .input(null, input_prefill, new MaterialDialog.InputCallback() {
                                                @Override
                                                public void onInput(MaterialDialog dialog, CharSequence input) {
                                                    String inputName = input.toString().trim();

                                                    File file = (File) viewHolder.imageView.getTag();
                                                    File newName = new File(Utils.folderWCVPath + inputName + Utils.extension);
                                                    file.renameTo(newName);
                                                    files.set(viewHolder.position, newName);
                                                    notifyDataSetChanged();
                                                }
                                            })
                                            .show();
                                    break;

                                case R.id.delete:
                                    File file = (File) viewHolder.imageView.getTag();
                                    files.remove(file);
                                    file.delete();
                                    notifyDataSetChanged();
                                    break;
                            }
                            return true;
                        }
                    });

                    popup.show();
                }
            });
            view.setTag(viewHolder);
            viewHolder.imageView.setTag(files.get(position));

        } else {
            view = convertView;
            ((ViewHolder) view.getTag()).imageView.setTag(files.get(position));
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.text.setText(files.get(position).getName().replaceFirst("[.][^.]+$", ""));
        holder.position = position;
        return view;
    }
}
