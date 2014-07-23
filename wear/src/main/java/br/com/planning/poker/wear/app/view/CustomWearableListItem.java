package br.com.planning.poker.wear.app.view;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.com.planning.poker.wear.app.R;

/**
 * Created by wakim on 21/07/14.
 */
public class CustomWearableListItem extends LinearLayout implements WearableListView.Item {

	TextView text1, text2;
	CircledImageView image;

	float mMinProximityValue = 1f, mMaxProximityValue = 1.25f;

	public CustomWearableListItem(Context context) {
		super(context);

		init(context);
	}

	public CustomWearableListItem(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public CustomWearableListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	void init(Context context) {
		mMinProximityValue = context.getResources().getDimension(R.dimen.min_proximity_value);
		mMaxProximityValue = context.getResources().getDimension(R.dimen.max_proximity_value);
	}

	public void populate(int iconResId, CharSequence text1, CharSequence text2) {
		getImage().setImageResource(iconResId);
		getText1().setText(text1);

		TextView textView2 = getText2();

		if(text2 == null) {
			textView2.setVisibility(View.GONE);
		} else {
			textView2.setVisibility(View.VISIBLE);
			textView2.setText(text2);
		}
	}

	@Override
	public float getProximityMinValue() {
		return mMinProximityValue;
	}

	@Override
	public float getProximityMaxValue() {
		return mMaxProximityValue;
	}

	@Override
	public float getCurrentProximityValue() {
		return getImage().getCircleRadius();
	}

	@Override
	public void setScalingAnimatorValue(float value) {
		getImage().setCircleRadius(value);
		getImage().setCircleRadiusPressed(value);
	}

	@Override
	public void onScaleUpStart() {
		getImage().setAlpha(1f);
		getText1().setAlpha(1f);
		getText2().setAlpha(1f);

		getImage().setCircleColor(getResources().getColor(R.color.green));
	}

	@Override
	public void onScaleDownStart() {
		getImage().setAlpha(0.5f);
		getText1().setAlpha(0.5f);
		getText2().setAlpha(0.5f);

		getImage().setCircleColor(getResources().getColor(R.color.grey));
	}

	CircledImageView getImage() {
		if(image == null) {
			image = (CircledImageView) findViewById(R.id.circled_image_view);
		}

		return image;
	}

	TextView getText1() {
		if(text1 == null) {
			text1 = (TextView) findViewById(R.id.text_view1);
		}

		return text1;
	}

	TextView getText2() {
		if(text2 == null) {
			text2 = (TextView) findViewById(R.id.text_view2);
		}

		return text2;
	}
}
