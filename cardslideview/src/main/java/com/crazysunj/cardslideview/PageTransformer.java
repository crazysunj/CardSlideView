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

import android.view.View;

import androidx.annotation.NonNull;

/**
 * 这里不提供position，在滑动速度太快的情况是不准的，你下次通过view获取的position已经变了，但你使用的那一刻调用{@link CardSlideView#getPosition(View)}绝对是准的
 *
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
