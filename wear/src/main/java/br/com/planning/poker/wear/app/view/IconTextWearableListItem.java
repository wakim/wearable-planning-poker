package br.com.planning.poker.wear.app.view;

import android.content.Context;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import br.com.planning.poker.wear.app.R;

/**
 * Created by wakim on 21/07/14.
 */
public class IconTextWearableListItem extends LinearLayout implements WearableListView.Item {

	CircledImageView image;
	TextView text;

	float mMinProximityValue, mMaxProximityValue;

	public IconTextWearableListItem(Context context) {
		super(context);

		init(context);
	}

	public IconTextWearableListItem(Context context, AttributeSet attrs) {
		super(context, attrs);

		init(context);
	}

	public IconTextWearableListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init(context);
	}

	void init(Context context) {
		mMinProximityValue = context.getResources().getDimension(R.dimen.min_proximity_value);
		mMaxProximityValue = context.getResources().getDimension(R.dimen.max_proximity_value);
	}

	public void populate(int iconResId, int textResId) {
		getImage().setImageResource(iconResId);
		getText().setText(textResId);
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
		getText().setAlpha(1f);

		getImage().setCircleColor(getResources().getColor(R.color.blue));
	}

	@Override
	public void onScaleDownStart() {
		getImage().setAlpha(0.5f);
		getText().setAlpha(0.5f);

		getImage().setCircleColor(getResources().getColor(R.color.grey));
	}

	CircledImageView getImage() {
		if(image == null) {
			image = (CircledImageView) findViewById(R.id.circled_image_view);
		}

		return image;
	}

	TextView getText() {
		if(text == null) {
			text = (TextView) findViewById(R.id.text_view);
		}

		return text;
	}
}
