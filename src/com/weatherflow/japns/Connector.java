package com.weatherflow.japns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;

/**
 * ApnsConnector is an abstract class that handles connecting to Apple's push notification services (APNS)
 * 
 * @author robert
 *
 */
public class Connector {
	private static final Logger log = Logger.getLogger("com.weatherflow.japnsClient.notification");

	public static final String APNS_HOST = "gateway.push.apple.com:2195";
	public static final String FEEDBACK_HOST = "feedback.push.apple.com:2196";

	public static final String DEV_APNS_HOST = "gateway.sandbox.push.apple.com:2195";
	public static final String DEV_FEEDBACK_HOST = "feedback.sandbox.push.apple.com:2196";

	private SSLSocket apnsSocket;

	private String host;
	
	private char[] keyPasswd;
	private FileInputStream keyFile;
	private SSLContext sslContext;

	/**
	 * Constructs a Connector object for connecting to APN services
	 * @param keyFilename Name of p12 file 
	 * @param keyPasswd Password of p12 file
	 * @param host Host:port of APN service
	 * @throws FileNotFoundException If keyFilename cannot be found
	 */
	protected Connector(String keyFilename, String keyPasswd, String host) throws FileNotFoundException {
		this.keyPasswd = keyPasswd.toCharArray();
		this.keyFile = new FileInputStream(keyFilename);
		this.host = host;

		// Attempt to load the key/cert file
		try {
			log.debug("Getting keystore instance PKCS12");
			KeyStore ks = KeyStore.getInstance("PKCS12");
			log.debug("Loading keyfile");
			ks.load(this.keyFile, this.keyPasswd);
	
			log.debug("Getting instance of KeyManagerFactory SunX509");
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(ks, this.keyPasswd);
	
			// Create the ssl connection using the provided key/cert
			log.debug("Loading SSLContext");
			sslContext = SSLContext.getInstance("TLS");
			log.debug("Initalizing SSLContext");
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
		} catch (KeyStoreException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (CertificateException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (UnrecoverableKeyException e) {
			throw new RuntimeException(e);
		} catch (KeyManagementException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	/**
	 * Establishes a connection to APN service 
	 * @return OutputStream to APN service
	 * @throws IOException When a connection could not be established
	 */
	protected OutputStream getOutputStream() throws IOException {
		SSLSocket conn = connect();
		return conn.getOutputStream(); 
	}

	/**
	 * Establishes a connection to APN service
	 * @return InputStream to APN service
	 * @throws IOException When a connection could not be established
	 */
	protected InputStream getInputStream() throws IOException {
		SSLSocket conn = connect();
		return conn.getInputStream();
	}
	
	/**
	 * Connects to APN service.  Uses existing connection if available.
	 * @return OutputStream to APN service
	 */
	protected SSLSocket connect() {
		log.debug("Connecting to APNS");
		
		if (this.apnsSocket == null || this.apnsSocket.isConnected() == false) {
			log.debug("Opening new socket");
			try  {
				SSLSocketFactory factory = sslContext.getSocketFactory();
	
				String[] hostPort = this.host.split(":");
				log.debug("Connecting to " + this.host);
				apnsSocket = (SSLSocket) factory.createSocket(hostPort[0], Integer.parseInt(hostPort[1]));
				apnsSocket.setTcpNoDelay(false);
				
				String[] suites = this.apnsSocket.getSupportedCipherSuites();
				apnsSocket.setEnabledCipherSuites(suites);
				

				//	Connect to service
				log.debug("Starting handshake");
				apnsSocket.startHandshake();
				
			} catch (IOException e) {
				log.error("Exception while trying to connect to APNS: make sure you're running Java <=1.6");
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} else {
			log.debug("Using existing connection");
		}
		
		return this.apnsSocket;
	}

	/**
	 * Closes the connection to the APN service
	 */
	protected void close() {
		if (apnsSocket != null) {
			try {
				log.debug("Closing socket");
				apnsSocket.close();
			} 
			catch (IOException e) {
				throw new RuntimeException(e);
			}

			apnsSocket = null;
		}
	}
	
	
}
