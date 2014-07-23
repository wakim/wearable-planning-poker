package br.com.planning.poker.wear.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.planning.poker.wear.app.R;
import br.com.planning.poker.wear.app.application.Application;
import br.com.planning.poker.wear.app.fragment.CardsGalleryFragment;
import br.com.planning.poker.wear.app.fragment.DeckPickerFragment;
import br.com.planning.poker.wear.app.fragment.SettingsFragment;
import br.com.planning.poker.wear.app.nodes.WearManager;
import br.com.planning.poker.wear.app.preferences.PreferencesManager;
import br.com.planning.poker.wear.app.utils.Params;

public class MainActivity extends Activity
	implements CardsGalleryFragment.CardsGalleryCallback, SettingsFragment.SettingsCallback,
		DeckPickerFragment.DeckPicker, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, WearManager.WearMessageCallback {

	GoogleApiClient mGoogleApiClient;
	WearManager mWearManager;

	private static final String GET_STATE = "/get-state", SET_STATE = "/set-state",
		GET_STATE_RESPONSE = "/get-state-response", SET_STATE_RESPONSE = "/set-state-response";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final WatchViewStub stub = (WatchViewStub) findViewById(R.id.am_watch_view_stub);

		stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
			@Override
			public void onLayoutInflated(WatchViewStub stub) {
				CardsGalleryFragment gallery = attachFragmentIfNeeded();
				gallery.loadCards(loadDeck(PreferencesManager.getDeckSelectedValue(MainActivity.this)));
			}
		});

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		mGoogleApiClient.connect();
	}

	CardsGalleryFragment attachFragmentIfNeeded() {
		CardsGalleryFragment fragment = getCardsGallery();

		if(fragment == null) {
			fragment = new CardsGalleryFragment();

			fragment.setCardsGalleryCallback(this);

			getFragmentManager().beginTransaction().add(R.id.amc_frame_layout, fragment, getString(R.string.fragment_cards_gallery_tag)).commit();
		}

		return fragment;
	}

	CardsGalleryFragment getCardsGallery() {
		return (CardsGalleryFragment) getFragmentManager().findFragmentByTag(getString(R.string.fragment_cards_gallery_tag));
	}

	@Override
	public void onMoreClicked() {
		SettingsFragment sf = new SettingsFragment();

		sf.setSettingsCallback(this);
		sf.show(getFragmentManager(), getString(R.string.fragment_settings_tag));
	}

	@Override
	public void onDeckClick(int position) {
		CardsGalleryFragment gallery = getCardsGallery();
		String deckName = PreferencesManager.getDeckNameByIndex(MainActivity.this, position);

		PreferencesManager.setDeck(deckName, this);

		gallery.loadCards(loadDeck(deckName), true);
	}

	List<String> loadDeck(String deckName) {
		if("deck_custom".equals(deckName)) {
			return new ArrayList<String>();
		} else {
			return PreferencesManager.getDeckByNameAsArrayList(this, deckName);
		}
	}

	@Override
	public void onSettingClick(int position) {
		int resId = SettingsFragment.sOptions[position];

		switch(resId) {
			case R.string.get_data:
				getData();
			break;
			case R.string.send_data:
				sendData();
			break;
			case R.string.change_deck:
				showDeckPicker();
			break;
		}
	}

	void sendData() {
		sendMessage(SET_STATE, obtainState());
	}

	void getData() {
		sendMessage(GET_STATE, null);
		// TODO Show progress indicator
	}

	public void sendMessage(String path, JSONObject data) {
		mWearManager.broadcastMessage(mGoogleApiClient, path, data);
	}

	public JSONObject obtainState() {
		CardsGalleryFragment gallery = getCardsGallery();
		JSONObject json = new JSONObject();

		try {
			json.put(Params.DECK_NAME, PreferencesManager.getDeckSelectedValue(this));
			json.put(Params.CURRENT_CARD, gallery.getSelectedCard());
			json.put(Params.CARD_VISIBILITY, gallery.isCardVisible());
		} catch(JSONException e){}

		return json;
	}

	void showDeckPicker() {
		DeckPickerFragment df = new DeckPickerFragment();

		df.setDeckPickerCallback(this);
		df.show(getFragmentManager(), getString(R.string.fragment_deckpicker_tag));
	}

	@Override
	protected void onStop() {
		if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {

			if(mWearManager != null) {
				Wearable.NodeApi.removeListener(mGoogleApiClient, mWearManager);
				Wearable.MessageApi.removeListener(mGoogleApiClient, mWearManager);
			}

			mGoogleApiClient.disconnect();
		}

		super.onStop();
	}

	@Override
	protected void onStart() {
		super.onStart();

		mGoogleApiClient.connect();
	}

	@Override
	public void onConnected(Bundle bundle) {
		mWearManager = new WearManager(mGoogleApiClient);
		mWearManager.setWearMessageCallback(this);

		Wearable.NodeApi.addListener(mGoogleApiClient, mWearManager);
		Wearable.MessageApi.addListener(mGoogleApiClient, mWearManager);
	}

	@Override
	public void onConnectionSuspended(int i) {
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Log.i(Application.TAG, "Connection Failed!");
	}

	@Override
	public void onMessageReceived(String path, String nodeId, JSONObject json) {
		if(SET_STATE_RESPONSE.equals(path)) {
			// TODO
		} else if(GET_STATE_RESPONSE.equals(path)) {
			// TODO
		}
	}

	@Override
	public void onMessageSendError(String path) {
		// TODO
	}
}
