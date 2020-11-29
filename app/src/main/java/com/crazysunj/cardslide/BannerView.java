package com.crazysunj.cardslide;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crazysunj.cardslideview.CardHolder;
import com.crazysunj.cardslideview.CardSlideView;
import com.crazysunj.cardslideview.OnPageChangeListener;
import com.crazysunj.cardslideview.OnPageItemClickListener;
import com.crazysunj.cardslideview.PageTransformer;

import java.util.List;


/**
 * @author sunjian
 * @date 2019-12-30 10:09
 */
public class BannerView<T> extends LinearLayout implements PageTransformer {

    private static final long DEFAULT_INTERVAL = 5000;
    private CardSlideView<T> mBanner;
    private CircleLineIndicator mIndicator;
    private Handler mHandler;
    private long mInterval;
    private boolean isTurning;

    private float lastOffsetPercent;
    private int lastPosition;
    private PageTransformer mPageTransformer;

    public BannerView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mInterval = DEFAULT_INTERVAL;
        setOrientation(VERTICAL);
        mHandler = new Handler(Looper.getMainLooper());
        View.inflate(context, R.layout.view_banner, this);
        mBanner = findViewById(R.id.banner);
        mIndicator = findViewById(R.id.indicator);
        mBanner.setItemTransformer(this);
    }

    @Override
    public void transformPage(@NonNull View view, float offsetPercent, int orientation) {
        transformSelf(view, offsetPercent, orientation);
        int position = mBanner.getPosition(view);
        View centerView = mBanner.getCenterView();
        if (centerView == view) {
            final int size = mBanner.getItemCount();
            final boolean isLeft = isLeft(offsetPercent, position, size);
            if (isLeft) {
                transformLeft(offsetPercent, position, size);
                return;
            }
            transformRight(offsetPercent, position, size);
        }
    }

    private void transformSelf(@NonNull View view, float offsetPercent, int orientation) {
        if (mPageTransformer != null) {
            mPageTransformer.transformPage(view, offsetPercent, orientation);
        }
    }

    private void transformRight(float offsetPercent, int position, int size) {
        float percent;
        if (offsetPercent >= 0) {
            position--;
            if (position < 0) {
                position = size - 1;
            }
            percent = 1 - Math.abs(offsetPercent);
        } else {
            percent = Math.abs(offsetPercent);
        }
        mIndicator.transform(false, position, percent);
    }

    private void transformLeft(float offsetPercent, int position, int size) {
        float percent;
        position--;
        if (position < 0) {
            position = size - 1;
        }
        if (offsetPercent <= 0) {
            position++;
            if (position >= size) {
                position = 0;
            }
            percent = Math.abs(offsetPercent);
        } else {
            percent = 1 - Math.abs(offsetPercent);
        }
        mIndicator.transform(true, position, percent);
    }

    private boolean isLeft(float offsetPercent, int position, int size) {
        boolean isLeft = false;
        if (position == lastPosition) {
            if (offsetPercent > lastOffsetPercent) {
                isLeft = true;
            }
        } else {
            int itemCount = size - 1;
            if (lastPosition == 0) {
                if (position == itemCount) {
                    isLeft = true;
                }
            } else if (lastPosition == itemCount) {
                if (position != 0) {
                    isLeft = true;
                }
            } else {
                if (position < lastPosition) {
                    isLeft = true;
                }
            }
        }
        lastPosition = position;
        lastOffsetPercent = offsetPercent;
        return isLeft;
    }

    public T getItemInfo(int position) {
        List<T> data = mBanner.getData();
        if (data == null || data.isEmpty() || position >= data.size()) {
            return null;
        }
        return data.get(position);
    }

    public void setItemRate(float itemRate) {
        mBanner.setItemRate(itemRate);
    }

    public void setItemTransformer(PageTransformer pageTransformer) {
        mPageTransformer = pageTransformer;
    }

    public void bind(List<T> data, @NonNull CardHolder<T> holder) {
        bind(data, holder, false);
    }

    public void bind(List<T> data, @NonNull CardHolder<T> holder, boolean isResetHolder) {
        boolean isTurning = this.isTurning;
        if (isTurning) {
            stop();
        }
        final int size = data == null ? 0 : data.size();
        if (size <= 1) {
            if (mBanner.getOrientation() == LinearLayout.HORIZONTAL) {
                mBanner.setCanScrollHorizontally(false);
            } else {
                mBanner.setCanScrollVertically(false);
            }
            mBanner.setLooper(false);
        }
        mBanner.bind(data, holder, isResetHolder);
        if (size <= 1) {
            mIndicator.setVisibility(GONE);
        } else {
            mIndicator.setSize(size);
            mIndicator.setVisibility(VISIBLE);
            mIndicator.transform(true, 0, 0);
        }
        if (isTurning) {
            start();
        }
    }

    public void setInterval(long interval) {
        boolean isTurning = this.isTurning;
        if (isTurning) {
            stop();
        }
        mInterval = interval;
        if (isTurning) {
            start();
        }
    }

    public void start() {
        final List<T> data = mBanner.getData();
        final int size = data == null ? 0 : data.size();
        if (size <= 1) {
            return;
        }
        if (isTurning) {
            stop();
        }
        isTurning = true;
        mHandler.postDelayed(bannerRunnable, mInterval);
    }

    public void stop() {
        isTurning = false;
        mHandler.removeCallbacks(bannerRunnable);
    }

    public void setCurrentItem(int item) {
        mBanner.setCurrentItem(item);
    }

    public void setCurrentItem(int item, boolean smoothScroll) {
        mBanner.setCurrentItem(item, smoothScroll);
    }

    public int getCurrentItem() {
        return mBanner.getCurrentItem();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
            start();
        } else if (action == MotionEvent.ACTION_DOWN) {
            stop();
        }
        return super.dispatchTouchEvent(ev);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        mBanner.setOnPageChangeListener(onPageChangeListener);
    }

    public void setOnPageItemClickListener(OnPageItemClickListener<T> listener) {
        mBanner.setOnPageItemClickListener(listener);
    }

    private Runnable bannerRunnable = new Runnable() {
        @Override
        public void run() {
            mBanner.setCurrentItem(mBanner.getCurrentItem() + 1, true);
            mHandler.postDelayed(this, mInterval);
        }
    };
}
