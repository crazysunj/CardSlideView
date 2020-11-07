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
package com.crazysunj.cardslide;

import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.crazysunj.cardslideview.PageTransformer;

/**
 * @author sunjian
 * @date 2019-07-16 09:49
 */
class ScaleTransformer implements PageTransformer {

    ScaleTransformer() {
    }

    @Override
    public void transformPage(@NonNull View view, float offsetPercent, int orientation) {
        final float finalPercent = 1 - Math.min(Math.abs(offsetPercent), 2.f) / 2.f;
        float rotation = -(60 * (1 - finalPercent) * (offsetPercent >= 0 ? 1 : -1));
        if (orientation == LinearLayout.HORIZONTAL) {
            if (offsetPercent > 0) {
                view.setPivotX(view.getWidth());
                view.setPivotY(view.getHeight() / 2.f);
            } else {
                view.setPivotX(0);
                view.setPivotY(view.getHeight() / 2.f);
            }
            view.setRotationY(rotation);
        } else {
            if (offsetPercent > 0) {
                view.setPivotX(view.getWidth() / 2.f);
                view.setPivotY(view.getHeight());
            } else {
                view.setPivotX(view.getWidth() / 2.f);
                view.setPivotY(0);
            }
            view.setRotationX(-rotation);
        }
        float scale = 0.8f + 0.2f * finalPercent;
        view.setScaleX(scale);
        view.setScaleY(scale);
        final float alpha = (float) Math.pow(finalPercent, 0.8);
        view.setAlpha(alpha);
    }
}
