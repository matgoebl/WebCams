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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.backup.BackupManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.nispok.snackbar.Snackbar;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.model.Category;

public class AddCategoryDialog extends DialogFragment {

    // Object for intrinsic lock
    public static final Object sDataLock = new Object();

    private String inputName;
    private View positiveAction;
    private EditText input;
    private Category category;

    private DatabaseHelper db;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(getActivity().getApplicationContext());

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.new_category)
                .customView(R.layout.add_edit_category_dialog, true)
                .positiveText(R.string.dialog_positive_text)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        inputName = input.getText().toString().trim();
                        synchronized (AddCategoryDialog.sDataLock) {
                            String iconPath = "@drawable/icon_manual";
                            category = new Category(iconPath, inputName);
                            db.createCategory(category);
                            db.closeDB();
                        }
                        BackupManager backupManager = new BackupManager(getActivity());
                        backupManager.dataChanged();

                        saveDone();
                    }
                })
                .build();

        ImageView category_icon = (ImageView) dialog.getCustomView().findViewById(R.id.category_icon);
        category_icon.setImageResource(R.drawable.icon_manual);
        category_icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Prepare grid view
//                GridView gridView = new GridView(this);
//
//                List<Integer>  mList = new ArrayList<Integer>();
//                for (int i = 1; i < 36; i++) {
//                    mList.add(i);
//                }
//
//                gridView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, mList));
//                gridView.setNumColumns(5);
//                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        // do something here
//                    }
//                });
//
//                // Set grid view to alertDialog
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setView(gridView);
//                builder.setTitle("Goto");
//                builder.show();
            }
        });

        input = (EditText) dialog.getCustomView().findViewById(R.id.category_name);
        input.requestFocus();
        input.setHint(R.string.new_category_hint);

        positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                positiveAction.setEnabled(s.toString().trim().length() > 0);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        positiveAction.setEnabled(false);

        return dialog;
    }

    private void saveDone() {
        Snackbar.with(getActivity().getApplicationContext())
                .text(R.string.dialog_positive_toast_message)
                .actionLabel(R.string.dismiss)
                .actionColor(getResources().getColor(R.color.yellow))
                .show(getActivity());
    }
}
