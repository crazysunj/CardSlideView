package com.crazysunj.cardslide;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crazysunj.cardslideview.CardHandler;

/**
 * description
 * <p>
 * Created by sunjian on 2017/6/24.
 */

public class MyCardHandler implements CardHandler<String> {

    @Override
    public View onBind(final Context context, final String data, final int position) {
        View view = View.inflate(context, R.layout.item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        Glide.with(context).load(data).into(imageView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "data:" + data + "position:" + position, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}
