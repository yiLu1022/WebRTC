package com.netatmo.mbidon.pocwebrtc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by mbidon on 23/03/18.
 */

public class RatioLayout extends FrameLayout {

    public RatioLayout(@NonNull Context context) {
        super(context);
    }

    public RatioLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * 448 / 800;

        int finalWidth, finalHeight;

        if (calculatedHeight > originalHeight)
        {
            finalWidth = originalHeight * 800 / 448;
            finalHeight = originalHeight;
        }
        else
        {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }
}
