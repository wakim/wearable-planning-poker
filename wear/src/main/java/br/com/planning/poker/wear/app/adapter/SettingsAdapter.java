package br.com.planning.poker.wear.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.planning.poker.wear.app.R;
import br.com.planning.poker.wear.app.view.IconTextWearableListItem;

/**
 * Created by wakim on 21/07/14.
 */
public class SettingsAdapter extends RecyclerView.Adapter {

	LayoutInflater mInflater;
	int[] mIcons, mOptionsText;

	public SettingsAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}

	public void setIcon(int[] icons) {
		mIcons = icons;
	}

	public void setOptionsText(int[] optionsText) {
		mOptionsText = optionsText;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		return new ViewHolder(mInflater.inflate(R.layout.item_setting, null));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		ViewHolder v = (ViewHolder) viewHolder;

		v.getItem().populate(mIcons[position], mOptionsText[position]);
	}

	@Override
	public int getItemCount() {
		return mOptionsText.length;
	}

	public static class ViewHolder extends WearableListView.ViewHolder {

		public ViewHolder(View itemView) {
			super(itemView);
		}

		public IconTextWearableListItem getItem() {
			return (IconTextWearableListItem) itemView;
		}
	}
}