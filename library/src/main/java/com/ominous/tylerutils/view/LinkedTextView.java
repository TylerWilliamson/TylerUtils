/*
 *     Copyright 2020 - 2021 Tyler Williamson
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

package com.ominous.tylerutils.view;

import android.content.Context;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

import com.ominous.tylerutils.R;
import com.ominous.tylerutils.browser.CustomTabs;
import com.ominous.tylerutils.util.ColorUtils;

public class LinkedTextView extends AppCompatTextView {
    private CustomTabs customTabs;

    public LinkedTextView(Context context) {
        this(context, null, 0);
    }

    public LinkedTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkedTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (customTabs == null) {
            customTabs = CustomTabs.getInstance(getContext());
        }

        setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        SpannedString currentText = new SpannedString(text);
        SpannableString newText = new SpannableString(currentText.toString());

        for (URLSpan span : currentText.getSpans(0,currentText.length(),URLSpan.class)) {
            if (customTabs == null) {
                customTabs = CustomTabs.getInstance(getContext());
            }

            newText.setSpan(new CustomTabsURLSpan(customTabs, span.getURL()),currentText.getSpanStart(span),currentText.getSpanEnd(span), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        super.setText(newText, type);
    }

    private class CustomTabsURLSpan extends URLSpan {
        private final CustomTabs customTabs;
        private final Uri uri;

        CustomTabsURLSpan(CustomTabs customTabs, String url) {
            super(url);

            uri = Uri.parse(url);

            this.customTabs = customTabs;

            customTabs.addLikelyUris(uri);
        }

        @Override
        public void onClick(View widget) {
            customTabs.launch(getContext(), uri);
        }
    }
}