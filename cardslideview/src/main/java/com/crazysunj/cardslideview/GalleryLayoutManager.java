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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 画廊布局
 *
 * @author sunjian
 * @date 2019-07-16 09:42
 */
class GalleryLayoutManager extends RecyclerView.LayoutManager {
    private static final String TAG = "GalleryLayoutManager";

    final static int LAYOUT_START = -1;
    final static int LAYOUT_END = 1;


    private int mFirstVisiblePosition = 0;
    private int mLastVisiblePosition = 0;
    private int mInitialSelectedPosition = 0;
    private int mCurItem = RecyclerView.NO_POSITION;

    /**
     * 用于滑动记录
     */
    private State mState;
    private InnerScrollListener mInnerScrollListener = new InnerScrollListener();

    /**
     * 滑动方向
     */
    private int mOrientation;
    private OrientationHelper mHorizontalHelper;
    private OrientationHelper mVerticalHelper;

    private OnPageChangeListener mOnPageChangeListener;
    private PageTransformer mPageTransformer;
    private RecyclerView mRecyclerView;

    private boolean isRebound;
    private boolean isLooper;
    private boolean canScrollHorizontally;
    private boolean canScrollVertically;
    private float itemMarginPercent;
    private int itemMargin;
    private OnPageScrollStateChangeListener mOnPageScrollStateChangeListener;

    GalleryLayoutManager(int orientation, boolean isLooper, boolean isRebound, float itemMarginPercent) {
        mOrientation = orientation;
        this.itemMarginPercent = itemMarginPercent;
        this.isLooper = isLooper;
        this.isRebound = isRebound;
        if (mOrientation == LinearLayout.HORIZONTAL) {
            canScrollHorizontally = true;
            canScrollVertically = false;
        } else {
            canScrollHorizontally = false;
            canScrollVertically = true;
        }
    }

    @Nullable
    View findCenterView() {
        final OrientationHelper helper = getOrientationHelper();
        int childCount = getChildCount();
        if (childCount == 0) {
            return null;
        }
        View closestChild = null;
        final int center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        int absClosest = Integer.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            int childCenter = helper.getDecoratedStart(child)
                    + (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);
            if (absDistance < absClosest) {
                absClosest = absDistance;
                closestChild = child;
            }
        }
        return closestChild;
    }

    /**
     * 绑定RecyclerView
     *
     * @param recyclerView RecyclerView
     */
    void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        recyclerView.setLayoutManager(this);
        recyclerView.removeOnScrollListener(mInnerScrollListener);
        recyclerView.addOnScrollListener(mInnerScrollListener);
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        if (mOrientation == LinearLayout.VERTICAL) {
            return new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            return new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            reset();
            removeAndRecycleAllViews(recycler);
            return;
        }
        if (state.isPreLayout()) {
            // 预布局
            return;
        }
        if (state.getItemCount() != 0 && !state.didStructureChange()) {
            // 数据集未改变且不需要重新布局时直接return
            if (!getState().layoutChanged) {
                return;
            }
        }
        reset();
        detachAndScrapAttachedViews(recycler);
        firstFill(recycler);
        getState().layoutChanged = false;
    }

    private void reset() {
        if (mCurItem != RecyclerView.NO_POSITION) {
            mInitialSelectedPosition = mCurItem;
        }
        mInitialSelectedPosition = Math.min(Math.max(0, mInitialSelectedPosition), getItemCount() - 1);
        mFirstVisiblePosition = mInitialSelectedPosition;
        mLastVisiblePosition = mInitialSelectedPosition;
        mCurItem = RecyclerView.NO_POSITION;
    }

    private void firstFill(RecyclerView.Recycler recycler) {
        if (mOrientation == LinearLayout.HORIZONTAL) {
            firstFillWithHorizontal(recycler);
        } else {
            firstFillWithVertical(recycler);
        }
        if (mPageTransformer != null) {
            View child;
            for (int i = 0, size = getChildCount(); i < size; i++) {
                child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                mPageTransformer.transformPage(child, calculateOffsetPercentToCenter(child, 0), mOrientation);
            }
        }
        mInnerScrollListener.onScrolled(mRecyclerView, 0, 0);
    }

    /**
     * 水平首次布局测量
     *
     * @param recycler RecyclerView.Recycler
     */
    private void firstFillWithHorizontal(RecyclerView.Recycler recycler) {
        final OrientationHelper helper = getOrientationHelper();
        final int leftEdge = helper.getStartAfterPadding();
        final int rightEdge = helper.getEndAfterPadding();
        final int centerPosition = mInitialSelectedPosition;
        final int width = getHorizontalSpace();
        final int height = getVerticalSpace();
        // 测量初始化位置view
        final View centerView = recycler.getViewForPosition(centerPosition);
        addView(centerView, 0);
        // 开始测量
        measureChildWithMargins(centerView, 0, 0);
        // 获取宽高，包含margin和ItemDecoration
        final int centerWidth = helper.getDecoratedMeasurement(centerView);
        final int centerHeight = helper.getDecoratedMeasurementInOther(centerView);
        final int left = (int) (getPaddingLeft() + (width - centerWidth) / 2.f);
        final int top = (int) (getPaddingTop() + (height - centerHeight) / 2.f);
        final int right = left + centerWidth;
        final int bottom = top + centerHeight;
        // 开始布局
        layoutDecoratedWithMargins(centerView, left, top, right, bottom);
        mFirstVisiblePosition = mLastVisiblePosition = centerPosition;
        itemMargin = (int) (itemMarginPercent * centerWidth);
        // 布局测量中心左边item
        firstFillLeft(recycler, centerPosition - 1, left, leftEdge);
        // 布局测量中心右边item
        firstFillRight(recycler, centerPosition + 1, right, rightEdge);
    }

    /**
     * 布局测量中心左边item
     *
     * @param recycler RecyclerView.Recycler
     * @param position 位置
     * @param itemLeft view相对父布局左边距离
     * @param leftEdge 总布局左边界，避免全部测量布局浪费资源，出父布局外就不用管了
     */
    private void firstFillLeft(RecyclerView.Recycler recycler, int position, int itemLeft, int leftEdge) {
        final OrientationHelper helper = getOrientationHelper();
        View itemView;
        int left, top, right, bottom;
        int itemWidth, itemHeight;
        final int height = getVerticalSpace();
        int itemRight = itemLeft - itemMargin;
        // 这里不考虑item的margin和ItemDecoration，否则会出现bind而实际并没有add的情况
        while (itemRight > leftEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView, 0);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurement(itemView);
            itemHeight = helper.getDecoratedMeasurementInOther(itemView);
            right = itemRight;
            left = right - itemWidth;
            top = (int) (getPaddingTop() + (height - itemHeight) / 2.f);
            bottom = top + itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemRight = left - itemMargin;
            mFirstVisiblePosition = position;
            position--;
        }
    }

    /**
     * 布局测量中心右边item
     *
     * @param recycler  RecyclerView.Recycler
     * @param position  位置
     * @param itemRight view相对父布局右边距离
     * @param rightEdge 总布局右边界
     */
    private void firstFillRight(RecyclerView.Recycler recycler, int position, int itemRight, int rightEdge) {
        final int size = getItemCount();
        final OrientationHelper helper = getOrientationHelper();
        View itemView;
        int left, top, right, bottom;
        int itemWidth, itemHeight;
        final int height = getVerticalSpace();
        int itemLeft = itemRight + itemMargin;
        while (itemLeft < rightEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurement(itemView);
            itemHeight = helper.getDecoratedMeasurementInOther(itemView);
            left = itemLeft;
            top = (int) (getPaddingTop() + (height - itemHeight) / 2.f);
            right = left + itemWidth;
            bottom = top + itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemLeft = right + itemMargin;
            mLastVisiblePosition = position;
            position++;
        }
    }

    /**
     * 垂直首次布局测量
     *
     * @param recycler RecyclerView.Recycler
     */
    private void firstFillWithVertical(RecyclerView.Recycler recycler) {
        final OrientationHelper helper = getOrientationHelper();
        final int topEdge = helper.getStartAfterPadding();
        final int bottomEdge = helper.getEndAfterPadding();
        final int centerPosition = mInitialSelectedPosition;
        final int width = getHorizontalSpace();
        final int height = getVerticalSpace();
        final View centerView = recycler.getViewForPosition(centerPosition);
        addView(centerView, 0);
        measureChildWithMargins(centerView, 0, 0);
        final int itemWidth = helper.getDecoratedMeasurementInOther(centerView);
        final int itemHeight = helper.getDecoratedMeasurement(centerView);
        final int left = (int) (getPaddingLeft() + (width - itemWidth) / 2.f);
        final int top = (int) (getPaddingTop() + (height - itemHeight) / 2.f);
        final int right = left + itemWidth;
        final int bottom = top + itemHeight;
        layoutDecoratedWithMargins(centerView, left, top, right, bottom);
        mFirstVisiblePosition = mLastVisiblePosition = centerPosition;
        itemMargin = (int) (itemMarginPercent * itemHeight);
        firstFillTop(recycler, centerPosition - 1, top, topEdge);
        firstFillBottom(recycler, centerPosition + 1, bottom, bottomEdge);
    }

    /**
     * 布局测量中心上边item
     *
     * @param recycler RecyclerView.Recycler recycler
     * @param position 位置
     * @param itemTop  view相对父布局上边距离
     * @param topEdge  总布局上边界
     */
    private void firstFillTop(RecyclerView.Recycler recycler, int position, int itemTop, int topEdge) {
        final OrientationHelper helper = getOrientationHelper();
        View itemView;
        int left, top, right, bottom;
        int itemWidth, itemHeight;
        final int width = getHorizontalSpace();
        int itemBottom = itemTop - itemMargin;
        while (itemBottom > topEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView, 0);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurementInOther(itemView);
            itemHeight = helper.getDecoratedMeasurement(itemView);
            left = (int) (getPaddingLeft() + (width - itemWidth) / 2.f);
            bottom = itemBottom;
            top = itemBottom - itemHeight;
            right = left + itemWidth;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemBottom = top - itemMargin;
            mFirstVisiblePosition = position;
            position--;
        }
    }

    /**
     * 布局测量中心下边item
     *
     * @param recycler   RecyclerView.Recycler
     * @param position   位置
     * @param itemBottom view相对父布局下边距离
     * @param bottomEdge 总布局下边界
     */
    private void firstFillBottom(RecyclerView.Recycler recycler, int position, int itemBottom, int bottomEdge) {
        final OrientationHelper helper = getOrientationHelper();
        View itemView;
        int left, top, right, bottom;
        int itemWidth, itemHeight;
        final int width = getHorizontalSpace();
        final int size = getItemCount();
        int itemTop = itemBottom + itemMargin;
        while (itemTop < bottomEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurementInOther(itemView);
            itemHeight = helper.getDecoratedMeasurement(itemView);
            left = (int) (getPaddingLeft() + (width - itemWidth) / 2.f);
            top = itemTop;
            right = left + itemWidth;
            bottom = top + itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemTop = bottom + itemMargin;
            mLastVisiblePosition = position;
            position++;
        }
    }

    /**
     * @param child  计算的view
     * @param offset view的滑动偏移量
     * @return 计算距离中心轴偏移百分比
     */
    private float calculateOffsetPercentToCenter(View child, float offset) {
        final float distance = calculateDistanceToCenter(child, offset);
        final OrientationHelper helper = getOrientationHelper();
        final int size = helper.getDecoratedMeasurement(child) + itemMargin;
        return distance * 1.f / size;
    }

    /**
     * @param child  计算的view
     * @param offset view的滑动偏移量
     * @return 返回view距离中心轴的距离
     */
    private float calculateDistanceToCenter(View child, float offset) {
        final OrientationHelper helper = getOrientationHelper();
        final float centerToStart = helper.getTotalSpace() / 2.f + helper.getStartAfterPadding();
        return helper.getDecoratedMeasurement(child) / 2.f + helper.getDecoratedStart(child) - centerToStart - offset;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    private State getState() {
        if (mState == null) {
            mState = new State();
        }
        return mState;
    }

    @NonNull
    OrientationHelper getOrientationHelper() {
        if (mOrientation == LinearLayout.HORIZONTAL) {
            if (mHorizontalHelper == null) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(this);
            }
            return mHorizontalHelper;
        }
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(this);
        }
        return mVerticalHelper;
    }

    void setCanScrollHorizontally(boolean canScrollHorizontally) {
        this.canScrollHorizontally = canScrollHorizontally;
    }

    void setCanScrollVertically(boolean canScrollVertically) {
        this.canScrollVertically = canScrollVertically;
    }

    @Override
    public boolean canScrollHorizontally() {
        return canScrollHorizontally && mOrientation == LinearLayout.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return canScrollVertically && mOrientation == LinearLayout.VERTICAL;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mOrientation == LinearLayout.VERTICAL) {
            return 0;
        }
        if (getChildCount() == 0 || dx == 0) {
            return 0;
        }
        int offset = -dx;
        final OrientationHelper helper = getOrientationHelper();
        final int centerToStart = (helper.getEndAfterPadding() - helper.getStartAfterPadding()) / 2 + helper.getStartAfterPadding();
        View child;
        if (dx > 0) {
            child = getChildAt(getChildCount() - 1);
            if (child != null && mLastVisiblePosition == getItemCount() - 1 && !isLooper) {
                // 计算全部加载完后item的偏移量，右边会留出空隙，回弹距离为滑动边的三分之一，暂时不支持回弹距离设置
                final int diff = helper.getDecoratedMeasurement(child) / 2 + helper.getDecoratedStart(child) - centerToStart + (isRebound ? helper.getTotalSpace() / 3 : 0);
                offset = -Math.max(0, Math.min(dx, diff));
            }
        } else {
            child = getChildAt(0);
            if (mFirstVisiblePosition == 0 && child != null && !isLooper) {
                // 计算首次加载item的偏移量，左边会留出空隙，回弹距离为滑动边的三分之一，暂时不支持回弹距离设置
                final int diff = helper.getDecoratedMeasurement(child) / 2 + helper.getDecoratedStart(child) - centerToStart - (isRebound ? helper.getTotalSpace() / 3 : 0);
                offset = -Math.min(0, Math.max(dx, diff));
            }
        }
        // 记录偏移量
        getState().scrollOffset = -offset;
        fill(recycler, -offset);
        offsetChildrenHorizontal(offset);
        return -offset;
    }

    private void fill(RecyclerView.Recycler recycler, int scrollOffset) {
        if (getItemCount() == 0) {
            return;
        }
        if (mOrientation == LinearLayout.HORIZONTAL) {
            fillWithHorizontal(recycler, scrollOffset);
        } else {
            fillWithVertical(recycler, scrollOffset);
        }
        if (mPageTransformer != null) {
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                if (child == null) {
                    continue;
                }
                mPageTransformer.transformPage(child, calculateOffsetPercentToCenter(child, scrollOffset), mOrientation);
            }
        }
    }

    /**
     * 水平填充布局测量
     *
     * @param recycler RecyclerView.Recycler
     * @param offset   水平偏移量
     */
    private void fillWithHorizontal(RecyclerView.Recycler recycler, int offset) {
        final OrientationHelper helper = getOrientationHelper();
        final int leftEdge = helper.getStartAfterPadding();
        final int rightEdge = helper.getEndAfterPadding();
        if (getChildCount() > 0) {
            if (offset >= 0) {
                removeAndRecyclerWithLeft(recycler, leftEdge + offset);
            } else {
                removeAndRecyclerWithRight(recycler, rightEdge + offset);
            }
        }
        if (offset >= 0) {
            // 右滑
            fillRight(recycler, rightEdge + offset);
        } else {
            // 左滑
            fillLeft(recycler, leftEdge + offset);
        }
    }

    private void fillLeft(RecyclerView.Recycler recycler, int leftEdge) {
        final OrientationHelper helper = getOrientationHelper();
        int position = mFirstVisiblePosition;
        int itemLeft = -1;
        View itemView;
        int itemWidth, itemHeight;
        final int height = getVerticalSpace();
        int left, top, right, bottom;
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            if (firstView != null) {
                position = getPosition(firstView) - 1;
                itemLeft = helper.getDecoratedStart(firstView);
            }
        }
        int itemRight = itemLeft - itemMargin;
        while (itemRight > leftEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView, 0);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurement(itemView);
            itemHeight = helper.getDecoratedMeasurementInOther(itemView);
            right = itemRight;
            left = right - itemWidth;
            top = (int) (getPaddingTop() + (height - itemHeight) / 2.f);
            bottom = top + itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemRight = left - itemMargin;
            mFirstVisiblePosition = position;
            position--;
        }
    }

    private void fillRight(RecyclerView.Recycler recycler, int rightEdge) {
        final OrientationHelper helper = getOrientationHelper();
        int position = mFirstVisiblePosition;
        int itemRight = -1;
        View itemView;
        int itemWidth, itemHeight;
        final int height = getVerticalSpace();
        int left, top, right, bottom;
        if (getChildCount() != 0) {
            View lastView = getChildAt(getChildCount() - 1);
            if (lastView != null) {
                position = getPosition(lastView) + 1;
                itemRight = helper.getDecoratedEnd(lastView);
            }
        }
        final int size = getItemCount();
        int itemLeft = itemRight + itemMargin;
        while (itemLeft < rightEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurement(itemView);
            itemHeight = helper.getDecoratedMeasurementInOther(itemView);
            left = itemLeft;
            top = (int) (getPaddingTop() + (height - itemHeight) / 2.f);
            right = left + itemWidth;
            bottom = top + itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemLeft = right + itemMargin;
            mLastVisiblePosition = position;
            position++;
        }
    }

    private void removeAndRecyclerWithRight(RecyclerView.Recycler recycler, int rightEdge) {
        View child;
        final OrientationHelper helper = getOrientationHelper();
        for (int i = getChildCount() - 1; i >= 0; i--) {
            child = getChildAt(i);
            if (child != null && helper.getDecoratedStart(child) > rightEdge) {
                // 离开屏幕右侧
                removeAndRecycleView(child, recycler);
                if (isLooper) {
                    if (mLastVisiblePosition == 0) {
                        mLastVisiblePosition = getItemCount();
                    }
                }
                mLastVisiblePosition--;
            } else {
                break;
            }
        }
    }

    private void removeAndRecyclerWithLeft(RecyclerView.Recycler recycler, int leftEdge) {
        View child;
        final OrientationHelper helper = getOrientationHelper();
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (child != null && helper.getDecoratedEnd(child) < leftEdge) {
                // 离开屏幕左侧，移除view，回收资源
                removeAndRecycleView(child, recycler);
                if (isLooper) {
                    if (mFirstVisiblePosition >= getItemCount() - 1) {
                        mFirstVisiblePosition = -1;
                    }
                }
                mFirstVisiblePosition++;
                // 被移除了，调整index
                i--;
            } else {
                break;
            }
        }
    }

    /**
     * 垂直填充布局测量
     *
     * @param recycler RecyclerView.Recycler
     * @param offset   垂直偏移量
     */
    private void fillWithVertical(RecyclerView.Recycler recycler, int offset) {
        final OrientationHelper helper = getOrientationHelper();
        final int topEdge = helper.getStartAfterPadding();
        final int bottomEdge = helper.getEndAfterPadding();
        if (getChildCount() > 0) {
            if (offset >= 0) {
                // 下滑
                removeAndRecyclerWithTop(recycler, topEdge + offset);
            } else {
                // 上滑
                removeAndRecyclerWithBottom(recycler, bottomEdge + offset);
            }

        }
        if (offset >= 0) {
            fillBottom(recycler, bottomEdge + offset);
        } else {
            fillTop(recycler, topEdge + offset);
        }
    }

    private void fillTop(RecyclerView.Recycler recycler, int topEdge) {
        final OrientationHelper helper = getOrientationHelper();
        int position = mFirstVisiblePosition;
        int itemTop = -1;
        int itemWidth, itemHeight;
        int width = getHorizontalSpace();
        int left, top, right, bottom;
        View itemView;
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            if (firstView != null) {
                position = getPosition(firstView) - 1;
                itemTop = helper.getDecoratedStart(firstView);
            }
        }
        int itemBottom = itemTop - itemMargin;
        while (itemBottom > topEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView, 0);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurementInOther(itemView);
            itemHeight = helper.getDecoratedMeasurement(itemView);
            left = (int) (getPaddingLeft() + (width - itemWidth) / 2.f);
            bottom = itemBottom;
            right = left + itemWidth;
            top = bottom - itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemBottom = top - itemMargin;
            mFirstVisiblePosition = position;
            position--;
        }
    }

    private void fillBottom(RecyclerView.Recycler recycler, int bottomEdge) {
        final OrientationHelper helper = getOrientationHelper();
        int position = mFirstVisiblePosition;
        int itemBottom = -1;
        int itemWidth, itemHeight;
        int width = getHorizontalSpace();
        int left, top, right, bottom;
        View itemView;
        if (getChildCount() != 0) {
            View lastView = getChildAt(getChildCount() - 1);
            if (lastView != null) {
                position = getPosition(lastView) + 1;
                itemBottom = helper.getDecoratedEnd(lastView);
            }
        }
        final int size = getItemCount();
        int itemTop = itemBottom + itemMargin;
        while (itemTop < bottomEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            itemView = recycler.getViewForPosition(position);
            addView(itemView);
            measureChildWithMargins(itemView, 0, 0);
            itemWidth = helper.getDecoratedMeasurementInOther(itemView);
            itemHeight = helper.getDecoratedMeasurement(itemView);
            left = (int) (getPaddingLeft() + (width - itemWidth) / 2.f);
            top = itemTop;
            right = left + itemWidth;
            bottom = top + itemHeight;
            layoutDecoratedWithMargins(itemView, left, top, right, bottom);
            itemTop = bottom + itemMargin;
            mLastVisiblePosition = position;
            position++;
        }
    }

    private void removeAndRecyclerWithBottom(RecyclerView.Recycler recycler, int bottomEdge) {
        final OrientationHelper helper = getOrientationHelper();
        View child;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            child = getChildAt(i);
            if (child != null && helper.getDecoratedStart(child) > bottomEdge) {
                removeAndRecycleView(child, recycler);
                if (isLooper) {
                    if (mLastVisiblePosition == 0) {
                        mLastVisiblePosition = getItemCount();
                    }
                }
                mLastVisiblePosition--;
            } else {
                break;
            }
        }
    }

    private void removeAndRecyclerWithTop(RecyclerView.Recycler recycler, int topEdge) {
        final OrientationHelper helper = getOrientationHelper();
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (child != null && helper.getDecoratedEnd(child) < topEdge) {
                // 移除顶部屏幕
                removeAndRecycleView(child, recycler);
                if (isLooper) {
                    if (mFirstVisiblePosition >= getItemCount() - 1) {
                        mFirstVisiblePosition = -1;
                    }
                }
                mFirstVisiblePosition++;
                i--;
            } else {
                break;
            }
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0 || dy == 0) {
            return 0;
        }
        int offset = -dy;
        final OrientationHelper helper = getOrientationHelper();
        int centerToStart = (helper.getEndAfterPadding() - helper.getStartAfterPadding()) / 2 + helper.getStartAfterPadding();
        View child;
        if (dy > 0) {
            child = getChildAt(getChildCount() - 1);
            if (child != null && mLastVisiblePosition == getItemCount() - 1 && !isLooper) {
                final int diff = helper.getDecoratedMeasurement(child) / 2 + helper.getDecoratedStart(child) - centerToStart + (isRebound ? helper.getTotalSpace() / 3 : 0);
                offset = -Math.max(0, Math.min(dy, diff));
            }
        } else {
            child = getChildAt(0);
            if (mFirstVisiblePosition == 0 && child != null && !isLooper) {
                final int diff = helper.getDecoratedMeasurement(child) / 2 + helper.getDecoratedStart(child) - centerToStart - (isRebound ? helper.getTotalSpace() / 3 : 0);
                offset = -Math.min(0, Math.max(dy, diff));
            }
        }
        getState().scrollOffset = -offset;
        fill(recycler, -offset);
        offsetChildrenVertical(offset);
        return -offset;
    }

    @Override
    public void scrollToPosition(int position) {
        mCurItem = position;
        getState().layoutChanged = true;
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final View centerView = findCenterView();
        if (centerView == null) {
            // 理论上是不存在找不到中间view的
            scrollToPosition(position);
            return;
        }
        final int curItem = getPosition(centerView);
        final int count;
        final int direction;
        // 无限循环下只会向最近的方向移动，默认相等情况向后移动，系统LinearSmoothScroller对无限循环扩展不方便
        if (position >= curItem) {
            if (isLooper) {
                int preCount = position - curItem;
                int postCount = curItem + getItemCount() - position;
                if (preCount <= postCount) {
                    count = preCount;
                    direction = LAYOUT_END;
                } else {
                    count = postCount;
                    direction = LAYOUT_START;
                }
            } else {
                count = position - curItem;
                direction = LAYOUT_END;
            }
        } else {
            if (isLooper) {
                int preCount = curItem - position;
                int postCount = position + getItemCount() - curItem;
                if (preCount < postCount) {
                    count = preCount;
                    direction = LAYOUT_START;
                } else {
                    count = postCount;
                    direction = LAYOUT_END;
                }
            } else {
                count = curItem - position;
                direction = LAYOUT_START;
            }
        }
        final OrientationHelper helper = getOrientationHelper();
        final int childSize = helper.getDecoratedMeasurement(centerView);
        final int itemDist = (int) (childSize * (1.f + itemMarginPercent) + 0.5f);
        int dist = count * itemDist;
        final int edge = direction == LAYOUT_END ? helper.getDecoratedEnd(centerView) : helper.getDecoratedStart(centerView);
        float offset = (helper.getTotalSpace() + childSize * direction) / 2.f;
        final int diff = (int) (offset - edge);
        dist -= diff * direction;
        if (mOrientation == LinearLayout.HORIZONTAL) {
            recyclerView.smoothScrollBy(dist * direction, 0);
            return;
        }
        recyclerView.smoothScrollBy(0, dist * direction);
    }

    void setOrientation(@CardSlideView.Orientation int orientation) {
        mOrientation = orientation;
        mInitialSelectedPosition = mCurItem;
        getState().layoutChanged = true;
        requestLayout();
    }

    int getOrientation() {
        return mOrientation;
    }

    int getCurrentItem() {
        return mCurItem;
    }

    void setLooper(boolean isLooper) {
        this.isLooper = isLooper;
        mInitialSelectedPosition = mCurItem;
        getState().layoutChanged = true;
        requestLayout();
    }

    void setItemTransformer(PageTransformer pageTransformer) {
        mPageTransformer = pageTransformer;
        mInitialSelectedPosition = mCurItem;
        getState().layoutChanged = true;
        requestLayout();
    }

    void setOnPageScrollStateChangeListener(OnPageScrollStateChangeListener listener) {
        mOnPageScrollStateChangeListener = listener;
    }

    void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    private static class LayoutParams extends RecyclerView.LayoutParams {

        LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        LayoutParams(int width, int height) {
            super(width, height);
        }

        LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }
    }

    private class InnerScrollListener extends RecyclerView.OnScrollListener {

        private boolean mScrolled = false;

        private void dispatchScrollState(int state) {
            if (mOnPageScrollStateChangeListener != null) {
                mOnPageScrollStateChangeListener.onScrollState(state);
            }
        }

        private void dispatchScrollSelected(int position) {
            mCurItem = position;
            if (mOnPageChangeListener != null) {
                mOnPageChangeListener.onPageSelected(mCurItem);
            }
            dispatchScrollState(RecyclerView.SCROLL_STATE_IDLE);
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx != 0 || dy != 0) {
                mScrolled = true;
                return;
            }
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return;
            }
            View centerView = findCenterView();
            if (centerView == null) {
                return;
            }
            int selectedPosition = layoutManager.getPosition(centerView);
            if (mCurItem == RecyclerView.NO_POSITION) {
                View lastView = findViewByPosition(mCurItem);
                if (lastView == null) {
                    dispatchScrollSelected(selectedPosition);
                }
            }
        }

        boolean snapToTargetExistingView() {
            if (mRecyclerView == null) {
                return false;
            }
            View centerView = findCenterView();
            if (centerView == null) {
                return false;
            }
            int distanceToCenter = (int) calculateDistanceToCenter(centerView, 0);
            boolean isIntercept = distanceToCenter != 0;
            if (isIntercept) {
                if (mOrientation == LinearLayout.HORIZONTAL) {
                    mRecyclerView.smoothScrollBy(distanceToCenter, 0, new DecelerateInterpolator(), 50);
                    return true;
                }
                mRecyclerView.smoothScrollBy(0, distanceToCenter, new DecelerateInterpolator(), 50);
                return true;
            }
            return false;
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE && mScrolled) {
                mScrolled = false;
                if (snapToTargetExistingView()) {
                    return;
                }
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    dispatchScrollState(RecyclerView.SCROLL_STATE_IDLE);
                    return;
                }
                View centerView = findCenterView();
                if (centerView == null) {
                    dispatchScrollState(RecyclerView.SCROLL_STATE_IDLE);
                    return;
                }
                int selectedPosition = layoutManager.getPosition(centerView);
                if (selectedPosition != mCurItem) {
                    View lastView = findViewByPosition(mCurItem);
                    if (lastView == null) {
                        dispatchScrollSelected(selectedPosition);
                        return;
                    }
                    final OrientationHelper helper = getOrientationHelper();
                    int lastLeft = helper.getDecoratedStart(lastView);
                    int lastRight = helper.getDecoratedEnd(lastView);
                    final int leftEdge = helper.getStartAfterPadding();
                    final int rightEdge = helper.getEndAfterPadding();
                    final int offset = (int) (((rightEdge - leftEdge - helper.getDecoratedMeasurement(centerView)) / 2.f + 0.5f) - itemMargin);
                    if ((lastLeft < leftEdge + offset && lastRight <= leftEdge + offset) || (lastLeft >= rightEdge - offset && lastRight > rightEdge - offset)) {
                        dispatchScrollSelected(selectedPosition);
                        return;
                    }
                }
                dispatchScrollState(RecyclerView.SCROLL_STATE_IDLE);
                return;
            }
            dispatchScrollState(newState);
        }
    }

    /**
     * 用于记录状态
     */
    private static class State {

        /**
         * 距离上一次滑动的偏移量
         */
        int scrollOffset;
        /**
         * 记录是否需要重新测量布局
         */
        boolean layoutChanged;

        State() {
            scrollOffset = 0;
            layoutChanged = false;
        }
    }
}
