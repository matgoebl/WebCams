/*
* ******************************************************************************
* Copyright (c) 2013-2015 Róbert Papp - Tomas Valenta.
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

package cz.yetanotherview.webcamviewer.app.adapter;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;

import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;

public class WebCamAdapterImages {

    private Context context;
    private WebCamAdapter.WebCamViewHolder webcamViewHolder;
    private int layoutId;
    private boolean isStream;
    private StringSignature stringSignature;
    private String source;
    private int count;

    public WebCamAdapterImages(Context context, int layoutId, WebCamAdapter.WebCamViewHolder webcamViewHolder, boolean isStream,
                               StringSignature stringSignature, String source) {
        this.context = context;
        this.layoutId = layoutId;
        this.webcamViewHolder = webcamViewHolder;
        this.isStream = isStream;
        this.stringSignature = stringSignature;
        this.source = source;
        count = 0;
        loadImage();
    }

    public void loadImage() {

        if (layoutId == 1) {
            Glide.with(context)
                    .load(HttpHeader.getUrl(source))
                    .centerCrop()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .signature(stringSignature)
                    .listener(new WebCamRequestListener())
                    .into(webcamViewHolder.vImage);
        }
        else {
            Glide.with(context)
                    .load(HttpHeader.getUrl(source))
                    .dontTransform()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .signature(stringSignature)
                    .listener(new WebCamRequestListener())
                    .into(webcamViewHolder.vImage);
        }
    }

    private class WebCamRequestListener implements RequestListener<GlideUrl, GlideDrawable> {

        @Override
        public boolean onException(Exception e, GlideUrl model, Target<GlideDrawable> target,
                                   boolean isFirstResource) {
            if (count == 6) {
                webcamViewHolder.vProgress.setVisibility(View.GONE);
                webcamViewHolder.vError.setVisibility(View.VISIBLE);
            }
            else {
                count++;
                loadImage();
            }
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, GlideUrl model, Target<GlideDrawable> target,
                                       boolean isFromMemoryCache, boolean isFirstResource) {
            webcamViewHolder.vProgress.setVisibility(View.GONE);
            webcamViewHolder.vError.setVisibility(View.GONE);
            if (isStream) {
                webcamViewHolder.vPlay.setVisibility(View.VISIBLE);
            } else webcamViewHolder.vPlay.setVisibility(View.GONE);
            return false;
        }
    }
}
