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

import java.util.HashSet;
import java.util.Set;

/**
 * Created by wakim on 22/07/14.
 */
public class WearManager implements NodeApi.NodeListener, MessageApi.MessageListener {

	Set<String> mNodes = new HashSet<String>();

	WearMessageCallback mCallback;

	boolean mErrorAlreadyHappend;

	public WearManager(GoogleApiClient googleApiClient) {
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

		if(! googleApiClient.isConnected()) {
			return;
		}

		if(mNodes.isEmpty()) {
			throw new NodesNotFoundException();
		}

		mErrorAlreadyHappend = false;

		for(String node : mNodes) {
			Wearable.MessageApi.sendMessage(googleApiClient, node, path, data == null ? null : JSONObjectToByteArray(data))
				.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
					@Override
					public void onResult(MessageApi.SendMessageResult sendMessageResult) {
						notifyError(path);
					}
				});
		}
	}

	void notifyError(String path) {
		if(mCallback != null && ! mErrorAlreadyHappend) {
			mCallback.onMessageSendError(path);
		}

		mErrorAlreadyHappend = true;
	}

	@Override
	public void onMessageReceived(MessageEvent messageEvent) {
		if(mCallback != null) {
			mCallback.onMessageReceived(messageEvent.getPath(),
				messageEvent.getSourceNodeId(),
				messageEvent.getData() == null ? null : byteArrayToJSONObject(messageEvent.getData()));
		}
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
