package br.com.planning.poker.wear.app.activity;

import android.app.IntentService;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import br.com.planning.poker.wear.R;
import br.com.planning.poker.wear.app.service.SynchronizationService;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

	Intent mAgilePlanningPokerIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		checkForAgilePlanningPoker();
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
		} else {
			showView(R.id.am_app_not_found_card);
			hideView(R.id.am_app_instructions_card);
			hideView(R.id.am_app_card);

			findViewById(R.id.am_app_not_found_card).setOnClickListener(this);
		}
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
			finish();
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
