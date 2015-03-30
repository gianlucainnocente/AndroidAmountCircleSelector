package com.gatedev.amountcircleselector;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils {
	public static float dip2pixel(Context context, float dipValue) {
		Resources resources = context.getResources();
		float pixelValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
		return pixelValue;
	}

	public static float dip2pixel(Context context, int dipValue) {
		Resources resources = context.getResources();
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, resources.getDisplayMetrics());
	}
	
	public static float pixel2dip(Context context, int pixelValue) {
		Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    return (pixelValue / (metrics.densityDpi / 160f));
	}

	public static float pixel2dip(Context context, float pixelValue) {
		Resources resources = context.getResources();
	    DisplayMetrics metrics = resources.getDisplayMetrics();
	    return (pixelValue / (metrics.densityDpi / 160f));
	}
}
