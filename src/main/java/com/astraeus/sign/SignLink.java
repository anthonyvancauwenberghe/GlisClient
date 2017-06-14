package com.astraeus.sign;

import java.applet.Applet;
import java.io.*;
import java.net.*;

import javax.sound.midi.*;
import com.astraeus.Configuration;

public final class SignLink implements Runnable {

	public static final int clientversion = 317;
	public static int uid;
	public static int storeid = 32;
	public static RandomAccessFile cache_dat = null;
	public static final RandomAccessFile[] indices = new RandomAccessFile[5];
	public static boolean sunjava;
	public static Applet mainapp = null;
	private static boolean active;
	private static int threadLiveId;
	private static InetAddress socketAddress;
	private static int socketRequest;
	private static Socket socket = null;
	private static int threadreqpri = 1;
	private static Runnable threadreq = null;
	private static String dnsreq = null;
	public static String dns = null;
	private static String urlRequest = null;
	private static DataInputStream urlStream = null;
	private static String savereq = null;
	private static int midipos;
	public static String midi = null;
	public static int midiVolume;
	public static int fadeMidi;
	private static int wavepos;
	public static int wavevol;
	public static boolean reporterror = true;
	public static String errorName = "";

	private SignLink() {
	}

	public static void startpriv(InetAddress inetaddress) {
		threadLiveId = (int) (Math.random() * 99999999D);
		if (active) {
			try {
				Thread.sleep(500L);
			} catch (Exception _ex) {
			}
			active = false;
		}
		socketRequest = 0;
		threadreq = null;
		dnsreq = null;
		savereq = null;
		urlRequest = null;
		socketAddress = inetaddress;
		Thread thread = new Thread(new SignLink());
		thread.setDaemon(true);
		thread.start();
		while (!active)
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
	}

	public void run() {
		active = true;
		uid = getUid(Configuration.CACHE_PATH.toString());
		try {
			cache_dat = new RandomAccessFile(Configuration.CACHE_PATH.resolve("main_file_cache.dat").toFile(), "rw");
			for (int index = 0; index < 5; index++)
				indices[index] = new RandomAccessFile(
						Configuration.CACHE_PATH.resolve("main_file_cache.idx" + index).toFile(), "rw");
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		for (int i = threadLiveId; threadLiveId == i;) {
			if (socketRequest != 0) {
				try {
					socket = new Socket(socketAddress, socketRequest);
				} catch (Exception _ex) {
					socket = null;
				}
				socketRequest = 0;
			} else if (threadreq != null) {
				Thread thread = new Thread(threadreq);
				thread.setDaemon(true);
				thread.start();
				thread.setPriority(threadreqpri);
				threadreq = null;
			} else if (dnsreq != null) {
				try {
					dns = InetAddress.getByName(dnsreq).getHostName();
				} catch (Exception _ex) {
					dns = "unknown";
				}
				dnsreq = null;
			} else if (savereq != null) {
				savereq = null;
			} else if (urlRequest != null) {
				try {
					System.out.println("urlstream");
					urlStream = new DataInputStream((new URL(mainapp.getCodeBase(), urlRequest)).openStream());
				} catch (Exception _ex) {
					urlStream = null;
				}
				urlRequest = null;
			}
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
		}
	}

	/**
	 * Sets the volume for the midi synthesizer.
	 * 
	 * @param value
	 */
	public static void setVolume(int value) {
		int CHANGE_VOLUME = 7;
		midiVolume = value;
		if (synthesizer.getDefaultSoundbank() == null) {
			try {
				ShortMessage volumeMessage = new ShortMessage();
				for (int i = 0; i < 16; i++) {
					volumeMessage.setMessage(ShortMessage.CONTROL_CHANGE, i, CHANGE_VOLUME, midiVolume);
					volumeMessage.setMessage(ShortMessage.CONTROL_CHANGE, i, 39, midiVolume);
					MidiSystem.getReceiver().send(volumeMessage, -1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			MidiChannel[] channels = synthesizer.getChannels();
			for (int c = 0; channels != null && c < channels.length; c++) {
				channels[c].controlChange(CHANGE_VOLUME, midiVolume);
				channels[c].controlChange(39, midiVolume);
			}
		}
	}

	public static Sequencer music = null;
	public static Sequence sequence = null;
	public static Synthesizer synthesizer = null;

	public static synchronized boolean saveWave(byte abyte0[], int i) {
		if (i > 0x1e8480)
			return false;
		if (savereq != null) {
			return false;
		} else {
			wavepos = (wavepos + 1) % 5;
			savereq = "sound" + wavepos + ".wav";
			return true;
		}
	}

	public static synchronized boolean replayWave() {
		if (savereq != null) {
			return false;
		} else {
			savereq = "sound" + wavepos + ".wav";
			return true;
		}
	}

	public static synchronized void saveMidi(byte abyte0[], int i) {
		if (i > 0x1e8480)
			return;
		if (savereq != null) {
		} else {
			midipos = (midipos + 1) % 5;
			savereq = "jingle" + midipos + ".mid";
		}
	}

	private static int getUid(String s) {

		File file = new File(s + "uid.dat");

		if (!file.exists() || file.length() < 4L) {
			try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(s + "uid.dat"))) {
				dos.writeInt((int) (Math.random() * 99999999D));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try (DataInputStream dis = new DataInputStream(new FileInputStream(s + "uid.dat"))) {
			int uid = dis.readInt();			
			return uid + 1;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static synchronized Socket openSocket(int port) throws IOException {
		for (socketRequest = port; socketRequest != 0;)
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
		if (socket == null)
			throw new IOException("could not open socket");
		else
			return socket;
	}

	public static synchronized DataInputStream openUrl(String url) throws IOException {
		for (urlRequest = url; urlRequest != null;) {
			try {
				Thread.sleep(50L);
			} catch (Exception ex) {
			}
		}

		if (urlStream == null) {
			throw new IOException("could not open: " + url);
		}
		return urlStream;
	}

	public static synchronized void dnslookup(String s) {
		dns = s;
		dnsreq = s;
	}

	public static synchronized void startthread(Runnable runnable, int i) {
		threadreqpri = i;
		threadreq = runnable;
	}

	public static synchronized boolean wavesave(byte abyte0[], int i) {
		if (i > 0x1e8480)
			return false;
		if (savereq != null) {
			return false;
		} else {
			wavepos = (wavepos + 1) % 5;
			savereq = "sound" + wavepos + ".wav";
			return true;
		}
	}

	public static synchronized boolean wavereplay() {
		if (savereq != null) {
			return false;
		} else {
			savereq = "sound" + wavepos + ".wav";
			return true;
		}
	}

	public static void reporterror(String s) {
		System.out.println("Error: " + s);
	}

	public static void setError(String error) {
		errorName = error;
	}
}
