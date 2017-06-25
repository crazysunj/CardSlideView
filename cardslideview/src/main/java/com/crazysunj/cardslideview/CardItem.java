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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * description
 * <p>card滑动条目
 * Created by sunjian on 2017/6/22.
 */
public class CardItem<T> extends BaseCardItem<T> {

    private T mData;
    private int mPosition;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mHandler == null) {
            throw new RuntimeException("please bind the handler !");
        }
        return mHandler.onBind(mContext, mData, mPosition);
    }

    public void bindData(T data, int position) {
        mData = data;
        mPosition = position;
    }
}
