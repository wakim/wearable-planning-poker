package br.com.planning.poker.wear.app.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import br.com.planning.poker.wear.R;
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

	boolean mPendingSynchronization = false;

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

		// Build client and connect to Google Play Services using Wearable.API
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

	SettingsFragment getSettings() {
		return (SettingsFragment) getFragmentManager().findFragmentByTag(getString(R.string.fragment_settings_tag));
	}

	@Override
	public void onMoreClicked() {
		SettingsFragment sf = new SettingsFragment();

		sf.setSettingsCallback(this);
		sf.setPendingSynchronization(mPendingSynchronization);
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
			return PreferencesManager.getCustomDeckAsArrayList(this);
		} else {
			return PreferencesManager.getDeckByNameAsArrayList(this, deckName);
		}
	}

	@Override
	public void onSettingClick(int position) {
		int resId = SettingsFragment.sOptions[position];

		switch(resId) {
			case R.string.get_data:
				getDataFromWearable();
			break;
			case R.string.send_data:
				sendDataToWearable();
			break;
			case R.string.change_deck:
				showDeckPicker();
			break;
		}
	}

	void sendDataToWearable() {
		sendMessageToWearable(SET_STATE, obtainCurrentState());
	}

	void getDataFromWearable() {
		sendMessageToWearable(GET_STATE, null);
	}

	public void sendMessageToWearable(String path, JSONObject data) {
		mPendingSynchronization = true;
		mWearManager.broadcastMessage(mGoogleApiClient, path, data);

		updatePendingSynchronizationState();
	}

	public JSONObject obtainCurrentState() {
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

		// Add one listener for Node Connection and Disconnection
		Wearable.NodeApi.addListener(mGoogleApiClient, mWearManager);
		Wearable.MessageApi.addListener(mGoogleApiClient, mWearManager);
	}

	@Override
	public void onConnectionSuspended(int i) {
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}

	@Override
	public void onMessageReceived(String path, String nodeId, JSONObject json) {
		mPendingSynchronization = false;

		if(SET_STATE_RESPONSE.equals(path)) {
			// TODO
		} else if(GET_STATE_RESPONSE.equals(path)) {
			try {
				applyState(json);
			} catch(JSONException e) {}
		}

		updatePendingSynchronizationState();
	}

	@Override
	public void onMessageSendError(String path) {
		// TODO
	}

	void updatePendingSynchronizationState() {
		SettingsFragment sf = getSettings();

		if(sf != null && sf.isVisible()) {
			sf.setPendingSynchronization(mPendingSynchronization);
		}
	}

	void applyState(JSONObject json) throws JSONException {
		String deckName = json.getString(Params.DECK_NAME);

		String customDeck = json.getString(Params.CUSTOM_DECK);

		// Avoid primitive problems (when an primitive value is not present, the default is returned =/)
		Integer backgroundColor = json.has(Params.BACKGROUND_COLOR) ? json.getInt(Params.BACKGROUND_COLOR) : null;
		Integer textColor = json.has(Params.TEXT_COLOR) ? json.getInt(Params.TEXT_COLOR) : null;
		Boolean visibility = json.has(Params.CARD_VISIBILITY) ? json.getBoolean(Params.CARD_VISIBILITY) : null;
		Integer selectedCard = json.has(Params.CURRENT_CARD) ? json.getInt(Params.CURRENT_CARD) : null;

		CardsGalleryFragment gallery = getCardsGallery();

		// Persist the deck on SharedPreferences
		PreferencesManager.setDeck(deckName, this);

		// Update Custom Deck on SharedPreferences
		if(customDeck != null) {
			PreferencesManager.setCustomDeck(this, customDeck);
		}

		// Change Deck on Fragment
		loadDeck(deckName);

		// Change the Background Color and persist
		if(backgroundColor != null) {
			PreferencesManager.setCardBackgroundColor(this, backgroundColor);
			gallery.setCardBackgroundColor(backgroundColor, false);
		}

		// Change the Text Color and persist
		if(textColor != null) {
			PreferencesManager.setCardTextColor(this, textColor);
			gallery.setCardTextColor(textColor, false);
		}

		// Change the selected card
		if(selectedCard != null) {
			gallery.setCurrentCard(selectedCard);
		}

		// Change card visibility
		if(visibility != null) {
			gallery.changeCardVisibility(visibility);
		}

		// TODO Notify user
	}
}
