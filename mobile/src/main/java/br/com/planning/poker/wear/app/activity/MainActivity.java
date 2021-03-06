package br.com.planning.poker.wear.app.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import br.com.planning.poker.wear.R;
import br.com.planning.poker.wear.app.service.SynchronizationService;
import br.com.planning.poker.wear.app.utils.Params;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

	Intent mAgilePlanningPokerIntent;
	private static final String START_WEARABLE = "/start-wearable";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		checkForAgilePlanningPoker();
	}

	void startWearablePlanningPoker() {
		Intent service = new Intent(this, SynchronizationService.class);

		service.putExtra(Params.METHOD, START_WEARABLE);

		startService(service);
	}

	@Override
	protected void onResume() {
		super.onResume();

		checkForAgilePlanningPoker();
	}

	void checkForAgilePlanningPoker() {
		PackageManager packageManager = getPackageManager();

		mAgilePlanningPokerIntent = packageManager.getLaunchIntentForPackage(SynchronizationService.AGILE_PLANNING_POKER_PACKAGE);

		if(mAgilePlanningPokerIntent != null) {
			showView(R.id.am_app_instructions_card);
			showView(R.id.am_app_card);
			hideView(R.id.am_app_not_found_card);

			findViewById(R.id.am_app_card).setOnClickListener(this);

			mAgilePlanningPokerIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		} else {
			showView(R.id.am_app_not_found_card);
			hideView(R.id.am_app_instructions_card);
			hideView(R.id.am_app_card);

			findViewById(R.id.am_app_not_found_card).setOnClickListener(this);
		}

		findViewById(R.id.am_app_wearable).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();

		if(id == R.id.am_app_not_found_card) {
			Uri uri = Uri.parse("market://details?id=" + SynchronizationService.AGILE_PLANNING_POKER_PACKAGE);
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

			try {
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + SynchronizationService.AGILE_PLANNING_POKER_PACKAGE)));
			}
		} else if(id == R.id.am_app_card) {
			startActivity(mAgilePlanningPokerIntent);
		} else if(id == R.id.am_app_wearable) {
			startWearablePlanningPoker();
		}
	}

	void showView(int resId) {
		View view = findViewById(resId);

		if(view != null) {
			view.setVisibility(View.VISIBLE);
		}
	}

	void hideView(int resId) {
		View view = findViewById(resId);

		if(view != null) {
			view.setVisibility(View.GONE);
		}
	}
}
