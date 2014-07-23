package br.com.planning.poker.wear.app.application;

import br.com.planning.poker.wear.app.preferences.SharedPreferencesWrapper;

/**
 * Created by wakim on 22/07/14.
 */
public class Application extends android.app.Application {

	public static final String TAG = "br.com.planning.poker.wear";

	@Override
	public void onCreate() {
		super.onCreate();

		SharedPreferencesWrapper.setContext(this);
	}
}
