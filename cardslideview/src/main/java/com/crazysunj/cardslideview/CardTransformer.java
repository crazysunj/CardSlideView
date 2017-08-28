/**
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

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * description
 * <p>调整ViewPager滑动视差
 * Created by sunjian on 2017/6/22.
 */
class CardTransformer implements ViewPager.PageTransformer {

    private int mMaxTranslateOffsetX;
    private float mScaleRate;
    private ViewPager mViewPager;

    CardTransformer(int maxOffset, float scaleRate) {
        mMaxTranslateOffsetX = maxOffset;
        mScaleRate = scaleRate;
    }


    @Override
    public void transformPage(View page, float position) {

        if (mViewPager == null) {
            mViewPager = (ViewPager) page.getParent();
        }

        int leftInScreen = page.getLeft() - mViewPager.getScrollX();
        int centerXInViewPager = leftInScreen + page.getMeasuredWidth() / 2;
        int offsetX = centerXInViewPager - mViewPager.getMeasuredWidth() / 2;
        float offsetRate = (float) offsetX * mScaleRate / mViewPager.getMeasuredWidth();
        float scaleFactor = 1 - Math.abs(offsetRate);

        if (scaleFactor > 0) {
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
            page.setTranslationX(-mMaxTranslateOffsetX * offsetRate);
        }
    }
}
