package com.somitsolutions.android.spectrumanalyzer;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Debug;
import android.util.Log;

import com.luugiathuy.apps.remotebluetooth.RemoteBluetooth;

import ca.uol.aig.fftpack.RealDoubleFFT;

public class RecordAudio extends AsyncTask<Void, double[], Void> {

    private int frequency = 8000;
    private int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    private boolean started = true; // was false..

    public final static int blockSize = 256;

    RemoteBluetooth activity = null;
    public RecordAudio(RemoteBluetooth a) {
        activity = a;
        transformer = new RealDoubleFFT(RecordAudio.blockSize);
    }

    @Override
    protected Void doInBackground(Void... params) {

        if(isCancelled()){
            return null;
        }

        //try {
        int bufferSize = AudioRecord.getMinBufferSize(frequency,
                channelConfiguration, audioEncoding);
                    /*AudioRecord */audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.DEFAULT, frequency,
                channelConfiguration, audioEncoding, bufferSize);
        int bufferReadResult;
        short[] buffer = new short[blockSize];
        double[] toTransform = new double[blockSize];
        try{
            audioRecord.startRecording();
        }
        catch(IllegalStateException e){
            Log.e("Recording failed", e.toString());

        }
        while (started) {

            bufferReadResult = audioRecord.read(buffer, 0, blockSize);

            if(isCancelled())
                break;

            for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
            }

            transformer.ft(toTransform);
            publishProgress(toTransform);
            if(isCancelled())
                break;
            //return null;
        }

        try{
            audioRecord.stop();
        }
        catch(IllegalStateException e){
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

    protected void onProgressUpdate(double[]... toTransform) {
        Log.e("RecordingProgress", "Displaying in progress");

        byte[] spectrum = new byte[toTransform[0].length * 4];
        StringBuilder transform_log = new StringBuilder();
        for (int i = 0; i < toTransform[0].length; i++) {
            // int x = i;
            int downy = (int) (150 - (toTransform[0][i] * 10));
            // int upy = 150;

            byte[] bytes = intToByteArray(downy);
            spectrum[i * 4] = bytes[0];
            spectrum[i * 4 + 1] = bytes[1];
            spectrum[i * 4 + 2] = bytes[2];
            spectrum[i * 4 + 3] = bytes[3];

            transform_log.append("[" + downy + "] ");
        }

        // Send data here!
        Log.d("Transform Log", transform_log.toString());
        activity.sendMessage(spectrum);

    }

    protected void onPostExecute(Void result) {
        try{
            audioRecord.stop();
        }
        catch(IllegalStateException e){
            Log.e("Stop failed", e.toString());

        }
        cancel(true);
    }

}
