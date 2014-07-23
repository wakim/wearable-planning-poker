package br.com.planning.poker.wear.app.animation;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

/**
 * Created by wakim on 14/05/14.
 */
public class FadeAnimationWrapper extends TransitionAnimationWrapper {

	public FadeAnimationWrapper (boolean fillAfter, boolean fillEnabled, int duration) {
		mHideAnimation = createAnimation(1f, 0f, fillAfter, fillEnabled, duration);
		mShowAnimation = createAnimation(0f, 1f, fillAfter, fillEnabled, duration);
	}

	public AlphaAnimation createAnimation(float from, float to, boolean fillAfter, boolean fillEnabled, int duration) {
		AlphaAnimation alphaAnimation = new AlphaAnimation(from, to);

		alphaAnimation.setDuration(duration);
		alphaAnimation.setFillAfter(fillAfter);
		alphaAnimation.setFillEnabled(fillEnabled);
		alphaAnimation.setInterpolator(new LinearInterpolator());

		return alphaAnimation;
	}

	@Override
	public void performShowAnimation(View primaryView, View secondaryView) {
		super.performHideAnimation(primaryView, secondaryView);

		mShowAnimation.setAnimationListener(getDefaultShowAnimationListener(primaryView, secondaryView));

		if(primaryView != null) {
			primaryView.startAnimation(mShowAnimation);
		}
	}

	@Override
	public void performHideAnimation(View primaryView, View secondaryView) {
		super.performHideAnimation(primaryView, secondaryView)
		;
		mHideAnimation.setAnimationListener(getDefaultHideAnimationListener(primaryView, secondaryView));

		if(primaryView != null) {
			primaryView.startAnimation(mHideAnimation);
		}
	}

	@Override
	public Animation.AnimationListener getDefaultHideAnimationListener(final View primaryView, final View secondaryView) {
		return new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if(primaryView != null) {
					primaryView.setVisibility(View.VISIBLE);
					primaryView.bringToFront();
				}

				if(secondaryView != null) {
					secondaryView.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if(primaryView != null) {
					primaryView.setVisibility(View.GONE);
				}

				if(secondaryView != null) {
					secondaryView.bringToFront();
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		};
	}

	@Override
	public Animation.AnimationListener getDefaultShowAnimationListener(final View primaryView, final View secondaryView) {
		return new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				if(primaryView != null) {
					primaryView.bringToFront();
					primaryView.setVisibility(View.VISIBLE);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if(secondaryView != null) {
					secondaryView.setVisibility(View.GONE);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {}
		};
	}
}
