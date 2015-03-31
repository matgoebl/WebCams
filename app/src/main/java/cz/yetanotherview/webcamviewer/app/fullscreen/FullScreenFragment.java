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

package cz.yetanotherview.webcamviewer.app.fullscreen;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.signature.StringSignature;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.SaveDialog;
import cz.yetanotherview.webcamviewer.app.actions.ShareDialog;
import cz.yetanotherview.webcamviewer.app.adapter.DiaporamaAdapter;

public class FullScreenFragment extends Fragment {

    private View view;
    private RelativeLayout mButtonsLayout;
    private TouchImageView touchImageView;
    private Animation fadeOut;
    private String signature;
    private StringSignature stringSignature;
    private String name;
    private String url;
    private float zoom;
    private double latitude;
    private double longitude;
    private boolean autoRefresh;
    private int autoRefreshInterval;
    private boolean fullScreen;
    private ImageButton backButton;
    private DiaporamaAdapter diaporamaAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.full_screen_layout, container, false);

        Intent intent = getActivity().getIntent();
        signature = intent.getExtras().getString("signature");
        name = intent.getExtras().getString("name");
        url = intent.getExtras().getString("url");
        zoom = intent.getExtras().getFloat("zoom");
        autoRefresh = intent.getExtras().getBoolean("autoRefresh");
        autoRefreshInterval = intent.getExtras().getInt("interval");
        latitude = intent.getExtras().getDouble("latitude");
        longitude = intent.getExtras().getDouble("longitude");
        fullScreen = intent.getExtras().getBoolean("fullScreen");

        stringSignature = new StringSignature(signature);

        // Auto Refresh timer
        if (autoRefresh) {
            autoRefreshTimer(autoRefreshInterval);
        }

        // Back button
        if (fullScreen) {
            backButton = (ImageButton) view.findViewById(R.id.back_button);
            backButton.setVisibility(View.VISIBLE);
        }

        initViews();
        initDiaporamaAdapter();
        setAnimation();
        loadImage();

        mButtonsLayout.startAnimation(fadeOut);

        return view;
    }

    private void initViews() {
        mButtonsLayout = (RelativeLayout) view.findViewById(R.id.buttons_layout);

        View.OnClickListener touchImageViewsListener = new View.OnClickListener() {
            public void onClick(View v) {
                mButtonsLayout.setVisibility(View.VISIBLE);
                mButtonsLayout.startAnimation(fadeOut);
            }
        };

        touchImageView = (TouchImageView) view.findViewById(R.id.touch_image);
        touchImageView.setMaxZoom(zoom);
        touchImageView.setOnClickListener(touchImageViewsListener);

        ImageButton mapButton = (ImageButton) view.findViewById(R.id.maps_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latitude != 0 && longitude != 0) {
                    ((FullScreenActivity) getActivity()).replaceFragments(true);
                }
                else {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.no_coordinates)
                            .content(R.string.no_coordinates_summary)
                            .positiveText(android.R.string.ok)
                            .show();
                }
            }
        });

        ImageButton shareButton = (ImageButton) view.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareDialog shareDialog = new ShareDialog();
                Bundle shareDialogBundle = new Bundle();
                shareDialogBundle.putString("url", url);
                shareDialog.setArguments(shareDialogBundle);
                shareDialog.show(getFragmentManager(), "ShareDialog");
            }
        });

        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        ImageButton saveButton = (ImageButton) view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new SaveDialog();
                Bundle bundle = new Bundle();
                bundle.putString("name", name);
                bundle.putString("url", url);
                newFragment.setArguments(bundle);
                newFragment.show(getFragmentManager(), "SaveDialog");
            }
        });

        if (fullScreen) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
    }

    private void initDiaporamaAdapter() {
        diaporamaAdapter = new DiaporamaAdapter(touchImageView);
        diaporamaAdapter.setPlaceholder(R.drawable.placeholder);
        diaporamaAdapter.setAnimationDuration(0);
    }

    private void setAnimation() {
        fadeOut = AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mButtonsLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void autoRefreshTimer(int interval) {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            refresh();
                        } catch (Exception ignored) {}
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, interval);
    }

    private void refresh() {
        stringSignature = new StringSignature(UUID.randomUUID().toString());
        loadImage();
    }

    private void loadImage() {
        diaporamaAdapter.loadNextImage(url, stringSignature);
    }
}
