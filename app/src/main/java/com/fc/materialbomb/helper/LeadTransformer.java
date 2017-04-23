package com.fc.materialbomb.helper;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.PageTransformer;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.fc.materialbomb.R;
import com.fc.materialbomb.widget.MyHorizotalScrollView;

/**
 * Created by Administrator on 2017/4/23 0023.
 */

public class LeadTransformer implements PageTransformer, OnPageChangeListener {

    private static final String TAG = "LeadTransformer";
    private static final float DEGREE = 30f;

    private int pageIndex = 0;
    private int prePageIndex = 0;

    private int color1;
    private int color2;
    private int color3;

    private int color;

    private ViewPager parent;

    private boolean isScrollingFromLeftToRight = true;//如果为false，则是正在从右侧滑向左侧
    private boolean hasChangedPage = true;//切换页面，用来判断每个页面的平滑动画
    private float prePositionOffset = 0f;

    private ImageView imageView0_2;
    private ImageView imageView0_1;



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //从左滑到右
        if (positionOffset > prePositionOffset && positionOffset != 0) {
            isScrollingFromLeftToRight = true;
        } else if (positionOffset < prePositionOffset && positionOffset != 0) {
            isScrollingFromLeftToRight = false;
        }

        ArgbEvaluator argbEvaluator = new ArgbEvaluator();
//        color = color1;//初始页为第一页，背景色为color1

        if (positionOffset > 0.99) {
            positionOffset = 1;
        }

        switch (position) {

            case 0:
                color = (int) argbEvaluator.evaluate(positionOffset, color1, color2);
                break;
            case 1:
                color = (int) argbEvaluator.evaluate(positionOffset, color2, color3);
                break;
            case 2:
                color = color3;
                break;
        }

        if (positionOffset == 0) {
            switch (position) {
                case 0:
                    color = color1;
                    break;
                case 1:
                    color = color2;
                    break;
                case 2:
                    color = color3;
                    break;
            }
        }

        if (parent != null) {
            parent.setBackgroundColor(color);
        }

        prePositionOffset = positionOffset;
    }

    @Override
    public void onPageSelected(int position) {
        pageIndex = position;

    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    /**
     * 此方法是滑动的时候每一个页面View都会调用该方法
     *
     * @param view:当前的页面
     * @param position:当前滑动的位置 视差效果：在View正常滑动的情况下，给当前View或者当前view里面的每一个子view来一个加速度
     *                         而且每一个加速度大小不一样。
     */
    @Override
    public void transformPage(View view, float position) {
        Log.e(TAG, "transformPage: " + position);

        RelativeLayout rl = (RelativeLayout) view.findViewById(R.id.rl);

        //如果mscv是全局变量，在设置监听的时候可能就不再是准确的mscv对象了
        final MyHorizotalScrollView mscv = (MyHorizotalScrollView) view.findViewById(R.id.mscv);
        FrameLayout bg_container = (FrameLayout) view.findViewById(R.id.bg_container);


        if (parent == null) {
            parent = getViewPager(view);
        }

        imageView0_2 = (ImageView) view.findViewById(R.id.imageView0_2);
        imageView0_1 = (ImageView) view.findViewById(R.id.imageView0_1);

        if (color1 == 0) {
            color1 = view.getContext().getResources().getColor(R.color.bg1_green);
            color2 = view.getContext().getResources().getColor(R.color.bg2_blue);
            color3 = view.getContext().getResources().getColor(R.color.bg3_green);
            color = color1;//初始化第一页背景色
            parent.setBackgroundColor(color);//初始化时设置颜色
        }
        Integer index = (Integer) view.getTag();

        //页面完全显示时，如果切换了页面，则显示动画，更改标志
        if (position == 0) {
            if (prePageIndex != index) {
                hasChangedPage = true;
            }
        }

        if (position == 0 && hasChangedPage) {

            ObjectAnimator obj1 = ObjectAnimator.ofFloat(imageView0_1, "translationX", 0f, -imageView0_1.getWidth());
            obj1.setDuration(400);


            ObjectAnimator obj2 = ObjectAnimator.ofFloat(imageView0_2, "translationX", imageView0_1.getWidth(), 0f);
            obj2.setDuration(400);

            mscv.smoothScrollTo(0,0);

            obj1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mscv.smoothScrollTo((int) (mscv.getWidth()*animation.getAnimatedFraction()),0);//获取动画的进度
                }
            });

            obj2.start();
            obj1.start();

            hasChangedPage = false;
            prePageIndex = index;
        }else if (position == -1 || position == 1) {

            imageView0_1.setTranslationX(0);
            imageView0_2.setTranslationX(imageView0_2.getWidth());
            mscv.scrollTo(0,0);

        }else if(position > -1 && position < 1){

            int height = imageView0_1.getHeight();
            int width = imageView0_1.getWidth();

            float degree = position*DEGREE;

            bg_container.setPivotX(width / 2);
            bg_container.setPivotY(height);
            bg_container.setRotation(degree);
        }





    }

    /**
     * 遍历父view找viewpager
     */
    private ViewPager getViewPager(@NonNull View view) {
        do {
            if (view instanceof ViewPager) {
                return (ViewPager) view;
            }

            if (view != null) {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        return null;
    }

}
