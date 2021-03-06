package br.com.planning.poker.wear.app.adapter;

import android.content.Context;
import android.graphics.Point;
import android.support.wearable.view.GridPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import br.com.planning.poker.wear.app.preferences.PreferencesManager;

/**
 * Created by wakim on 19/07/14.
 */
public class CardsGridPagerAdapter extends GridPagerAdapter {

	List<String> mCards = new ArrayList<String>();
	CardViewHelper mHelper;
	View.OnLongClickListener mLongListener;

	public CardsGridPagerAdapter(Context context, List<String> cards, Point size) {
		int backgroundColor = PreferencesManager.getCardBackgroundColor(context),
			textColor = PreferencesManager.getCardTextColor(context);

		mCards = cards;
		mHelper = new CardViewHelper(size == null ? 0 : size.x, size == null ? 0 : size.y, backgroundColor, textColor);
		mHelper.setLayoutInflater(LayoutInflater.from(context));
	}

	public CardsGridPagerAdapter(Context context, List<String> cards) {
		this(context, cards, null);
	}

	public void setSize(Point size) {
		mHelper.setWidth(size.x);
		mHelper.setHeight(size.y);
	}

	public void setOnLongClickListener(View.OnLongClickListener longListener) {
		mLongListener = longListener;
	}

	@Override
	public int getRowCount() {
		return 1;
	}

	@Override
	public int getColumnCount(int i) {
		return mCards.size();
	}

	@Override
	protected Object instantiateItem(ViewGroup container, int row, int column) {
		String card = mCards.get(column);

		View view = mHelper.getView(card, column, null);

		view.setTag(card);

		if(mLongListener != null) {
			view.setOnLongClickListener(mLongListener);
		}

		container.addView(view);

		return view;
	}

	@Override
	protected void destroyItem(ViewGroup container, int row, int column, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object o) {
		return view == o;
	}

	public boolean hasSize() {
		return mHelper.getWidth() > 0 && mHelper.getHeight() > 0;
	}

	public void setCards(List<String> cards) {
		mCards = cards;
		notifyDataSetChanged();
	}

	public void onDestroy() {
		if(mCards != null) {
			mCards.clear();
		}

		mCards = null;
		mHelper.destroy();
		mHelper = null;

		mLongListener = null;
	}

	public void setCardBackgroundColor(int backgroundColor, boolean notify) {
		mHelper.setBackgroundColor(backgroundColor);

		if(notify) {
			notifyDataSetChanged();
		}
	}

	public void setCardTextColor(int textColor, boolean notify) {
		mHelper.setTextColor(textColor);

		if(notify) {
			notifyDataSetChanged();
		}
	}
}
