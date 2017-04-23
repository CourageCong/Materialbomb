package com.fc.materialbomb;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.fc.materialbomb.fragment.LeadFragment;
import com.fc.materialbomb.helper.LeadTransformer;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 引导页
 *
 * @author fucong
 * @version 1.0.0
 * @see ""
 */


public class LeadActivity extends BaseActivity {

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private int layoutId[] = new int[]{
            R.layout.fragment_lead1,
            R.layout.fragment_lead2,
            R.layout.fragment_lead3
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leak_activity);
        ButterKnife.bind(this);
        initViewPager();
    }

    private void initViewPager() {
        mViewPager.setAdapter(new LeadAdapter(getSupportFragmentManager()));
        mViewPager.setOffscreenPageLimit(3);

        LeadTransformer leadTransformer = new LeadTransformer();

        mViewPager.setPageTransformer(true,leadTransformer);// TODO: 2017/4/23 0023 写成false试试
        mViewPager.addOnPageChangeListener(leadTransformer);
    }

    class LeadAdapter extends FragmentPagerAdapter {


        public LeadAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            LeadFragment leadFragment = new LeadFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("layoutId", layoutId[position]);
            bundle.putInt("index", position);
            leadFragment.setArguments(bundle);

            return leadFragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }


}
