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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * description
 * <p>可调整宽高比的CardView，默认开启阴影效果
 * Created by sunjian on 2017/6/22.
 */
public class ElasticCardView extends CardView {

    private final float RATIO;

    public ElasticCardView(Context context) {
        this(context, null);
    }

    public ElasticCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ElasticCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ElasticCardView);
        RATIO = array.getFloat(R.styleable.ElasticCardView_ratio, 1.0f);
        array.recycle();

        setPreventCornerOverlap(true);
        setUseCompatPadding(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (RATIO > 0) {
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec((int) (MeasureSpec.getSize(widthMeasureSpec) * RATIO), MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
