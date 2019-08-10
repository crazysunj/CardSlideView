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
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

/**
 * 画廊布局
 *
 * @author sunjian
 * @date 2019-07-16 09:42
 */
class GalleryLayoutManager extends RecyclerView.LayoutManager implements RecyclerView.SmoothScroller.ScrollVectorProvider {
    private static final String TAG = "GalleryLayoutManager";

    private final static int LAYOUT_START = -1;
    private final static int LAYOUT_END = 1;


    private int mFirstVisiblePosition = 0;
    private int mLastVisiblePosition = 0;
    private int mInitialSelectedPosition = 0;
    private int mCurItem = -1;

    /**
     * 用于滑动记录
     */
    private State mState;

    private SnapHelper mSnapHelper = new CardPagerSnapHelper();

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

    private boolean isLooper;

    GalleryLayoutManager() {
        this(LinearLayout.HORIZONTAL, false);
    }

    GalleryLayoutManager(int orientation) {
        this(orientation, false);
    }

    GalleryLayoutManager(int orientation, boolean isLooper) {
        mOrientation = orientation;
        this.isLooper = isLooper;
    }

    void setSnapHelper(SnapHelper snapHelper) {
        if (mRecyclerView == null) {
            return;
        }
        mSnapHelper = snapHelper;
        mRecyclerView.setOnFlingListener(null);
        mSnapHelper.attachToRecyclerView(mRecyclerView);
    }

    /**
     * 绑定RecyclerView
     *
     * @param recyclerView RecyclerView
     */
    void attachToRecyclerView(@NonNull RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        recyclerView.setLayoutManager(this);
        mSnapHelper.attachToRecyclerView(recyclerView);
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
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (state.isPreLayout()) {
            // 预布局
            return;
        }
        if (state.getItemCount() != 0 && !state.didStructureChange()) {
            // 数据集未改变
            if (!getState().layoutChanged) {
                return;
            }
        }
        if (getChildCount() == 0 || state.didStructureChange()) {
            reset();
        }
        mInitialSelectedPosition = Math.min(Math.max(0, mInitialSelectedPosition), getItemCount() - 1);
        detachAndScrapAttachedViews(recycler);
        firstFill(recycler);
        getState().layoutChanged = false;
    }

    private void reset() {
        if (mState != null) {
            mState.itemsRect.clear();
        }
        if (mCurItem != -1) {
            mInitialSelectedPosition = mCurItem;
        }
        mInitialSelectedPosition = Math.min(Math.max(0, mInitialSelectedPosition), getItemCount() - 1);
        mFirstVisiblePosition = mInitialSelectedPosition;
        mLastVisiblePosition = mInitialSelectedPosition;
        mCurItem = -1;
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
        final int leftEdge = getOrientationHelper().getStartAfterPadding();
        final int rightEdge = getOrientationHelper().getEndAfterPadding();
        final int centerPosition = mInitialSelectedPosition;
        final Rect scrapRect = new Rect();
        final int height = getVerticalSpace();
        // 测量初始化位置view
        final View scrap = recycler.getViewForPosition(mInitialSelectedPosition);
        addView(scrap, 0);
        // 开始测量
        measureChildWithMargins(scrap, 0, 0);
        // 获取宽高
        final int scrapWidth = getDecoratedMeasuredWidth(scrap);
        final int scrapHeight = getDecoratedMeasuredHeight(scrap);
        // view距离父布局顶部的距离
        final int top = (int) (getPaddingTop() + (height - scrapHeight) / 2.f);
        // view距离父布局左边距离
        final int left = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
        // 初始化view所在位置
        scrapRect.set(left, top, left + scrapWidth, top + scrapHeight);
        // 开始布局
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
        // 记录该view的位置
        if (getState().itemsRect.get(centerPosition) == null) {
            getState().itemsRect.put(centerPosition, scrapRect);
        } else {
            getState().itemsRect.get(centerPosition).set(scrapRect);
        }
        mFirstVisiblePosition = mLastVisiblePosition = centerPosition;
        // 再次获取该view的相对父布局的左右距离，防止在布局的时候有所改动
        final int finalLeft = getDecoratedLeft(scrap);
        final int finalRight = getDecoratedRight(scrap);
        // 布局测量中心左边item
        firstFillLeft(recycler, mInitialSelectedPosition - 1, finalLeft, leftEdge);
        // 布局测量中心右边item
        firstFillRight(recycler, mInitialSelectedPosition + 1, finalRight, rightEdge);
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
        View scrap;
        int top;
        int scrapWidth, scrapHeight;
        final Rect scrapRect = new Rect();
        final int height = getVerticalSpace();
        while (itemLeft > leftEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            scrap = recycler.getViewForPosition(position);
            addView(scrap, 0);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            top = (int) (getPaddingTop() + (height - scrapHeight) / 2.f);
            scrapRect.set(itemLeft - scrapWidth, top, itemLeft, top + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemLeft = scrapRect.left;
            mFirstVisiblePosition = position;
            if (getState().itemsRect.get(position) == null) {
                getState().itemsRect.put(position, scrapRect);
            } else {
                getState().itemsRect.get(position).set(scrapRect);
            }
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
        View scrap;
        int top;
        int scrapWidth, scrapHeight;
        final Rect scrapRect = new Rect();
        final int height = getVerticalSpace();
        final int size = getItemCount();
        while (itemRight < rightEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            scrap = recycler.getViewForPosition(position);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            top = (int) (getPaddingTop() + (height - scrapHeight) / 2.f);
            scrapRect.set(itemRight, top, itemRight + scrapWidth, top + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemRight = scrapRect.right;
            mLastVisiblePosition = position;
            if (getState().itemsRect.get(position) == null) {
                getState().itemsRect.put(position, scrapRect);
            } else {
                getState().itemsRect.get(position).set(scrapRect);
            }
            position++;
        }
    }

    /**
     * 垂直首次布局测量
     *
     * @param recycler RecyclerView.Recycler
     */
    private void firstFillWithVertical(RecyclerView.Recycler recycler) {
        final int topEdge = getOrientationHelper().getStartAfterPadding();
        final int bottomEdge = getOrientationHelper().getEndAfterPadding();
        final int centerPosition = mInitialSelectedPosition;
        final Rect scrapRect = new Rect();
        final int width = getHorizontalSpace();
        View scrap = recycler.getViewForPosition(mInitialSelectedPosition);
        addView(scrap, 0);
        measureChildWithMargins(scrap, 0, 0);
        final int scrapWidth = getDecoratedMeasuredWidth(scrap);
        final int scrapHeight = getDecoratedMeasuredHeight(scrap);
        final int left = (int) (getPaddingLeft() + (width - scrapWidth) / 2.f);
        final int top = (int) (getPaddingTop() + (getVerticalSpace() - scrapHeight) / 2.f);
        scrapRect.set(left, top, left + scrapWidth, top + scrapHeight);
        layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
        if (getState().itemsRect.get(centerPosition) == null) {
            getState().itemsRect.put(centerPosition, scrapRect);
        } else {
            getState().itemsRect.get(centerPosition).set(scrapRect);
        }
        mFirstVisiblePosition = mLastVisiblePosition = centerPosition;
        final int finalTop = getDecoratedTop(scrap);
        final int finalBottom = getDecoratedBottom(scrap);
        firstFillTop(recycler, mInitialSelectedPosition - 1, finalTop, topEdge);
        firstFillBottom(recycler, mInitialSelectedPosition + 1, finalBottom, bottomEdge);
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
        View scrap;
        int leftOffset;
        int scrapWidth, scrapHeight;
        final Rect scrapRect = new Rect();
        final int width = getHorizontalSpace();
        while (itemTop > topEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            scrap = recycler.getViewForPosition(position);
            addView(scrap, 0);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.f);
            scrapRect.set(leftOffset, itemTop - scrapHeight, leftOffset + scrapWidth, itemTop);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemTop = scrapRect.top;
            mFirstVisiblePosition = position;
            if (getState().itemsRect.get(position) == null) {
                getState().itemsRect.put(position, scrapRect);
            } else {
                getState().itemsRect.get(position).set(scrapRect);
            }
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
        View scrap;
        int leftOffset;
        int scrapWidth, scrapHeight;
        final Rect scrapRect = new Rect();
        final int width = getHorizontalSpace();
        final int size = getItemCount();
        while (itemBottom < bottomEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            scrap = recycler.getViewForPosition(position);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.f);
            scrapRect.set(leftOffset, itemBottom, leftOffset + scrapWidth, itemBottom + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemBottom = scrapRect.bottom;
            mLastVisiblePosition = position;
            if (getState().itemsRect.get(position) == null) {
                getState().itemsRect.put(position, scrapRect);
            } else {
                getState().itemsRect.get(position).set(scrapRect);
            }
            position++;
        }
    }

    /**
     * @param child  计算的view
     * @param offset view的滑动偏移量
     * @return 计算距离中心轴相对自身的偏移百分比
     */
    private float calculateOffsetPercentToCenter(View child, float offset) {
        final int distance = calculateDistanceToCenter(child, offset);
        final int size = mOrientation == LinearLayout.HORIZONTAL ? child.getWidth() : child.getHeight();
        return Math.max(-1.f, Math.min(1.f, distance * 1.f / size));
    }

    /**
     * @param child  计算的view
     * @param offset view的滑动偏移量
     * @return 返回view距离中心轴的距离
     */
    private int calculateDistanceToCenter(View child, float offset) {
        final OrientationHelper orientationHelper = getOrientationHelper();
        final int centerToStart = (orientationHelper.getEndAfterPadding() - orientationHelper.getStartAfterPadding()) / 2 + orientationHelper.getStartAfterPadding();
        if (mOrientation == LinearLayout.HORIZONTAL) {
            return (int) (child.getWidth() / 2 - offset + child.getLeft() - centerToStart);
        } else {
            return (int) (child.getHeight() / 2 - offset + child.getTop() - centerToStart);
        }
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        final int direction = calculateScrollDirectionForPosition(targetPosition);
        PointF outVector = new PointF();
        if (direction == 0) {
            return null;
        }
        if (mOrientation == LinearLayout.HORIZONTAL) {
            outVector.x = direction;
            outVector.y = 0;
        } else {
            outVector.x = 0;
            outVector.y = direction;
        }
        return outVector;
    }

    private int calculateScrollDirectionForPosition(int position) {
        if (getChildCount() == 0) {
            return LAYOUT_START;
        }
        final int firstChildPos = mFirstVisiblePosition;
        return position < firstChildPos ? LAYOUT_START : LAYOUT_END;
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

    private OrientationHelper getOrientationHelper() {
        if (mOrientation == LinearLayout.HORIZONTAL) {
            if (mHorizontalHelper == null) {
                mHorizontalHelper = OrientationHelper.createHorizontalHelper(this);
            }
            return mHorizontalHelper;
        } else {
            if (mVerticalHelper == null) {
                mVerticalHelper = OrientationHelper.createVerticalHelper(this);
            }
            return mVerticalHelper;
        }
    }

    @Override
    public boolean canScrollHorizontally() {
        return mOrientation == LinearLayout.HORIZONTAL;
    }

    @Override
    public boolean canScrollVertically() {
        return mOrientation == LinearLayout.VERTICAL;
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
        final OrientationHelper orientationHelper = getOrientationHelper();
        final int centerToStart = (orientationHelper.getEndAfterPadding() - orientationHelper.getStartAfterPadding()) / 2 + orientationHelper.getStartAfterPadding();
        View child;
        if (dx > 0) {
            child = getChildAt(getChildCount() - 1);
            if (child != null && getPosition(child) == getItemCount() - 1 && !isLooper) {
                // 计算全部加载完后item的偏移量，右边会留出空隙
                offset = -Math.max(0, Math.min(dx, (child.getRight() - child.getLeft()) / 2 + child.getLeft() - centerToStart));
            }
        } else {
            child = getChildAt(0);
            if (mFirstVisiblePosition == 0 && child != null && !isLooper) {
                // 计算首次加载item的偏移量，左边会留出空隙
                offset = -Math.min(0, Math.max(dx, ((child.getRight() - child.getLeft()) / 2 + child.getLeft()) - centerToStart));
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
        final OrientationHelper orientationHelper = getOrientationHelper();
        final int leftEdge = orientationHelper.getStartAfterPadding();
        final int rightEdge = orientationHelper.getEndAfterPadding();
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
        int position = mFirstVisiblePosition;
        int itemLeft = -1;
        Rect scrapRect;
        View scrap;
        int scrapWidth, scrapHeight;
        final int height = getVerticalSpace();
        int topOffset;
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            if (firstView != null) {
                position = getPosition(firstView) - 1;
                itemLeft = getDecoratedLeft(firstView);
            }
        }
        while (itemLeft > leftEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    Log.e(TAG, "break");
                    break;
                }
            }
            scrapRect = getState().itemsRect.get(position);
            scrap = recycler.getViewForPosition(position);
            addView(scrap, 0);
            if (scrapRect == null) {
                scrapRect = new Rect();
                getState().itemsRect.put(position, scrapRect);
            }
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.f);
            scrapRect.set(itemLeft - scrapWidth, topOffset, itemLeft, topOffset + scrapHeight);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemLeft = scrapRect.left;
            mFirstVisiblePosition = position;
            position--;
        }
    }

    private void fillRight(RecyclerView.Recycler recycler, int rightEdge) {
        int position = mFirstVisiblePosition;
        int itemRight = -1;
        Rect scrapRect;
        View scrap;
        int scrapWidth, scrapHeight;
        final int height = getVerticalSpace();
        int topOffset;
        if (getChildCount() != 0) {
            View lastView = getChildAt(getChildCount() - 1);
            if (lastView != null) {
                position = getPosition(lastView) + 1;
                itemRight = getDecoratedRight(lastView);
            }
        }
        final int size = getItemCount();
        while (itemRight < rightEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            scrapRect = getState().itemsRect.get(position);
            scrap = recycler.getViewForPosition(position);
            addView(scrap, getChildCount());
            if (scrapRect == null) {
                scrapRect = new Rect();
                getState().itemsRect.put(position, scrapRect);
            }
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            topOffset = (int) (getPaddingTop() + (height - scrapHeight) / 2.f);
            if (itemRight == -1 && position == 0 && !isLooper) {
                // 第一个特殊处理，左边需要间隙
                int left = (int) (getPaddingLeft() + (getHorizontalSpace() - scrapWidth) / 2.f);
                scrapRect.set(left, topOffset, left + scrapWidth, topOffset + scrapHeight);
            } else {
                scrapRect.set(itemRight, topOffset, itemRight + scrapWidth, topOffset + scrapHeight);
            }
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemRight = scrapRect.right;
            mLastVisiblePosition = position;
            position++;
        }
    }

    private void removeAndRecyclerWithRight(RecyclerView.Recycler recycler, int rightEdge) {
        View child;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            child = getChildAt(i);
            if (child != null && getDecoratedLeft(child) > rightEdge) {
                // 离开屏幕右侧
                removeAndRecycleView(child, recycler);
                if (isLooper) {
                    if (mLastVisiblePosition == 0) {
                        mLastVisiblePosition = getItemCount();
                    }
                }
                mLastVisiblePosition--;
            }
        }
    }

    private void removeAndRecyclerWithLeft(RecyclerView.Recycler recycler, int leftEdge) {
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (child != null && getDecoratedRight(child) < leftEdge) {
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
        final OrientationHelper orientationHelper = getOrientationHelper();
        final int topEdge = orientationHelper.getStartAfterPadding();
        final int bottomEdge = orientationHelper.getEndAfterPadding();
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
        int position = mFirstVisiblePosition;
        int itemTop = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect;
        int width = getHorizontalSpace();
        int leftOffset;
        View scrap;
        if (getChildCount() > 0) {
            View firstView = getChildAt(0);
            if (firstView != null) {
                position = getPosition(firstView) - 1;
                itemTop = getDecoratedTop(firstView);
            }
        }
        while (itemTop > topEdge) {
            if (position < 0) {
                if (isLooper) {
                    position = getItemCount() - 1;
                } else {
                    break;
                }
            }
            scrapRect = getState().itemsRect.get(position);
            scrap = recycler.getViewForPosition(position);
            addView(scrap, 0);
            if (scrapRect == null) {
                scrapRect = new Rect();
                getState().itemsRect.put(position, scrapRect);
            }
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.f);
            scrapRect.set(leftOffset, itemTop - scrapHeight, leftOffset + scrapWidth, itemTop);
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemTop = scrapRect.top;
            mFirstVisiblePosition = position;
            position--;
        }
    }

    private void fillBottom(RecyclerView.Recycler recycler, int bottomEdge) {
        int position = mFirstVisiblePosition;
        int itemBottom = -1;
        int scrapWidth, scrapHeight;
        Rect scrapRect;
        int width = getHorizontalSpace();
        int leftOffset;
        View scrap;
        if (getChildCount() != 0) {
            View lastView = getChildAt(getChildCount() - 1);
            if (lastView != null) {
                position = getPosition(lastView) + 1;
                itemBottom = getDecoratedBottom(lastView);
            }
        }
        final int size = getItemCount();
        while (itemBottom < bottomEdge) {
            if (position >= size) {
                if (isLooper) {
                    position = 0;
                } else {
                    break;
                }
            }
            scrapRect = getState().itemsRect.get(position);
            scrap = recycler.getViewForPosition(position);
            addView(scrap);
            if (scrapRect == null) {
                scrapRect = new Rect();
                getState().itemsRect.put(position, scrapRect);
            }
            measureChildWithMargins(scrap, 0, 0);
            scrapWidth = getDecoratedMeasuredWidth(scrap);
            scrapHeight = getDecoratedMeasuredHeight(scrap);
            leftOffset = (int) (getPaddingLeft() + (width - scrapWidth) / 2.f);
            if (itemBottom == -1 && position == 0 && !isLooper) {
                // 顶部特殊处理
                int top = (int) (getPaddingTop() + (getVerticalSpace() - scrapHeight) / 2.f);
                scrapRect.set(leftOffset, top, leftOffset + scrapWidth, top + scrapHeight);
            } else {
                scrapRect.set(leftOffset, itemBottom, leftOffset + scrapWidth, itemBottom + scrapHeight);
            }
            layoutDecorated(scrap, scrapRect.left, scrapRect.top, scrapRect.right, scrapRect.bottom);
            itemBottom = scrapRect.bottom;
            mLastVisiblePosition = position;
            position++;
        }
    }

    private void removeAndRecyclerWithBottom(RecyclerView.Recycler recycler, int bottomEdge) {
        View child;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            child = getChildAt(i);
            if (child != null && getDecoratedTop(child) > bottomEdge) {
                removeAndRecycleView(child, recycler);
                mLastVisiblePosition--;
            } else {
                break;
            }
        }
    }

    private void removeAndRecyclerWithTop(RecyclerView.Recycler recycler, int topEdge) {
        View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            if (child != null && getDecoratedBottom(child) < topEdge) {
                // 移除顶部屏幕
                removeAndRecycleView(child, recycler);
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
        int centerToStart = (getOrientationHelper().getEndAfterPadding() - getOrientationHelper().getStartAfterPadding()) / 2 + getOrientationHelper().getStartAfterPadding();
        View child;
        if (dy > 0) {
            child = getChildAt(getChildCount() - 1);
            if (child != null && getPosition(child) == getItemCount() - 1) {
                offset = -Math.max(0, Math.min(dy, (getDecoratedBottom(child) - getDecoratedTop(child)) / 2 + getDecoratedTop(child) - centerToStart));
            }
        } else {
            child = getChildAt(0);
            if (mFirstVisiblePosition == 0 && child != null) {
                offset = -Math.min(0, Math.max(dy, (getDecoratedBottom(child) - getDecoratedTop(child)) / 2 + getDecoratedTop(child) - centerToStart));
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
        GallerySmoothScroller linearSmoothScroller = new GallerySmoothScroller(recyclerView.getContext());
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
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

        private int state;
        private boolean isDrag;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            if (layoutManager == null) {
                return;
            }
            if (mSnapHelper == null) {
                return;
            }
            View snap = mSnapHelper.findSnapView(layoutManager);
            if (snap == null) {
                return;
            }
            int selectedPosition = layoutManager.getPosition(snap);
            if (selectedPosition != mCurItem && mOnPageChangeListener != null && isDrag) {
                mCurItem = selectedPosition;
                isDrag = false;
                mOnPageChangeListener.onPageSelected(mCurItem);
                return;
            }
            if (mCurItem == -1 && mOnPageChangeListener != null) {
                mCurItem = selectedPosition;
                mOnPageChangeListener.onPageSelected(mCurItem);
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            state = newState;
            if (state == RecyclerView.SCROLL_STATE_DRAGGING) {
                isDrag = true;
            }
            if (state == RecyclerView.SCROLL_STATE_IDLE) {
                isDrag = false;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager == null) {
                    return;
                }
                if (mSnapHelper == null) {
                    return;
                }
                View snap = mSnapHelper.findSnapView(layoutManager);
                if (snap == null) {
                    return;
                }
                int selectedPosition = layoutManager.getPosition(snap);
                if (mOnPageChangeListener != null && selectedPosition != mCurItem) {
                    mCurItem = selectedPosition;
                    mOnPageChangeListener.onPageSelected(mCurItem);
                }
            }
        }
    }

    private static class GallerySmoothScroller extends LinearSmoothScroller {

        private GallerySmoothScroller(Context context) {
            super(context);
        }

        private int calculateDxToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollHorizontally()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();
            final int left = layoutManager.getDecoratedLeft(view) - params.leftMargin;
            final int right = layoutManager.getDecoratedRight(view) + params.rightMargin;
            final int start = layoutManager.getPaddingLeft();
            final int end = layoutManager.getWidth() - layoutManager.getPaddingRight();
            final int childCenter = left + (int) ((right - left) / 2.f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }

        private int calculateDyToMakeCentral(View view) {
            final RecyclerView.LayoutManager layoutManager = getLayoutManager();
            if (layoutManager == null || !layoutManager.canScrollVertically()) {
                return 0;
            }
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)
                    view.getLayoutParams();
            final int top = layoutManager.getDecoratedTop(view) - params.topMargin;
            final int bottom = layoutManager.getDecoratedBottom(view) + params.bottomMargin;
            final int start = layoutManager.getPaddingTop();
            final int end = layoutManager.getHeight() - layoutManager.getPaddingBottom();
            final int childCenter = top + (int) ((bottom - top) / 2.f);
            final int containerCenter = (int) ((end - start) / 2.f);
            return containerCenter - childCenter;
        }


        @Override
        protected void onTargetFound(View targetView, RecyclerView.State state, Action action) {
            final int dx = calculateDxToMakeCentral(targetView);
            final int dy = calculateDyToMakeCentral(targetView);
            final int distance = (int) Math.sqrt(dx * dx + dy * dy);
            final int time = calculateTimeForDeceleration(distance);
            if (time > 0) {
                action.update(-dx, -dy, time, mDecelerateInterpolator);
            }
        }
    }

    /**
     * 用于记录状态
     */
    private static class State {
        /**
         * 所有item前一次位置
         */
        SparseArray<Rect> itemsRect;

        /**
         * 距离上一次滑动的偏移量
         */
        int scrollOffset;
        /**
         * 记录是否需要重新测量布局
         */
        boolean layoutChanged;

        State() {
            itemsRect = new SparseArray<>();
            scrollOffset = 0;
            layoutChanged = false;
        }
    }
}
