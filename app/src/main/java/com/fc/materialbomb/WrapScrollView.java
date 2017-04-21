package com.fc.materialbomb;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

import com.fc.materialbomb.listener.TranslucentListener;

/**
 * Created by gx on 2017/4/21.
 */

public class WrapScrollView extends ScrollView{

    private static final String TAG = "WrapScrollView";
    private TranslucentListener mListener;
    private Context mContext;
    private int windowHeight;


    public WrapScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        windowHeight = mContext.getResources().getDisplayMetrics().heightPixels;
        Log.e(TAG, "windowHeight: "+windowHeight);
    }

    public void setTranslucentListener(TranslucentListener listener) {
        mListener = listener;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if (mListener != null) {

            int scrollY = getScrollY();//变大的时候是向上滑动

            if(scrollY <= windowHeight/3){
                float alpha = 1-scrollY/(windowHeight/3f);
                mListener.onTranslucent(alpha);
            }else{
                mListener.onTranslucent(0);
            }
        }
    }
}
