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

import java.io.Serializable;

/**
 * description
 * <p>card滑动条目
 * Created by sunjian on 2017/6/22.
 */
public class CardItem<T extends Serializable> extends BaseCardItem<T> {

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        CardHandler<T> handler = (CardHandler<T>) bundle.getSerializable(ARGUMENTS_HANDLER);
        T data = (T) bundle.getSerializable(ARGUMENTS_DATA);
        int position = bundle.getInt(ARGUMENTS_POSITION, 0);
        if (handler == null) {
            throw new RuntimeException("please bind the handler !");
        }
        return handler.onBind(mContext, data, position, currentMode);
    }

    public void bindData(T data, int position) {
        Bundle bundle = getArguments();
        if (bundle == null) {
            bundle = new Bundle();
            setArguments(bundle);
        }
        bundle.putSerializable(ARGUMENTS_DATA, data);
        bundle.putInt(ARGUMENTS_POSITION, position);
    }

}
