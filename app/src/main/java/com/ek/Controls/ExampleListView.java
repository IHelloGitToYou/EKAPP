package com.ek.Controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ExampleListView extends ListView {

    private ListViewListener mListener;

    public ExampleListView(Context context) {
        super(context);
    }

    public ExampleListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExampleListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
        if (mListener != null) {
            mListener.onChangeFinished();
        }
    }

    public void setListener(ListViewListener listener) {
        mListener = listener;
    }

    public interface ListViewListener {
        void onChangeFinished();
    }
}