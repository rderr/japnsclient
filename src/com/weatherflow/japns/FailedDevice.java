package com.weatherflow.japns;

import java.util.Date;

/**
 * <p>A failed device record from Apple's feedback service.</p>
 * <br>
 * <a href="http://developer.apple.com/iphone/library/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingWIthAPS/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW3">See Feedback Service documentation for additional information</a>
 * 
 * @author robert
 *
 */
public class FailedDevice {
	private Date failedTimestamp;
	private String deviceToken;
	
	/**
	 * Constructs a FailedDevice object
	 * @param failedTimestamp Timestamp when device became unvalid
	 * @param deviceToken token ID
	 */
	public FailedDevice(Date failedTimestamp, String deviceToken) {
		this.failedTimestamp = failedTimestamp;
		this.deviceToken = deviceToken;
	}
	
	/**
	 * Constructs a FailedDevice object
	 * @param failedTimestamp Reported timestamp of failed device
	 * @param deviceToken binary device token ID
	 */
	public FailedDevice(Date failedTimestamp, byte[] deviceToken) {
		StringBuilder dt = new StringBuilder();
		
		this.failedTimestamp = failedTimestamp;
		for (int i = 0; i < deviceToken.length; i++) {
			if ((i % 8) == 0) {
				dt.append(" ");
			}
			dt.append(java.lang.Integer.toHexString((int)deviceToken[i] & 0xFF));
		}
		
		this.deviceToken = dt.toString();
	}

	/**
	 * @return the failedTimestamp
	 */
	public Date getFailedTimestamp() {
		return failedTimestamp;
	}

	/**
	 * @return the deviceToken
	 */
	public String getDeviceToken() {
		return deviceToken;
	}
	
	
	
}
