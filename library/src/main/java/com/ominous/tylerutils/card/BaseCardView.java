package com.ominous.tylerutils.card;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.cardview.widget.CardView;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.listener.RippleTouchListener;

public abstract class BaseCardView extends CardView implements View.OnClickListener {
    public BaseCardView(Context context) {
        this(context, null, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, R.style.AppTheme_Card);

        this.setOnClickListener(this);
        this.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (Build.VERSION.SDK_INT >= 21) {
            this.setForeground(new RippleDrawable(ColorStateList.valueOf(getResources().getColor(R.color.card_background_pressed)), getForeground(), getBackground()));
            this.setOnTouchListener(new RippleTouchListener());
        }
    }
}
