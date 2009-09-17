package com.weatherflow.japns;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 * <p>The FeedbackService class is used to extract the list of invalid device tokens from Apple.</p>
 * <br> 
 * <a href="http://developer.apple.com/iphone/library/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/CommunicatingWIthAPS/CommunicatingWIthAPS.html#//apple_ref/doc/uid/TP40008194-CH101-SW3">See iPhone Reference Library for additional information.</a>
 *   
 * @author robert
 *
 */
public class FeedbackService {
	private Connector connector;
	
	/**
	 * @param keyFilename fill path to cert/key PKCS12 file
	 * @param keyPasswd Password of cert/key file
	 * @param sandbox False will connect to production APNS true will connect to sandbox   
	 * @throws FileNotFoundException PKCS12 file cannot be found
	 */
	public FeedbackService(String keyFilename, String keyPasswd, boolean sandbox) throws FileNotFoundException {
		if (sandbox) {
			connector = new Connector(keyFilename, keyPasswd, Connector.DEV_FEEDBACK_HOST);
		} else {
			connector = new Connector(keyFilename, keyPasswd, Connector.FEEDBACK_HOST);
		}
	}

	/**
	 * Connects to apple's feedback service and gets a list of failed devices
	 * @return List of failed devices
	 */
	public FailedDevice[] getFailedDevices() {
		Vector<FailedDevice> failedDevices = new Vector<FailedDevice>();
		
		try {
			DataInputStream inputStream = new DataInputStream(connector.getInputStream());
			
			Date failedDate = new Date((long)inputStream.readInt());
			
			int tokenSize = inputStream.readUnsignedShort();
			if (tokenSize != 32) throw new IOException("Corrupt data: Invalid token size");
			
			byte[] token = new byte[32];
			tokenSize = inputStream.read(token);
			if (tokenSize != 32) throw new IOException("Unexpected end of stream");
			
			failedDevices.add(new FailedDevice(failedDate, token));
			
		} catch (EOFException e) {
			// Do nothing at eof
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 

		return (FailedDevice[])failedDevices.toArray();
	}
	
}
