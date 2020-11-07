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
