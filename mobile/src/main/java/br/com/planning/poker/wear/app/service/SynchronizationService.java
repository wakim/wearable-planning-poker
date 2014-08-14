package br.com.planning.poker.wear.app.service;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import br.com.planning.poker.wear.app.application.Application;
import br.com.planning.poker.wear.app.utils.MulticastPendingResponse;
import br.com.planning.poker.wear.app.utils.Params;
import br.com.planning.poker.wear.app.utils.PendingResponse;
import br.com.planning.poker.wear.app.utils.SimplePendingResponse;

/**
 * Created by wakim on 22/07/14.
 */
public class SynchronizationService extends WearableListenerService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

	private static final String GET_STATE = "/get-state", SET_STATE = "/set-state",
			GET_STATE_RESPONSE = "/get-state-response", SET_STATE_RESPONSE = "/set-state-response",
			APP_NOT_FOUND = "/app-not-found", APP_STARTED = "/app-started", START_WEARABLE = "/start-wearable";

	public static final String AGILE_PLANNING_POKER_PACKAGE = "br.com.planning.poker";
	public static final String WEAR_PLANNING_POKER_PACKAGE = "br.com.planning.poker.wear";

	GoogleApiClient mGoogleApiClient;

	List<PendingResponse> mPendingResponses = new ArrayList<PendingResponse>();

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int flag = super.onStartCommand(intent, flags, startId);

		if(intent == null) {
			return flag;
		}

		String method = intent.hasExtra(Params.METHOD) ? intent.getStringExtra(Params.METHOD) : null;

		// Response from AGILE PLANNING POKER
		if(method == null) {
			stopSelf();
			return flag;
		}

		if(START_WEARABLE.equals(method)) {
			sendResponse(new MulticastPendingResponse(this, method));
		} else {
			sendResponseToNode(intent.getExtras());
		}

		return flag;
	}

	void connectedToGoogleServices() {
		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Wearable.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();

		mGoogleApiClient.connect();
	}

	@Override
	public void onDestroy() {

		if(mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}

		super.onDestroy();
	}

	@Override
	public void onConnected(Bundle bundle) {
		sendPendingResponses();
	}

	@Override
	public void onConnectionSuspended(int i) {}

	void sendResponseToNode(Bundle extras) {
		String method = extras.getString(Params.METHOD);
		String nodeId = extras.getString(Params.CUSTOM_PARAM);
		byte[] data;

		if(! SET_STATE_RESPONSE.equals(method) && ! GET_STATE_RESPONSE.equals(method)) {
			return;
		}

		JSONObject json = populateJSON(extras);
		data = JSONObjectToByteArray(json);

		sendResponse(new SimplePendingResponse(nodeId, method, data));
	}

	void doAppNotFoundResponse(String nodeId) {
		sendResponse(new SimplePendingResponse(nodeId, APP_NOT_FOUND, null));
	}

	void doAppStartedResponse(String nodeId) {
		sendResponse(new SimplePendingResponse(nodeId, APP_STARTED, null));
	}

	void sendResponse(PendingResponse pendingResponse) {
		if(mGoogleApiClient == null) {
			connectedToGoogleServices();
			mPendingResponses.add(pendingResponse);
			return;
		} else {
			if(mGoogleApiClient.isConnected() && ! mGoogleApiClient.isConnecting()) {
				pendingResponse.send(mGoogleApiClient);
			}
		}
	}

	void sendPendingResponses() {
		for(PendingResponse pr : mPendingResponses) {
			sendResponse(pr);
		}

		mPendingResponses.clear();
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		super.onMessageReceived(messageEvent);

		Bundle bundle = new Bundle();
		byte[] data = messageEvent.getData();
		String path = messageEvent.getPath();

		if(! GET_STATE.equals(path) && ! SET_STATE.equals(path)) {
			return;
		}

		JSONObject json = data == null ? null : byteArrayToJSONObject(data);

		if(json != null) {
			populateBundle(bundle, json);
		}

		bundle.putString(Params.METHOD, path);
		bundle.putString(Params.REMOTE_SERVICE_PACKAGE, getPackageName());
		bundle.putString(Params.REMOTE_SERVICE_CLASS, getClass().getPackage().getName().concat(".").concat(getClass().getSimpleName()));
		bundle.putString(Params.CUSTOM_PARAM, messageEvent.getSourceNodeId()); // Source from this message

		if(! startAgilePlanningPokerActivity(bundle, messageEvent.getSourceNodeId())) {
			doAppNotFoundResponse(messageEvent.getSourceNodeId());
			startActivity(WEAR_PLANNING_POKER_PACKAGE, null);
		}
	}

	JSONObject populateJSON(Bundle data) {
		JSONObject json = new JSONObject();

		try {
			if(data.containsKey(Params.CARD_VISIBILITY)) {
				json.put(Params.CARD_VISIBILITY, data.getBoolean(Params.CARD_VISIBILITY));
			}

			if(data.containsKey(Params.DECK_NAME)) {
				json.put(Params.DECK_NAME, data.getString(Params.DECK_NAME));
			}

			if(data.containsKey(Params.CURRENT_CARD)) {
				json.put(Params.CURRENT_CARD, data.getInt(Params.CURRENT_CARD));
			}

			if(data.containsKey(Params.BACKGROUND_COLOR)) {
				json.put(Params.BACKGROUND_COLOR, data.getInt(Params.BACKGROUND_COLOR));
			}

			if(data.containsKey(Params.TEXT_COLOR)) {
				json.put(Params.TEXT_COLOR, data.getInt(Params.TEXT_COLOR));
			}

			if(data.containsKey(Params.CUSTOM_DECK)) {
				json.put(Params.CUSTOM_DECK, data.getString(Params.CUSTOM_DECK));
			}

			json.put(Params.TIMESTAMP, data.getLong(Params.TIMESTAMP));
		} catch(JSONException e){
			Log.i(Application.TAG, e.getLocalizedMessage() + "\n" + e.getMessage());
		}

		return json;
	}

	Bundle populateBundle(Bundle data, JSONObject json) {
		try {

			if(json.has(Params.CARD_VISIBILITY)) {
				data.putBoolean(Params.CARD_VISIBILITY, json.getBoolean(Params.CARD_VISIBILITY));
			}

			if(json.has(Params.CURRENT_CARD)) {
				data.putInt(Params.CURRENT_CARD, json.getInt(Params.CURRENT_CARD));
			}

			if(json.has(Params.DECK_NAME)) {
				data.putString(Params.DECK_NAME, json.getString(Params.DECK_NAME));
			}

			// Mandatory
			data.putLong(Params.TIMESTAMP, json.getLong(Params.TIMESTAMP));
		} catch(JSONException e){
			Log.i(Application.TAG, e.getLocalizedMessage() + "\n" + e.getMessage());
		}

		return data;
	}

	JSONObject byteArrayToJSONObject(byte[] data) {
		try {
			return new JSONObject(new String(data));
		} catch (JSONException e) {
			return null;
		}
	}

	byte[] JSONObjectToByteArray(JSONObject json) {
		return json.toString().getBytes();
	}

	boolean startAgilePlanningPokerActivity(Bundle data, String nodeId) {

		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(Integer.MAX_VALUE);

		// App was started with the Intent
		boolean appStarted = true;

		// Search if Agile Planning Poker is already running
		for(ActivityManager.RunningTaskInfo runningTaskInfo : tasks) {
			if(AGILE_PLANNING_POKER_PACKAGE.equals(runningTaskInfo.baseActivity.getPackageName())) {
				appStarted = false;
				break;
			}
		}

		if(appStarted) {
			doAppStartedResponse(nodeId);
		}

		return startActivity(AGILE_PLANNING_POKER_PACKAGE, data);
	}

	boolean startActivity(String pakage, Bundle data) {
		final PackageManager pm = getPackageManager();
		Intent i = pm.getLaunchIntentForPackage(pakage);

		if(i != null) {

			if(data != null) {
				i.putExtras(data);
			}

			startActivity(i.setAction(Intent.ACTION_MAIN)
							.addCategory(Intent.CATEGORY_LAUNCHER)
							.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
			);

			return true;
		} else {
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null)
					.addCategory(Intent.CATEGORY_LAUNCHER);

			List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);

			for (ResolveInfo resolveInfo : appList) {
				if (AGILE_PLANNING_POKER_PACKAGE.equals(resolveInfo.activityInfo.packageName)) {
					startActivity(resolveInfo.activityInfo, data);
					return true;
				}
			}
		}

		return false;
	}

	void startActivity(ActivityInfo ai, Bundle data) {
		ComponentName name = new ComponentName(ai.applicationInfo.packageName, ai.name);

		Intent i = new Intent(Intent.ACTION_MAIN);

		i.addCategory(Intent.CATEGORY_LAUNCHER)
			.setComponent(name)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		if(data != null) {
			i.putExtras(data);
		}

		startActivity(i);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {}
}
