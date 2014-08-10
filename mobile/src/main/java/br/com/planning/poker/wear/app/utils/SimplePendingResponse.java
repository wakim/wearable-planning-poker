package br.com.planning.poker.wear.app.utils;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by wakim on 09/08/14.
 */
public class SimplePendingResponse implements PendingResponse {
	private String mNodeId;
	private String mPath;
	private byte[] mData;

	public SimplePendingResponse(String nodeId, String path, byte[] data) {
		this.mNodeId = nodeId;
		this.mPath = path;
		this.mData = data;
	}

	public void send(GoogleApiClient googleApiClient) {
		Wearable.MessageApi.sendMessage(googleApiClient, mNodeId, mPath, mData);
	}
}
