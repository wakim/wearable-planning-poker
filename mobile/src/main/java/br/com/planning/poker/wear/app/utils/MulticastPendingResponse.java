package br.com.planning.poker.wear.app.utils;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by wakim on 09/08/14.
 */
public class MulticastPendingResponse implements PendingResponse {

	String mMethod;

	public MulticastPendingResponse(String method) {
		mMethod = method;
	}

	@Override
	public void send(final GoogleApiClient googleApiClient) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

				for(Node node : nodes.getNodes()) {
					Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), mMethod, null);
				}

				return null;
			}
		}.execute();
	}
}
