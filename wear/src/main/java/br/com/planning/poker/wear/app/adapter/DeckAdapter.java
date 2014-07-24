package br.com.planning.poker.wear.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import br.com.planning.poker.wear.R;
import br.com.planning.poker.wear.app.preferences.PreferencesManager;
import br.com.planning.poker.wear.app.view.CustomWearableListItem;

/**
 * Created by Wakim on 18/03/14.
 */
public class DeckAdapter extends RecyclerView.Adapter {

	String[] mDecks;

	int mCheckedDeckIndex;

	LayoutInflater mInflater;

	public DeckAdapter(Context context) {
		mDecks = PreferencesManager.getDeckEntries(context);
		mCheckedDeckIndex = PreferencesManager.getDeckSelectedValueIndex(context);

		mInflater = LayoutInflater.from(context);
	}

	public void setCheckedDeckIndex(int checkedDeckIndex) {
		mCheckedDeckIndex = checkedDeckIndex;
		notifyDataSetChanged();
	}

	public int getCheckedDeckIndex() {
		return mCheckedDeckIndex;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		return new ViewHolder(mInflater.inflate(R.layout.item_deck, null));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		String[] split = mDecks[position].split(";");
		ViewHolder v = (ViewHolder) viewHolder;

		int iconResId = (mCheckedDeckIndex == position) ? R.drawable.ic_check_box_outline : R.drawable.ic_check_box_outline_blank;

		CustomWearableListItem item = v.getItem();

		item.populate(iconResId, split[0], split.length > 1 ? split[1] : null);
	}

	@Override
	public int getItemCount() {
		return mDecks.length;
	}

	public static class ViewHolder extends WearableListView.ViewHolder {

		public ViewHolder(View itemView) {
			super(itemView);
		}

		public CustomWearableListItem getItem() {
			return (CustomWearableListItem) itemView;
		}
	}
}
