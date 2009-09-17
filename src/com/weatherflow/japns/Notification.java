package com.weatherflow.japns;

import java.util.Vector;
import java.io.UnsupportedEncodingException;
import org.json.simple.JSONValue;

/**
 * <p>The Notification class represents a single notification to be sent to a single device.  The whole notification cannot be
 * more than 256 bytes.  If the notification is larger than 255 bytes the toByteArray function will throw an InvalidNotificationException.</p>
 * 
 * @author robert
 *
 */
public class Notification {
	public static final int MAX_NOTIFICATION_SIZE = 255;
	
	private String deviceToken;
	private Payload payload = null;
	
	/**
	 * Constructs a Notification object
	 * @param deviceToken The token of the device where the alert is to be sent to
	 */
	public Notification(String deviceToken) {
		this.deviceToken = deviceToken.replace(" ", "");
	}
	
	/**
	 * Constructs a Notification object
	 * @param deviceToken The token of the device where the alert is to be sent to
	 * @param alert Alert string to send to the device
	 */
	public Notification(String deviceToken, String alert) {
		this(deviceToken, new Payload(alert));
	}
	
	/**
	 * Constructs a Notification object
	 * @param deviceToken The token of the device where the alert is to be sent to
	 * @param playload A custom payload object that is sent to the device
	 */
	public Notification(String deviceToken, Payload playload) {
		this(deviceToken);
		this.payload = playload;
	}
	
	/**
	 * Constructs a Notification object
	 * @param deviceToken The token of the device where the alert is to be sent to.
	 * @param aps An Aps object
	 */
	public Notification(String deviceToken, Aps aps) {
		this(deviceToken);
		this.payload = new Payload(aps);
	}
	
	/**
	 * Puts in an Aps object into the payload.  Will replace an existing Aps object if one is already assigned.
	 * @param aps An Aps object
	 */
	public void putAps(Aps aps) {
		this.payload.put("Aps", aps);
	}

	/**
	 * Used to add a custom payload to the notification. 
	 * @param key Name of custom payload
	 * @param payload Custom payload.  Some valid data types are string, integer, array, or map. 
	 */
	public void put(String key, Object payload) {
		this.payload.put(key, payload);
	}
	
	/**
	 * Converts the payload to a JSON object
	 */
	public String toString() {
		return JSONValue.toJSONString(this.payload);
	}
	
	/**
	 * Converts the Notification object to a byte array that can be sent directly to Apple's push notification service.
	 * @return Byte array of Notification object
	 */
	public byte[] toByteArray() throws InvalidNotificationException {
		Vector<Byte> messageArray = new Vector<Byte>();
		byte[] output;

		// First byte of message is null
		messageArray.add((byte)0);

		// Token size is always 32 bytes
		messageArray.add((byte)0);
		messageArray.add((byte)32);
		for (int i = 0; i < deviceToken.length(); i += 2) {
			String t = deviceToken.substring(i, i + 2);
			messageArray.add((byte)Integer.parseInt(t, 16));
		}

		// Get the payload string and convert it to byte[]
		String payload = JSONValue.toJSONString(this.payload);

		byte[] rawPayload;
		try {
			rawPayload = payload.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		// Throw an exception if the payload is too large
		if (rawPayload.length > Notification.MAX_NOTIFICATION_SIZE) {
			throw new InvalidNotificationException(this, "Payload exceeds maximum size");
		}

		messageArray.add((byte)0);
		messageArray.add((byte)rawPayload.length);
		for (int i = 0; i < rawPayload.length; i++)
			messageArray.add(rawPayload[i]);


		// Convert from Byte collection to byte[]
		output = new byte[messageArray.size()];
		for (int i = 0; i < messageArray.size(); i++) {
			output[i] = messageArray.get(i);
		}
		
		return output;
	}
}
