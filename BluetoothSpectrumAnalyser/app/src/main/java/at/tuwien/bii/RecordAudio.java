package at.tuwien.bii;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class RecordAudio extends AsyncTask<Void, double[], Void> {

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    public final static int blockSize = 128;

    BluetoothSpectrumAnalyser activity = null;
    public RecordAudio(BluetoothSpectrumAnalyser a) {
        activity = a;
        transformer = new RealDoubleFFT(RecordAudio.blockSize);
    }

    @Override
    protected Void doInBackground(Void... params) {

        if (isCancelled()) {
            return null;
        }

        transformer = new RealDoubleFFT(blockSize);

        int bufferSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);
                audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.DEFAULT, frequency,
                    channelConfiguration, audioEncoding, bufferSize);
        int bufferReadResult;
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];
        try {
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            Log.e("Recording failed", e.toString());

        }
        while (BluetoothSpectrumAnalyser.started) {

            bufferReadResult = audioRecord.read(buffer, 0, blockSize);

            if (isCancelled())
                break;

            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / 32768.0;
            }

            transformer.ft(toTransform);
            publishProgress(toTransform);
            if (isCancelled())
                break;
        }

        try {
            audioRecord.stop();
        } catch (IllegalStateException e) {
            Log.e("Stop failed", e.toString());

        }

        return null;
    }

    private byte[] intToByteArray(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i >> 0);

        return result;
    }

    long timestamp = System.nanoTime();
    long counter = 0;

    protected void onProgressUpdate(double[]... toTransform) {
        Log.e("RecordingProgress", "Displaying in progress");

        if (++counter > 5) {
            counter = 0;
        } else {
            return;
        }

        activity.clearDisplay();

        byte[] spectrum = new byte[toTransform[0].length * 4];

        for (int i = 0; i < toTransform[0].length; i++) {
            int x = i;
            int downy = (int) (150 - (toTransform[0][i] * 10));
            int upy = 150;

            activity.drawSpectrum(x, downy, upy);

            byte[] bytes = intToByteArray(downy);
            spectrum[i * 4] = bytes[0];
            spectrum[i * 4 + 1] = bytes[1];
            spectrum[i * 4 + 2] = bytes[2];
            spectrum[i * 4 + 3] = bytes[3];
        }

        activity.sendSpectrum(spectrum);
        activity.refreshDisplay();

    }

    private int byteArrayToInt(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    protected void onPostExecute(Void result) {
        try {
            audioRecord.stop();
        } catch (IllegalStateException e) {
            Log.e("Stop failed", e.toString());

        }
        cancel(true);

        /*
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        */
    }

}

