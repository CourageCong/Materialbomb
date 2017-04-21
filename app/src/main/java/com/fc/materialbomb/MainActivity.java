package com.fc.materialbomb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fc.materialbomb.listener.TranslucentListener;
import com.fc.materialbomb.util.FastBlurUtil;
import com.fc.materialbomb.util.ImageUtil;
import com.xiaomi.music.materialbomb.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.graphics.Color.argb;

public class MainActivity extends BaseActivity implements TranslucentListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.scrollView)
    WrapScrollView mScrollView;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.rel_home)
    RelativeLayout mRelHome;
    @BindView(R.id.navigation)
    NavigationView mNavigation;
    @BindView(R.id.drawerlayout)
    DrawerLayout mDrawerlayout;
    @BindView(R.id.img)
    ImageView mImg;
    @BindView(R.id.txt)
    TextView mTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
    }

    /**
     * 初始化view
     */
    private void initView() {
        setSupportActionBar(mToolbar);
        initToolbar();
        initNavigationView();
        initScrollView();
        initPalette();
    }


    /**
     * 配置toolbar
     */
    private void initToolbar() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    /**
     * 配置侧滑栏,侧滑头部毛玻璃效果
     */
    private void initNavigationView() {
        View view = mNavigation.getHeaderView(0);
        CircleImageView circleImageView = (CircleImageView) view.findViewById(R.id.profile_image);
        Glide.with(this).load(R.drawable.a_head).into(circleImageView);

        Bitmap b = getScaledBitmap(R.drawable.a_head, 50);
        Bitmap blurBitmap = FastBlurUtil.doBlur(b, 8, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ImageUtil.bitmapToDrawable(blurBitmap));
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }


    /**
     * 获取压缩后的bitmap
     *
     * @param resourceId   资源id
     * @param inSampleSize 压缩比例
     */
    public Bitmap getScaledBitmap(int resourceId, int inSampleSize) {

        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resourceId, options);
//     第二个参数 isMutable True if the resulting bitmap should be mutable (i.e.its pixels can be modified)
//        如果不复制一份的话 则不能进行毛玻璃效果 bitmap.setPixels（）会报错，在源码注释中有原因
        Bitmap newBitmap = bitmap.copy(bitmap.getConfig(), true);
        return newBitmap;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView s = (SearchView) MenuItemCompat.getActionView(searchItem);
        initSearchView(s);

//        view.setIconified(true);


        return true;
    }


    /**
     * 初始化设置toolbar上的searchview
     */
    private void initSearchView(SearchView s) {

        s.setSubmitButtonEnabled(true);
        //设置搜索图标
        ImageView img = (ImageView) s.findViewById(R.id.search_go_btn);
        img.setImageResource(R.mipmap.search);

        SearchView.SearchAutoComplete complete = (SearchView.SearchAutoComplete) s.findViewById(R.id.search_src_text);
        complete.setHint("请输入商品名");
        complete.setHintTextColor(Color.GRAY);
        complete.setTextColor(Color.WHITE);

        s.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "前往搜索", Snackbar.LENGTH_SHORT)
                        .setAction("取消", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "取消搜索", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });
    }


    /**
     * 初始化scrollview ，设置监听来监听滑动
     */
    private void initScrollView() {
        mScrollView.setTranslucentListener(this);
    }

    @Override
    public void onTranslucent(float alpha) {
        mToolbar.setAlpha(alpha);
    }

    /**
     * 调色板模块
     */
    private void initPalette() {

        BitmapDrawable d = (BitmapDrawable) mImg.getDrawable();
        Bitmap m = d.getBitmap();

        Palette.from(m).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                //暗、柔和的颜色
                int darkMutedColor = palette.getDarkMutedColor(Color.BLUE);//如果分析不出来，则返回默认颜色
                //暗、柔和
                int lightMutedColor = palette.getLightMutedColor(Color.BLUE);
                //暗、鲜艳
                int darkVibrantColor = palette.getDarkVibrantColor(Color.BLUE);
                //亮、鲜艳
                int lightVibrantColor = palette.getLightVibrantColor(Color.BLUE);
                //柔和
                int mutedColor = palette.getMutedColor(Color.BLUE);
                //柔和
                int vibrantColor = palette.getVibrantColor(Color.BLUE);
                //获取某种特性颜色的样品
                Palette.Swatch lightSwathc = palette.getLightVibrantSwatch();
                int backgroundColor = lightSwathc.getRgb();
                int txtColor = lightSwathc.getTitleTextColor();
                int mBackgroundColor = getTranslucentColor(0.5f, backgroundColor);

                mTxt.setBackgroundColor(mBackgroundColor);
                mTxt.setTextColor(txtColor);
                mTxt.setText("今天天气真好，应该去旅游");

            }
        });

    }

    public int getTranslucentColor(float percent, int color) {

        int blue = color & 0xff;
        int green = color >> 8 & 0xff;
        int red = color >> 16 & 0xff;
        int alpha = color >>> 24;

//        int blue = Color.blue(color);
        alpha = Math.round(alpha * percent);
        int aColor = Color.argb(alpha, red, green, blue);
        return aColor;
    }

}
