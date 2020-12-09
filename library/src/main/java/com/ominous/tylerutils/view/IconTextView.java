package com.ominous.tylerutils.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.ominous.tylerutils.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class IconTextView extends FrameLayout {
    private TextView textView;
    private ImageView imageView;

    public IconTextView(Context context) {
        this(context, null, 0);
    }

    public IconTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IconTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public IconTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_icontextview,this,true);

        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.icon);
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
