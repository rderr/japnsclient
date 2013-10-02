package com.weatherflow.japns;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLSocket;

import org.apache.log4j.Logger;

/**
 * <p>The NotificationService class handles the connection to the APN service and sends notifications.</p>
 * 
 * @author robert
 *
 */
public class NotificationService  {
	private static final Logger log = Logger.getLogger("com.weatherflow.japnsClient.NotificationService");
	
	private static final int CAPACITY = 65535;
	private static final byte COMMAND = 2;

	private Connector connector;

	private String errorMessage = "Success";
	
	private int notificationCounter = 12;

	
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
	 * Sends an single Notification object.  
	 * Multiple calls to send will reuse an existing connection or create a new one if the connection is terminated or does not exist.
	 * The first message will take longer to send since the connection must be established first.  You can avoid the delay by calling 
	 * the connect method before sending a Notification object.
	 * @param notification Notification object to send.
	 * @throws InvalidNotificationException Is thrown when an invalid Noitification object is detected.  Contains a reference to the last notification sent, but may not be the actual invalid notification.
	 */
	public void sendNotification(Notification notification) throws InvalidNotificationException {
		// TODO: support queuing with timeout to increase efficiency 
		sendNotifications(new ArrayList<Notification>(Arrays.asList(notification)));
	}
	

	/**
	 * Sends an array of Notification objects.
	 * @param notifications Array of Notification objects.
	 * @throws InvalidNotificationException Is thrown when an invalid Noitification object is detected.  Contains a reference to the last notification sent, but may not be the actual invalid notification.
	 */
	public void sendNotifications(List<Notification> notifications) throws InvalidNotificationException {
		ByteBuffer output = ByteBuffer.allocate(CAPACITY);
		output.order(ByteOrder.BIG_ENDIAN);

		if (notifications.size() == 0) {
			log.info("Empty notification list");
			return;
		}
		
		for(Notification notification : notifications) {
			log.info("Marshalling notification: " + notification.toString());
			output.put(COMMAND);
			output.putInt(0); // Frame length
			int mark = output.position();

			notification.setNotificationId(notificationCounter++);
			
			try {
				byte[] data = notification.toByteArray();
				output.put(data);
			} catch (BufferOverflowException e) {
				log.debug("BufferOverflowException: allocating more space for notifications");
				ByteBuffer bb = ByteBuffer.allocate(output.capacity()+CAPACITY);
				output.order(ByteOrder.BIG_ENDIAN);
				bb.put(output);
				output = bb;
				output.put(notification.toByteArray());
			}
			
			int size = output.position() - mark;
			output.putInt(mark-4, size);
		}
		output.flip();

		log.info("Sending " + notifications.size() + " notifications");
		int notificationId = send(output);
		
		// Skip over the bad notification and continue on with the rest of the missed notifications
		if (notificationId != -1) {
			log.info("Invalid notification id: " + notificationId);
			for (int idx = 0; idx < notifications.size(); idx++) {
				Notification n = notifications.get(idx);
				if (n.getNotificationId() == notificationId) {
					System.out.println(getLastError() + "|" + n.getToken() + "|" + n);
					if (idx+1 < notifications.size()) {// Don't try and resend the last notification
						log.info("Resending notification starting from: " + n.getNotificationId() + " " + n.toString());
						sendNotifications(notifications.subList(idx+1, notifications.size()));
					}
				}
			}
		}
	}
	
	public String getLastError() {
		String rtn = errorMessage;
		errorMessage = "Success";
		return rtn;
	}

	
	private int send(ByteBuffer output) {
		SSLSocket socket = connector.connect();
		int rtn = -1;

		OutputStream os = null;
		InputStream is = null;
		
		try {
			os = socket.getOutputStream();
			is = socket.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		byte[] outputBuffer = new byte[output.limit()];
		output.get(outputBuffer);
		
		try {
			log.debug("Sending alerts");
			os.write(outputBuffer);
			log.debug("Flusing buffer");
			os.flush();
		} catch (IOException e) {
			log.error("IOException while sending alerts");
			throw new RuntimeException(e);
		}
		
		// Wait 2 seconds for apple to respond
		try {
			socket.setSoTimeout(2000);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		
		byte[] errorResponse = new byte[6];
		try {
			log.debug("Waiting 2 seconds for error-response packet");
			int size = is.read(errorResponse);
			if (size > 0) {
				// Get the error identifier
				assert(errorResponse[0] == 8);

				int j;
				
				rtn = 0;
				j = (errorResponse[2] & 0xff); rtn += (j << 24);
				j = (errorResponse[3] & 0xff); rtn += (j << 16);
				j = (errorResponse[4] & 0xff); rtn += (j << 8);
				j = (errorResponse[5] & 0xff); rtn += (j);				
				
				log.info("Received error response for notification: " + rtn);
				
				switch((int)(errorResponse[1] & 0xff)) {
					case 0: // No error
						rtn = -1;
						log.warn("Received No Error response packet");
						break;
					case 1: 
						errorMessage = "Processing error";
						connector.close();
						break;
					case 2: 
						errorMessage = "Missing device Token";
						connector.close();
						break;
					case 3: 
						errorMessage = "Missing Topic";
						connector.close();
						break;
					case 4: 
						errorMessage = "Missing Payload";
						connector.close();
						break;
					case 5: 
						errorMessage = "Invalid token size";
						connector.close();
						break;
					case 6: 
						errorMessage = "Invalid topic size";
						connector.close();
						break;
					case 7: 
						errorMessage = "Invalid playload size";
						connector.close();
						break;
					case 8: 
						errorMessage = "Invalid token";
						connector.close();
						break;
					case 10: 
						errorMessage = "Shutdown";
						connector.close();
						break;
					case 255: 
						errorMessage = "Unknown error";
						connector.close();
						break;
				}
			}
		} catch (IOException e) {
			// Timeout reading socket..  No notification from apple..
		}
		
		return rtn;
	}
}
