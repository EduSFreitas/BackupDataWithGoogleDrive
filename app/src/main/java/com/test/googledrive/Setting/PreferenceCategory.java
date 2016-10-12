package com.test.googledrive.Setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by ANDT on 8/19/2016.
 */
public class PreferenceCategory extends android.preference.PreferenceCategory {
    public PreferenceCategory(Context context) {
        super(context);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        final View view = super.onCreateView(parent);
        final TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setAllCaps(true);
        return view;
    }
}
