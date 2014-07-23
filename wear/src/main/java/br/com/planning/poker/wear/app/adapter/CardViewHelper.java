package br.com.planning.poker.wear.app.adapter;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.HashMap;

import br.com.planning.poker.wear.app.R;

/**
 * Created by Wakim on 12/03/14.
 */
public class CardViewHelper {

	int mWidth;
	int mHeight;
	int mBackgroundColor;
	int mTextColor;

	static HashMap<String, Float> mFontCache = new HashMap<String, Float>();
	Paint mPaint;

	LayoutInflater mInflater;

	public CardViewHelper() {
		mPaint = new Paint();
		mPaint.setTextAlign(Paint.Align.CENTER);
	}

	public CardViewHelper(int width, int height, int background, int textColor) {
		this();

		mWidth = width;
		mHeight = height;
		mBackgroundColor = background;
		mTextColor = textColor;
	}

	public View getView(String card, int position, View convertView) {
		TextView textView;
		View view;

		if(convertView == null) {
			view = mInflater.inflate(R.layout.card_template, null);
		} else {
			view = convertView;
		}

		CardHolder ch = (CardHolder) view.getTag(R.layout.card_template);

		if(ch == null) {
			ch = new CardHolder();

			ch.tv = (TextView) view.findViewById(R.id.ct_card);

			ch.tv.setTextColor(mTextColor);
			changeColor(mBackgroundColor, ch.tv.getBackground());

			view.setTag(R.layout.card_template, ch);
		}

		textView = ch.tv;

		view.setTag(R.id.ct_card, position);

		setCardTextSize(textView, card);

		textView.setText(card);
		textView.setWidth(mWidth);
		textView.setHeight(mHeight);

		return view;
	}

	public int getPosition(View view) {
		CardHolder ch = (CardHolder) view.getTag(R.layout.card_template);

		if(ch == null) {
			return -1;
		}

		return (Integer) ch.tv.getTag();
	}

	public void setCardTextSize(TextView textView, String cardLabel) {
		float measuredTextSize = measuredTextSize(cardLabel);

		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, measuredTextSize);
	}

	protected float calculateTextSize(String label) {
		int labelLength = (label == null ? 0 : label.length());
		int labelLength2 = labelLength / 2;

		float size = mWidth * 0.7f;

		return size / (labelLength2 == 1 ? labelLength : labelLength - 1);
	}

	protected float measuredTextSize(String cardLabel) {
		float textSize = (mWidth + mHeight) / 3f;
		float measuredWidth = 0;
		float fWidth = ((float) mWidth) * 0.8f;

		String cacheKey = (cardLabel + mWidth) + mHeight;

		if(cardLabel == null || cardLabel.isEmpty() || mWidth <= 0 || mHeight <= 0) {
			return textSize;
		}

		if(mFontCache.containsKey(cacheKey)) {
			textSize = mFontCache.get(cacheKey);
		}

		while((measuredWidth = getMeasuredWidth(cardLabel, textSize)) > fWidth) {
			textSize -= 0.9;
		}

		// Possivel Emoji
		if(measuredWidth == 0) {
			textSize = calculateTextSize(cardLabel);
		}

		// Faz cache do tamando para evitar recalculo
		mFontCache.put(cacheKey, textSize);

		return textSize;
	}

	protected float getMeasuredWidth(String label, float currentSize) {
		mPaint.setTextSize(currentSize);
		return mPaint.measureText(label, 0, label.length());
	}

	protected Paint getPaint() {
		if(mPaint == null) {
			mPaint = new Paint();
			mPaint.setTextAlign(Paint.Align.CENTER);
		}

		return mPaint;
	}

	public static void changeColor(int newColor, Drawable drawable) {
		changeColor(null, newColor, drawable);
	}

	public static void changeColor(Integer previousColor, int newColor, Drawable drawable) {
		if(drawable instanceof GradientDrawable) {
			GradientDrawable gDrawable = (GradientDrawable) drawable;
			gDrawable.setColor(newColor);
		} else if(drawable instanceof StateListDrawable) {
			StateListDrawable listDrawable = (StateListDrawable) drawable;

			DrawableContainer.DrawableContainerState drawableContainerState =
				(DrawableContainer.DrawableContainerState) listDrawable.getConstantState();

			int c = drawableContainerState.getChildCount();
			Drawable[] children = drawableContainerState.getChildren();

			for(int i = 0; i < c; ++i) {
				GradientDrawable child = (GradientDrawable) children[i];
				int[] states = child.getState();

				// Esta invertido e nao sei porque.
				if(states.length == 0) {
					child.setColor(adjustBrightness(newColor, 0.8f));
				} else {
					child.setColor(newColor);
				}
			}
		}
	}

	protected static int adjustBrightness(int color, float adjustment) {
		int alpha = (color & 0xFF000000);
		int r = (color & 0x00FF0000) >> 16;
		int g = (color & 0x0000FF00) >> 8;
		int b = (color & 0x000000FF);

		float rf = (float) r * adjustment;
		float rg = (float) g * adjustment;
		float rb = (float) b * adjustment;

		r = (int) rf;
		g = (int) rg;
		b = (int) rb;

		if(r > 0xFF) {
			r = 0xFF;
		} else if(r < 0) {
			r = 0;
		}

		if(g > 0xFF) {
			g = 0xFF;
		} else if(g < 0) {
			g = 0;
		}

		if(b > 0xFF) {
			b = 0xFF;
		} else if (b < 0) {
			b = 0;
		}

		return alpha | (r << 16) | (g << 8) | b;
	}

	public void setLayoutInflater(LayoutInflater layoutInflater) {
		mInflater = layoutInflater;
	}

	public CardViewHelper setHeight(int height) {
		mHeight = height;
		return this;
	}

	public CardViewHelper setWidth(int width) {
		mWidth = width;
		return this;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}

	public void setTextColor(int textColor) {
		this.mTextColor = textColor;
	}

	public void setBackgroundColor(int backgroundColor) {
		mBackgroundColor = backgroundColor;
	}

	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	private static class CardHolder {
		TextView tv;
	}

	public void destroy() {
		mPaint = null;
		mInflater = null;
	}
}