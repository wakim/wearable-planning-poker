package br.com.planning.poker.wear.app.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.GridViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.List;

import br.com.planning.poker.wear.R;
import br.com.planning.poker.wear.app.adapter.CardViewHelper;
import br.com.planning.poker.wear.app.adapter.CardsGridPagerAdapter;
import br.com.planning.poker.wear.app.animation.TransitionAnimationBuilder;
import br.com.planning.poker.wear.app.animation.TransitionAnimationWrapper;
import br.com.planning.poker.wear.app.preferences.PreferencesManager;
import br.com.planning.poker.wear.app.utils.DisplayHelper;

/**
 * Created by wakim on 19/07/14.
 */
public class CardsGalleryFragment extends Fragment implements View.OnClickListener {

	CardsGridPagerAdapter mAdapter;
	GridViewPager mGridViewPager;
	TextView mHiddenCard;
	CircledImageView mHideButton, mShowButton;

	DismissOverlayView mDismissOverlayView;

	List<String> mCards;
	TransitionAnimationWrapper mAnimationWrapper;

	CardsGalleryCallback mCallback;

	View.OnLongClickListener mDismissListener;

	public CardsGalleryFragment() {}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_cards_gallery, null);

		mGridViewPager = (GridViewPager) view.findViewById(R.id.fcg_grid_view_pager);
		mHiddenCard = (TextView) view.findViewById(R.id.fcg_hidden_card);

		mHideButton = (CircledImageView) view.findViewById(R.id.fcg_button_hide);
		mShowButton = (CircledImageView) view.findViewById(R.id.fcg_button_show);

		mHideButton.setOnClickListener(this);
		mShowButton.setOnClickListener(this);

		view.findViewById(R.id.fcg_button_more).setOnClickListener(this);

		mDismissOverlayView = (DismissOverlayView) view.findViewById(R.id.dismiss_overlay);

		view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				setScaledSize();
				view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});

		mDismissListener = new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mDismissOverlayView.show();
				return true;
			}
		};

		view.setOnLongClickListener(mDismissListener);
		mGridViewPager.setOnLongClickListener(mDismissListener);

		mHiddenCard.setTextColor(PreferencesManager.getCardTextColor(getActivity()));
		CardViewHelper.changeColor(PreferencesManager.getCardBackgroundColor(getActivity()), mHiddenCard.getBackground());

		mDismissOverlayView.setIntroText(R.string.dismiss_overlay_intro);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroyView();

		mAdapter.onDestroy();

		mAdapter = null;
		mGridViewPager = null;
		mCards = null;
		mHideButton = null;
		mShowButton = null;

		mCallback = null;

		mDismissListener = null;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		tryInstantiateAdapter();
	}

	public void loadCards(List<String> cards, boolean animated) {

		if(animated) {
			mGridViewPager.setAlpha(0f);

			loadCards(cards);

			mGridViewPager.animate().alpha(1f).setDuration(300l).start();
		} else {
			loadCards(cards);
		}
	}

	public void loadCards(List<String> cards) {

		mCards = cards;

		if(getActivity() == null) {
			return;
		}

		tryInstantiateAdapter();
	}

	void tryInstantiateAdapter() {

		if(mGridViewPager == null || mCards == null) {
			return;
		}

		if(mAdapter != null) {
			mAdapter.setCards(mCards);
		} else {
			mAdapter = new CardsGridPagerAdapter(getActivity(), mCards);
			mAdapter.setOnLongClickListener(mDismissListener);
			mGridViewPager.setAdapter(mAdapter);
		}

		mCards = null;
	}

	void setScaledSize() {
		Point scaledSize = getScaledSize();

		mAdapter.setSize(scaledSize);
		mAdapter.notifyDataSetChanged();

		setHiddenCardSize(scaledSize);

		buildAnimationWrapper();
	}

	void setHiddenCardSize(Point size) {
		ViewGroup.LayoutParams lp = mHiddenCard.getLayoutParams();

		lp.width = size.x;
		lp.height = size.y;

		mHiddenCard.setMaxWidth(size.x);
		mHiddenCard.setMaxHeight(size.y);

		mHiddenCard.setLayoutParams(lp);
	}

	void buildAnimationWrapper() {

		if(mAnimationWrapper != null) {
			mAnimationWrapper.destroy();
			mAnimationWrapper = null;
		}

		if(! mAdapter.hasSize()) {
			return;
		}

		mAnimationWrapper = TransitionAnimationBuilder
				.init()
				.to(mGridViewPager, mHiddenCard)
				.during(300)
				.enableFill()
				.fillingAfter()
				.build(getActivity());
	}

	Point getScaledSize() {
		Point windowSize = DisplayHelper.getWindowSize(getActivity());

		if(mGridViewPager != null) {
			windowSize = DisplayHelper.scaleWithAspectRatio(windowSize.x, windowSize.y, mGridViewPager.getWidth(), mGridViewPager.getHeight());
		}

		float fX = (float) windowSize.x, fY = (float) windowSize.y;

		fX *= 0.7;
		fY *= 0.85;

		windowSize.x = (int) fX;
		windowSize.y = (int) fY;

		return windowSize;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		if(id == R.id.fcg_button_hide) {
			handleCardVisibilityChange(false);
		} else if(id == R.id.fcg_button_show) {
			handleCardVisibilityChange(true);
		} else if(id == R.id.fcg_button_more) {
			if(mCallback != null) {
				mCallback.onMoreClicked();
			}
		}
	}

	void handleCardVisibilityChange(boolean show) {
		if(show) {
			showCard();

			mShowButton.setVisibility(View.GONE);
			mHideButton.setVisibility(View.VISIBLE);
		} else {
			hideCard();

			mShowButton.setVisibility(View.VISIBLE);
			mHideButton.setVisibility(View.GONE);
		}
	}

	void showCard() {
		mAnimationWrapper.performShowAnimation(mGridViewPager, mHiddenCard);
	}

	void hideCard() {
		mAnimationWrapper.performHideAnimation(mGridViewPager, mHiddenCard);
	}

	public void setCardsGalleryCallback(CardsGalleryCallback callback) {
		mCallback = callback;
	}

	public Integer getSelectedCard() {
		if(mGridViewPager != null) {
			return mGridViewPager.getCurrentItem().x;
		}

		return null;
	}

	public boolean isCardVisible() {
		if(mAnimationWrapper != null) {
			return mAnimationWrapper.isPrimaryVisible();
		}

		return true;
	}

	public void setCurrentCard(Integer selectedCard) {
		if(mGridViewPager != null) {
			mGridViewPager.setCurrentItem(1, selectedCard, true);
		}
	}

	public void setCardBackgroundColor(int backgroundColor, boolean notify) {
		mAdapter.setCardBackgroundColor(backgroundColor, notify);

		CardViewHelper.changeColor(backgroundColor, mHiddenCard.getBackground());
	}

	public void setCardTextColor(int textColor, boolean notify) {
		mAdapter.setCardTextColor(textColor, notify);

		mHiddenCard.setTextColor(textColor);
	}

	public void changeCardVisibility(boolean visibility) {
		if(visibility == mAnimationWrapper.isPrimaryVisible()) {
			return;
		}

		handleCardVisibilityChange(visibility);
	}

	public static interface CardsGalleryCallback {
		void onMoreClicked();
	}
}
