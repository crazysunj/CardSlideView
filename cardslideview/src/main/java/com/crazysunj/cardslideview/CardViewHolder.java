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

import android.util.SparseArray;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author sunjian
 * @date 2019-07-19 16:32
 */
public class CardViewHolder extends RecyclerView.ViewHolder {

    private SparseArray<View> mViewArray;

    CardViewHolder(@NonNull View itemView) {
        super(itemView);
        mViewArray = new SparseArray<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T getView(@IdRes int id) {
        View view = mViewArray.get(id);
        if (view == null) {
            view = itemView.findViewById(id);
            mViewArray.put(id, view);
        }
        return (T) view;
    }
}
