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

package cz.yetanotherview.webcamviewer.app.stream;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.MaterialDialog;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.ImmersiveMode;

public class LiveStreamActivity extends Activity implements IVLCVout.Callback,
        MediaPlayer.EventListener, LibVLC.HardwareAccelerationError {

    private final static String TAG = "LiveStreamActivity";

    private String mFilePath;
    private MaterialDialog dialog;

    // display surface
    private SurfaceView mSurface;
    private FrameLayout playButton;

    // media player
    private LibVLC mLibVLC;
    private IVLCVout vlcVout;
    private MediaPlayer mMediaPlayer;
    private int mVideoWidth;
    private int mVideoHeight;
    private boolean mHardwareAccelerationError;

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
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createPlayer();
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

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        setSize(width, height);
    }

    @Override
    public void onSurfacesCreated(IVLCVout ivlcVout) {}

    @Override
    public void onSurfacesDestroyed(IVLCVout ivlcVout) {}

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

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(mSurface == null)
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

        // set display size
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    private void createPlayer() {
        releasePlayer();
        try {
            mLibVLC = new LibVLC();
            mLibVLC.setOnHardwareAccelerationError(this);

            mMediaPlayer = new MediaPlayer(mLibVLC);
            vlcVout = mMediaPlayer.getVLCVout();
            vlcVout.setVideoView(mSurface);
            vlcVout.addCallback(this);
            vlcVout.attachViews();

            mMediaPlayer.setEventListener(this);
            Media media = new Media(mLibVLC, Uri.parse(mFilePath));
            if (mHardwareAccelerationError) {
                media.setHWDecoderEnabled(false, false);
            }
            mMediaPlayer.setMedia(media);
            mMediaPlayer.play();
        } catch (Exception e) {
            streamError();
        }
    }

    private void releasePlayer() {
        if (mLibVLC == null)
            return;
        vlcVout.detachViews();
        vlcVout.removeCallback(this);
        mLibVLC.release();
        mMediaPlayer.release();

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

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

    @Override
    public void eventHardwareAccelerationError() {
        Log.e(TAG, "Error with hardware acceleration");
        mHardwareAccelerationError = true;
        dialog.dismiss();
        releasePlayer();
        String content = getString(R.string.error_hw) + " " + getString(R.string.disable_and_try_again);
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(content)
                .positiveText(R.string.Yes)
                .negativeText(R.string.No)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        createPlayer();
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
}