/*
* ******************************************************************************
* Copyright (c) 2013-2014 Tomas Valenta.
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;
import cz.yetanotherview.webcamviewer.app.model.Link;

public class LinkAdapter extends BaseAdapter {

    private Context context;
    private List<Link> linkList;

    public LinkAdapter(Context context, List<Link> linkList) {
        super();
        this.context = context;
        this.linkList = linkList;
    }

    public class ViewHolder {
        ImageView linkImageView;
        //TextView linkUrl;
    }

    @Override
    public int getCount() {
        return linkList.size();
    }

    @Override
    public Object getItem(int position) {
        return linkList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.link_list_item, parent, false);
            holder = new ViewHolder();
            holder.linkImageView = (ImageView) convertView.findViewById(R.id.link_image_view);
            //holder.linkUrl = (TextView) convertView.findViewById(R.id.link_url);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Link link = linkList.get(position);
        Glide.with(context)
                .load(HttpHeader.getUrl(link.getUrl()))
                .crossFade()
                .fitCenter()
                .placeholder(R.drawable.placeholder_small)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.linkImageView);
        //holder.linkUrl.setText(link.getUrl());

        return convertView;
    }
}
