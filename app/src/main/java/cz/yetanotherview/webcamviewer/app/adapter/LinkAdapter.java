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
import android.widget.TextView;

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
        TextView imageDimensions;
        TextView imageSize;
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
            holder.linkImageView = (ImageView) convertView.findViewById(R.id.link_image);
            holder.imageDimensions = (TextView) convertView.findViewById(R.id.link_text_dim);
            holder.imageSize = (TextView) convertView.findViewById(R.id.link_text_size);
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
        String widthAndHeightString = link.getWidthAndHeightString();
        String dimensionsString;
        if (widthAndHeightString != null) {
            dimensionsString = widthAndHeightString;
        }
        else dimensionsString = context.getString(R.string.not_detected);
        holder.imageDimensions.setText(" " + dimensionsString);
        holder.imageSize.setText(" " + link.getSizeString());

        int width = link.getWidth();
        if (width > 900) {
            holder.imageDimensions.setTextColor(context.getResources().getColor(R.color.hyperlink));
        }
        else if (width < 900 && width > 600) {
            holder.imageDimensions.setTextColor(context.getResources().getColor(R.color.blue));
        }
        else if (width < 600 && width > 1) {
            holder.imageDimensions.setTextColor(context.getResources().getColor(R.color.orange));
        }
        else holder.imageDimensions.setTextColor(context.getResources().getColor(R.color.fab_normal));

        long size = link.getSize();
        if (size > 100000) {
            holder.imageSize.setTextColor(context.getResources().getColor(R.color.hyperlink));
        }
        else if (size < 100000 && size > 50000) {
            holder.imageSize.setTextColor(context.getResources().getColor(R.color.blue));
        }
        else holder.imageSize.setTextColor(context.getResources().getColor(R.color.orange));

        return convertView;
    }
}
