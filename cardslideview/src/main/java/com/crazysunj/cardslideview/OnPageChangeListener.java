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

/**
 * 用于监听滑动位置，规则如下：
 * 模仿viewPager的滑动机制，如果是手指滑动，那么position都会回调，由于是pager机制，不存在惯性滑动
 * 如果是参数滑动（指定某个位置），那么只会回调最后一个
 *
 * @author sunjian
 * @date 2019-07-19 15:31
 */
public interface OnPageChangeListener {
    /**
     * 滑动回调
     *
     * @param position 位置，相同位置，只会回调一次，即时循环几圈回来也是一样
     */
    void onPageSelected(int position);
}
