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
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.listener.SimpleIntentOnClickListener;

public class LibrariesDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.pref_libraries)
                .customView(R.layout.libraries_dialog, true)
                .positiveText(android.R.string.ok)
                .build();

        initLibrary(dialog, R.id.libraries_title_support_libraries,
                getString(R.string.support_libraries) + " " + Utils.SUPPORT_LIBRARIES_VERSION,
                R.id.libraries_support_libraries_container,
                new SimpleIntentOnClickListener(getActivity(),
                        "http://developer.android.com/tools/support-library/index.html"));

        initLibrary(dialog, R.id.libraries_title_glide,
                getString(R.string.glide) + " " + Utils.GLIDE_VERSION,
                R.id.libraries_glide_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/bumptech/glide"));

        initLibrary(dialog, R.id.libraries_title_lib_vlc,
                getString(R.string.lib_vlc) + " " + Utils.LIB_VLC_VERSION,
                R.id.libraries_lib_vlc_container,
                new SimpleIntentOnClickListener(getActivity(), "https://wiki.videolan.org/LibVLC/"));

        initLibrary(dialog, R.id.libraries_title_material_dialogs,
                getString(R.string.material_dialogs) + " " + Utils.MATERIAL_DIALOGS_VERSION,
                R.id.libraries_material_dialogs_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/afollestad/material-dialogs"));

        initLibrary(dialog, R.id.libraries_title_gson,
                getString(R.string.gson) + " " + Utils.GOOGLE_GSON_VERSION,
                R.id.libraries_gson_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/google/gson"));

        initLibrary(dialog, R.id.libraries_title_jsoup,
                getString(R.string.jsoup) + " " + Utils.JSOUP_VERSION,
                R.id.libraries_jsoup_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/jhy/jsoup"));

        initLibrary(dialog, R.id.libraries_title_mapbox,
                getString(R.string.mapbox) + " " + Utils.MAPBOX_VERSION,
                R.id.libraries_mapbox_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/mapbox/mapbox-android-sdk"));

        initLibrary(dialog, R.id.libraries_title_fab,
                getString(R.string.floating_action_button) + " " + Utils.FAB_VERSION,
                R.id.libraries_fab_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/Clans/FloatingActionButton"));

        initLibrary(dialog, R.id.libraries_title_snackbar,
                getString(R.string.snackbar) + " " + Utils.SNACKBAR_VERSION,
                R.id.libraries_snackbar_container,
                new SimpleIntentOnClickListener(getActivity(), "https://github.com/nispok/snackbar"));

        return dialog;
    }

    private void initLibrary(MaterialDialog dialog, int libraries_title, String text, int libraries_container, SimpleIntentOnClickListener l) {
        TextView mLibrariesTitle = (TextView) dialog.findViewById(libraries_title);
        mLibrariesTitle.setText(text);

        LinearLayout mLibrariesContainer = (LinearLayout) dialog.findViewById(libraries_container);
        mLibrariesContainer.setOnClickListener(l);
    }
}
