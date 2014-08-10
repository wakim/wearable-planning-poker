package br.com.planning.poker.wear.app.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import br.com.planning.poker.wear.R;

/**
 * Created by wakim on 09/08/14.
 */
public class MulticastPendingResponse implements PendingResponse {

	String mMethod;
	Context mContext;

	public MulticastPendingResponse(Context context, String method) {
		mMethod = method;
		mContext = context;
	}

	@Override
	public void send(final GoogleApiClient googleApiClient) {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

				List<Node> nodes = nodesResult.getNodes();

				if(nodes == null || nodes.isEmpty()) {
					return mContext.getString(R.string.no_devices_found);
				}

				boolean partialResult = true;

				for(Node node : nodes) {
					MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), mMethod, null).await();

					if(! result.getStatus().isSuccess()) {
						return result.getStatus().getStatusMessage();
					}
				}

				return null;
			}

			@Override
			protected void onPostExecute(String message) {
				super.onPostExecute(message);

				if(message != null) {
					Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
				}

				mContext = null;
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();

				mContext = null;
			}

			@Override
			protected void onCancelled(String s) {
				super.onCancelled(s);

				mContext = null;
			}
		}.execute();
	}
}
