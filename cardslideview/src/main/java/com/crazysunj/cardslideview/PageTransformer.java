package com.crazysunj.cardslideview;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * @author sunjian
 * @date 2019-07-19 15:32
 */
public interface PageTransformer {
    /**
     * 滑动中变换
     *
     * @param view          当前view
     * @param offsetPercent 前后距离中心轴相对自身的偏移量百分比
     * @param orientation   方向
     */
    void transformPage(@NonNull View view, float offsetPercent, int orientation);
}
