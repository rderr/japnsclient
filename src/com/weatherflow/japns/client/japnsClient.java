package com.weatherflow.japns.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.weatherflow.japns.FailedDevice;
import com.weatherflow.japns.FeedbackService;
import com.weatherflow.japns.InvalidNotificationException;
import com.weatherflow.japns.Notification;
import com.weatherflow.japns.NotificationService;
import com.weatherflow.japns.Payload;

/**
 * <p>japnsClient is a command-line application that simplifies sending alerts to Apple's push notification server.  
 * The application can process alerts from standard in or from a file specified on the command-line.  The alerts are 
 * in pipe-delimited format with the device token in the first field and the JSON-formatted payload as the second field
 * and a carriage return indicating the end of record.</p>  
 * Example:<br>
 * 00000000 11111111 22222222 33333333 44444444 55555555 66666666 77777777|{"aps":{"alert":"Test Alert"}}<br>
 * <br>
 * <p>The japnsClient will verify that the JSON is valid, but it does not attempt to verify that the payload is a valid 
 * APNS message.</p>
 * <br>
 * Parameters:<br>
 * 	-keyFile			p12 Keyfile (no addtional steps are necessary after exporting p12 file from keyring).<br>
 *  -password			p12 keyfile password.<br>
 *  -sandbox			Tells the japnsClient to connect to the sandbox server.<br>
 *  -notificationFile	Location of file containing notifications.  STDIN is used when notification file is not specified.<br>
 *  -feedbackService	Connects to feedback service and prints out list of invalid device tokens.  <br>
 *  -verbose			Enables INFO level logging.  <br>
 *  -debug				Enables DEBUG level logging.  <br>
 * <br>
 * Usage:<br>
 * 	java -jar japnsClient.jar -keyFile /path/to/kefile.p12 -password keyfilePassword -notificationFile /path/to/file/containing/notifications.txt<br>
 * or<br>
 * 	echo '00000000 11111111 22222222 33333333 44444444 55555555 66666666 77777777|{"aps":{"alert":"Test Alert"}}' | java -jar japnsClient.jar -keyFile /path/to/kefile.p12 -password keyfilePassword<br>
 * <br>
 * Gotcha's:<br>
 * 	<p>If the token is invalid Apple's service will simply terminate the socket, but there is currently no reliable way to 
 * 	determine which token cause the disconnect and which messages were delivered.</p>  
 * 
 *	<p>It's important to connect to the feedback service and retrieve a list of device token's that should not receive 
 *	alerts.</p>
 * 
 * @author robert
 *
 */
public class japnsClient {
	private static final Logger log = Logger.getLogger("com.weatherflow.japnsClient");

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String notificationFile = null;
		String keyFile = null;
		String password = null;
		boolean sandbox = false;
		boolean feedbackService = false;
		
		PropertyConfigurator.configure(log.getClass().getClassLoader().getResource("main/resources/log4j.properties"));
		
		for(int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-keyFile")) {
				keyFile = args[++i];
				log.debug("Using keyfile: " + keyFile);
			}
			if (args[i].equalsIgnoreCase("-password")) {
				password = args[++i];
			}
			if (args[i].equalsIgnoreCase("-sandbox")) {
				sandbox = true;
				log.debug("Sandbox mode enabled");
			}
			if (args[i].equalsIgnoreCase("-notificationFile")) {
				notificationFile = args[++i];
				log.debug("Notification file: " + notificationFile);
			}
			if (args[i].equalsIgnoreCase("-feedbackService")) {
				feedbackService = true;
				log.debug("Using feedback service");
			}
			if (args[i].equalsIgnoreCase("-verbose")) {
				Logger.getRootLogger().setLevel(Level.INFO);
			}
			if (args[i].equalsIgnoreCase("-debug")) {
				Logger.getRootLogger().setLevel(Level.DEBUG);
				log.debug("Debug logging statements enabled");
			}
		}
		
		if (keyFile == null) {
			System.out.println("Usage: japnsClient -keyFile KEYFILE -password KEYFILE_PASSWORD [-sandbox] [-notificationFile DATA_FILE] [-feedbackService] [-verbose|-debug]");
			System.out.println("");
			System.out.println("DATA_FILE is a pipe delimited file containing the device token and json payload");
			System.out.println("Example:");
			System.out.println("00000000 11111111 22222222 33333333 44444444 55555555 66666666 77777777|{\"aps\":{\"alert\":\"test\"}}");
			System.out.println("11111111 11111111 22222222 33333333 44444444 55555555 66666666 77777777|{\"aps\":{\"alert\":\"test2\"}}");
			System.out.println("");
			System.out.println("japnsClient will read from stdin if no DATA_FILE is specified");
			return;
		}
		
		if (feedbackService) {
			japnsClient.printFailedDevices(keyFile, password, sandbox);
		} else {
			BufferedReader stdin;
			
			try {
				if (notificationFile != null) {
					stdin = new BufferedReader(new InputStreamReader(new FileInputStream(notificationFile), "UTF-8"));
					log.debug("Reading alerts from notificationFile: " + notificationFile);
				} else {
					stdin = new BufferedReader(new InputStreamReader(System.in, "UTF-8"));
					log.debug("Reading alerts from STDIN");
				}
			} catch(FileNotFoundException e) {
				log.error("Could not open notification file: " + notificationFile);
				return;
			} catch (UnsupportedEncodingException e) {
				log.error("Unsupported encoding in notification file.  Make sure file is in UTF-8.");
				throw new RuntimeException(e);
			}
			
			try {
				NotificationService ns = new NotificationService(keyFile, password, sandbox);
				List<Notification> notifications = new ArrayList<Notification>();
	
				while (stdin.ready()) {
					String message = stdin.readLine();
					log.debug("Message read: " + message);
	
					String[] parts = message.split("\\|");
					
					String token = parts[0];
					String payload = parts[1];
					
					log.debug("Token: " + token);
					log.debug("Payload: " + payload);
					
					log.debug("Parsing payload");
					JSONObject json = (JSONObject)JSONValue.parse(payload);
					
					Payload p = new Payload();
					p.putAll((Map<String, Object>)json);
					notifications.add(new Notification(token, p));
				}

				log.debug("Sending notifications");
				ns.sendNotifications(notifications);
			} catch (InvalidNotificationException e) {
				log.error("Invalid notification");
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Connectes to Apple's feedback service and prints out a list if failed devices.
	 * @param keyFile Path to keyfile
	 * @param password Password of keyfile
	 * @param sandbox sandbox flag
	 */
	public static void printFailedDevices(String keyFile, String password, boolean sandbox) {
		try {
			FeedbackService fs = new FeedbackService(keyFile, password, sandbox);
			FailedDevice[] fds = fs.getFailedDevices();
			
			for(int i = 0; i < fds.length; i++) {
				System.out.println(fds[i].getFailedTimestamp().toString() + "|" + fds[i].getDeviceToken());
			}
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
