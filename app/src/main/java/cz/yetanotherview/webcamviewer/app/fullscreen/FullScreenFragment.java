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
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.signature.StringSignature;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.actions.SaveDialog;
import cz.yetanotherview.webcamviewer.app.actions.ShareDialog;
import cz.yetanotherview.webcamviewer.app.actions.simple.NoCoordinatesDialog;
import cz.yetanotherview.webcamviewer.app.adapter.DiaporamaAdapter;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;

public class FullScreenFragment extends Fragment {

    private View view;
    private RelativeLayout mButtonsLayout;
    private TouchImageView touchImageView;
    private ImageView errorImageView;
    private Animation fadeOut;
    private String name;
    private String url;
    private StringSignature stringSignature;
    private double latitude, longitude;
    private boolean fullScreen;
    private ImageButton backButton;
    private DiaporamaAdapter diaporamaAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.full_screen_layout, container, false);

        Bundle bundle = getActivity().getIntent().getExtras();
        String signature = bundle.getString("signature");
        name = bundle.getString("name");
        url = bundle.getString("url");
        boolean autoRefresh = bundle.getBoolean("autoRefresh");
        int autoRefreshInterval = bundle.getInt("interval");
        latitude = bundle.getDouble("latitude");
        longitude = bundle.getDouble("longitude");
        fullScreen = bundle.getBoolean("fullScreen");

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
                if (!fullScreen) {
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
                mButtonsLayout.startAnimation(fadeOut);
            }
        };

        touchImageView = (TouchImageView) view.findViewById(R.id.touch_image);
        touchImageView.setMaxZoom(2);
        touchImageView.setOnClickListener(touchImageViewsListener);

        errorImageView = (ImageView) view.findViewById(R.id.action_error_full);

        ImageButton mapButton = (ImageButton) view.findViewById(R.id.maps_button);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (latitude != 0 || longitude != 0) {
                    ((FullScreenActivity) getActivity()).replaceFragments();
                }
                else {
                    new NoCoordinatesDialog().show(getFragmentManager(), "NoCoordinatesDialog");
                }
            }
        });

        ImageButton shareButton = (ImageButton) view.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (touchImageView.getDrawable() != null) {
                    ShareDialog shareDialog = new ShareDialog();
                    Bundle shareDialogBundle = new Bundle();
                    shareDialogBundle.putString("url", url);
                    shareDialog.setArguments(shareDialogBundle);
                    shareDialog.show(getFragmentManager(), "ShareDialog");
                }
            }
        });

        ImageButton refreshButton = (ImageButton) view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
                mButtonsLayout.startAnimation(fadeOut);
            }
        });

        ImageButton saveButton = (ImageButton) view.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (touchImageView.getDrawable() != null) {
                    DialogFragment newFragment = new SaveDialog();
                    Bundle bundle = new Bundle();
                    bundle.putInt("from", 1);
                    bundle.putString("name", name);
                    bundle.putString("url", url);
                    newFragment.setArguments(bundle);
                    newFragment.show(getFragmentManager(), "SaveDialog");
                }
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
        diaporamaAdapter = new DiaporamaAdapter(touchImageView, errorImageView);
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
                if (!fullScreen) {
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }
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
        diaporamaAdapter.loadNextImage(HttpHeader.getUrl(url), stringSignature);
    }
}
