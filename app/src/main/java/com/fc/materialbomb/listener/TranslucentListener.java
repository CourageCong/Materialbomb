package com.fc.materialbomb.listener;

/**
 * class description here
 *
 * @author fucong
 * @version 1.0.0
 * @see ""
 */

public interface TranslucentListener {

    /**
     * Scrollview 滑动时回调透明度
     * @param alpha 0-1
     * */
    // TODO: 2017/4/21 添加垂直滑动
    void onTranslucent(float alpha);

}
