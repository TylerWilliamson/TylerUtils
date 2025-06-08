/*
 * Copyright 2020 - 2025 Tyler Williamson
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.anim.OpenCloseHandler;
import com.ominous.tylerutils.util.WindowUtils;

import java.util.ArrayList;
import java.util.List;

//TODO fix when canAdvance(true) changes to canAdvance(false)
public abstract class OnboardingActivity2 extends AppCompatActivity implements View.OnClickListener {
    private static final String KEY_VIEWPAGER_PAGE = "KEY_VIEWPAGER_PAGE";
    private CoordinatorLayout coordinatorLayout;
    private ConstraintLayout advancedMenuLayout;
    private ConstraintLayout onboardingMenuLayout;

    private ViewPager2 viewPager;
    private ImageButton nextButton;
    private TextView finishButton;
    private TextView advancedButton;
    private TextView advancedButtonClose;

    private LinearLayout indicators;
    private OnboardingPagerAdapter onboardingAdapter;
    private ViewPager2.OnPageChangeCallback viewPagerCallback;
    private List<OnboardingContainer> onboardingContainers;
    private OnboardingContainer advancedOnboardingContainer;
    private List<ImageView> onboardingIndicators;

    private OpenCloseHandler advancedMenuHandler;
    private ValueAnimator advancedMenuCloseAnimator;
    private ValueAnimator advancedMenuOpenAnimator;

    private final OnBackPressedCallback viewPagerBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            switch (advancedMenuHandler.getState()) {
                case OPEN:
                case OPENING:
                    closeAdvancedMenu();
                    break;
                case CLOSED:
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                    break;
                case CLOSING:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_onboarding);

        coordinatorLayout = findViewById(R.id.coordinator_layout);
        onboardingMenuLayout = findViewById(R.id.onboarding_menu_layout);
        advancedMenuLayout = findViewById(R.id.advanced_menu_layout);
        FrameLayout advancedMenuFrame = findViewById(R.id.advanced_menu_frame);

        nextButton = findViewById(R.id.button_next);
        finishButton = findViewById(R.id.button_finish);
        advancedButton = findViewById(R.id.button_advanced);
        advancedButtonClose = findViewById(R.id.button_advanced_close);

        viewPager = findViewById(R.id.container);
        indicators = findViewById(R.id.indicators);

        onboardingContainers = createOnboardingContainers();
        advancedOnboardingContainer = createAdvancedMenuOnboardingContainer();

        this.createIndicators();
        this.updateIndicators(0);

        onboardingAdapter = new OnboardingPagerAdapter(onboardingContainers, savedInstanceState == null ? 1 : savedInstanceState.getInt(KEY_VIEWPAGER_PAGE, 0) + 1);

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
                advancedButton.setVisibility(advancedOnboardingContainer != null &&
                        (position == onboardingContainers.size() - 1) &&
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

        if (savedInstanceState != null) {
            viewPager.setCurrentItem(savedInstanceState.getInt(KEY_VIEWPAGER_PAGE, 0), false);
        }

        WindowUtils.setLightNavBar(getWindow(),
                (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES);

        if (advancedOnboardingContainer == null) {
            advancedMenuLayout.setVisibility(View.GONE);
        } else {
            coordinatorLayout
                    .getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int coordinatorLayoutHeight = coordinatorLayout.getHeight();

                            advancedMenuLayout.setTranslationY(coordinatorLayoutHeight);

                            advancedMenuCloseAnimator = ValueAnimator.ofFloat(0, coordinatorLayoutHeight)
                                    .setDuration(500);
                            advancedMenuOpenAnimator = ValueAnimator.ofFloat(coordinatorLayoutHeight, 0)
                                    .setDuration(500);

                            ValueAnimator.AnimatorUpdateListener animatorUpdateListener = animation -> {
                                float translate = (Float) animation.getAnimatedValue();

                                onboardingMenuLayout.setTranslationY(translate - coordinatorLayoutHeight);
                                advancedMenuLayout.setTranslationY(translate);
                            };

                            advancedMenuCloseAnimator.addUpdateListener(animatorUpdateListener);
                            advancedMenuOpenAnimator.addUpdateListener(animatorUpdateListener);

                            advancedMenuCloseAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    onboardingMenuLayout.setTranslationY(0);
                                    advancedMenuLayout.setTranslationY(coordinatorLayoutHeight);

                                    advancedOnboardingContainer.onPageDeselected();
                                }
                            });

                            advancedMenuOpenAnimator.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    onboardingMenuLayout.setTranslationY(coordinatorLayoutHeight);
                                    advancedMenuLayout.setTranslationY(0);

                                    advancedOnboardingContainer.onPageSelected();
                                }
                            });

                            advancedMenuHandler = new OpenCloseHandler(
                                    advancedMenuOpenAnimator,
                                    advancedMenuCloseAnimator);

                            coordinatorLayout
                                    .getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        }
                    });

            View advancedMenuView = advancedOnboardingContainer.inflate(advancedMenuFrame);
            advancedMenuFrame.addView(advancedMenuView);
            advancedOnboardingContainer.onCreateView(advancedMenuView);
            advancedOnboardingContainer.onBindView(advancedMenuView);
            advancedOnboardingContainer.setInstantiated();

            advancedButton.setOnClickListener(v -> openAdvancedMenu());
            advancedButtonClose.setOnClickListener(v -> closeAdvancedMenu());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        notifyViewPager();
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

                advancedOnboardingContainer.onFinish();

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

    public void openAdvancedMenu() {
        openAdvancedMenu(false);
    }

    public void openAdvancedMenu(boolean now) {
        advancedMenuHandler.open(now);
    }

    public void closeAdvancedMenu() {
        closeAdvancedMenu(false);
    }

    public void closeAdvancedMenu(boolean now) {
        advancedMenuHandler.close(now);
    }

    public void setFinishButtonText(CharSequence text) {
        finishButton.setText(text);
    }

    public void setAdvancedButtonText(CharSequence text) {
        advancedButton.setText(text);
    }

    public void setNextButtonDescription(CharSequence description) {
        nextButton.setContentDescription(description);
    }

    public void setCloseButtonText(CharSequence text) {
        advancedButtonClose.setText(text);
    }

    public abstract List<OnboardingContainer> createOnboardingContainers();

    public List<OnboardingContainer> getOnboardingContainers() {
        return onboardingContainers;
    }

    public abstract OnboardingContainer createAdvancedMenuOnboardingContainer();

    public OnboardingContainer getAdvancedMenuOnboardingContainer() {
        return advancedOnboardingContainer;
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

        private View inflate(ViewGroup parent) {
            return LayoutInflater
                    .from(parent.getContext())
                    .inflate(getViewRes(), parent, false);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        viewPager.setCurrentItem(
                savedInstanceState.getInt(KEY_VIEWPAGER_PAGE, 0), false);

        for (int i = 0, l = onboardingContainers.size(); i < l; i++) {
            Bundle bundle = savedInstanceState.getBundle(Integer.toString(i));

            if (bundle != null && onboardingContainers.get(i).isInstantiated()) {
                onboardingContainers.get(i).onRestoreInstanceState(bundle);
            }
        }

        Bundle advancedBundle = savedInstanceState.getBundle("ADV");

        if (advancedBundle != null) {
            advancedOnboardingContainer.onRestoreInstanceState(advancedBundle);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_VIEWPAGER_PAGE, viewPager.getCurrentItem());

        for (int i = 0, l = onboardingContainers.size(); i < l; i++) {
            if (onboardingContainers.get(i).isInstantiated()) {
                Bundle bundle = new Bundle();

                onboardingContainers.get(i).onSaveInstanceState(bundle);

                outState.putBundle(Integer.toString(i), bundle);
            }
        }
        Bundle advancedMenuBundle = new Bundle();
        advancedOnboardingContainer.onSaveInstanceState(advancedMenuBundle);
        outState.putBundle("ADV", advancedMenuBundle);
    }

    private static class OnboardingPagerAdapter extends RecyclerView.Adapter<OnboardingViewHolder> {
        private final List<OnboardingContainer> onboardingContainers;
        private final int minSize;

        public OnboardingPagerAdapter(List<OnboardingContainer> onboardingContainers, int minSize) {
            this.onboardingContainers = onboardingContainers;
            this.minSize = minSize;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public OnboardingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = onboardingContainers.get(viewType).inflate(parent);

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
            int count = minSize;

            for (int i = minSize - 1, l = onboardingContainers.size(); i < l; i++) {
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
