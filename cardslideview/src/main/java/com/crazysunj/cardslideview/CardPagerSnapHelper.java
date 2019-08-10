package com.crazysunj.cardslideview;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author sunjian
 * @date 2019-08-05 17:20
 */
public class CardPagerSnapHelper extends PagerSnapHelper {
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        int position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (position >= layoutManager.getItemCount()) {
            return 0;
        }
        return position;
    }
}
