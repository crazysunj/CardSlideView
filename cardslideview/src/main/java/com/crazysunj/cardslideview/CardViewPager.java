/**
 * Copyright 2017 Sun Jian
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.crazysunj.cardslideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * description
 * <p>封装卡片ViewPager
 * Created by sunjian on 2017/6/22.
 */
public class CardViewPager extends ViewPager {

    private int mMaxOffset;
    private float mScaleRate;
    private boolean mIsLoop = false;
    private CardTransformer mTransformer;

    public CardViewPager(Context context) {
        this(context, null);
    }

    public CardViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        setClipToPadding(false);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CardViewPager);
        int padding = typedArray
                .getDimensionPixelOffset(R.styleable.CardViewPager_card_padding,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, displayMetrics));
        setPadding(getPaddingLeft() + padding, getPaddingTop(), getPaddingRight() + padding, getPaddingBottom());

        int margin = typedArray
                .getDimensionPixelOffset(R.styleable.CardViewPager_card_margin,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics));
        setPageMargin(margin);

        mMaxOffset = typedArray
                .getDimensionPixelOffset(R.styleable.CardViewPager_card_max_offset,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, displayMetrics));

        mIsLoop = typedArray.getBoolean(R.styleable.CardViewPager_card_loop, mIsLoop);

        mScaleRate = typedArray.getFloat(R.styleable.CardViewPager_card_scale_rate, 0.38f);

        typedArray.recycle();
    }

    /**
     * 请在设置adapter之前调用，即在bind方法之前调用
     *
     * @param maxOffset 移动偏移量
     * @param scaleRate 缩放比例
     */
    public void setCardTransformer(int maxOffset, float scaleRate) {
        mTransformer = new CardTransformer(maxOffset, scaleRate);
        setPageTransformer(false, mTransformer);
    }


    public <T extends Serializable> void bind(FragmentManager fm, CardHandler<T> handler, List<T> data) {
        List<CardItem> cardItems = getCardItems(handler, data, mIsLoop);
        if (mTransformer == null) {
            mTransformer = new CardTransformer(mMaxOffset, mScaleRate);
            setPageTransformer(false, mTransformer);
        }
        CardPagerAdapter adapter = new CardPagerAdapter(fm, cardItems, mIsLoop);
        setAdapter(adapter);
    }

    @NonNull
    private <T extends Serializable> List<CardItem> getCardItems(CardHandler<T> handler, List<T> data, boolean isLoop) {
        List<CardItem> cardItems = new ArrayList<CardItem>();
        int dataSize = data.size();
        int cacheCount = 6;
        boolean isExpand = isLoop && dataSize < cacheCount;
        int size = isExpand ? cacheCount : dataSize;
        for (int i = 0; i < size; i++) {
            int position = isExpand ? i % dataSize : i;
            T t = data.get(position);
            CardItem<T> item = new CardItem<T>();
            item.bindHandler(handler);
            item.bindData(t, position);
            cardItems.add(item);
        }
        return cardItems;
    }
}
