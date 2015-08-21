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

package cz.yetanotherview.webcamviewer.app.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class WidgetConfigureAdapter extends RecyclerView.Adapter<WidgetConfigureAdapter.WidgetConfigureViewHolder> {

    private final List<WebCam> webCamList;
    private ClickListener clickListener;

    public WidgetConfigureAdapter(List<WebCam> webCamList) {
        this.webCamList = webCamList;
    }

    @Override
    public int getItemCount() {
        return webCamList.size();
    }

    @Override
    public void onBindViewHolder(WidgetConfigureViewHolder widgetConfigureViewHolder, int i) {
        WebCam webCam = webCamList.get(i);

        String url;
        if (webCam.isStream()) {
            url = webCam.getThumbUrl();
        }
        else url = webCam.getUrl();

        Glide.with(widgetConfigureViewHolder.vImage.getContext())
                .load(HttpHeader.getUrl(url))
                .centerCrop()
                .crossFade()
                .error(R.drawable.error)
                .placeholder(R.drawable.placeholder_small)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(widgetConfigureViewHolder.vImage);

        widgetConfigureViewHolder.vName.setText(webCam.getName());
    }

    @Override
    public WidgetConfigureViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.widget_configure_list_item, viewGroup, false);

        return new WidgetConfigureViewHolder(itemView);
    }

    public class WidgetConfigureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        final ImageView vImage;
        final TextView vName;

        public WidgetConfigureViewHolder(View v) {
            super(v);
            vImage = (ImageView) v.findViewById(R.id.widget_configure_list_image);
            vName =  (TextView) v.findViewById(R.id.widget_configure_list_text);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition());
        }
    }

    public interface ClickListener {
        void onClick(View v, int position);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }
}
