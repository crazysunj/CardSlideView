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
