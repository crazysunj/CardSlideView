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
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * 建议用ViewPager2自行封装，该控件更系统，官方长期维护
 * 不过咱也算良心制作了，理论上目前是比ViewPager2功能更强大
 * 目前支持
 * 1. ItemDecoration
 * 2. 可设item之间间距，可做到叠加
 * 3. 可设各个view自身的padding和margin
 * 4. 横竖两个方向
 * 5. 无限循环
 * 6. page和linear两种滑动方式
 * 7. 百分比适配，但只会根据宽高其中一个维度适配
 * 8. 非无循环模式边界支持回弹，可动态设置是否开启
 * 注意：目前不支持宽高自适应，每页活动范围就是根据宽高比计算的范围
 *
 * @author sunjian
 * @date 2019-07-19 14:21
 */
public class CardSlideView<T> extends FrameLayout {

    public static final int MODE_LINEAR = 0;
    public static final int MODE_PAGE = 1;

    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;
    public static final int VERTICAL = LinearLayout.VERTICAL;
    private OnPageItemClickListener<T> mOnPageItemClickListener;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({HORIZONTAL, VERTICAL})
    public @interface Orientation {
    }

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
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0, height = 0;
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = Math.round(Math.round((width - getPaddingLeft() - getPaddingRight()) / (mSideOffsetPercent * 2 + 1)) * mItemRate) + getPaddingTop() + getPaddingBottom();
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
            if (width == 0) {
                width = Math.round(Math.round((height - getPaddingTop() - getPaddingBottom()) / (mSideOffsetPercent * 2 + 1)) / mItemRate) + getPaddingLeft() + getPaddingRight();
            }
        }
        if (width == 0 || height == 0) {
            throw new RuntimeException("宽高必须固定其一，如果想以宽为适配那就固定宽，默认固定宽，反之，想以高为适配就固定高");
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    private void initView(Context context, @Nullable AttributeSet attrs) {
        final int orientation;
        final int cardMode;
        // item之间间距，可设负值（叠加），百分比是相对自身宽度或高度
        final float itemMarginPercent;
        final boolean isRebound;
        // 预加载数量，默认0
        final int pageLimit;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CardSlideView);
            mSideOffsetPercent = ta.getFloat(R.styleable.CardSlideView_card_side_offset_percent, 0.25f);
            mIsLoop = ta.getBoolean(R.styleable.CardSlideView_card_loop, false);
            isRebound = ta.getBoolean(R.styleable.CardSlideView_card_rebound, true);
            mItemRate = ta.getFloat(R.styleable.CardSlideView_card_item_rate, 1.3f);
            itemMarginPercent = Math.min(1, Math.max(-1, ta.getFloat(R.styleable.CardSlideView_card_item_margin_percent, 0f)));
            orientation = ta.getInt(R.styleable.CardSlideView_android_orientation, LinearLayout.HORIZONTAL);
            cardMode = ta.getInt(R.styleable.CardSlideView_card_page_mode, MODE_PAGE);
            pageLimit = ta.getInteger(R.styleable.CardSlideView_card_page_limit, 0);
            ta.recycle();
        } else {
            mSideOffsetPercent = 0.25f;
            mIsLoop = false;
            isRebound = true;
            mItemRate = 1.3f;
            itemMarginPercent = 0;
            orientation = LinearLayout.HORIZONTAL;
            cardMode = MODE_PAGE;
            pageLimit = 0;
        }
        mSideOffsetPercent = Math.min(1, Math.max(0, mSideOffsetPercent));
        mCardListView = new InnerRecyclerView(context);
        mCardListView.setMode(cardMode);
        mCardListView.setHasFixedSize(true);
        mCardListView.setNestedScrollingEnabled(false);
        mCardListView.setOverScrollMode(OVER_SCROLL_NEVER);
        mCardListView.setItemMarginPercent(itemMarginPercent);
        addView(mCardListView);
        mLayoutManager = new GalleryLayoutManager(orientation, mIsLoop, isRebound, itemMarginPercent);
        mLayoutManager.setOffscreenPageLimit(pageLimit);
        mLayoutManager.setItemTransformer(new DefaultTransformer());
        mLayoutManager.attachToRecyclerView(mCardListView);
    }

    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor, int index) {
        mCardListView.addItemDecoration(decor, index);
    }

    public void addItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mCardListView.addItemDecoration(decor);
    }

    @NonNull
    public RecyclerView.ItemDecoration getItemDecorationAt(int index) {
        return mCardListView.getItemDecorationAt(index);
    }

    public int getItemDecorationCount() {
        return mCardListView.getItemDecorationCount();
    }

    public void removeItemDecorationAt(int index) {
        mCardListView.removeItemDecorationAt(index);
    }

    public void removeItemDecoration(@NonNull RecyclerView.ItemDecoration decor) {
        mCardListView.removeItemDecoration(decor);
    }

    /**
     * 设置偏移百分比，必须在{@link #bind(List, CardHolder, boolean)}之前调用
     */
    public void setSideOffsetPercent(float sideOffsetPercent) {
        mSideOffsetPercent = sideOffsetPercent;
    }

    /**
     * 设置宽高比，必须在{@link #bind(List, CardHolder, boolean)}之前调用
     */
    public void setItemRate(float itemRate) {
        mItemRate = itemRate;
    }

    public void bind(List<T> data, @NonNull CardHolder<T> holder) {
        bind(data, holder, false);
    }

    public void bind(List<T> data, @NonNull CardHolder<T> holder, boolean isResetHolder) {
        final int orientation = getOrientation();
        if (orientation == LinearLayout.HORIZONTAL) {
            if (mHorizontalAdapter == null || isResetHolder) {
                mHorizontalAdapter = new CardAdapter<>(data, holder, mSideOffsetPercent, orientation, mItemRate, itemClickListener);
                mCardListView.setAdapter(mHorizontalAdapter);
                return;
            }
            mHorizontalAdapter.notifyChanged(data);
            return;
        }
        if (mVerticalAdapter == null || isResetHolder) {
            mVerticalAdapter = new CardAdapter<>(data, holder, mSideOffsetPercent, orientation, mItemRate, itemClickListener);
            mCardListView.setAdapter(mVerticalAdapter);
            return;
        }
        mVerticalAdapter.notifyChanged(data);
    }

    public void setOrientation(@Orientation int orientation) {
        mCardListView.setOrientation(orientation);
        mLayoutManager.setOrientation(orientation);
    }

    public int getOrientation() {
        return mLayoutManager.getOrientation();
    }

    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        final int itemCount = getItemCount();
        if (itemCount <= 0) {
            return;
        }
        final int currentItem = getCurrentItem();
        if (item == currentItem) {
            return;
        }
        if (mIsLoop) {
            // 非循环模式下不校正item
            if (item < 0) {
                item = (item + itemCount) % itemCount;
            }
            if (item >= itemCount) {
                item = item % itemCount;
            }
        }
        if (item < 0 || item >= itemCount) {
            return;
        }
        if (smoothScroll) {
            mCardListView.smoothScrollToPosition(item);
            return;
        }
        mCardListView.scrollToPosition(item);
    }

    public int getPosition(@NonNull View view) {
        return mLayoutManager.getPosition(view);
    }

    public List<T> getData() {
        final int orientation = getOrientation();
        if (orientation == LinearLayout.HORIZONTAL) {
            return mHorizontalAdapter == null ? null : mHorizontalAdapter.getData();
        }
        return mVerticalAdapter == null ? null : mVerticalAdapter.getData();
    }

    public int getItemCount() {
        return mLayoutManager.getItemCount();
    }

    public int getCurrentItem() {
        return mLayoutManager.getCurrentItem();
    }

    public View getCenterView() {
        return mLayoutManager.findCenterView();
    }

    public void setLooper(boolean isLooper) {
        mLayoutManager.setLooper(isLooper);
    }

    public void setCanScrollHorizontally(boolean canScrollHorizontally) {
        mLayoutManager.setCanScrollHorizontally(canScrollHorizontally);
    }

    public void setCanScrollVertically(boolean canScrollVertically) {
        mLayoutManager.setCanScrollVertically(canScrollVertically);
    }

    public void setItemTransformer(PageTransformer pageTransformer) {
        mLayoutManager.setItemTransformer(pageTransformer);
    }

    public void setOnPageScrollStateChangeListener(OnPageScrollStateChangeListener listener) {
        mLayoutManager.setOnPageScrollStateChangeListener(listener);
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mLayoutManager.setOnPageChangeListener(listener);
    }

    public void setOnPageItemClickListener(OnPageItemClickListener<T> listener) {
        mOnPageItemClickListener = listener;
    }

    private OnPageItemClickListener<T> itemClickListener = new OnPageItemClickListener<T>() {
        @Override
        public void onItemClick(View view, T data, int position) {
            if (getCenterView() != view) {
                setCurrentItem(position);
                return;
            }
            if (mOnPageItemClickListener != null) {
                mOnPageItemClickListener.onItemClick(view, data, position);
            }
        }
    };

    static class CardAdapter<T> extends RecyclerView.Adapter<CardViewHolder> {

        private List<T> mData;
        @NonNull
        private CardHolder<T> mHolder;
        private float mSideOffsetPercent;
        private int mOrientation;
        private float mItemRate;
        private OnPageItemClickListener<T> mItemClickListener;

        CardAdapter(List<T> data, @NonNull CardHolder<T> holder, float sideOffsetPercent, int orientation, float itemRate, OnPageItemClickListener<T> itemClickListener) {
            mData = data;
            mHolder = holder;
            mSideOffsetPercent = sideOffsetPercent;
            mOrientation = orientation;
            mItemRate = itemRate;
            mItemClickListener = itemClickListener;
        }

        void notifyChanged(List<T> data) {
            mData = data;
            notifyDataSetChanged();
        }

        List<T> getData() {
            return mData;
        }

        @NonNull
        @Override
        public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            RecyclerView recyclerView = (RecyclerView) parent;
            final Context context = parent.getContext();
            final View view = mHolder.onCreateView(LayoutInflater.from(context), parent);
            final int insertsLeft, insertsTop, insertsRight, insertsBottom;
            if (recyclerView.getItemDecorationCount() > 0) {
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager == null) {
                    insertsLeft = insertsTop = insertsRight = insertsBottom = 0;
                } else {
                    Rect outRect = new Rect();
                    manager.calculateItemDecorationsForChild(view, outRect);
                    insertsLeft = outRect.left;
                    insertsTop = outRect.top;
                    insertsRight = outRect.right;
                    insertsBottom = outRect.bottom;
                }
            } else {
                insertsLeft = insertsTop = insertsRight = insertsBottom = 0;
            }
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            // 由于是内部控制RecyclerView，所以可以忽略其padding和margin
            if (mOrientation == LinearLayout.HORIZONTAL) {
                final int width = Math.round((parent.getMeasuredWidth()) / (mSideOffsetPercent * 2 + 1));
                params.width = width - params.leftMargin - params.rightMargin - insertsLeft - insertsRight;
                params.height = Math.round(width * mItemRate) - params.topMargin - params.bottomMargin - insertsTop - insertsBottom;
            } else {
                final int height = Math.round(parent.getMeasuredHeight() / (mSideOffsetPercent * 2 + 1));
                params.height = height - params.topMargin - params.bottomMargin - insertsTop - insertsBottom;
                params.width = Math.round(height / mItemRate) - params.leftMargin - params.rightMargin - insertsLeft - insertsRight;
            }
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CardViewHolder holder, final int position) {
            final T data = mData.get(position);
            mHolder.onBindView(holder, data, position);
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(view, data, position);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }
    }

    private static class InnerRecyclerView extends RecyclerView {

        private float downX;
        private float downY;
        private int mode;

        private float itemMarginPercent;
        private Scroller scroller;
        private float countRate;
        private int touchSlop;
        private int orientation;

        private InnerRecyclerView(Context context) {
            super(context);
            mode = MODE_PAGE;
            orientation = LinearLayout.HORIZONTAL;
            touchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
            setChildrenDrawingOrderEnabled(true);
            scroller = new Scroller(context, new DecelerateInterpolator());
            // 最终还是决定自己写滚动算法 - -! 官方的各版本不一样，还不太好用
            setOnFlingListener(new OnFlingListener() {
                @Override
                public boolean onFling(int velocityX, int velocityY) {
                    final int minFlingVelocity = getMinFlingVelocity();
                    if (!(Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity)) {
                        return false;
                    }
                    LayoutManager layoutManager = getLayoutManager();
                    if (layoutManager instanceof GalleryLayoutManager) {
                        final GalleryLayoutManager galleryLayoutManager = (GalleryLayoutManager) layoutManager;
                        final View centerView = galleryLayoutManager.findCenterView();
                        if (centerView != null) {
                            final OrientationHelper helper = galleryLayoutManager.getOrientationHelper();
                            final int childSize = helper.getDecoratedMeasurement(centerView);
                            final int itemDist = childSize + (int) (childSize * itemMarginPercent);
                            final int orientation = galleryLayoutManager.getOrientation();
                            final int velocity = orientation == LinearLayout.HORIZONTAL ? velocityX : velocityY;
                            final int count = mode == MODE_PAGE ? 1 : (int) (getDist(velocityX, velocityY, orientation) / itemDist * countRate);
                            int dist = count * itemDist;
                            final int edge;
                            final int direction;
                            if (velocity > 0) {
                                direction = GalleryLayoutManager.LAYOUT_END;
                                edge = helper.getDecoratedEnd(centerView);
                            } else {
                                direction = GalleryLayoutManager.LAYOUT_START;
                                edge = helper.getDecoratedStart(centerView);
                            }
                            dist -= ((int) ((helper.getTotalSpace() + childSize * direction) / 2.f) - edge) * direction;
                            if (orientation == LinearLayout.HORIZONTAL) {
                                smoothScrollBy(dist * direction, 0);
                                return true;
                            }
                            smoothScrollBy(0, dist * direction);
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }

        private void setMode(int mode) {
            this.mode = mode;
            if (mode == MODE_PAGE) {
                touchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
                setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
                return;
            }
            touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_DEFAULT);
        }

        private void setItemMarginPercent(float itemMarginPercent) {
            this.itemMarginPercent = itemMarginPercent;
            countRate = itemMarginPercent < 0 ? 1 + Math.max(-0.9f, itemMarginPercent) : 1 + Math.min(1.f, itemMarginPercent);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            final int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float endX = ev.getX();
                    float endY = ev.getY();
                    float distanceX = Math.abs(endX - downX);
                    float distanceY = Math.abs(endY - downY);
                    // 仍然以45度为边界值
                    if (orientation == HORIZONTAL && distanceX > touchSlop && distanceX > distanceY) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    } else if (orientation == VERTICAL && distanceY > touchSlop && distanceY > distanceX) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    break;
                default:
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }

        @Override
        protected int getChildDrawingOrder(int childCount, int i) {
            if (itemMarginPercent < 0) {
                // 小于0的情况会出现叠加
                LayoutManager layoutManager = getLayoutManager();
                if (layoutManager instanceof GalleryLayoutManager) {
                    GalleryLayoutManager galleryLayoutManager = (GalleryLayoutManager) layoutManager;
                    View centerView = galleryLayoutManager.findCenterView();
                    int order;
                    int childDiff = i - indexOfChild(centerView);
                    if (childDiff < 0) {
                        order = i;
                    } else {
                        order = childCount - 1 - childDiff;
                    }
                    if (order < 0) {
                        order = 0;
                    } else if (order > childCount - 1) {
                        order = childCount - 1;
                    }
                    return order;
                }
            }
            return super.getChildDrawingOrder(childCount, i);
        }

        private double getDist(int velocityX, int velocityY, int orientation) {
            scroller.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if (orientation == LinearLayout.HORIZONTAL) {
                return Math.abs(scroller.getFinalX());
            }
            return Math.abs(scroller.getFinalY());
        }
    }
}
