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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.Locale;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.listener.SimpleIntentOnClickListener;

public class TranslatorsDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(R.string.pref_translators)
                .customView(R.layout.translators_dialog, true)
                .positiveText(android.R.string.ok)
                .neutralText(R.string.want_to_translate)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        Intent i = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://goo.gl/MUJqYZ"));
                        startActivity(i);
                    }
                })
                .build();

        initTranslator(dialog, "en", R.id.translators_language_english_flag, "us",
                R.id.translators_language_english, R.id.translators_language_english_container,
                new SimpleIntentOnClickListener(getActivity(),
                        "https://brothermat.wordpress.com/"));

        initTranslator(dialog, "en", R.id.translators_language_english2_flag, "gb",
                R.id.translators_language_english2, R.id.translators_language_english2_container,
                new SimpleIntentOnClickListener(getActivity(),
                        Utils.YAV));

        initTranslator(dialog, "de", R.id.translators_language_german_flag, "de",
                R.id.translators_language_german, R.id.translators_language_german_container,
                new SimpleIntentOnClickListener(getActivity(),
                        "http://blog.strubbl.de/"));

        initTranslator(dialog, "ja", R.id.translators_language_japanese_flag, "jp",
                R.id.translators_language_japanese, R.id.translators_language_japanese_container,
                new SimpleIntentOnClickListener(getActivity(),
                        "https://github.com/naofum"));

        initTranslator(dialog, "nb", R.id.translators_language_norwegian_flag, "no",
                R.id.translators_language_norwegian, R.id.translators_language_norwegian_container,
                new SimpleIntentOnClickListener(getActivity(),
                        "http://0p.no/"));

        initTranslator(dialog, "cs", R.id.translators_language_czech_flag, "cz",
                R.id.translators_language_czech, R.id.translators_language_czech_container,
                new SimpleIntentOnClickListener(getActivity(),
                        Utils.YAV));

        initTranslator(dialog, "sk", R.id.translators_language_slovak_flag, "sk",
                R.id.translators_language_slovak, R.id.translators_language_slovak_container,
                new SimpleIntentOnClickListener(getActivity(),
                        Utils.YAV));

        return dialog;
    }

    private void initTranslator(MaterialDialog dialog, String language, int translator_language_flag, String country_flag,
                                int translator_language, int translator_container, SimpleIntentOnClickListener l) {

        String lang = new Locale(language, "").getDisplayLanguage();
        String capLang = lang.substring(0, 1).toUpperCase() + lang.substring(1);

        ImageView mTranslatorLanguageFlag = (ImageView) dialog.getCustomView().findViewById(translator_language_flag);
        mTranslatorLanguageFlag.setImageResource(Utils.getResId(country_flag, R.drawable.class));

        TextView mTranslatorLanguage = (TextView) dialog.getCustomView().findViewById(translator_language);
        mTranslatorLanguage.setText(capLang);

        LinearLayout mTranslatorLanguageContainer = (LinearLayout) dialog.getCustomView().findViewById(translator_container);
        mTranslatorLanguageContainer.setOnClickListener(l);
    }
}
