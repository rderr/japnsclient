package com.weatherflow.japns;

/**
 * <p>The InvalidNotificationException is thrown when the APN service rejects the notification.</p> 
 * 
 * @author robert
 *
 */
public class InvalidNotificationException extends java.lang.Exception {
	private static final long serialVersionUID = -7692264724809863398L;
	private Notification notification;
	
	/**
	 * Constructs InvalidNotificationException.
	 * @param n Reference to the invalid notification object.
	 */
	public InvalidNotificationException(Notification n) {
		notification = n;
	}

	/**
	 * Constructs InvalidNotificationExcetion also specifies message.
	 * @param n Reference to the invalid notification object.
	 * @param message A String specifying the reason the notification is invalid.
	 */
	public InvalidNotificationException(Notification n, String message) {
		super(message);
		notification = n;
	}

	/**
	 * Gets the invalid Notification object.
	 * @return Refence to the invalid Noitification object.
	 */
	public Notification getInvalidNotification() {
		return notification;
	}
}
