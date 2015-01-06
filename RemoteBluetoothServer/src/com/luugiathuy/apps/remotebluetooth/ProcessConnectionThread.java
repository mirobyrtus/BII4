package com.luugiathuy.apps.remotebluetooth;

import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import javax.microedition.io.StreamConnection;

import at.bii.display.FFT_Display;

public class ProcessConnectionThread implements Runnable {

	private StreamConnection mConnection;

	// Constant that indicate command from devices
	private static final int EXIT_CMD = -1;
	private static final int KEY_RIGHT = 1;
	private static final int KEY_LEFT = 2;

	public boolean connected = true;

	private FFT_Display myDisplay;

	public ProcessConnectionThread(StreamConnection connection) {
		mConnection = connection;
		myDisplay = new FFT_Display();
		myDisplay.setVisible(true);
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
				System.out.println(i++);

				byte[] response1 = new byte[blockSize * 4]; // 4 * 8 = 32
				// byte[] response2 = new byte[blockSize * 4]; // 4 * 8 = 32
				// connected =(inputStream.read(response) <0 );
				inputStream.read(response1);
				// inputStream.read(response2);
				/*
				 * if (command == EXIT_CMD) {
				 * System.out.println("finish process"); break; }
				 */

				int[] procInt = processResponse(response1);
				// printResponse(response2);

				// byte[] combineBytes = new byte[blockSize*4];
				// System.arraycopy(response1, 0, combineBytes, 0,
				// (blockSize-1)*4);
				// System.arraycopy(response2, 0, combineBytes,
				// (blockSize-2)*4+1 ,4);
				//
				// printResponse(combineBytes);

				if (procInt != null) {
					myDisplay.displayData(procInt);
				}

				// processCommand(command);

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
