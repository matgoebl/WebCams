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

package cz.yetanotherview.webcamviewer.app.settings;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.simple.TranslatorsDialog;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.actions.simple.AboutDialog;
import cz.yetanotherview.webcamviewer.app.actions.ExportDialog;
import cz.yetanotherview.webcamviewer.app.actions.ImportDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.LibrariesDialog;
import cz.yetanotherview.webcamviewer.app.helper.DatabaseHelper;
import cz.yetanotherview.webcamviewer.app.helper.DeleteAllWebCams;
import cz.yetanotherview.webcamviewer.app.listener.SeekBarChangeListener;

public class SettingsFragment extends PreferenceFragmentCompat {

    private Context context;
    private MaterialDialog indeterminateProgress;
    private SeekBar seekBar;
    private TextView seekBarText;
    private int seekBarProgress, seekBarCorrection;
    private String units;
    private DatabaseHelper db;
    private SharedPreferences sharedPref;
    private Preference prefAutoRefreshFullScreen, prefAutoRefreshInterval;
    private PreferenceCategory preferenceCategory;
    private DialogFragment dialogFragment;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        context = getActivity().getApplicationContext();

        db = new DatabaseHelper(context);

        setAutoRefreshInterval();
        setAutoHideListener();

        deleteAllWebCams();

        importFromExt();
        exportToExt();

        cleanCacheAndTmpFolder();
        showTranslators();
        showLibraries();
        showAbout();

        sharedPref = getPreferenceManager().getSharedPreferences();
    }

    private void setAutoRefreshInterval() {
        Preference pref_auto_refresh_interval = findPreference("pref_auto_refresh_interval");
        pref_auto_refresh_interval.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                        .title(R.string.interval)
                        .customView(R.layout.seekbar_dialog, false)
                        .positiveText(R.string.dialog_positive_text)
                        .iconRes(R.drawable.settings_auto_refresh_interval)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                sharedPref.edit().putInt("pref_auto_refresh_interval",
                                        (seekBar.getProgress() + seekBarCorrection) * 1000).apply();

                                saveDone();
                            }
                        })
                        .build();

                seekBar = (SeekBar) dialog.findViewById(R.id.seekbar_seek);
                seekBarText = (TextView) dialog.findViewById(R.id.seekbar_text);

                units = "s";
                seekBarCorrection = 5;
                seekBar.setMax(359);
                seekBarProgress = (sharedPref.getInt("pref_auto_refresh_interval", 30000) / 1000);
                seekBar.setProgress(seekBarProgress - seekBarCorrection);
                seekBarText.setText((seekBar.getProgress() + seekBarCorrection) + units);

                seekBar.setOnSeekBarChangeListener(new SeekBarChangeListener(seekBar, seekBarText,
                        seekBarCorrection, units));

                dialog.show();

                return true;
            }
        });
    }

    private void setAutoHideListener() {
        preferenceCategory = (PreferenceCategory)findPreference("pref_category_general");
        SwitchPreferenceCompat prefA = (SwitchPreferenceCompat)findPreference("pref_auto_refresh");

        prefAutoRefreshFullScreen = findPreference("pref_auto_refresh_fullscreen");
        prefAutoRefreshInterval = findPreference("pref_auto_refresh_interval");

        Boolean state = getPreferenceManager().getSharedPreferences().getBoolean("pref_auto_refresh", false);
        if (!state) {
            preferenceCategory.removePreference(prefAutoRefreshFullScreen);
            preferenceCategory.removePreference(prefAutoRefreshInterval);
        }

        prefA.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                boolean switchedOn = (Boolean) newValue;
                if (switchedOn) {
                    preferenceCategory.addPreference(prefAutoRefreshFullScreen);
                    preferenceCategory.addPreference(prefAutoRefreshInterval);

                } else {
                    preferenceCategory.removePreference(prefAutoRefreshFullScreen);
                    preferenceCategory.removePreference(prefAutoRefreshInterval);
                }

                return true;
            }
        });
    }

    private void deleteAllWebCams() {
        Preference pref_delete_all = findPreference("pref_delete_all");
        pref_delete_all.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                if (db.getWebCamCount() > 0) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.pref_delete_all_webcams)
                            .content(R.string.delete_also_categories)
                            .positiveText(R.string.Yes)
                            .negativeText(R.string.No)
                            .neutralText(android.R.string.cancel)
                            .iconRes(R.drawable.settings_delete_all)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    showIndeterminateProgress();
                                    new deleteAllBackgroundTask().execute(true);
                                }
                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    showIndeterminateProgress();
                                    new deleteAllBackgroundTask().execute(false);
                                }
                            })
                            .show();
                } else listIsEmpty();

                return true;
            }
        });
    }

    private class deleteAllBackgroundTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... booleans) {

            DeleteAllWebCams.execute(context, booleans[0]);
            showDeletedSnackBar();
            return null;
        }
    }

    private void exportToExt() {
        Preference pref_export_to_ext = findPreference("pref_export_to_ext");
        pref_export_to_ext.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                dialogFragment = new ExportDialog();
                dialogFragment.show(getActivity().getFragmentManager(), "ExportDialog");
                return true;
            }
        });
    }

    private void importFromExt() {
        Preference pref_import_from_ext = findPreference("pref_import_from_ext");
        pref_import_from_ext.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                dialogFragment = new ImportDialog();
                dialogFragment.show(getActivity().getFragmentManager(), "ImportDialog");
                return true;
            }
        });
    }

    private void cleanCacheAndTmpFolder() {
        Preference pref_clean_cache_and_tmp = findPreference("pref_clean_cache_and_tmp");
        pref_clean_cache_and_tmp.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                new MaterialDialog.Builder(getActivity())
                        .title(R.string.pref_clear_cache_and_tmp)
                        .content(R.string.are_you_sure)
                        .positiveText(R.string.Yes)
                        .negativeText(android.R.string.cancel)
                        .iconRes(R.drawable.settings_clear_cache_and_tmp)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                Utils.deleteCache(context);
                                deleteDone();
                            }
                        })
                        .show();

                return true;
            }
        });
    }

    private void showTranslators() {
        Preference pref_translators = findPreference("pref_translators");
        pref_translators.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                dialogFragment = new TranslatorsDialog();
                dialogFragment.show(getActivity().getFragmentManager(), "TranslatorsDialog");

                return true;
            }
        });
    }

    private void showLibraries() {
        Preference pref_libraries = findPreference("pref_libraries");
        pref_libraries.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                dialogFragment = new LibrariesDialog();
                dialogFragment.show(getActivity().getFragmentManager(), "LibrariesDialog");

                return true;
            }
        });
    }

    private void showAbout() {
        Preference pref_about = findPreference("pref_about");
        pref_about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {

                dialogFragment = new AboutDialog();
                dialogFragment.show(getActivity().getFragmentManager(), "AboutDialog");

                return true;
            }
        });
    }

    private void showIndeterminateProgress() {
        indeterminateProgress = new MaterialDialog.Builder(getActivity())
                .content(R.string.please_wait)
                .progress(true, 0)
                .show();
    }

    private void showDeletedSnackBar() {

        getActivity().runOnUiThread(new Runnable() {
            public void run() {

                indeterminateProgress.dismiss();
                deleteDone();

            }
        });
    }

    private void listIsEmpty() {
        Snackbar.make(getActivity().findViewById(R.id.settings_fragment), R.string.list_is_empty,
                Snackbar.LENGTH_SHORT).show();
    }

    private void deleteDone() {
        Snackbar.make(getActivity().findViewById(R.id.settings_fragment), R.string.action_deleted,
                Snackbar.LENGTH_SHORT).show();
    }

    private void saveDone() {
        Snackbar.make(getActivity().findViewById(R.id.settings_fragment), R.string.dialog_positive_toast_message,
                        Snackbar.LENGTH_SHORT).show();
    }
}
