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
import br.com.planning.poker.wear.app.utils.InformationAnimationHelper;
import br.com.planning.poker.wear.app.utils.Params;

public class MainActivity extends Activity
	implements CardsGalleryFragment.CardsGalleryCallback, SettingsFragment.SettingsCallback,
		DeckPickerFragment.DeckPicker, GoogleApiClient.ConnectionCallbacks,
		GoogleApiClient.OnConnectionFailedListener, WearManager.WearMessageCallback {

	GoogleApiClient mGoogleApiClient;
	WearManager mWearManager;
	CardsGalleryFragment mFragment;

	private static final String GET_STATE = "/get-state", SET_STATE = "/set-state",
		GET_STATE_RESPONSE = "/get-state-response", SET_STATE_RESPONSE = "/set-state-response",
		APP_NOT_FOUND = "/app-not-found", APP_STARTED = "/app-started";

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

			mFragment = fragment;
		}

		return fragment;
	}

	CardsGalleryFragment getCardsGallery() {
		if(mFragment == null) {
			mFragment = (CardsGalleryFragment) getFragmentManager().findFragmentByTag(getString(R.string.fragment_cards_gallery_tag));
		}

		return mFragment;
	}

	SettingsFragment getSettings() {
		return (SettingsFragment) getFragmentManager().findFragmentByTag(getString(R.string.fragment_settings_tag));
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

		PreferencesManager.setDeck(this, deckName);

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
				getDataFromHandheld();
			break;
			case R.string.send_data:
				sendDataToHandheld();
			break;
			case R.string.change_deck:
				showDeckPicker();
			break;
		}
	}

	void sendDataToHandheld() {
		sendMessageToHandheld(SET_STATE, obtainCurrentState());
	}

	void getDataFromHandheld() {
		sendMessageToHandheld(GET_STATE, null);
	}

	public void sendMessageToHandheld(String path, JSONObject data) {
		try {
			mWearManager.broadcastMessage(mGoogleApiClient, path, data);
		} catch(WearManager.NodesNotFoundException e) {
			InformationAnimationHelper.showAnimation(this, R.string.no_devices_found, InformationAnimationHelper.InformationType.FAIULRE);
		}
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
		// TODO
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		InformationAnimationHelper.showAnimation(MainActivity.this, R.string.connection_error, InformationAnimationHelper.InformationType.FAIULRE);
	}

	@Override
	public void onMessageReceived(String path, String nodeId, JSONObject json) {
		if(SET_STATE_RESPONSE.equals(path)) {
			notifyStateSynchronized();
		} else if(GET_STATE_RESPONSE.equals(path)) {
			try {
				applyState(json);
			} catch(JSONException e) {}
		} else if(APP_NOT_FOUND.equals(path)) {
			InformationAnimationHelper.showAnimation(MainActivity.this, R.string.app_not_found, InformationAnimationHelper.InformationType.FAIULRE);
		} else if(APP_STARTED.equals(path)) {
			InformationAnimationHelper.showAnimation(MainActivity.this, R.string.app_started, InformationAnimationHelper.InformationType.OPEN_ON_PHONE);
		}
	}

	@Override
	public void onMessageSendError(String path) {
		InformationAnimationHelper.showAnimation(MainActivity.this, R.string.connection_error, InformationAnimationHelper.InformationType.FAIULRE);
	}

	void applyState(JSONObject json) throws JSONException {

		final String deckName = json.has(Params.DECK_NAME) ? json.getString(Params.DECK_NAME) : null;
		final String customDeck = json.has(Params.CUSTOM_DECK) ? json.getString(Params.CUSTOM_DECK) : null;
		final Integer backgroundColor = json.has(Params.BACKGROUND_COLOR) ? json.getInt(Params.BACKGROUND_COLOR) : null;
		final Integer textColor = json.has(Params.TEXT_COLOR) ? json.getInt(Params.TEXT_COLOR) : null;
		final Boolean visibility = json.has(Params.CARD_VISIBILITY) ? json.getBoolean(Params.CARD_VISIBILITY) : null;
		final Integer selectedCard = json.has(Params.CURRENT_CARD) ? json.getInt(Params.CURRENT_CARD) : null;

		final CardsGalleryFragment gallery = getCardsGallery();

		// Persist the deck on SharedPreferences
		PreferencesManager.setDeck(this, deckName);

		// Update Custom Deck on SharedPreferences
		if(customDeck != null) {
			PreferencesManager.setCustomDeck(this, customDeck);
		}

		// Change the selected card
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				// Change the Background Color and persist
				if(backgroundColor != null) {
					PreferencesManager.setCardBackgroundColor(MainActivity.this, backgroundColor);
					gallery.setCardBackgroundColor(backgroundColor, false);
				}

				// Change the Text Color and persist
				if(textColor != null) {
					PreferencesManager.setCardTextColor(MainActivity.this, textColor);
					gallery.setCardTextColor(textColor, false);
				}

				if(selectedCard != null) {
					gallery.setCurrentCard(selectedCard);
				}

				// Change card visibility
				if(visibility != null) {
					gallery.changeCardVisibility(visibility);
				}

				// Change Deck on Fragment
				if(deckName != null) {
					gallery.loadCards(loadDeck(deckName));
				}

				InformationAnimationHelper.showAnimation(MainActivity.this, R.string.state_synchronized, InformationAnimationHelper.InformationType.SUCCESS);
			}
		});
	}

	void notifyStateSynchronized() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				InformationAnimationHelper.showAnimation(MainActivity.this, R.string.state_synchronized, InformationAnimationHelper.InformationType.SUCCESS);
			}
		});
	}
}
