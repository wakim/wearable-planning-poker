package br.com.planning.poker.wear.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.planning.poker.wear.R;
import br.com.planning.poker.wear.app.adapter.SettingsAdapter;

/**
 * Created by wakim on 20/07/14.
 */
public class SettingsFragment extends SemiTransparentDialogFragment implements WearableListView.ClickListener {

	public static final int[] sOptions = {R.string.send_data, R.string.get_data, R.string.change_deck};
	public static final int[] sIcons = {R.drawable.ic_file_upload, R.drawable.ic_file_download, R.drawable.ic_view_module};

	SettingsCallback mCallback;

	public SettingsFragment() {
		setShowsDialog(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_settings, null);

		WearableListView list = (WearableListView) view.findViewById(R.id.fs_wearable_listview);

		SettingsAdapter adapter = new SettingsAdapter(getActivity());

		adapter.setIcon(sIcons);
		adapter.setOptionsText(sOptions);

		list.setClickListener(this);
		list.setAdapter(adapter);

		return view;
	}

	@Override
	public void onClick(WearableListView.ViewHolder viewHolder) {
		if(mCallback != null) {
			mCallback.onSettingClick(viewHolder.getPosition());
		}

		dismiss();
	}

	@Override
	public void onTopEmptyRegionClick() {}

	public void setSettingsCallback(SettingsCallback callback) {
		mCallback = callback;
	}

	public static interface SettingsCallback {
		public void onSettingClick(int optionResId);
	}
}
