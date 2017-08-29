package com.crazysunj.cardslide;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crazysunj.cardslideview.CardHandler;
import com.crazysunj.cardslideview.CardViewPager;
import com.crazysunj.cardslideview.ElasticCardView;

/**
 * description
 * <p>
 * Created by sunjian on 2017/6/24.
 */

public class MyCardHandler implements CardHandler<MyBean> {

    @Override
    public View onBind(final Context context, final MyBean data, final int position, @CardViewPager.TransformerMode int mode) {
        View view = View.inflate(context, R.layout.item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.image);
        ElasticCardView cardView = (ElasticCardView) view.findViewById(R.id.cardview);
        final boolean isCard = mode == CardViewPager.MODE_CARD;
        cardView.setPreventCornerOverlap(isCard);
        cardView.setUseCompatPadding(isCard);
        final String img = data.getImg();
        Glide.with(context).load(img).into(imageView);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "data:" + data + "position:" + position, Toast.LENGTH_SHORT).show();
                TestActivity.start(context, img);
            }
        });
        return view;
    }
}
