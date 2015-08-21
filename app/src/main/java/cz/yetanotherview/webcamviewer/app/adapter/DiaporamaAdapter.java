package cz.yetanotherview.webcamviewer.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.signature.StringSignature;

import java.util.Arrays;

/**
 * Helper class to use when you  want to load several images consecutively without
 * displaying the default placeholder between them.<br>
 * To initiate a load, instead of the usual {@code Glide.with(ctx).load(model).into(view);}, you need to call
 * {@link DiaporamaAdapter#loadNextImage(Object, StringSignature)} or {@link DiaporamaAdapter#loadNextImage(Object, StringSignature, BitmapTransformation...)}.
 *
 * @author François
 */
public class DiaporamaAdapter {

    @Nullable
    private Drawable mPlaceholder;

    @NonNull
    private final ImageView mImageView;
    private final ImageView mErrorImage;

    private final Context mContext;

    /**
     * In order to do not conflict with Glide's clear() calls, we need to use two targets and to
     * switch between them each time an image is loaded.
     */
    @NonNull
    private DiaporamaViewTarget mCurrentTarget;
    @NonNull
    private DiaporamaViewTarget mNextTarget;

    private int mAnimationDuration;
    private int mLastLoadHash;
    private boolean firstTime;

    /**
     * Default constructor, initiate a Diaporama Adapter with the terminal's {@code config_mediumAnimTime}
     * for animation duration and no placeholder.
     *
     * @param imageView the imageView where the images will be loaded
     */
    public DiaporamaAdapter(final @NonNull ImageView imageView, final ImageView errorImage) {
        this(imageView, errorImage, -1, -1);
        firstTime = true;
    }

    /**
     * @param imageView         the imageView where the images will be loaded
     * @param animationDuration the animationDuration to use or {@code -1} to use the platform's {@code config_mediumAnimTime}
     * @param placeholderResId  the drawableResId to use or {@code -1} if you don't want to display one.
     */
    private DiaporamaAdapter(final @NonNull ImageView imageView,
                             final ImageView errorImage,
                             final int animationDuration,
                             final @DrawableRes int placeholderResId) {
        mImageView = imageView;
        mErrorImage = errorImage;
        mContext = imageView.getContext();

        mCurrentTarget = new DiaporamaViewTarget(mImageView);
        mNextTarget = new DiaporamaViewTarget(mImageView);
        mCurrentTarget.setPreviousTarget(mNextTarget);
        mNextTarget.setPreviousTarget(mCurrentTarget);


        if (animationDuration != -1) {
            mAnimationDuration = animationDuration;
        } else {
            mAnimationDuration = mImageView.getResources().getInteger(
                    android.R.integer.config_mediumAnimTime);
        }

        if (placeholderResId != -1) setPlaceholder(placeholderResId);
    }


    //////////////////////////////////////////////////////////////////////////////////////
    // public methods
    //////////////////////////////////////////////////////////////////////////////////////

    public <T> void loadNextImage(T model, @NonNull StringSignature stringSignature,
                                  @NonNull BitmapTransformation... transformations) {
        //noinspection MagicNumber
        int hash = model.hashCode() + 31 * Arrays.hashCode(transformations);
        if (mLastLoadHash == hash) return;
        Glide.with(mContext).load(model).asBitmap().transform(transformations).signature(stringSignature).into(mCurrentTarget);
        mLastLoadHash = hash;
    }

    public <T> void loadNextImage(T model, @NonNull StringSignature stringSignature) {
        if (firstTime) {
            Glide.with(mContext)
                    .load(model)
                    .asBitmap()
                    .fitCenter()
                    .signature(stringSignature)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .listener(new RequestListener<T, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, T model, Target<Bitmap> target, boolean isFirstResource) {
                            mErrorImage.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, T model, Target<Bitmap> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            mErrorImage.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mCurrentTarget);
            firstTime = false;       }
        else {
            Glide.get(mContext).clearMemory();
            Glide.with(mContext)
                    .load(model)
                    .asBitmap()
                    .fitCenter()
                    .signature(stringSignature)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .listener(new RequestListener<T, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, T model, Target<Bitmap> target, boolean isFirstResource) {
                            mErrorImage.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, T model, Target<Bitmap> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            mErrorImage.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mCurrentTarget);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Setters
    //////////////////////////////////////////////////////////////////////////////////////

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    private void setPlaceholder(@DrawableRes int resId) {
        Bitmap bitmap = BitmapFactory.decodeResource(mImageView.getResources(), resId);
        mPlaceholder = new BitmapDrawable(mImageView.getResources(), bitmap);
        mImageView.setImageDrawable(mPlaceholder);
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // ViewTarget
    //////////////////////////////////////////////////////////////////////////////////////

    private class DiaporamaViewTarget extends ViewTarget<ImageView, Bitmap> {

        @NonNull
        private DiaporamaViewTarget mPreviousTarget;

        @Nullable
        private BitmapDrawable mLoadedDrawable;

        @Nullable
        private Request mRequest;

        public DiaporamaViewTarget(ImageView view) {
            super(view);
        }

        public void setPreviousTarget(@NonNull DiaporamaViewTarget previousTarget) {
            mPreviousTarget = previousTarget;
        }

        @Nullable
        public BitmapDrawable getLoadedDrawable() {
            return mLoadedDrawable;
        }

        @Override
        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
            Drawable previousDrawable = getPreviousImageForTransition();
            mLoadedDrawable = new BitmapDrawable(mImageView.getResources(), resource);

            // we null the other Target loaded drawable so it can eventually be reclaimed.
            mPreviousTarget.mLoadedDrawable = null;

            DiaporamaAdapter.this.mCurrentTarget = mPreviousTarget;
            DiaporamaAdapter.this.mNextTarget = this;


            if (previousDrawable != null) {
                final Drawable[] layers = new Drawable[2];
                // Prevent cascade of TransitionDrawables.
                if (previousDrawable instanceof TransitionDrawable) {
                    final TransitionDrawable previousTransitionDrawable =
                            (TransitionDrawable) previousDrawable;
                    layers[0] = previousTransitionDrawable.getDrawable(
                            previousTransitionDrawable.getNumberOfLayers() - 1);
                } else {
                    layers[0] = previousDrawable;
                }
                layers[1] = mLoadedDrawable;
                TransitionDrawable drawable = new TransitionDrawable(layers);
                view.setImageDrawable(drawable);
                drawable.startTransition(mAnimationDuration);
            } else {
                AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
                alphaAnimation.setDuration(mAnimationDuration);
                mImageView.setAnimation(alphaAnimation);
                alphaAnimation.start();
                view.setImageDrawable(mLoadedDrawable);
            }
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            mLastLoadHash = 0;
        }

        @Override
        public void setRequest(@Nullable Request request) {
            mRequest = request;
        }

        @Override
        @Nullable
        public Request getRequest() {
            return mRequest;
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            super.onLoadCleared(placeholder);
            // we need to null the drawable in order to make sure that a recycled bitmap won't be reused.
            mLoadedDrawable = null;
        }


        @Nullable
        private Drawable getPreviousImageForTransition() {
            if (mPreviousTarget.getLoadedDrawable() != null) {
                return mPreviousTarget.getLoadedDrawable();
            }
            if (mPlaceholder != null) return mPlaceholder;

            return null;
        }
    }
}