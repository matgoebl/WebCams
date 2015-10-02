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
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;

public class ThumbnailDialog extends DialogFragment {

    private String title;
    private String tags;
    private String url;
    private MaterialDialog materialDialog;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        title = bundle.getString("title", "");
        tags = bundle.getString("tags", "");
        url = bundle.getString("url", "");

        materialDialog = new MaterialDialog.Builder(getActivity())
                .title(title)
                .customView(R.layout.thumbnail_dialog, true)
                .positiveText(R.string.close)
                .build();

        TextView thumbnailText = (TextView) materialDialog.findViewById(R.id.thumbnail_dialog_tags);
        thumbnailText.setText(tags);

        ImageView thumbnailImage = (ImageView) materialDialog.findViewById(R.id.thumbnail_dialog_image);
        Glide.with(getActivity())
                .load(HttpHeader.getUrl(url))
                .dontTransform()
                .crossFade()
                .into(thumbnailImage);

        return materialDialog;
    }
}
