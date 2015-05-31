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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import cz.yetanotherview.webcamviewer.app.R;

public class ReportDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.report)
                .customView(R.layout.report_dialog, true)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.settings_about)
                .build();

        Bundle bundle = this.getArguments();
        int newWebCams = bundle.getInt("newWebCams", 0);
        int duplicityWebCams = bundle.getInt("duplicityWebCams", 0);
        int updatedWebCams = bundle.getInt("updatedWebCams", 0);

        TextView newWebCamsTvText, newWebCamsTv, duplicityWebCamsTvText, duplicityWebCamsTv,
                updatedWebCamsTvText, updatedWebCamsTv;
        newWebCamsTvText = (TextView) dialog.findViewById(R.id.report_newWebCams_text);
        newWebCamsTv = (TextView) dialog.findViewById(R.id.report_newWebCams);
        newWebCamsTv.setText(String.valueOf(newWebCams));
        if (newWebCams != 0) {
            newWebCamsTvText.setTextColor(getResources().getColor(R.color.primary));
            newWebCamsTv.setTextColor(getResources().getColor(R.color.primary));
        }
        duplicityWebCamsTvText = (TextView) dialog.findViewById(R.id.report_duplicityWebCams_text);
        duplicityWebCamsTv = (TextView) dialog.findViewById(R.id.report_duplicityWebCams);
        duplicityWebCamsTv.setText(String.valueOf(duplicityWebCams));
        if (duplicityWebCams != 0) {
            duplicityWebCamsTvText.setTextColor(getResources().getColor(R.color.orange));
            duplicityWebCamsTv.setTextColor(getResources().getColor(R.color.orange));
        }
        updatedWebCamsTvText = (TextView) dialog.findViewById(R.id.report_updatedWebCams_text);
        updatedWebCamsTv = (TextView) dialog.findViewById(R.id.report_updatedWebCams);
        updatedWebCamsTv.setText(String.valueOf(updatedWebCams));
        if (updatedWebCams != 0) {
            updatedWebCamsTvText.setTextColor(getResources().getColor(R.color.blue));
            updatedWebCamsTv.setTextColor(getResources().getColor(R.color.blue));
        }

        return dialog;
    }
}
