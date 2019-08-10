package com.crazysunj.cardslideview;

import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author sunjian
 * @date 2019-08-05 17:21
 */
public class CardLinearSnapHelper extends LinearSnapHelper {
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
        int position = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
        if (position >= layoutManager.getItemCount() - 1) {
            return -1;
        }
        if (position == 0) {
            return -1;
        }
        return position;
    }
}
