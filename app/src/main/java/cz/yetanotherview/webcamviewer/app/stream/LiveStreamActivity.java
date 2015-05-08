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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.LibVlcUtil;

import java.lang.ref.WeakReference;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.ImmersiveMode;

public class LiveStreamActivity extends Activity implements SurfaceHolder.Callback,
        IVideoPlayer {
    public final static String TAG = "LiveStreamActivity";

    private String mFilePath;
    private MaterialDialog dialog;

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;
    private ImageView playButton;

    // media player
    private LibVLC mLibVLC;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_screen_video_layout);

        // Receive path to play from intent
        Bundle extras = getIntent().getExtras();
        mFilePath = extras.getString("url");

        // Go FullScreen only on KitKat and up
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && extras.getBoolean("fullScreen")) {
            new ImmersiveMode().goFullScreen(this);
        }

        Log.d(TAG, "Playing back " + mFilePath);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();
        holder.addCallback(this);

        playButton = (ImageView) findViewById(R.id.play_button);

        dialog = new MaterialDialog.Builder(this)
                .content(R.string.buffering)
                .progress(true, 0)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
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

    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format,
                               int width, int height) {
        if (mLibVLC != null)
            mLibVLC.attachSurface(holder.getSurface(), this);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    }

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

    @Override
    public void setSurfaceLayout(int width, int height, int visible_width,
                                 int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    private void createPlayer() {
        releasePlayer();
        try {
            mLibVLC = new LibVLC();
            mLibVLC.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            mLibVLC.setSubtitlesEncoding("");
            mLibVLC.setAout(LibVLC.AOUT_OPENSLES);
            mLibVLC.setTimeStretching(true);
            mLibVLC.setVerboseMode(true);
            mLibVLC.setVout(LibVLC.VOUT_ANDROID_WINDOW);
            mLibVLC.destroy();
            mLibVLC.init(this);
            EventHandler.getInstance().addHandler(mHandler);
            holder.setKeepScreenOn(true);
            mLibVLC.playMRL(mFilePath);
        } catch (Exception e) {
            streamError();
        }
    }

    private void releasePlayer() {
        if (mLibVLC == null)
            return;
        EventHandler.getInstance().removeHandler(mHandler);
        mLibVLC.stop();
        mLibVLC.detachSurface();
        holder = null;
        mLibVLC = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<LiveStreamActivity> mOwner;

        public MyHandler(LiveStreamActivity owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            LiveStreamActivity player = mOwner.get();

            // SamplePlayer events
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            // LibVLC events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerPlaying:
                    Log.d(TAG, "MediaPlayerStartReached");
                    player.dialogDismiss();
                    break;
                case EventHandler.MediaPlayerEndReached:
                    Log.d(TAG, "MediaPlayerEndReached");
                    player.showRePlayButton();
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerEncounteredError:
                    Log.d(TAG, "MediaPlayerErrorReached");
                    player.showErrorDialog();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void eventHardwareAccelerationError() {
        // Handle errors with hardware acceleration
        Log.e(TAG, "Error with hardware acceleration");
        this.releasePlayer();
        new MaterialDialog.Builder(this)
                .title(R.string.error)
                .content(R.string.error_hw)
                .positiveText(android.R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    public int configureSurface(Surface surface, int width, int height, int hal) {
        Log.d(TAG, "configureSurface: width = " + width + ", height = " + height);
        if (LibVlcUtil.isICSOrLater() || surface == null)
            return -1;
        if (width * height == 0)
            return 0;
        if(hal != 0)
            holder.setFormat(hal);
        holder.setFixedSize(width, height);
        return 1;
    }

    private void dialogDismiss() {
        dialog.dismiss();
    }

    private void showRePlayButton() {
        playButton.setVisibility(View.VISIBLE);
    }

    public void rePlay(View view) {
        playButton.setVisibility(View.GONE);
        recreate();
    }

    private void showErrorDialog() {
        dialog.dismiss();
        streamError();
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
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        finish();
                    }
                })
                .show();
    }
}