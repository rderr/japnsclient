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

/**
 * ApnsConnector is an abstract class that handles connecting to Apple's push notification services (APNS)
 * 
 * @author robert
 *
 */
public class Connector {
	public static final String APNS_HOST = "gateway.push.apple.com:2195";
	public static final String FEEDBACK_HOST = "feedback.push.apple.com:2196";

	public static final String DEV_APNS_HOST = "gateway.sandbox.push.apple.com:2195";
	public static final String DEV_FEEDBACK_HOST = "feedback.sandbox.push.apple.com:2196";

	private SSLSocket apnsSocket;

	private String host;
	
	private char[] keyPasswd;
	private FileInputStream keyFile;

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
	private SSLSocket connect() {
		
		if (this.apnsSocket == null || this.apnsSocket.isConnected() == false) {
			try  {

				// Attempt to load the key/cert file
				KeyStore ks = KeyStore.getInstance("PKCS12");
				ks.load(this.keyFile, this.keyPasswd);
				KeyManagerFactory tmf = KeyManagerFactory.getInstance("SunX509");
				tmf.init(ks, keyPasswd);
	
				// Create the ssl connection using the provided key/cert
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(tmf.getKeyManagers(), null, null);
			
				SSLSocketFactory factory = sslContext.getSocketFactory();
	
				String[] hostPort = this.host.split(":");
				this.apnsSocket = (SSLSocket) factory.createSocket(hostPort[0], Integer.parseInt(hostPort[1]));
				this.apnsSocket.setTcpNoDelay(true);

				String[] suites = this.apnsSocket.getSupportedCipherSuites();
				this.apnsSocket.setEnabledCipherSuites(suites);
				
				//	Connect to service
				this.apnsSocket.startHandshake();
				
			} catch (KeyStoreException e) {
				throw new RuntimeException("Cannot get instance of KeyStore type PKCS12", e);
			} catch (CertificateException e) {
				throw new RuntimeException("Problem loading certificate", e);
			} catch (IOException e) {
				throw new RuntimeException("Cannot read certificate file", e);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (UnrecoverableKeyException e) {
				throw new RuntimeException("Problem loading certificate", e);
			} catch (KeyManagementException e) {
				throw new RuntimeException("Problem loading certificate", e);
			}
		}
		
		return this.apnsSocket;
	}

	/**
	 * Closes the connection to the APN service
	 */
	protected void close() {
		if (apnsSocket != null) {
			try {
				apnsSocket.close();
			} 
			catch (IOException e) {
				throw new RuntimeException(e);
			}

			apnsSocket = null;
		}
	}
	
	
}
