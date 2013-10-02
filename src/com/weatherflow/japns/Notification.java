package com.weatherflow.japns;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;

/**
 * <p>The Notification class represents a single notification to be sent to a single device.  The whole notification cannot be
 * more than 256 bytes.  If the notification is larger than 255 bytes the toByteArray function will throw an InvalidNotificationException.</p>
 * 
 * @author robert
 *
 */
public class Notification {
	private static final Logger log = Logger.getLogger("com.weatherflow.japnsClient.Notification");
	
	public static final int MAX_NOTIFICATION_SIZE = 255;

	private static final byte DEVICE_ITEM = 1;
	private static final byte PAYLOAD_ITEM = 2;
	private static final byte NOTIFICATION_ITEM = 3;
	private static final byte EXPIRATION_ITEM = 4;
	private static final byte PRIORITY_ITEM  = 5;
	
	private String deviceToken;
	private Payload payload = null;
	private Integer notificationId = null;
	
	/**
	 * Constructs a Notification object
	 * @param deviceToken The token of the device where the alert is to be sent to
	 */
	public Notification(String deviceToken) {
		this.deviceToken = deviceToken.replace(" ", "");
		log.debug("Device token: " + this.deviceToken);
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
	
	
	
	public Integer getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Integer notificationId) {
		this.notificationId = notificationId;
	}
	
	public String getToken() {
		return this.deviceToken;
	}
	
	public void setToken(String deviceToken) {
		this.deviceToken = deviceToken;
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
		ByteBuffer message = ByteBuffer.allocate(1024);
		message.order(ByteOrder.BIG_ENDIAN);
		
		log.debug("Marshalling notification");

		log.debug("Adding notification identifier to frame: " + this.getNotificationId());
		message.put(NOTIFICATION_ITEM);
		message.putShort((short)4);
		message.putInt(this.getNotificationId());
		
		// Payload
		log.debug("Adding payload to frame: " + this.toString());
		byte[] payload;
		try {
			payload = this.toString().getBytes("UTF-8");
			if (payload.length > MAX_NOTIFICATION_SIZE) {
				throw new InvalidNotificationException(this, "Notification larger than 256 bytes");
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		message.put(PAYLOAD_ITEM);
		message.putShort((short)payload.length);
		message.put(payload);

		log.debug("Adding token to frame: " + deviceToken);
		message.put(DEVICE_ITEM);
		message.putShort((short)32);
		for (int i = 0; i < deviceToken.length(); i+= 2) {
			message.put((byte)Integer.parseInt(deviceToken.substring(i, i + 2), 16));
		}

		// TODO: Support expiration date 
		log.debug("Adding expiration date to frame: " + deviceToken);
		message.put(EXPIRATION_ITEM);
		message.putShort((short)4);
		message.putInt(0);
		
		// TODO: Support priority
		log.debug("Adding priority to frame: " + deviceToken);
		message.put(PRIORITY_ITEM);
		message.putShort((short)1);
		message.put((byte)10);
		
		message.flip();

		byte[] outputBuffer = new byte[message.limit()];
		message.get(outputBuffer);
		
		return outputBuffer;
	}
}
