package com.ominous.tylerutils.activity;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.ominous.tylerutils.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public abstract class OnboardingActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    //TODO fix when canAdvance(true) changes to canAdvance(false)

    private ViewPager viewPager;
    private ImageButton nextButton;
    private TextView finishButton;
    private LinearLayout indicators;
    private OnboardingPagerAdapter onboardingAdapter;

    private List<FragmentContainer> fragmentContainers = new ArrayList<>();

    protected abstract void addFragments();

    public void addFragment(Class<? extends OnboardingFragment> fragmentClass) {
        fragmentContainers.add(new FragmentContainer(fragmentClass));
    }

    private class FragmentContainer {
        ImageView indicator;
        Class<? extends OnboardingFragment> fragmentClass;
        OnboardingFragment fragment;

        FragmentContainer(Class<? extends OnboardingFragment> fragmentClass) {
            this.fragmentClass = fragmentClass;
        }
    }

    public List<Fragment> getInstantiatedFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();

        for (FragmentContainer container : fragmentContainers) {
            fragments.add(container.fragment);
        }

        return fragments;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO WindowInsetsController for API 30
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= 26) {
            int nightModeFlags =
                    getResources().getConfiguration().uiMode &
                            Configuration.UI_MODE_NIGHT_MASK;
            switch (nightModeFlags) {
                case Configuration.UI_MODE_NIGHT_YES:
                    getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    break;

                case Configuration.UI_MODE_NIGHT_NO:
                case Configuration.UI_MODE_NIGHT_UNDEFINED:
                    getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    break;
            }
        }

        this.setContentView(R.layout.activity_onboarding);

        nextButton = findViewById(R.id.button_next);
        finishButton = findViewById(R.id.button_finish);
        viewPager = findViewById(R.id.container);
        indicators = findViewById(R.id.indicators);

        this.addFragments();
        this.createIndicators();
        this.updateIndicators(0);

        onboardingAdapter = new OnboardingPagerAdapter(getSupportFragmentManager(), fragmentContainers);

        viewPager.setAdapter(onboardingAdapter);
        viewPager.addOnPageChangeListener(this);
        viewPager.setPageMargin((int) getResources().getDimension(R.dimen.margin_standard));

        findViewById(R.id.button_next).setOnClickListener(this);
        findViewById(R.id.button_finish).setOnClickListener(this);
        findViewById(android.R.id.content).setBackgroundColor(getResources().getColor(R.color.background_primary));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_next) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        } else if (id == R.id.button_finish) {
            if (onboardingAdapter.fragmentContainers.get(viewPager.getCurrentItem()).fragment.canAdvanceToNextFragment()) {
                for (FragmentContainer fragmentContainer : fragmentContainers) {
                    fragmentContainer.fragment.onFinish();
                }

                this.onFinish();
                this.finish();
            }
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        updateIndicators(position);

        for (int i = 0, l = fragmentContainers.size(); i < l; i++) {
            if (fragmentContainers.get(i).fragment != null) {
                if (i == position) {
                    fragmentContainers.get(i).fragment.onPageSelected();
                } else {
                    fragmentContainers.get(i).fragment.onPageDeselected();
                }
            }
        }

        nextButton.setVisibility(position != fragmentContainers.size() - 1 && fragmentContainers.get(position).fragment != null && fragmentContainers.get(position).fragment.canAdvanceToNextFragment() ? View.VISIBLE : View.GONE);
        finishButton.setVisibility(position == (fragmentContainers.size() - 1) && fragmentContainers.get(position).fragment != null && fragmentContainers.get(position).fragment.canAdvanceToNextFragment() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private void createIndicators() {
        int marginHalf = (int) getResources().getDimension(R.dimen.margin_half);
        FragmentContainer fragmentContainer;


        for (int i = 0, l = fragmentContainers.size(); i < l; i++) {
            fragmentContainer = fragmentContainers.get(i);
            fragmentContainer.indicator = new ImageView(this);

            fragmentContainer.indicator.setBackgroundResource(R.drawable.indicator_selected);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginHalf, marginHalf);

            if (i + 1 < l) {
                if (Build.VERSION.SDK_INT >= 17) {
                    layoutParams.setMarginEnd(marginHalf);
                } else {
                    layoutParams.setMargins(0, 0, marginHalf, 0);
                }
            }

            indicators.addView(fragmentContainer.indicator, layoutParams);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0, l = fragmentContainers.size(); i < l; i++) {
            if (Build.VERSION.SDK_INT >= 21) {
                fragmentContainers.get(i).indicator.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, i == position ? R.color.text_primary_emphasis : R.color.text_primary_disabled)));
            } else {
                fragmentContainers.get(i).indicator.getBackground().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, i == position ? R.color.text_primary_emphasis : R.color.text_primary_disabled), PorterDuff.Mode.SRC_IN));
            }
        }
    }

    public abstract void onFinish();

    private void notifyViewPager() {
        onboardingAdapter.notifyDataSetChanged();

        onPageSelected(viewPager.getCurrentItem());
    }

    private class OnboardingPagerAdapter extends FragmentPagerAdapter {
        private FragmentManager fm;

        private final List<FragmentContainer> fragmentContainers;

        //TODO fix when user tries to resume stopped app
        public OnboardingPagerAdapter(@NonNull FragmentManager fm, List<FragmentContainer> fragmentContainers) {
            super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

            this.fragmentContainers = fragmentContainers;
            this.fm = fm;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            fragmentContainers.get(position).fragment = (OnboardingFragment) fm.getFragmentFactory().instantiate(getClassLoader(),fragmentContainers.get(position).fragmentClass.getName());
            return fragmentContainers.get(position).fragment;
        }

        @Override
        public int getCount() {
            int count = 1;

            for (int i = 0, l = fragmentContainers.size(); i < l; i++) {
                if (fragmentContainers.get(i).fragment != null && fragmentContainers.get(i).fragment.canAdvanceToNextFragment()) {
                    count++;
                }
            }

            return Math.min(count, fragmentContainers.size());
        }
    }

    public static abstract class OnboardingFragment extends Fragment {
        private boolean canAdvance = false;
        private WeakReference<FragmentActivity> activity;

        public void notifyViewPager(boolean canAdvance) {
            this.canAdvance = canAdvance;

            if (this.getActivity() != null) {
                ((OnboardingActivity) this.getActivity()).notifyViewPager();
            }
        }

        private boolean canAdvanceToNextFragment() {
            return canAdvance;
        }

        public abstract void onFinish();

        public FragmentActivity getFragmentActivity() {
            return activity.get();
        }

        @Override
        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);

            this.activity = new WeakReference<>(getActivity());
        }

        public void onPageSelected() { //TODO FragmentLifecycle https://stackoverflow.com/a/33363283

        }

        public void onPageDeselected() {

        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        } else {
            super.onBackPressed();
        }
    }
}
