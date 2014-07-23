package br.com.planning.poker.wear.app.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.planning.poker.wear.app.R;
import br.com.planning.poker.wear.app.adapter.DeckAdapter;

/**
 * Created by wakim on 20/07/14.
 */
public class DeckPickerFragment extends SemiTransparentDialogFragment implements WearableListView.ClickListener {

	DeckPicker mCallback;

	public DeckPickerFragment() {
		setShowsDialog(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_deckpicker, null);

		WearableListView list = (WearableListView) view.findViewById(R.id.fd_wearable_listview);

		list.setClickListener(this);
		list.setAdapter(new DeckAdapter(getActivity()));

		return view;
	}

	@Override
	public void onClick(WearableListView.ViewHolder viewHolder) {
		if(mCallback != null) {
			mCallback.onDeckClick(viewHolder.getPosition());
		}

		dismiss();
	}

	@Override
	public void onTopEmptyRegionClick() {}

	public void setDeckPickerCallback(DeckPicker callback) {
		mCallback = callback;
	}

	public static interface DeckPicker {
		public void onDeckClick(int position);
	}
}
