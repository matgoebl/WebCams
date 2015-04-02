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

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.signature.StringSignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cz.yetanotherview.webcamviewer.app.R;
import cz.yetanotherview.webcamviewer.app.Utils;
import cz.yetanotherview.webcamviewer.app.model.WebCam;

public class WebCamAdapter extends RecyclerView.Adapter<WebCamAdapter.WebCamViewHolder> {

    private final int mLayoutId;
    private final int mOrientation;
    private boolean imageOn;
    private boolean simpleList;

    private StringSignature mStringSignature;

    private final Context mContext;
    private List<WebCam> webCamItems;
    private ArrayList<WebCam> filteredList;
    private ClickListener clickListener;

    public WebCamAdapter(Context context, List<WebCam> webCamItems, int orientation, int layoutId,
                         StringSignature stringSignature, boolean imageOn, boolean simpleList) {
        this.webCamItems = webCamItems;
        mContext = context;
        mLayoutId = layoutId;
        mOrientation = orientation;
        mStringSignature = stringSignature;
        this.imageOn = imageOn;
        this.simpleList = simpleList;
        this.filteredList = new ArrayList<>();
        this.filteredList.addAll(webCamItems);
    }

    public void swapData(List<WebCam> webCamItems) {
        this.webCamItems = webCamItems;
        this.filteredList = new ArrayList<>();
        this.filteredList.addAll(webCamItems);
        notifyDataSetChanged();
    }

    public void filter(String charText) {
        charText = Utils.getNameStrippedAccents(charText.toLowerCase().trim());
        webCamItems.clear();
        if (charText.length() == 0) {
            webCamItems.addAll(filteredList);
        }
        else
        {
            for (WebCam webCam : filteredList)
            {
                if (Utils.getNameStrippedAccents(webCam.getName()).toLowerCase().contains(charText))
                {
                    webCamItems.add(webCam);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public WebCamViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if (mLayoutId == 1) {
            if (!imageOn && simpleList) {
                return new WebCamViewHolder(LayoutInflater.from(mContext).inflate(R.layout.webcam_layout_list_simple, viewGroup, false));
            }
            else {
                if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
                    return new WebCamViewHolder(LayoutInflater.from(mContext).inflate(R.layout.webcam_layout_list_pre_lollipop, viewGroup, false));
                }
                else return new WebCamViewHolder(LayoutInflater.from(mContext).inflate(R.layout.webcam_layout_list, viewGroup, false));
            }
        }
        else return new WebCamViewHolder(LayoutInflater.from(mContext).inflate(R.layout.webcam_layout_grid, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(WebCamViewHolder webcamViewHolder, int position) {
        WebCam webCam = webCamItems.get(position);
        webcamViewHolder.vName.setText(webCam.getName());

        if (imageOn) {
            webcamViewHolder.vProgress.setVisibility(View.VISIBLE);
            if (webcamViewHolder.vImage.getWidth() == 0) {
                webcamViewHolder.vImage.setMinimumHeight(Utils.getImageHeight(mContext, mLayoutId));
            }
            loadImages(webcamViewHolder, webCam.getUrl());
        }
        else if (simpleList){
            webcamViewHolder.vProgress.setVisibility(View.GONE);
            webcamViewHolder.vImage.setVisibility(View.GONE);
            webcamViewHolder.vNumber.setText((position + 1) + ". ");
            webcamViewHolder.vName.setShadowLayer(0,0,0,0);
            webcamViewHolder.vName.setTextColor(mContext.getResources().getColor(R.color.primary));
            webcamViewHolder.vButton.setImageResource(R.drawable.ic_action_navigation_more_vert_dark);
        }
        else {
            webcamViewHolder.vProgress.setVisibility(View.GONE);

            if (mLayoutId == 1) {
                webcamViewHolder.vImage.setMaxHeight(Utils.getImageHeight(mContext, mLayoutId));
            }

            webcamViewHolder.vImage.setImageResource(Utils.getRandomImage());
        }
    }

    private void loadImages(final WebCamViewHolder webcamViewHolder, String source) {

        if (mLayoutId == 1) {
            Glide.with(mContext)
                    .load(source)
                    .centerCrop()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .signature(mStringSignature)
                    .into(new GlideDrawableImageViewTarget(webcamViewHolder.vImage) {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            webcamViewHolder.vProgress.setVisibility(View.GONE);
                        }
                    });
        }
        else {
            Glide.with(mContext)
                    .load(source)
                    .dontTransform()
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .signature(mStringSignature)
                    .into(new GlideDrawableImageViewTarget(webcamViewHolder.vImage) {
                        @Override
                        public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            webcamViewHolder.vProgress.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public class WebCamViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        protected TextView vNumber;
        protected TextView vName;
        protected ImageView vImage;
        protected ImageButton vButton;

        protected ProgressBar vProgress;

        public WebCamViewHolder(View itemLayoutView) {
            super(itemLayoutView);
            vNumber = (TextView) itemLayoutView.findViewById(R.id.numberTextView);
            vName = (TextView) itemLayoutView.findViewById(R.id.titleTextView);
            vImage = (ImageView) itemLayoutView.findViewById(R.id.imageView);
            vButton = (ImageButton) itemLayoutView.findViewById(R.id.action_edit);

            vProgress = (ProgressBar) itemLayoutView.findViewById(R.id.loadingProgressBar);


            vButton.setOnClickListener(this);

            if (!imageOn && simpleList) {
                vName.setOnClickListener(this);
                vName.setOnLongClickListener(this);
            }
            else {
                vImage.setOnClickListener(this);
                vImage.setOnLongClickListener(this);
            }

            final int small = mContext.getResources().getDimensionPixelSize(R.dimen.small_padding);
            final int middle = mContext.getResources().getDimensionPixelSize(R.dimen.middle_padding);
            final int big = mContext.getResources().getDimensionPixelSize(R.dimen.big_padding);

            if (mLayoutId == 1) {
                vNumber.setTextSize(28);
                vName.setTextSize(28);
                if (!imageOn && simpleList) {
                    vNumber.setPadding(big,0,0,middle);
                }
                else vName.setPadding(big,0,0,middle);
            }
            else if (mLayoutId == 2 && mOrientation == 1) {
                vNumber.setTextSize(16);
                vName.setTextSize(16);
                if (!imageOn && simpleList) {
                    vNumber.setPadding(middle,0,0,small);
                }
                else vName.setPadding(middle,0,0,small);
                vButton.setMaxHeight(120);
            }
            else if (mLayoutId == 2 && mOrientation == 2) {
                vNumber.setTextSize(26);
                vName.setTextSize(26);
                if (!imageOn && simpleList) {
                    vNumber.setPadding(middle,0,0,small);
                }
                else vName.setPadding(middle,0,0,small);
            }
            else if (mLayoutId == 3) {
                vNumber.setTextSize(19);
                vName.setTextSize(19);
                if (!imageOn && simpleList) {
                    vNumber.setPadding(small,0,0,0);
                }
                else vName.setPadding(small,0,0,0);
                vButton.setMaxHeight(120);
            }
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageButton){
                clickListener.onClick(v, getAdapterPosition(), true, false);
            } else {
                clickListener.onClick(v, getAdapterPosition(), false, false);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            clickListener.onClick(v, getAdapterPosition(), false, true);
            return true;
        }
    }

    public interface ClickListener {

        public void onClick(View v, int position, boolean isEditClick, boolean isLongClick);
    }

    /* Setter for listener. */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return webCamItems.size();
    }

    public Object getItem(int location) {
        return webCamItems.get(location);
    }

    public List<WebCam> getItems() {
        return webCamItems;
    }

    public WebCam getItemAt(int position) {
        if (position < webCamItems.size())
            return webCamItems.get(position);
        return null;
    }

    public void addItem(int position, WebCam webCam) {
        webCamItems.add(position, webCam);
        notifyItemInserted(position);
    }

    public void modifyItem(int position, WebCam webCam) {
        webCamItems.set(position, webCam);
        notifyItemChanged(position);
    }

    public void removeItem(WebCam webCam) {
        int position = webCamItems.indexOf(webCam);
        webCamItems.remove(position);
        notifyItemRemoved(position);
    }

    public void moveItemUp(WebCam webCam) {
        int position = webCamItems.indexOf(webCam);
        int newPosition = position - 1;
        if (newPosition >= 0) {
            Collections.swap(webCamItems, position, newPosition);
            notifyItemMoved(position, newPosition);
        }
    }

    public void moveItemDown(WebCam webCam) {
        int position = webCamItems.indexOf(webCam);
        int newPosition = position + 1;
        if (newPosition < webCamItems.size()) {
            Collections.swap(webCamItems, position, newPosition);
            notifyItemMoved(position, newPosition);
        }
    }

    public void refreshViewImages(StringSignature stringSignature) {
        mStringSignature = stringSignature;
        notifyDataSetChanged();
    }
}
