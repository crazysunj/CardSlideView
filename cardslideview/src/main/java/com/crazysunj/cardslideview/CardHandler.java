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
import android.view.View;

import java.io.Serializable;

/**
 * description
 * <p>构建view并处理UI逻辑
 * Created by sunjian on 2017/6/22.
 */
public interface CardHandler<T> extends Serializable {

    View onBind(Context context, T data, int position, @CardViewPager.TransformerMode int mode);
}
