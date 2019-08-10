/*
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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * @author sunjian
 * @date 2019-07-19 14:21
 */
public class CardSlideView<T> extends FrameLayout {

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL, VERTICAL})
    public @interface Orientation {
    }

    private static final float MAX_OFFSET_PERCENT = 1.f;
    private static final float MIN_OFFSET_PERCENT = 0f;

    /**
     * 除了中间固定的一个外，两侧的其中一侧偏移量百分比，因为是中心对称，只要考虑一边就行了
     * 默认0.4f，范围是0~1
     */
    private float mSideOffsetPercent;
    /**
     * 是否无限循环
     * 默认非无限循环
     */
    private boolean mIsLoop;
    /**
     * item的宽高比，高:宽
     */
    private float mItemRate;
    private InnerRecyclerView mCardListView;
    private GalleryLayoutManager mLayoutManager;
    private CardAdapter<T> mHorizontalAdapter;
    private CardAdapter<T> mVerticalAdapter;

    public CardSlideView(@NonNull Context context) {
        super(context);
        initView(context, null);
    }

    public CardSlideView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public CardSlideView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardSlideView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getOrientation() == HORIZONTAL) {
            final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
            if (widthMode != MeasureSpec.EXACTLY) {
                throw new RuntimeException("水平方向，宽度必须固定，可以设置MATCH_PARENT");
            }
        }

        if (getOrientation() == VERTICAL) {
            final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
            if (heightMode != MeasureSpec.EXACTLY) {
                throw new RuntimeException("垂直方向，高度必须固定，可以设置MATCH_PARENT");
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        int orientation = LinearLayout.HORIZONTAL;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardSlideView);
            mSideOffsetPercent = ta.getFloat(R.styleable.CardSlideView_card_side_offset_percent, 0.25f);
            mIsLoop = ta.getBoolean(R.styleable.CardSlideView_card_loop, mIsLoop);
            mItemRate = ta.getFloat(R.styleable.CardSlideView_card_item_rate, 1.3f);
            orientation = ta.getInt(R.styleable.CardSlideView_android_orientation, orientation);
            ta.recycle();
        }
        if (mSideOffsetPercent < MIN_OFFSET_PERCENT) {
            mSideOffsetPercent = MIN_OFFSET_PERCENT;
        }
        if (mSideOffsetPercent > MAX_OFFSET_PERCENT) {
            mSideOffsetPercent = MAX_OFFSET_PERCENT;
        }
        mCardListView = new InnerRecyclerView(context);
        mCardListView.setHasFixedSize(true);
        mCardListView.setNestedScrollingEnabled(false);
        mCardListView.setOverScrollMode(OVER_SCROLL_NEVER);
        addView(mCardListView);
        mLayoutManager = new GalleryLayoutManager(orientation, mIsLoop);
        mLayoutManager.setItemTransformer(new ScaleTransformer());
        mLayoutManager.attachToRecyclerView(mCardListView);

    }

    public void setSnapHelper(SnapHelper snapHelper) {
        mLayoutManager.setSnapHelper(snapHelper);
    }

    public void bind(List<T> data, @NonNull CardHolder<T> holder) {
        final int orientation = getOrientation();
        if (orientation == LinearLayout.HORIZONTAL) {
            if (mHorizontalAdapter == null) {
                mHorizontalAdapter = new CardAdapter<>(data, holder, mSideOffsetPercent, orientation, mItemRate);
                mCardListView.setAdapter(mHorizontalAdapter);
                return;
            }
            mHorizontalAdapter.notifyChanged(data);
            return;
        }
        if (mVerticalAdapter == null) {
            mVerticalAdapter = new CardAdapter<>(data, holder, mSideOffsetPercent, orientation, mItemRate);
            mCardListView.setAdapter(mVerticalAdapter);
            return;
        }
        mVerticalAdapter.notifyChanged(data);
    }

    public void setOrientation(@Orientation int orientation) {
        mLayoutManager.setOrientation(orientation);
    }

    public int getOrientation() {
        return mLayoutManager.getOrientation();
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        if (smoothScroll) {
            mCardListView.smoothScrollToPosition(item);
            return;
        }
        mCardListView.scrollToPosition(item);
    }

    public int getCurrentItem() {
        return mLayoutManager.getCurrentItem();
    }

    public void setLooper(boolean isLooper) {
        mLayoutManager.setLooper(isLooper);
    }

    public void setItemTransformer(PageTransformer pageTransformer) {
        mLayoutManager.setItemTransformer(pageTransformer);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mLayoutManager.setOnPageChangeListener(onPageChangeListener);
    }

    static class CardAdapter<T> extends RecyclerView.Adapter<CardViewHolder> {

        private List<T> mData;
        @NonNull
        private CardHolder<T> mHolder;
        private float mSideOffsetPercent;
        private int mOrientation;
        private float mItemRate;

        CardAdapter(List<T> data, @NonNull CardHolder<T> holder, float sideOffsetPercent, int orientation, float itemRate) {
            mData = data;
            mHolder = holder;
            mSideOffsetPercent = sideOffsetPercent;
            mOrientation = orientation;
            mItemRate = itemRate;
        }

        void notifyChanged(List<T> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final Context context = parent.getContext();
            final View view = mHolder.onCreateView(LayoutInflater.from(context), parent);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            if (mOrientation == LinearLayout.HORIZONTAL) {
                params.width = Math.round(parent.getMeasuredWidth() / (mSideOffsetPercent * 2 + 1));
                params.height = Math.round(params.width * mItemRate);
            } else {
                params.height = Math.round(parent.getMeasuredHeight() / (mSideOffsetPercent * 2 + 1));
                params.width = Math.round(params.height / mItemRate);
            }
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
            mHolder.onBindView(holder, mData.get(position), position);
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }
    }

    private class InnerRecyclerView extends RecyclerView {

        /**
         * 水平可以顺畅滑动的范围-45~45和135~225
         */
        public static final double RANGE_VALUE_ABS = Math.PI / 4D;

        private float downX;
        private float downY;

        private InnerRecyclerView(Context context) {
            super(context);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            final int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = ev.getX();
                    float moveY = ev.getY();
                    double atan = Math.atan(Math.abs(moveY - downY) / Math.abs(moveX - downX));
                    if (atan >= 0 && atan <= RANGE_VALUE_ABS) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                default:
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }
    }
}
