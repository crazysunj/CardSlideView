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
     * @param position 位置
     */
    void onPageSelected(int position);
}
