package br.com.planning.poker.wear.app.animation;

import android.content.Context;
import android.view.View;

/**
 * Created by wakim on 14/05/14.
 */
public class TransitionAnimationBuilder {
	private int mDuration;
	private boolean mFillAfter = false, mFillEnabled = false;
	private View mPrimaryView, mSecondaryView;

	private TransitionAnimationBuilder() {
	}

	public static TransitionAnimationBuilder init() {
		return new TransitionAnimationBuilder();
	}

	public TransitionAnimationBuilder to(View primaryView, View secondaryView) {
		mPrimaryView = primaryView;
		mSecondaryView = secondaryView;

		return this;
	}

	public TransitionAnimationBuilder during(int duration) {
		mDuration = duration;
		return this;
	}

	public TransitionAnimationBuilder fillingAfter() {
		mFillAfter = true;
		return this;
	}

	public TransitionAnimationBuilder enableFill() {
		mFillEnabled = true;
		return this;
	}

	public TransitionAnimationWrapper build(Context context) {
		TransitionAnimationWrapper wrapper = null;

		if(mPrimaryView == null) {
			return null;
		}

		wrapper = new FadeAnimationWrapper(mFillAfter, mFillEnabled, mDuration);

		mPrimaryView = mSecondaryView = null;

		return wrapper;
	}
}
