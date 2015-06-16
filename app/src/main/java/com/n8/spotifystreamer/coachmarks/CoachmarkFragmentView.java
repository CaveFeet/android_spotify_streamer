package com.n8.spotifystreamer.coachmarks;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.n8.spotifystreamer.BaseFragmentView;
import com.n8.spotifystreamer.R;
import com.n8.spotifystreamer.UiUtils;
import com.viewpagerindicator.LinePageIndicator;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Custom view to handle showing of other coachmark views in a viewpager
 */
public class CoachmarkFragmentView extends BaseFragmentView<CoachmarkController> {

    private static final int NUMBER_OF_COACHMARKS = 2;

    @InjectView(R.id.fragment_coachmark_viewPager)
    ViewPager mViewPager;

    @InjectView(R.id.fragment_coachmark_toolbar)
    Toolbar mToolbar;

    @InjectView(R.id.coachmark_3_got_it_button)
    Button mDoneButton;

    @InjectView(R.id.coachmark_3_show_again_checkBox)
    CheckBox mShowAgainCheckBox;

    @InjectView(R.id.fragment_coachmark_viewpager_indicator)
    LinePageIndicator mViewPagerIndicator;

    public CoachmarkFragmentView(Context context) {
        super(context);
    }

    public CoachmarkFragmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoachmarkFragmentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CoachmarkFragmentView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void setupView() {
        ButterKnife.inject(this);

        mDoneButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.onDoneClicked();
            }
        });

        mShowAgainCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mController.onShowCoachmarkCheckChanged(isChecked);
            }
        });

        mViewPager.setAdapter(new CoachmarkPagerAdapter());

        mViewPagerIndicator.setSelectedColor(mActivity.getResources().getColor(R.color.primary));
        mViewPagerIndicator.setLineWidth(UiUtils.dipsToPixels(15));
        mViewPagerIndicator.setViewPager(mViewPager);
    }

    private class CoachmarkPagerAdapter extends PagerAdapter {

        public Object instantiateItem(ViewGroup collection, int position) {
            View view = null;
            LayoutInflater layoutInflater = mActivity.getLayoutInflater();

            if (position == 0) {
                view = layoutInflater.inflate(R.layout.coachmark_search, collection, false);
            }else if (position == 1) {
                view = layoutInflater.inflate(R.layout.coachmark_results, collection, false);
            }

            ((ViewPager) collection).addView(view,position);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            ((ViewPager) collection).removeView((View)view);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return NUMBER_OF_COACHMARKS;
        }

        @Override
        public boolean isViewFromObject(View view, Object arg1) {
            return view == ((View) arg1);
        }
    }
}
