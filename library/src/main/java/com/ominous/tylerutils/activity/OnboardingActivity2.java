/*
 *     Copyright 2020 - 2022 Tyler Williamson
 *
 *     This file is part of TylerUtils.
 *
 *     TylerUtils is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     TylerUtils is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with TylerUtils.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ominous.tylerutils.activity;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.util.WindowUtils;

import java.util.ArrayList;
import java.util.List;

//TODO fix when canAdvance(true) changes to canAdvance(false)
public abstract class OnboardingActivity2 extends AppCompatActivity implements View.OnClickListener {
    private ViewPager2 viewPager;
    private ImageButton nextButton;
    private TextView finishButton;
    private LinearLayout indicators;
    private OnboardingPagerAdapter onboardingAdapter;
    private ViewPager2.OnPageChangeCallback viewPagerCallback;
    private List<OnboardingContainer> onboardingContainers;
    private List<ImageView> onboardingIndicators;

    private final OnBackPressedCallback viewPagerBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_onboarding);

        nextButton = findViewById(R.id.button_next);
        finishButton = findViewById(R.id.button_finish);
        viewPager = findViewById(R.id.container);
        indicators = findViewById(R.id.indicators);

        onboardingContainers = createOnboardingContainers();

        this.createIndicators();
        this.updateIndicators(0);

        onboardingAdapter = new OnboardingPagerAdapter(onboardingContainers);

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

                for (int i = 0; i <= position; i++) {
                    if (onboardingContainers.get(i).isInstantiated()) {
                        if (i == position) {
                            onboardingContainers.get(i).onPageSelected();
                        } else {
                            onboardingContainers.get(i).onPageDeselected();
                        }
                    }
                }

                nextButton.setVisibility(position != onboardingContainers.size() - 1 &&
                        onboardingContainers.get(position).isInstantiated() &&
                        onboardingContainers.get(position).canAdvanceToNextPage() ? View.VISIBLE : View.GONE);
                finishButton.setVisibility(position == (onboardingContainers.size() - 1) &&
                        onboardingContainers.get(position).isInstantiated() &&
                        onboardingContainers.get(position).canAdvanceToNextPage() ? View.VISIBLE : View.GONE);
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
    protected void onResume() {
        super.onResume();

        notifyViewPager();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        WindowUtils.setLightNavBar(getWindow(),
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.button_next) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        } else if (id == R.id.button_finish) {
            if (onboardingAdapter.onboardingContainers.get(viewPager.getCurrentItem()).canAdvanceToNextPage()) {
                for (OnboardingContainer onboardingContainer : onboardingContainers) {
                    onboardingContainer.onFinish();
                }

                this.onFinish();
                this.finish();
            }
        }
    }

    private void createIndicators() {
        int marginHalf = (int) getResources().getDimension(R.dimen.margin_half);

        onboardingIndicators = new ArrayList<>(onboardingContainers.size());

        for (int i = 0, l = onboardingContainers.size(); i < l; i++) {
            ImageView indicator = new ImageView(this);
            indicator.setBackgroundResource(R.drawable.indicator_selected);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(marginHalf, marginHalf);

            if (i + 1 < l) {
                if (Build.VERSION.SDK_INT >= 17) {
                    layoutParams.setMarginEnd(marginHalf);
                } else {
                    layoutParams.setMargins(0, 0, marginHalf, 0);
                }
            }

            onboardingIndicators.add(indicator);
            indicators.addView(indicator, layoutParams);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0, l = onboardingIndicators.size(); i < l; i++) {
            int indicatorColor = ContextCompat.getColor(this, i == position ? R.color.text_primary_emphasis : R.color.text_primary_disabled);

            if (Build.VERSION.SDK_INT >= 21) {
                onboardingIndicators.get(i)
                        .setBackgroundTintList(
                                ColorStateList.valueOf(indicatorColor));
            } else {
                onboardingIndicators.get(i)
                        .getBackground().setColorFilter(
                                new PorterDuffColorFilter(indicatorColor, PorterDuff.Mode.SRC_IN));
            }
        }
    }

    public abstract void onFinish();

    public void notifyViewPager() {
        viewPagerCallback.onPageSelected(viewPager.getCurrentItem());
    }

    public void setCurrentPage(int page) {
        setCurrentPage(page, true);
    }

    public void setCurrentPage(int page, boolean smoothScroll) {
        viewPager.setCurrentItem(page, smoothScroll);
    }

    public abstract List<OnboardingContainer> createOnboardingContainers();

    public List<OnboardingContainer> getOnboardingContainers() {
        return onboardingContainers;
    }

    public abstract static class OnboardingContainer {
        private final Context context;
        private boolean isInstantiated = false;

        public OnboardingContainer(Context context) {
            this.context = context;
        }

        public Context getContext() {
            return context;
        }

        public abstract int getViewRes();

        public abstract boolean canAdvanceToNextPage();

        public void onSaveInstanceState(@NonNull Bundle outState) {
        }

        public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        }

        public void onPageSelected() {
        }

        public void onPageDeselected() {
        }

        public void onCreateView(View v) {
        }

        public void onBindView(View v) {
        }

        public void onFinish() {
        }

        protected boolean isInstantiated() {
            return isInstantiated;
        }

        protected void setInstantiated() {
            isInstantiated = true;
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        for (int i = 0, l = onboardingContainers.size(); i < l; i++) {
            Bundle bundle = savedInstanceState.getBundle(Integer.toString(i));

            if (bundle != null && onboardingContainers.get(i).isInstantiated()) {
                onboardingContainers.get(i).onRestoreInstanceState(bundle);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        for (int i = 0, l = onboardingContainers.size(); i < l; i++) {
            if (onboardingContainers.get(i).isInstantiated()) {
                Bundle bundle = new Bundle();

                onboardingContainers.get(i).onSaveInstanceState(bundle);

                outState.putBundle(Integer.toString(i), bundle);
            }
        }
    }

    private static class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingViewHolder> {
        private final List<OnboardingContainer> onboardingContainers;

        public OnboardingPagerAdapter(List<OnboardingContainer> onboardingContainers) {
            this.onboardingContainers = onboardingContainers;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(onboardingContainers.get(viewType).getViewRes(), parent, false);

            onboardingContainers.get(viewType).onCreateView(v);
            onboardingContainers.get(viewType).setInstantiated();

            return new OnboardingViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull OnboardingViewHolder holder, int position) {
            onboardingContainers.get(position).onBindView(holder.itemView);
        }

        @Override
        public int getItemCount() {
            int count = 1;

            for (int i = 0, l = onboardingContainers.size(); i < l; i++) {
                if (onboardingContainers.get(i).isInstantiated() &&
                        onboardingContainers.get(i).canAdvanceToNextPage()) {
                    count++;
                }
            }

            return Math.min(count, onboardingContainers.size());
        }
    }

    private static class OnboardingViewHolder extends RecyclerView.ViewHolder {
        public OnboardingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
