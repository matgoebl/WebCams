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

package cz.yetanotherview.webcamviewer.app.actions.simple;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;

public class LibrariesDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.pref_libraries)
                .customView(R.layout.libraries_dialog, true)
                .positiveText(android.R.string.ok)
                .build();

        TextView mLibrariesTitleSupportLibraries = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_support_libraries);
        mLibrariesTitleSupportLibraries.setText(getString(R.string.support_libraries) + " " + Utils.SUPPORT_LIBRARIES_VERSION);

        LinearLayout mLibrariesSupportLibrariesContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_support_libraries_container);
        mLibrariesSupportLibrariesContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://developer.android.com/tools/support-library/index.html"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleGlide = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_glide);
        mLibrariesTitleGlide.setText(getString(R.string.glide) + " " + Utils.GLIDE_VERSION);

        LinearLayout mLibrariesGlideContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_glide_container);
        mLibrariesGlideContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/bumptech/glide"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleLibVlc = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_lib_vlc);
        mLibrariesTitleLibVlc.setText(getString(R.string.lib_vlc) + " " + Utils.LIB_VLC_VERSION);

        LinearLayout mLibrariesLibVlcContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_lib_vlc_container);
        mLibrariesLibVlcContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wiki.videolan.org/LibVLC/"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleMaterialDialogs = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_material_dialogs);
        mLibrariesTitleMaterialDialogs.setText(getString(R.string.material_dialogs) + " " + Utils.MATERIAL_DIALOGS_VERSION);

        LinearLayout mLibrariesMaterialDialogsContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_material_dialogs_container);
        mLibrariesMaterialDialogsContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/afollestad/material-dialogs"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleGson = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_gson);
        mLibrariesTitleGson.setText(getString(R.string.gson) + " " + Utils.GOOGLE_GSON_VERSION);

        LinearLayout mLibrariesGsonContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_gson_container);
        mLibrariesGsonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/google/gson"));
                startActivity(i);
            }
        });


        TextView mLibrariesTitleJsoup = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_jsoup);
        mLibrariesTitleJsoup.setText(getString(R.string.jsoup) + " " + Utils.JSOUP_VERSION);

        LinearLayout mLibrariesJsoupContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_jsoup_container);
        mLibrariesJsoupContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/jhy/jsoup"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleMapbox = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_mapbox);
        mLibrariesTitleMapbox.setText(getString(R.string.mapbox) + " " + Utils.MAPBOX_VERSION);

        LinearLayout mLibrariesMapboxContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_mapbox_container);
        mLibrariesMapboxContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/mapbox/mapbox-android-sdk"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleFab = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_fab);
        mLibrariesTitleFab.setText(getString(R.string.floating_action_button) + " " + Utils.FAB_VERSION);

        LinearLayout mLibrariesFabContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_fab_container);
        mLibrariesFabContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/futuresimple/android-floating-action-button"));
                startActivity(i);
            }
        });

        TextView mLibrariesTitleSnackbar = (TextView) dialog.getCustomView().findViewById(R.id.libraries_title_snackbar);
        mLibrariesTitleSnackbar.setText(getString(R.string.snackbar) + " " + Utils.SNACKBAR_VERSION);

        LinearLayout mLibrariesSnackbarContainer = (LinearLayout) dialog.getCustomView().findViewById(R.id.libraries_snackbar_container);
        mLibrariesSnackbarContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/nispok/snackbar"));
                startActivity(i);
            }
        });

        return dialog;
    }
}
