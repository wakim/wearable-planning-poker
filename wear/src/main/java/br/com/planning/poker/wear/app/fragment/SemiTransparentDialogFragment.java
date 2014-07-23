package br.com.planning.poker.wear.app.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

/**
 * Created by wakim on 22/07/14.
 */
public class SemiTransparentDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog d = super.onCreateDialog(savedInstanceState);

		Drawable drawable = new ColorDrawable(Color.WHITE);

		drawable.setAlpha(multiply(drawable.getAlpha(), 0.75f));

		d.getWindow().setBackgroundDrawable(drawable);

		return d;
	}

	int multiply(int value, float multiplier) {
		float fValue = (float) value;

		return (int) (fValue * multiplier);
	}
}
