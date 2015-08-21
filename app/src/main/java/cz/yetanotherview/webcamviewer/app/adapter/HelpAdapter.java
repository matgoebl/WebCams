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

import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.model.HelpItem;

public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ViewHolder> {

    private final List<HelpItem> helpData;
    private ClickListener clickListener;

    public HelpAdapter(List<HelpItem> helpData) {
        this.helpData = helpData;
    }

    @Override
    public HelpAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_help_item, parent, false);

        return new ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        viewHolder.imgViewIcon.setImageResource(helpData.get(position).getImageUrl());
        viewHolder.txtViewTitle.setText(helpData.get(position).getTitle());

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView imgViewIcon;
        final ImageView playOverlay;
        final TextView txtViewTitle;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            imgViewIcon = (ImageView) itemLayoutView.findViewById(R.id.help_image);
            playOverlay = (ImageView) itemLayoutView.findViewById(R.id.playOverlay);
            txtViewTitle = (TextView) itemLayoutView.findViewById(R.id.help_title);

            imgViewIcon.setOnClickListener(this);
            playOverlay.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getAdapterPosition());
        }
    }

    public interface ClickListener {
        void onClick(View v, int position);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return helpData.size();
    }

    public Object getItem(int location) {
        return helpData.get(location);
    }
}
