package br.com.planning.poker.wear.app.nodes;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import br.com.planning.poker.wear.app.utils.Params;

/**
 * Created by wakim on 22/07/14.
 */
public class WearManager implements NodeApi.NodeListener, MessageApi.MessageListener {

	Set<String> mNodes = new HashSet<String>();

	WearMessageCallback mCallback;

	boolean mErrorAlreadyHappened;

	/**
	 * Used to filter the last sent synchronization request, don't process previous responses to not generate inconsistency.
	 * If a previous response is processed while another is waiting for response, the state can be inconsistent for the User
	 */
	Calendar mTimestamp = Calendar.getInstance();

	public WearManager(GoogleApiClient googleApiClient) {
		// Start with the current Connected Nodes
		Wearable.NodeApi.getConnectedNodes(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
			@Override
			public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
				for(Node node : getConnectedNodesResult.getNodes()) {
					mNodes.add(node.getId());
				}
			}
		});
	}

	@Override
	public void onPeerConnected(Node node) {
		mNodes.add(node.getId());
	}

	@Override
	public void onPeerDisconnected(Node node) {
		mNodes.remove(node.getId());
	}

	public void broadcastMessage(GoogleApiClient googleApiClient, final String path, JSONObject data) {

		// Not connected to Google Play Services, nothing to do...
		if(! googleApiClient.isConnected()) {
			return;
		}

		// If there are no Nodes, notify the caller
		if(mNodes.isEmpty()) {
			throw new NodesNotFoundException();
		}

		mErrorAlreadyHappened = false;

		if(data == null) {
			data = new JSONObject();
		}

		updateTimestamp(data);

		for(String node : mNodes) {
			Wearable.MessageApi.sendMessage(googleApiClient, node, path, JSONObjectToByteArray(data))
				.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
					@Override
					public void onResult(MessageApi.SendMessageResult sendMessageResult) {
						if (! sendMessageResult.getStatus().isSuccess()) {
							notifyError(path);
						}
					}
				});
		}
	}

	// Add the timestamp to be sent to the other device, and store this one.
	void updateTimestamp(JSONObject data) {
		mTimestamp = Calendar.getInstance();

		try {
			data.put(Params.TIMESTAMP, mTimestamp.getTimeInMillis());
		} catch (JSONException e) {}
	}

	void notifyError(String path) {
		if(mCallback != null && ! mErrorAlreadyHappened) {
			mCallback.onMessageSendError(path);
		}

		mErrorAlreadyHappened = true;
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {

		byte[] data = messageEvent.getData();

		// Ignore empty messages, must have at least the Timestamp
		if(data == null) {
			return;
		}

		JSONObject json = byteArrayToJSONObject(data);

		// Data always must be JSON Valid
		if(json != null && ! validateResponse(json)) {
			return;
		}

		if(mCallback != null) {
			mCallback.onMessageReceived(messageEvent.getPath(), messageEvent.getSourceNodeId(), json);
		}
	}

	boolean validateResponse(JSONObject json) {
		Calendar timestamp = Calendar.getInstance();

		try {
			timestamp.setTimeInMillis(json.getLong(Params.TIMESTAMP));
		} catch (JSONException e) {
			return false;
		}

		// Drop this response, another is on the way
		return ! timestamp.before(mTimestamp);
	}

	public static class NodesNotFoundException extends RuntimeException {
	}

	byte[] JSONObjectToByteArray(JSONObject json) {
		return json.toString().getBytes();
	}

	JSONObject byteArrayToJSONObject(byte[] data) {
		try {
			return new JSONObject(new String(data));
		} catch (JSONException e) {
			return null;
		}
	}

	public void setWearMessageCallback(WearMessageCallback callback) {
		mCallback = callback;
	}

	public static interface WearMessageCallback {
		public void onMessageReceived(String path, String nodeId, JSONObject json);
		public void onMessageSendError(String path);
	}
}
