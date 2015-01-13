package com.luugiathuy.apps.remotebluetooth;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.io.StreamConnection;

import at.bii.display.FFT_Display;
import at.bii.display.TeePrintStream;

public class ProcessConnectionThread implements Runnable {

	private StreamConnection mConnection;

	// Constant that indicate command from devices
	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;

	public boolean connected = true;


	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");


	private FFT_Display myDisplay;

	private long start;

	public ProcessConnectionThread(StreamConnection connection) {
		mConnection = connection;
		myDisplay = new FFT_Display();
		myDisplay.setVisible(true);
		
		
		FileOutputStream file = null;
		try {
			file = new FileOutputStream("C:\\Temp\\log_BluetoothConnect.txt");
//			out = new PrintStream(new FileOutputStream("C:\\log_BluetoothConnect.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TeePrintStream tee = new TeePrintStream(file, System.out);
		System.setOut(tee);
	}

	public final static int blockSize = 256;

	private int byteArrayToInt(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16
				| (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	@Override
	public void run() {
		try {

			// prepare to receive data
			InputStream inputStream = mConnection.openInputStream();

			System.out.println("waiting for input");
			int i = 0;
			while (connected) {
				// int command = inputStream.read();
				if (i==0) {
					start=System.currentTimeMillis();
				}
				System.out.println("Processing Chunk#: "+ i++ + " (at "+ sdf.format(new Date()) + ") [live for " +(System.currentTimeMillis()-start)+ " ms]");

				byte[] response1 = new byte[blockSize * 4]; // 4 * 8 = 32
				inputStream.read(response1);

				int[] procInt = processResponse(response1);

				if (procInt != null) {
					myDisplay.displayData(procInt);
				} else {
					break;
				}

			}
			System.out.println(i + " connected=" + connected);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process the command from client
	 * 
	 * @param command
	 *            the command code
	 */
	/*
	 * unused cause its a continuous running thread... private void
	 * processCommand(int command) { try { Robot robot = new Robot(); switch
	 * (command) { case KEY_RIGHT: robot.keyPress(KeyEvent.VK_RIGHT);
	 * System.out.println("Right"); // release the key after it is pressed.
	 * Otherwise the event just keeps getting trigged
	 * robot.keyRelease(KeyEvent.VK_RIGHT); break; case KEY_LEFT:
	 * robot.keyPress(KeyEvent.VK_LEFT); System.out.println("Left"); // release
	 * the key after it is pressed. Otherwise the event just keeps getting
	 * trigged robot.keyRelease(KeyEvent.VK_LEFT); break; } } catch (Exception
	 * e) { e.printStackTrace(); } }
	 */

	/**
	 * Transform it back into integervalues
	 * 
	 * @param spectrum
	 * @return
	 */
	private int[] processResponse(byte[] spectrum) {
		StringBuilder message = new StringBuilder();
		int[] ret = new int[blockSize];
		int totalValue = 0;

		for (int i = 0; i < blockSize; i++) {

			byte[] bytes = new byte[4];
			bytes[0] = spectrum[i * 4];
			bytes[1] = spectrum[i * 4 + 1];
			bytes[2] = spectrum[i * 4 + 2];
			bytes[3] = spectrum[i * 4 + 3];
			int value = byteArrayToInt(bytes);
			totalValue += value;
			ret[i] = value;

			// if (value == 0) connected = false;

			message.append(value + ", ");
		}
		System.out.println("Spectrum = " + message.toString());
		if (totalValue == 0) {
			return null;
		}
		return ret;
	}
}
