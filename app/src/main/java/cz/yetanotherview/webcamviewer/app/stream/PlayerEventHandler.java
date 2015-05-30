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

import android.os.Message;
import android.util.Log;

import org.videolan.libvlc.EventHandler;

public class PlayerEventHandler extends WeakHandler<LiveStreamActivity> {

    public PlayerEventHandler(LiveStreamActivity owner) {
        super(owner);
    }

    @Override
    public void handleMessage(Message msg) {
        LiveStreamActivity activity = getOwner();
        if(activity == null) return;

        if (activity.mSwitchingView) return;

        if (msg.what == LiveStreamActivity.VideoSizeChanged) {
            activity.setSize(msg.arg1, msg.arg2);
            return;
        }

        switch (msg.getData().getInt("event")) {
            case EventHandler.MediaPlayerPlaying:
                Log.d(LiveStreamActivity.TAG, "MediaPlayerStartReached");
                activity.dialogDismiss();
                break;
            case EventHandler.MediaPlayerEndReached:
                Log.d(LiveStreamActivity.TAG, "MediaPlayerEndReached");
                activity.releasePlayer();
                activity.showRePlayButton();
                break;
            case EventHandler.MediaPlayerEncounteredError:
                Log.d(LiveStreamActivity.TAG, "MediaPlayerErrorReached");
                activity.showErrorDialog();
                break;
            default:
                break;
        }
    }
}