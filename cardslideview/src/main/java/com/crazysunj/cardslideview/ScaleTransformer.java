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
 * 默认缩放效果，可自定义
 *
 * @author sunjian
 * @date 2019-07-16 09:49
 */
class ScaleTransformer implements PageTransformer {

    ScaleTransformer() {
    }

    @Override
    public void transformPage(@NonNull View view, float offsetPercent, int orientation) {
        float scale = 1 - 0.2f * Math.abs(offsetPercent);
        view.setScaleX(scale);
        view.setScaleY(scale);
    }
}
