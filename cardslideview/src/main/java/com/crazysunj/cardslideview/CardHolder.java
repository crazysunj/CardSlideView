package com.crazysunj.cardslideview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * @author sunjian
 * @date 2019-07-19 15:01
 */
public interface CardHolder<T> {
    /**
     * 创建一个view
     *
     * @param inflater  LayoutInflater
     * @param container 父布局
     * @return View
     */
    View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container);

    /**
     * 回调数据和位置，进行渲染
     *
     * @param holder   CardViewHolder
     * @param data     数据
     * @param position 位置
     */
    void onBindView(@NonNull CardViewHolder holder, T data, int position);
}
