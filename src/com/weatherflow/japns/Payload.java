package com.weatherflow.japns;

import java.util.HashMap;

/**
 * <p>The Payload object represents the most basic payload for a Notification.
 * All payloads are just a HashMap that is serialized to a JSON object before being sent 
 * to the APN service. </p>
 * <br>
 * <a href="http://developer.apple.com/iphone/library/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/ApplePushService/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW1">See Notification Payload for additiona information.</a>
 * 
 * @author robert
 *
 */
public class Payload extends HashMap<String, Object> {
	private static final long serialVersionUID = 4072763327144104728L;

	/**
	 * Constructs an empty payload.
	 */
	public Payload() {
	}

	/**
	 * Constructs the most basic valid notification Payload.
	 * @param alert The alert string sent to the device. 
	 */
	public Payload(String alert) {
		this.put("aps", new Aps(alert));
	}

	/**
	 * Constructs a Payload with the specified Aps object
	 * @param aps
	 */
	public Payload(Aps aps) {
		this.put("aps", aps);
	}
}
