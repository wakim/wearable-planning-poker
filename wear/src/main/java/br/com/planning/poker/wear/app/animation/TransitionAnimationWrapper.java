package br.com.planning.poker.wear.app.animation;

import android.view.View;
import android.view.animation.Animation;

/**
 * Created by wakim on 14/05/14.
 */
public abstract class TransitionAnimationWrapper {

	protected Animation mShowAnimation, mHideAnimation;

	boolean mPrimaryVisible = true;

	public void performShowAnimation(View primaryView, View secondaryView) {
		mPrimaryVisible = true;
	}

	public void performHideAnimation(View primaryView, View secondaryView) {
		mPrimaryVisible = false;
	}

	public void destroy() {
		mShowAnimation = mHideAnimation = null;
	}

	public Animation.AnimationListener getDefaultHideAnimationListener(final View primaryView, final View secondaryView) {
		return new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if(secondaryView != null) {
					secondaryView.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				if(secondaryView != null) {
					secondaryView.setVisibility(View.VISIBLE);
					secondaryView.bringToFront();
				}

				if(primaryView != null) {
					primaryView.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		};
	}

	public Animation.AnimationListener getDefaultShowAnimationListener(final View primaryView, final View secondaryView) {
		return new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if(secondaryView != null) {
					secondaryView.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if(primaryView != null) {
					primaryView.setVisibility(View.VISIBLE);
					primaryView.bringToFront();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		};
	}

	public boolean isPrimaryVisible() {
		return mPrimaryVisible;
	}

	public boolean isSecondaryVisible() {
		return ! mPrimaryVisible;
	}
}
