/*
 * Copyright (C) 2013 Luca Casartelli luca@plasticpanda.com, Plastic Panda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plasticpanda.rainbow.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.core.ImagesHelper;
import com.plasticpanda.rainbow.core.MainActivity;
import com.plasticpanda.rainbow.db.Message;

import static android.graphics.Color.parseColor;

public class GalleryHelper {

    // static
    private static GalleryHelper sharedInstance;

    private MainActivity context;

    private Animator mCurrentAnimator;
    private int animationDuration;
    private ImageView expandedImageView;
    private View thumbView;
    private Rect startBounds;
    private float startScaleFinal;


    private GalleryHelper(MainActivity activity) {
        this.context = activity;
        this.animationDuration = this.context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
    }

    public static synchronized GalleryHelper getInstance(MainActivity activity) {
        if (sharedInstance == null) {
            sharedInstance = new GalleryHelper(activity);
        }
        return sharedInstance;
    }


    public void pushImageView(final View thumbView, Message message) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        context.setDefaultBackAction(false);

        this.thumbView = thumbView;

        // Load the high-resolution "zoomed-in" image.
        expandedImageView = (ImageView) context.findViewById(R.id.full_screen_img);
        expandedImageView.setImageBitmap(ImagesHelper.getInstance(context).getImage(message));

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        startBounds = new Rect();
        Rect finalBounds = new Rect();
        Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        context.findViewById(R.id.container)
            .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
            > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder play = set.play(ObjectAnimator.ofFloat(expandedImageView, View.X,
            startBounds.left, finalBounds.left));
        if (play != null) {
            play.with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                startBounds.top, finalBounds.top));
            play.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        }


        set.setDuration(animationDuration)
            .setInterpolator(new DecelerateInterpolator());

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
                final ObjectAnimator backgroundColorAnimator = ObjectAnimator.ofObject(expandedImageView,
                    "backgroundColor",
                    new ArgbEvaluator(),
                    Color.TRANSPARENT,
                    parseColor("#D9000000"));
                backgroundColorAnimator
                    .setDuration(animationDuration)
                    .start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popImageView();
            }
        });

    }

    public void popImageView() {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        context.setDefaultBackAction(true);

        expandedImageView.setBackgroundColor(Color.TRANSPARENT);
        // Animate the four positioning/sizing properties in parallel,
        // back to their original values.
        AnimatorSet set = new AnimatorSet();
        AnimatorSet.Builder play = set.play(ObjectAnimator
            .ofFloat(expandedImageView, View.X, startBounds.left));
        if (play != null) {
            play.with(ObjectAnimator
                .ofFloat(expandedImageView,
                    View.Y, startBounds.top));
            play.with(ObjectAnimator
                .ofFloat(expandedImageView,
                    View.SCALE_X, startScaleFinal));
            play.with(ObjectAnimator
                .ofFloat(expandedImageView,
                    View.SCALE_Y, startScaleFinal));
        }

        set.setDuration(animationDuration)
            .setInterpolator(new DecelerateInterpolator());

        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                thumbView.setAlpha(1f);
                expandedImageView.setVisibility(View.GONE);
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }
}
