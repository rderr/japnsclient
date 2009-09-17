package com.weatherflow.japns;

import java.io.*;

/**
 * <p>The NotificationService class handles the connection to the APN service and sends notifications.</p>
 * 
 * @author robert
 *
 */
public class NotificationService  {
	private Connector connector;
	
	/**
	 * @param keyFilename fill path to cert/key PKCS12 file
	 * @param keyPasswd Password of cert/key file
	 * @param sandbox False will connect to production APNS true will connect to sandbox   
	 * @throws FileNotFoundException PKCS12 file cannot be found
	 */
	public NotificationService(String keyFilename, String keyPasswd, boolean sandbox) throws FileNotFoundException {
		if (sandbox) {
			connector = new Connector(keyFilename, keyPasswd, Connector.DEV_APNS_HOST);
		} else {
			connector = new Connector(keyFilename, keyPasswd, Connector.APNS_HOST);
		}
	}

	/**
	 * Sends an array of Notification objects.
	 * @param notifications Array of Notification objects.
	 * @throws InvalidNotificationException Is thrown when an invalid Noitification object is detected.  Contains a reference to the last notification sent, but may not be the actual invalid notification.
	 */
	public void send(Notification[] notifications) throws InvalidNotificationException {
		for (Notification notification : notifications) {
			send(notification);
		}
	}
	
	/**
	 * Sends an single Notification object.  
	 * Multiple calls to send will reuse an existing connection or create a new one if the connection is terminated or does not exist.
	 * The first message will take longer to send since the connection must be established first.  You can avoid the delay by calling 
	 * the connect method before sending a Notification object.
	 * @param notification Notification object to send.
	 * @throws InvalidNotificationException Is thrown when an invalid Noitification object is detected.  Contains a reference to the last notification sent, but may not be the actual invalid notification.
	 */
	public void send(Notification notification) throws InvalidNotificationException {
		byte[] payload = notification.toByteArray();
		
		try {
			OutputStream os = connector.getOutputStream();

			os.write(payload);
			os.flush();
		} catch(IOException e) {
			connector.close();
			throw new InvalidNotificationException(notification);
		}
	}
	
	/**
	 * Connects to the APN service.
	 * @throws IOException Thrown if the connection could not be established.
	 */
	public void connect() throws IOException {
		connector.getOutputStream();
	}
}
