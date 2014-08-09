package br.com.planning.poker.wear.app.service;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import br.com.planning.poker.wear.app.activity.MainActivity;

/**
 * Created by wakim on 09/08/14.
 */
public class WakeUpService extends WearableListenerService {

	private static final String START_WEARABLE = "/start-wearable";

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		if(START_WEARABLE.equals(messageEvent.getPath())) {
			Intent activity = new Intent(this, MainActivity.class);

			activity
				.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
				.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			startActivity(activity);
		} else {
			super.onMessageReceived(messageEvent);
		}
	}
}
