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
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.helper.Utils;
import cz.yetanotherview.webcamviewer.app.helper.HttpHeader;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class ManualSelectionAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private List<WebCam> webCamList;
    private List<WebCam> origList;

    public ManualSelectionAdapter(Context context, List<WebCam> webCamList) {
        super();
        this.context = context;
        this.webCamList = webCamList;
    }

    public class ViewHolder {
        CheckBox selCheckBox;
        ImageView selImageView;
        TextView selTextName;
        TextView selTextTags;
        TextView selTextCountry;
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final List<WebCam> results = new ArrayList<>();
                if (origList == null)
                    origList = webCamList;
                if (constraint != null) {
                    if (origList != null && origList.size() > 0) {
                        for (final WebCam g : origList) {
                            String strippedNameAndCountry;
                            if (g.getCountry() != null) {
                                strippedNameAndCountry = Utils.getNameStrippedAccents(g.getName() +
                                        " " + g.getTags() + " " + new Locale("", g.getCountry()).getDisplayCountry());
                            }
                            else {
                                strippedNameAndCountry = Utils.getNameStrippedAccents(g.getName());
                            }
                            String strippedInput = Utils.getNameStrippedAccents(constraint.toString().trim());
                            if (strippedNameAndCountry.toLowerCase().contains(strippedInput)) {
                                results.add(g);
                            }
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                webCamList = (List<WebCam>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return webCamList.size();
    }

    public int getCheckedCount() {
        List<WebCam> tempList = new ArrayList<>();
        for (WebCam webCam : webCamList) {
            if (webCam.isSelected()) {
                tempList.add(webCam);
            }
        }
        return tempList.size();
    }

    public void setAllChecked() {
        for (WebCam webCam : webCamList) {
            webCam.setSelected(true);
        }
        notifyDataSetChanged();
    }

    public void setAllUnChecked() {
        for (WebCam webCam : webCamList) {
            webCam.setSelected(false);
        }
        notifyDataSetChanged();
    }

    public void setSelected(List<WebCam> webCams) {
        for (WebCam webCam : webCams) {
            for (WebCam webCamFromList : webCamList) {
                if (webCamFromList.getId() == webCam.getId()) {
                    webCamFromList.setSelected(true);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int position) {
        return webCamList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.filtered_list_view_item, parent, false);
            holder = new ViewHolder();
            holder.selCheckBox = (CheckBox) convertView.findViewById(R.id.sel_checkbox);
            holder.selImageView = (ImageView) convertView.findViewById(R.id.sel_image);
            holder.selTextName = (TextView) convertView.findViewById(R.id.sel_text_name);
            holder.selTextTags = (TextView) convertView.findViewById(R.id.sel_text_tags);
            holder.selTextCountry = (TextView) convertView.findViewById(R.id.sel_text_country);
            convertView.setTag(holder);

            holder.selCheckBox.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    WebCam webCam = (WebCam) cb.getTag();

                    webCam.setSelected(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        WebCam webCam = webCamList.get(position);

        String url;
        if (webCam.isStream()) {
            url = webCam.getThumbUrl();
        }
        else url = webCam.getUrl();

        holder.selCheckBox.setChecked(webCam.isSelected());
        Glide.with(context)
                .load(HttpHeader.getUrl(url))
                .centerCrop()
                .crossFade()
                .error(R.drawable.error)
                .placeholder(R.drawable.placeholder_small)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.selImageView);
        holder.selTextName.setText(webCam.getName());
        if (webCam.getCountry() != null) {
            holder.selTextCountry.setText("(" + new Locale("", webCam.getCountry()).getDisplayCountry() + ")");
            holder.selTextTags.setText(webCam.getTags());
        }
        else {
            holder.selTextCountry.setVisibility(View.GONE);
            holder.selTextTags.setVisibility(View.GONE);
            holder.selTextName.setSingleLine(false);
            holder.selTextName.setTypeface(null, Typeface.NORMAL);
        }
        holder.selCheckBox.setTag(webCam);

        return convertView;
    }
}
