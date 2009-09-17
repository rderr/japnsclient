package com.weatherflow.japns;

import java.util.HashMap;

/**
 * <p>The Aps class represents the aps payload used by APNS.</p>
 * 
 * <a href="http://developer.apple.com/iphone/library/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/ApplePushService/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW1">See the APNS payload documentation for further information.</a>
 * 
 * 
 * @author robert
 *
 */
public class Aps extends HashMap<String, Object> {
	private static final long serialVersionUID = -8260737521962388125L;

	/**
	 * Constructs an empty Aps object
	 */
	public Aps() {
	}
	
	/**
	 * Constructs an Aps object with an alert string
	 * @param alert Alert string that will be displayed on device.  Can be UTF-8 encoded.
	 */
	public Aps(String alert) {
		this.addAlert(alert);
	}
	
	/**
	 * Constructs an Aps object with an alert string and badge number
	 * @param alert Alert string that will be displayed on device.  Can be UTF-8 encoded.
	 * @param badge Sets the badge number on the application's icon.
	 */
	public Aps(String alert, int badge) {
		this.addBadge(badge);
	}
	
	/**
	 * Constructs and Aps object with an alert string, badge number, and sound.
	 * @param alert Alert string that will be displayed on device.  Can be UTF-8 encoded.
	 * @param badge Sets the badge number on the application's icon.
	 * @param sound The sound that will be played when the notification is received.
	 */
	public Aps(String alert, int badge, String sound) {
		this(alert, badge);
		this.addSound(sound);
	}
	
	/**
	 * Constructs an Aps object with specified Alert object (JSON dictionary)
	 * @param alert Alert object that specifies the Alert body and any localization parameters.
	 */
	public Aps(Alert alert) {
		this.addAlert(alert);
	}
	
	/**
	 * Constructs an Aps object with an alert object and badge number
	 * @param alert Alert object that specifies the Alert body and any localization parameters.
	 * @param badge Sets the badge number on the application's icon.
	 */
	public Aps(Alert alert, int badge) {
		this(alert);
		this.addBadge(badge);
	}
	
	/**
	 * Constructs and Aps object with an alert string, badge number, and sound.
	 * @param alert Alert object that specifies the Alert body and any localization parameters.
	 * @param badge Sets the badge number on the application's icon.
	 * @param sound The sound that will be played when the notification is received.
	 */
	public Aps(Alert alert, int badge, String sound) {
		this(alert, badge);
		this.addSound(sound);
	}

	/**
	 * Adds/replaces the alert string.
	 * @param alert Alert string sent to device.
	 */
	public void addAlert(String alert) {
		this.put("alert", alert);
	}
	
	/**
	 * Adds/replaces alert object.
	 * @param alert Alert object that specifies the Alert body and any localization parameters.
	 */
	public void addAlert(Alert alert) {
		this.put("alert", alert);
	}

	/**
	 * Adds/replaces badge value
	 * @param badge Bage value.  Pass null to remove badge value.
	 */
	public void addBadge(Integer badge) {
		if (badge == null) {
			this.remove("badge");
		} else {
			this.put("badge", (int)badge);
		}
	}
	
	/**
	 * Adds/replaces sound.
	 * @param sound Name of sound file in application bundle.  Pass null to remove sound value.
	 */
	public void addSound(String sound) {
		if (sound == null) {
			this.remove("sound");
		} else {
			this.put("sound", sound);
		}
	}
	
}
