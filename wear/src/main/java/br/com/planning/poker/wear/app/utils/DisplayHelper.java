package br.com.planning.poker.wear.app.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;

public class DisplayHelper {

	public static Point getAdjustedPortraitWindowSize(Activity activity) {
		Point windowSize = getPortraitWindowSize(activity);

		return windowSize;
	}

	public static Point getPortraitWindowSize(Activity activity) {
		Point cloneSize = getWindowSize(activity);
		int tmp = cloneSize.x;
		
		if(activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			cloneSize.x = cloneSize.y;
			cloneSize.y = tmp;
		}
		
		return cloneSize;
	}
	
	public static Point scaleWithAspectRatio(double sourceWidth, double sourceHeight, double destWidth, double destHeight) {
		Point size = new Point();

		double scaleHeight = destHeight / sourceHeight;
		double scaleWidth = destWidth / sourceWidth;
		double scale = scaleHeight > scaleWidth ? scaleWidth : scaleHeight;
		
		size.set((int) (sourceWidth * scale), (int) (sourceHeight * scale));
		
		return size;
	}

	@SuppressWarnings("deprecation")
    @TargetApi(13)
	public static Point getWindowSize(Activity context) {
		Display display = context.getWindowManager().getDefaultDisplay();

		if(Build.VERSION.SDK_INT >= 13) {
			Point p = new Point();
			display.getSize(p);

			return p;
		} else {
			return new Point(display.getWidth(), display.getHeight());
		}
	}
}
