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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * description
 * <p>封装卡片ViewPager
 * Created by sunjian on 2017/6/22.
 */
public class CardViewPager extends ViewPager {

    static final int CACHE_COUNT = 8;

    public static final int MODE_CARD = 0;
    public static final int MODE_NORMAL = 1;

    private static final int MARGIN_MIN = -60;
    private static final int MARGIN_MAX = 60;
    private static final int PADDING_MIN = 0;
    private static final int PADDING_MAX = 100;

    @IntDef({MODE_CARD, MODE_NORMAL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TransformerMode {
    }

    private int mMaxOffset;
    private float mScaleRate;
    private boolean mIsLoop = false;
    private int mCardPaddingLeft;
    private int mCardPaddingTop;
    private int mCardPaddingRight;
    private int mCardPaddingBottom;
    private CardTransformer mTransformer;
    @TransformerMode
    private int mCurrentMode = MODE_CARD;

    boolean isNotify;
    private int size;

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
        mCardPaddingLeft = getPaddingLeft();
        mCardPaddingTop = getPaddingTop();
        mCardPaddingRight = getPaddingRight();
        mCardPaddingBottom = getPaddingBottom();
        final int paddingMin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_MIN, displayMetrics);
        final int paddingMax = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING_MAX, displayMetrics);
        if (padding < paddingMin) {
            padding = paddingMin;
        }
        if (padding > paddingMax) {
            padding = paddingMax;
        }
        setPadding(mCardPaddingLeft + padding, mCardPaddingTop, mCardPaddingRight + padding, mCardPaddingBottom);

        int margin = typedArray
                .getDimensionPixelOffset(R.styleable.CardViewPager_card_margin,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics));
        final int marginMin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_MIN, displayMetrics);
        final int marginMax = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MARGIN_MAX, displayMetrics);
        if (margin < marginMin) {
            margin = marginMin;
        }
        if (margin > marginMax) {
            margin = marginMax;
        }
        setPageMargin(margin);

        mMaxOffset = typedArray
                .getDimensionPixelOffset(R.styleable.CardViewPager_card_max_offset,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, displayMetrics));

        mIsLoop = typedArray.getBoolean(R.styleable.CardViewPager_card_loop, mIsLoop);

        mScaleRate = typedArray.getFloat(R.styleable.CardViewPager_card_scale_rate, 0.38f);

        typedArray.recycle();
        setOffscreenPageLimit(3);
    }

    @Override
    public void setOffscreenPageLimit(int limit) {
        if (limit < 3) {
            limit = 3;
        }
        super.setOffscreenPageLimit(limit);
    }

    /**
     * 请在设置adapter之前调用，即在bind方法之前调用
     *
     * @param maxOffset 移动偏移量
     * @param scaleRate 缩放比例
     */
    public void setCardTransformer(float maxOffset, float scaleRate) {
        int cardMaxOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxOffset, getResources().getDisplayMetrics());
        mTransformer = new CardTransformer(cardMaxOffset, scaleRate);
        setPageTransformer(false, mTransformer);
    }

    /**
     * 设置卡片左右padding
     *
     * @param padding 值，自动转dp
     */
    public void setCardPadding(float padding) {
        if (padding < PADDING_MIN) {
            padding = PADDING_MIN;
        }
        if (padding > PADDING_MAX) {
            padding = PADDING_MAX;
        }
        int cardPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, padding, getResources().getDisplayMetrics());
        setPadding(mCardPaddingLeft + cardPadding, mCardPaddingTop, mCardPaddingRight + cardPadding, mCardPaddingBottom);
    }

    /**
     * 设置卡片margin
     *
     * @param margin 值，自动转dp
     */
    public void setCardMargin(float margin) {
        if (margin < MARGIN_MIN) {
            margin = MARGIN_MIN;
        }
        if (margin > MARGIN_MAX) {
            margin = MARGIN_MAX;
        }
        int cardMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, margin, getResources().getDisplayMetrics());
        setPageMargin(cardMargin);
    }

    /**
     * 根据模式刷新通知UI刷新
     *
     * @param mode 模式
     */
    public void notifyUI(@TransformerMode int mode) {
        final CardPagerAdapter adapter = (CardPagerAdapter) getAdapter();
        if (adapter == null) {
            throw new NullPointerException("adapter is null");
        }
        mCurrentMode = mode;
        isNotify = true;
        adapter.setCardMode(mCurrentMode);
        setAdapter(adapter);
        isNotify = false;
    }

    boolean isCardMode() {
        return mCurrentMode == MODE_CARD;
    }

    public int getCurrentMode() {
        return mCurrentMode;
    }

    /**
     * 绑定数据源
     *
     * @param fm      FragmentManager
     * @param handler 数据处理类
     * @param data    数据源
     * @param <T>     泛型，必须实现Serializable
     */
    public <T extends Serializable> void bind(FragmentManager fm, CardHandler<T> handler, List<T> data) {
        List<CardItem> cardItems;
        if (data == null || data.isEmpty()) {
            cardItems = new ArrayList<>();
        } else {
            cardItems = getCardItems(handler, data, mIsLoop);
        }
        if (mTransformer == null) {
            mTransformer = new CardTransformer(mMaxOffset, mScaleRate);
            setPageTransformer(false, mTransformer);
        }
        CardPagerAdapter adapter = new CardPagerAdapter(fm, cardItems, mIsLoop);
        setAdapter(adapter);
    }

    @NonNull
    private <T extends Serializable> List<CardItem> getCardItems(CardHandler<T> handler, List<T> data, boolean isLoop) {
        List<CardItem> cardItems = new ArrayList<>();
        final int dataSize = data.size();
        size = dataSize;
        boolean isExpand = isLoop && dataSize < CACHE_COUNT;
        int radio = CACHE_COUNT / dataSize < 2 ? 2 : (int) Math.ceil(CACHE_COUNT * 1.0d / dataSize);
        int size = isExpand ? dataSize * radio : dataSize;
        for (int i = 0; i < size; i++) {
            int position = isExpand ? i % dataSize : i;
            T t = data.get(position);
            CardItem<T> item = new CardItem<>();
            item.bindHandler(handler);
            item.bindData(t, position);
            cardItems.add(item);
        }
        return cardItems;
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        if (!(adapter instanceof CardPagerAdapter)) {
            throw new RuntimeException("please set CardPagerAdapter!");
        }
        super.setAdapter(adapter);
    }

    @Deprecated
    @Override
    public void setOnPageChangeListener(@NonNull final OnPageChangeListener listener) {
        super.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                listener.onPageScrolled(position % size, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                listener.onPageSelected(position % size);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                listener.onPageScrollStateChanged(state);
            }
        });
    }

    @Override
    public void addOnPageChangeListener(@NonNull final OnPageChangeListener listener) {
        super.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                listener.onPageScrolled(position % size, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                listener.onPageSelected(position % size);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                listener.onPageScrollStateChanged(state);
            }
        });
    }

    public int getCurrentIndex() {
        final int currentItem = super.getCurrentItem();
        return currentItem % size;
    }

    public void setCurrentIndex(int index) {
        if (!mIsLoop) {
            super.setCurrentItem(index);
            return;
        }
        final CardPagerAdapter adapter = (CardPagerAdapter) getAdapter();
        if (adapter == null) {
            throw new NullPointerException("adapter is null");
        }
        super.setCurrentItem(adapter.getLastItem(index));
    }

    public void setCurrentIndex(int index, boolean smoothScroll) {
        if (!mIsLoop) {
            // smoothScroll为false的时候滑动间隔过多条目会显示异常，暂时先特殊处理，下同
            if (smoothScroll) {
                super.setCurrentItem(index, true);
            } else {
                super.setCurrentItem(getPreIndex(index), false);
                super.setCurrentItem(index);
            }
            return;
        }
        final CardPagerAdapter adapter = (CardPagerAdapter) getAdapter();
        if (adapter == null) {
            throw new NullPointerException("adapter is null");
        }
        if (smoothScroll) {
            super.setCurrentItem(adapter.getLastItem(index), true);
        } else {
            super.setCurrentItem(adapter.getLastItem(getPreIndex(index)), false);
            super.setCurrentItem(adapter.getLastItem(index));
        }
    }

    private int getPreIndex(int index) {
        return index - 1 < 0 ? index + 1 : index - 1;
    }

    /**
     * 请使用{@link #setCurrentIndex(int)}替代
     */
    @Deprecated
    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    /**
     * 请使用{@link #setCurrentIndex(int, boolean)}替代
     */
    @Deprecated
    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }
}
