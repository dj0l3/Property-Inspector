package com.sakthi.propertyinspector.views;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by sakthivel on 6/8/2016.
 */
public class SwipeToRemoveHelper extends ItemTouchHelper.Callback {


    private SwipeToRemoveCallback mRemoveCallback;

    public SwipeToRemoveHelper(SwipeToRemoveCallback callback){
        mRemoveCallback=callback;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }


    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if(mRemoveCallback!=null)mRemoveCallback.onItemRemoved(viewHolder.getAdapterPosition());
    }


    public interface SwipeToRemoveCallback {
        public void onItemRemoved(int position);
    }
}
