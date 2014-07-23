package br.com.planning.poker.wear.app.view;

import android.content.Context;
import android.support.wearable.view.GridViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wakim on 20/07/14.
 */
public class CustomGridViewPager extends GridViewPager {

	public CustomGridViewPager(Context context) {
		super(context);
	}

	public CustomGridViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CustomGridViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		if(getVisibility() == View.VISIBLE) {
			return super.onTouchEvent(ev);
		}

		return false;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {

		if(getVisibility() == View.VISIBLE) {
			return super.onInterceptTouchEvent(event);
		}

		return false;
	}
}
