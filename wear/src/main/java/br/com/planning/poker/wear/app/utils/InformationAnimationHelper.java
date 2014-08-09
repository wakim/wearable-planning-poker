package br.com.planning.poker.wear.app.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.wearable.activity.ConfirmationActivity;

import br.com.planning.poker.wear.R;

/**
 * Created by wakim on 07/08/14.
 */
public abstract class InformationAnimationHelper {

	public static enum InformationType {
		OPEN_ON_PHONE(ConfirmationActivity.OPEN_ON_PHONE_ANIMATION), SUCCESS(ConfirmationActivity.SUCCESS_ANIMATION), FAIULRE(ConfirmationActivity.FAILURE_ANIMATION);

		int flag;

		InformationType(int flag) {
			this.flag = flag;
		}

		public int getFlag() {
			return flag;
		}
	}

	public static void showAnimation(Context context, InformationType type) {
		showAnimation(context, null, type);
	}

	public static void showAnimation(Context context, @StringRes Integer stringResId, InformationType type) {
		Intent confirmationActivity = new Intent(context, ConfirmationActivity.class)
				.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION)
				.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, type.getFlag());

		if(stringResId != null) {
			confirmationActivity.putExtra(ConfirmationActivity.EXTRA_MESSAGE, context.getString(stringResId));
		}

		context.startActivity(confirmationActivity);
	}
}
