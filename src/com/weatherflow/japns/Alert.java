package com.weatherflow.japns;

import java.util.HashMap;

/**
 * <p>Alert object.  Only use this object if you plan on using localization strings in the notification.
 * The Aps.addAlert(String alert) function will do the proper optimization required by Apple if localization 
 * is not required.</p>
 * 
 * <a href="http://developer.apple.com/iphone/library/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/ApplePushService/ApplePushService.html#//apple_ref/doc/uid/TP40008194-CH100-SW1">See notification payload documentation from Apple for additional information.</a>
 * 
 * 
 * @author robert
 *
 */
public class Alert extends HashMap<String, Object> {
	
	private static final long serialVersionUID = 7170250805937311287L;
	
	/**
	 * Constructs an Alert
	 * @param body The body text of the alert
	 */
	public Alert(String body) {
		this.addBody(body);
	}
	
	/**
	 * Constructs an Alert
	 * @param body The body text of the alert
	 * @param locKey The localization key
	 */
	public Alert(String body, String locKey) {
		
		this.put("action-key", locKey);
	}

	/**
	 * Adds/replaces the body text of the alert
	 * @param body The text of the alert body
	 */
	public void addBody(String body) {
		this.put("body", body);
	}

	/**
	 * Adds/replaces the action localization key 
	 * @param actionLocKey The key to the action locatization
	 */
	public void addActionLocKey(String actionLocKey) {
		this.put("action-loc-key", actionLocKey);
	}

	/**
	 * Adds/replaces the locatization key
	 * @param locKey The string specifying the localization key
	 */
	public void addLocKey(String locKey) {
		this.put("loc-key", locKey);
	}
	
	/**
	 * Adds/replaces the array of strings specifying the localization arguments
	 * @param locArgs Array of localization arguments
	 */
	public void addLocArgs(String[] locArgs) {
		this.put("loc-args", locArgs);
	}
}
