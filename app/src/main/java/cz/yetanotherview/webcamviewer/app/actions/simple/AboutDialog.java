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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.CircleTransform;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.listener.SimpleIntentOnClickListener;

public class AboutDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.app_name) + " " + getAppVersion())
                .customView(R.layout.about_dialog, true)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.settings_about)
                .build();

        loadAuthorImage((ImageView) dialog.findViewById(R.id.about_image));

        initAbout(dialog, R.id.about_author_container, new SimpleIntentOnClickListener(getActivity(),
                Utils.YAV));

        initAbout(dialog, R.id.about_contribute_container, new SimpleIntentOnClickListener(getActivity(),
                "https://github.com/TomasValenta/WebCamViewer"));

        initAbout(dialog, R.id.about_community_container, new SimpleIntentOnClickListener(getActivity(),
                "https://plus.google.com/u/0/communities/111088416819552930725"));

        initAbout(dialog, R.id.about_tomas_borov, new SimpleIntentOnClickListener(getActivity(),
                "https://plus.google.com/111192437899852242451/"));

        initAbout(dialog, R.id.about_radek_matouch, new SimpleIntentOnClickListener(getActivity(),
                "https://plus.google.com/104896455725487013630/"));

        return dialog;
    }

    private String getAppVersion() {
        final String VERSION_UNAVAILABLE = "N/A";
        PackageManager pm = getActivity().getPackageManager();
        String packageName = getActivity().getPackageName();
        String versionName;
        try {
            PackageInfo info = pm.getPackageInfo(packageName, 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = VERSION_UNAVAILABLE;
        }
        return versionName;
    }

    private void initAbout(MaterialDialog dialog, int about_container, SimpleIntentOnClickListener l) {
        LinearLayout mAboutAuthorContainer = (LinearLayout) dialog.findViewById(about_container);
        mAboutAuthorContainer.setOnClickListener(l);
    }

    private void loadAuthorImage(ImageView imageView) {
        String url = "http://www.gravatar.com/avatar/3be466b04b57b07a9f6efce06685713f.jpg?s=256";
        Glide.with(getActivity())
                .load(HttpHeader.getUrl(url))
                .crossFade()
                .transform(new CircleTransform(getActivity()))
                .placeholder(R.drawable.ic_identity)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }
}
