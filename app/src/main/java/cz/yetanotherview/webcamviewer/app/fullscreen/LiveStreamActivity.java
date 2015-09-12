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

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.ImmersiveMode;

public class LiveStreamActivity extends AppCompatActivity implements IVLCVout.Callback,
        MediaPlayer.EventListener, LibVLC.HardwareAccelerationError {

    private final static String TAG = "LiveStreamActivity";

    private String mFilePath;
    private MaterialDialog dialog;

    private FrameLayout playButton;

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;

    private boolean mHardwareAccelerationError, hwAcceleration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_video_layout);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

        // Receive path to play from intent
        Bundle extras = getIntent().getExtras();
        mFilePath = extras.getString("url");
        String mName = extras.getString("name");
        if (mName == null) {
            mName = "";
        }

        // Go FullScreen only on KitKat and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && extras.getBoolean("fullScreen")) {
            new ImmersiveMode().goFullScreen(this);
        }

        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();

        playButton = (FrameLayout) findViewById(R.id.play_button);

        dialog = new MaterialDialog.Builder(this)
                .title(mName)
                .content(R.string.buffering)
                .progress(true, 0)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();

        mHardwareAccelerationError = false;
        hwAcceleration = extras.getBoolean("hwAcceleration");

        String toastText;
        if (hwAcceleration) {
            toastText = getString(R.string.hw_acceleration_enabled);
        } else toastText = getString(R.string.hw_acceleration_disabled);
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createPlayer(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    /*************
     * Surface
     *************/
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(holder == null || mSurface == null)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    /*************
     * Player
     *************/

    private void createPlayer(String media) {
        releasePlayer();
        try {
            // Create LibVLC
            ArrayList<String> options = new ArrayList<>();
            //options.add("--aout=opensles");
            //options.add("--audio-time-stretch"); // time stretching
            //options.add("-vvv"); // verbosity
            libvlc = new LibVLC(options);
            libvlc.setOnHardwareAccelerationError(this);
            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);
            mMediaPlayer.setEventListener(this);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc, Uri.parse(media));
            if (!hwAcceleration || mHardwareAccelerationError) {
                m.setHWDecoderEnabled(false, false);
                Log.d("HW Acc: ", "Disabled");
            }
            else {
                m.setHWDecoderEnabled(true, false);
                Log.d("HW Acc: ", "Enabled");
            }
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
            streamError();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /*************
     * Events
     *************/

    @Override
    public void onEvent(MediaPlayer.Event event) {
        switch (event.type) {
            case MediaPlayer.Event.Playing:
                Log.i(TAG, "MediaPlayerPlaying");
                dialog.dismiss();
                break;
            case MediaPlayer.Event.EndReached:
                Log.i(TAG, "MediaPlayerEndReached");
                releasePlayer();
                playButton.setVisibility(View.VISIBLE);
                break;
            case MediaPlayer.Event.EncounteredError:
                Log.i(TAG, "MediaPlayerEncounteredError");
                dialog.dismiss();
                streamError();
                break;
        }
    }

    @Override
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {}

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {}

    @Override
    public void eventHardwareAccelerationError() {
        Log.e(TAG, "Error with hardware acceleration");
        mHardwareAccelerationError = true;
        dialog.dismiss();
        this.releasePlayer();
        String content = getString(R.string.error_hw) + " " + getString(R.string.disable_and_try_again);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(content)
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        createPlayer(mFilePath);
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        finish();
                    }
                })
                .build();
        if(!isFinishing()) {
            dialog.show();
        }
    }

    /*************
     * Others
     *************/

    public void rePlay(View view) {
        playButton.setVisibility(View.GONE);
        recreate();
    }

    private void streamError() {
        String errorMessage = getString(R.string.stream_error_description) + "\n\n"
                + "• " + getString(R.string.stream_error_0) + "\n"
                + "• " + getString(R.string.stream_error_1) + "\n"
                + "• " + getString(R.string.stream_error_2) + "\n";

        new MaterialDialog.Builder(this)
                .title(R.string.something_is_wrong)
                .content(errorMessage)
                .positiveText(android.R.string.ok)
                .iconRes(R.drawable.warning)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        finish();
                    }
                })
                .show();
    }
}