/*
 * Copyright 2020 - 2023 Tyler Williamson
 *
 * This file is part of TylerUtils.
 *
 * TylerUtils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TylerUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.activity;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ominous.tylerutils.R;

import java.util.ArrayList;
import java.util.List;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @Deprecated Please use OnboardingActivity2 instead
 */
public abstract class OnboardingActivity extends AppCompatActivity implements View.OnClickListener {

    //TODO fix when canAdvance(true) changes to canAdvance(false)

    private final List<FragmentContainer> fragmentContainers = new ArrayList<>();
    private ViewPager2 viewPager;
    private final OnBackPressedCallback viewPagerBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        }
    };
    private ImageButton nextButton;
    private TextView finishButton;
    private LinearLayout indicators;
    private OnboardingPagerAdapter onboardingAdapter;
    private ViewPager2.OnPageChangeCallback viewPagerCallback;

    protected abstract void addFragments();

    public void addFragment(Class<? extends OnboardingFragment> fragmentClass) {
        fragmentContainers.add(new FragmentContainer(fragmentClass));
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

        this.setContentView(R.layout.activity_onboarding);

        nextButton = findViewById(R.id.button_next);
        finishButton = findViewById(R.id.button_finish);
        viewPager = findViewById(R.id.container);
        indicators = findViewById(R.id.indicators);

        this.addFragments();
        this.createIndicators();
        this.updateIndicators(0);

        onboardingAdapter = new OnboardingPagerAdapter(this, fragmentContainers);

        viewPager.setAdapter(onboardingAdapter);
        viewPager.setPageTransformer(new MarginPageTransformer((int) getResources().getDimension(R.dimen.margin_standard)));

        viewPagerCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                viewPagerBackPressedCallback.setEnabled(position > 0);

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
        };

        viewPager.registerOnPageChangeCallback(viewPagerCallback);

        getOnBackPressedDispatcher().addCallback(viewPagerBackPressedCallback);

        nextButton.setOnClickListener(this);
        finishButton.setOnClickListener(this);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;

        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController wic = getWindow().getInsetsController();

            if (wic != null) {
                wic.setSystemBarsAppearance(
                        nightModeFlags == Configuration.UI_MODE_NIGHT_YES ? 0 :
                                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS |
                                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS |
                                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

            if (Build.VERSION.SDK_INT >= 26) {
                if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                    getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
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
        viewPagerCallback.onPageSelected(viewPager.getCurrentItem());
    }

    public void setCurrentPage(int page) {
        viewPager.setCurrentItem(page);
    }

    private static class FragmentContainer {
        final Class<? extends OnboardingFragment> fragmentClass;
        ImageView indicator;
        OnboardingFragment fragment;

        FragmentContainer(Class<? extends OnboardingFragment> fragmentClass) {
            this.fragmentClass = fragmentClass;
        }
    }

    @SuppressWarnings("EmptyMethod")
    public static abstract class OnboardingFragment extends Fragment {
        private boolean canAdvance = false;

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

        public void onPageSelected() { //TODO FragmentLifecycle https://stackoverflow.com/a/33363283

        }

        public void onPageDeselected() {

        }
    }

    private class OnboardingPagerAdapter extends FragmentStateAdapter {
        private final List<FragmentContainer> fragmentContainers;
        private final FragmentManager fragmentManager;

        //TODO fix when user tries to resume stopped app
        public OnboardingPagerAdapter(@NonNull FragmentActivity activity, List<FragmentContainer> fragmentContainers) {
            super(activity);

            this.fragmentContainers = fragmentContainers;
            this.fragmentManager = activity.getSupportFragmentManager();
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (fragmentContainers.get(position).fragment == null) {
                try {
                    fragmentContainers.get(position).fragment = (OnboardingFragment) fragmentManager.getFragmentFactory()
                            .instantiate(getClassLoader(), fragmentContainers.get(position).fragmentClass.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return fragmentContainers.get(position).fragment;
        }

        @Override
        public int getItemCount() {
            int count = 1;

            for (int i = 0, l = fragmentContainers.size(); i < l; i++) {
                if (fragmentContainers.get(i).fragment != null && fragmentContainers.get(i).fragment.canAdvanceToNextFragment()) {
                    count++;
                }
            }

            return Math.min(count, fragmentContainers.size());
        }
    }
}
