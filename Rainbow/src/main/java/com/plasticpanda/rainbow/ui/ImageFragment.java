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

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.plasticpanda.rainbow.R;
import com.plasticpanda.rainbow.core.ImagesHelper;
import com.plasticpanda.rainbow.db.Message;


public class ImageFragment extends Fragment {

    private static ImageFragment sharedInstance;

    private Activity context;

    private Message message;

    public ImageFragment(Activity context) {
        super();
        this.context = context;
    }

    public static synchronized ImageFragment getInstance(Activity context) {
        if (sharedInstance == null) {
            sharedInstance = new ImageFragment(context);
        }
        return sharedInstance;
    }

    public void setMessage(Message msg) {
        this.message = msg;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Bitmap bmp = ImagesHelper.getInstance(context).getImage(message);
        TouchImageView imgView = (TouchImageView) view.findViewById(R.id.full_screen_img);
        imgView.maintainZoomAfterSetImage(false);
        imgView.setImageBitmap(bmp);
        imgView.maintainZoomAfterSetImage(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }
}
