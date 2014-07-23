package br.com.planning.poker.wear.app.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.com.planning.poker.wear.app.application.Application;

/**
 * Created by wakim on 22/07/14.
 */
public class SynchronizationService extends WearableListenerService {

	private static final String GET_STATE = "/get-state", SET_STATE = "/set-state";

	private static final String AGILE_PLANNING_POKER_PACKAGE = "br.com.poker.planning";

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		super.onMessageReceived(messageEvent);

		Log.i(Application.TAG, "onMessageReceived: " + messageEvent.getPath());

		if(! startAgilePlanningPokerActivity()) {
			// TODO Error Handling to Wearable
		} else {
			// TODO Request Data or Send Data
			Log.i(Application.TAG, "onMessageReceived: " + messageEvent.getPath());
		}
	}

	boolean startAgilePlanningPokerActivity() {

		final PackageManager pm = getPackageManager();
		Intent i = pm.getLaunchIntentForPackage(AGILE_PLANNING_POKER_PACKAGE);

		if(i != null) {
			startActivity(i.setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER));
			return true;
		} else {
			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);

			List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);

			for (ResolveInfo resolveInfo : appList) {
				if (AGILE_PLANNING_POKER_PACKAGE.equals(resolveInfo.activityInfo.packageName)) {
					startActivity(resolveInfo.activityInfo);
					return true;
				}
			}
		}

		return false;
	}

	void startActivity(ActivityInfo ai) {
		ComponentName name=new ComponentName(ai.applicationInfo.packageName, ai.name);
		Intent i = new Intent(Intent.ACTION_MAIN);

		i.addCategory(Intent.CATEGORY_LAUNCHER);
		i.setComponent(name);

		startActivity(i);
	}
}
