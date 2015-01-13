package com.luugiathuy.apps.remotebluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import ca.uol.aig.fftpack.RealDoubleFFT;

public class RemoteBluetooth extends Activity implements
        OnClickListener {

    // Layout view
    //private TextView mTitle;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothCommandService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for Bluetooth Command Service
    private BluetoothCommandService mCommandService = null;


    /// SoundRecordAndAnalysis properties
    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    int blockSize;// = 256;
    Button startStopButton;
    boolean started = false;

    RecordAudio recordTask;
    ImageView imageViewDisplaySectrum;
    MyImageView imageViewScale;
    Bitmap bitmapDisplaySpectrum;

    Canvas canvasDisplaySpectrum;

    Paint paintSpectrumDisplay;
    Paint paintScaleDisplay;
    static RemoteBluetooth mainActivity;
    LinearLayout main;
    int width;
    int height;
    int left_Of_BimapScale;
    int left_Of_DisplaySpectrum;
    private final static int ID_BITMAPDISPLAYSPECTRUM = 1;
    private final static int ID_IMAGEVIEWSCALE = 2;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        /// Sound 
        Display display = getWindowManager().getDefaultDisplay();
        // Point size = new Point();
        // display.get(size);
        width = display.getWidth();
        height = display.getHeight();
        // if (width > 512) { blockSize = 512; } else{
        blockSize = 128;// 256; TODO
        // }

        // Sound from onStart() 
        /// Sound
        main = new LinearLayout(this);
        main.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.FILL_PARENT));
        main.setOrientation(LinearLayout.VERTICAL);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        transformer = new RealDoubleFFT(blockSize);

        imageViewDisplaySectrum = new ImageView(this);
        if (width > 512) {
            bitmapDisplaySpectrum = Bitmap.createBitmap((int) 512, (int) 300,
                    Bitmap.Config.ARGB_8888);
        } else {
            bitmapDisplaySpectrum = Bitmap.createBitmap((int) 256, (int) 150,
                    Bitmap.Config.ARGB_8888);
        }
        LinearLayout.LayoutParams layoutParams_imageViewScale = null;
        // Bitmap scaled = Bitmap.createScaledBitmap(bitmapDisplaySpectrum, 320,
        // 480, true);
        canvasDisplaySpectrum = new Canvas(bitmapDisplaySpectrum);
        // canvasDisplaySpectrum = new Canvas(scaled);
        paintSpectrumDisplay = new Paint();
        paintSpectrumDisplay.setColor(Color.GREEN);
        imageViewDisplaySectrum.setImageBitmap(bitmapDisplaySpectrum);
        if (width > 512) {
            // imageViewDisplaySectrum.setLayoutParams(new
            // LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams layoutParams_imageViewDisplaySpectrum = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ((MarginLayoutParams) layoutParams_imageViewDisplaySpectrum)
                    .setMargins(100, 600, 0, 0);
            imageViewDisplaySectrum
                    .setLayoutParams(layoutParams_imageViewDisplaySpectrum);
            layoutParams_imageViewScale = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            // layoutParams_imageViewScale.gravity = Gravity.CENTER_HORIZONTAL;
            ((MarginLayoutParams) layoutParams_imageViewScale).setMargins(100,
                    20, 0, 0);

        }

        else if ((width > 320) && (width < 512)) {
            LinearLayout.LayoutParams layoutParams_imageViewDisplaySpectrum = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ((MarginLayoutParams) layoutParams_imageViewDisplaySpectrum)
                    .setMargins(60, 250, 0, 0);
            // layoutParams_imageViewDisplaySpectrum.gravity =
            // Gravity.CENTER_HORIZONTAL;
            imageViewDisplaySectrum
                    .setLayoutParams(layoutParams_imageViewDisplaySpectrum);

            // imageViewDisplaySectrum.setLayoutParams(new
            // LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutParams_imageViewScale = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            ((MarginLayoutParams) layoutParams_imageViewScale).setMargins(60,
                    20, 0, 100);
            // layoutParams_imageViewScale.gravity = Gravity.CENTER_HORIZONTAL;
        }

        else if (width < 320) {
            /*
             * LinearLayout.LayoutParams
             * layoutParams_imageViewDisplaySpectrum=new
             * LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
             * LinearLayout.LayoutParams.WRAP_CONTENT); ((MarginLayoutParams)
             * layoutParams_imageViewDisplaySpectrum).setMargins(30, 100, 0,
             * 100); imageViewDisplaySectrum.setLayoutParams(
             * layoutParams_imageViewDisplaySpectrum);
             */
            imageViewDisplaySectrum
                    .setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.FILL_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
            layoutParams_imageViewScale = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            // layoutParams_imageViewScale.gravity = Gravity.CENTER;
        }
        imageViewDisplaySectrum.setId(ID_BITMAPDISPLAYSPECTRUM);
        main.addView(imageViewDisplaySectrum);

        // ((MarginLayoutParams) layoutParams_imageViewScale).setMargins(0, 20,
        // 0, 20);

        imageViewScale = new MyImageView(this);
        imageViewScale.setLayoutParams(layoutParams_imageViewScale);
        imageViewScale.setId(ID_IMAGEVIEWSCALE);

        // imageViewScale.setLayoutParams(new
        // LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        main.addView(imageViewScale);

        startStopButton = new Button(this);
        startStopButton.setText("Start");
        startStopButton.setOnClickListener(this);
        startStopButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        main.addView(startStopButton);

        setContentView(main);
        // recordTask = new RecordAudio();

        /* left_Of_BimapScale = main.getChildAt(1).getLeft(); */

        mainActivity = this;

        /*
        /// Bakc froum bluetooth
        // Set up the window layout
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        mTitle.setText(R.string.app_name);
        mTitle = (TextView) findViewById(R.id.title_right_text);
        */

    }

    /// Sound
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // left_Of_BimapScale = main.getC.getLeft();
        MyImageView scale = (MyImageView) main.findViewById(ID_IMAGEVIEWSCALE);
        ImageView bitmap = (ImageView) main
                .findViewById(ID_BITMAPDISPLAYSPECTRUM);
        left_Of_BimapScale = scale.getLeft();
        left_Of_DisplaySpectrum = bitmap.getLeft();
    }

    long timestamp = System.nanoTime();
    long counter = 0;

    /// Sound
    private class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... params) {

            if (isCancelled()) {
                return null;
            }
            // try {
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
            /* AudioRecord */audioRecord = new AudioRecord(
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
            while (started) {

                /*
                 * if(width > 512){ bufferReadResult = audioRecord.read(buffer,
                 * 0, 512); } else{
                 */
                bufferReadResult = audioRecord.read(buffer, 0, blockSize);
                // }
                if (isCancelled())
                    break;

                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    toTransform[i] = (double) buffer[i] / 32768.0; // signed 16
                    // bit
                }

                transformer.ft(toTransform);
                /*
                 * if(width > 512){
                 * 
                 * publishProgress(toTransform); }
                 */
                publishProgress(toTransform);
                if (isCancelled())
                    break;
                // return null;
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

        protected void onProgressUpdate(double[]... toTransform) {
            Log.e("RecordingProgress", "Displaying in progress");

            if (++counter > 5) {
                counter = 0;
            } else {
                return;
            }

            if (width > 512) {

                StringBuilder out = new StringBuilder();
                byte[] spectrum = new byte[toTransform[0].length * 4];

                for (int i = 0; i < toTransform[0].length; i++) {
                    int x = 2 * i;
                    int downy = (int) (150 - (toTransform[0][i] * 10));
                    int upy = 150;
                    canvasDisplaySpectrum.drawLine(x, downy, x, upy,
                            paintSpectrumDisplay);

                    // if (i == 0) mCommandService.write(downy);
                    byte[] bytes = intToByteArray(downy);
                    spectrum[i * 4] = bytes[0];
                    spectrum[i * 4 + 1] = bytes[1];
                    spectrum[i * 4 + 2] = bytes[2];
                    spectrum[i * 4 + 3] = bytes[3];

                    // if (i < 20) out.append("[" + downy + "] ");
                }

                // mCommandService.write(spectrum);

                // Log.i("DOWNY", out.toString());
                imageViewDisplaySectrum.invalidate();
            }

            else {

                StringBuilder out = new StringBuilder();
                byte[] spectrum = new byte[toTransform[0].length * 4];
                // byte[] spectrum = new byte[blocksize * 4];

                for (int i = 0; i < toTransform[0].length; i++) {
                    int x = i;
                    int downy = (int) (150 - (toTransform[0][i] * 10));
                    int upy = 150;
                    canvasDisplaySpectrum.drawLine(x, downy, x, upy,
                            paintSpectrumDisplay);

                    out.append("[" + downy + "] ");

                    // if (i < 4) {
                    // if (i == 0) mCommandService.write(downy);
                    byte[] bytes = intToByteArray(downy);
                    spectrum[i * 4] = bytes[0];
                    spectrum[i * 4 + 1] = bytes[1];
                    spectrum[i * 4 + 2] = bytes[2];
                    spectrum[i * 4 + 3] = bytes[3];
                    // }

                }

                mCommandService.write(spectrum);

                // Log.i("DOWNY", out.toString());

                imageViewDisplaySectrum.invalidate();
            }

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
            recordTask.cancel(true);
            // }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

    }

    /// Sound
    public void onClick(View v) {

        if (started == true) {
            started = false;
            startStopButton.setText("Start");
            recordTask.cancel(true);
            // recordTask = null;
            canvasDisplaySpectrum.drawColor(Color.BLACK);
        } else {
            started = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }

    }

    /// Sound
    static RemoteBluetooth getMainActivity() {
        return mainActivity;
    }

    /// Sound
    public void onStop() {
        super.onStop();
        /*
         * started = false; startStopButton.setText("Start");
         */
        // if(recordTask != null){
        recordTask.cancel(true);
        // }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();

        // If BT is not on, request that it be enabled.
        // setupCommand() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        // otherwise set up the command service
        else {
            if (mCommandService==null)
                setupCommand();
        }

    }

    /// Sound
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // if(recordTask != null){
        recordTask.cancel(true);
        // }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mCommandService != null) {
            if (mCommandService.getState() == BluetoothCommandService.STATE_NONE) {
                mCommandService.start(); // TODO uncomment back! 
            }
        }
    }

    private void setupCommand() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mCommandService = new BluetoothCommandService(this, mHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCommandService != null)
            mCommandService.stop();

        /// Sound
        recordTask.cancel(true);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /// Sound
    // Custom Imageview Class
    public class MyImageView extends ImageView {
        Paint paintScaleDisplay;
        Bitmap bitmapScale;
        Canvas canvasScale;

        // Bitmap scaled;
        public MyImageView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            if (width > 512) {
                bitmapScale = Bitmap.createBitmap((int) 512, (int) 50,
                        Bitmap.Config.ARGB_8888);
            } else {
                bitmapScale = Bitmap.createBitmap((int) 256, (int) 50,
                        Bitmap.Config.ARGB_8888);
            }

            paintScaleDisplay = new Paint();
            paintScaleDisplay.setColor(Color.WHITE);
            paintScaleDisplay.setStyle(Paint.Style.FILL);

            canvasScale = new Canvas(bitmapScale);

            setImageBitmap(bitmapScale);
            invalidate();

        }

        @Override
        protected void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.onDraw(canvas);

            // int x_Of_BimapScale = bitmapScale.

            if (width > 512) {
                canvasScale.drawLine(0, 30, 512, 30, paintScaleDisplay);
                for (int i = 0, j = 0; i < 512; i = i + 128, j++) {
                    for (int k = i; k < (i + 128); k = k + 16) {
                        canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
                    }
                    canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
                    String text = Integer.toString(j) + " KHz";
                    canvasScale.drawText(text, i, 45, paintScaleDisplay);
                }
                canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);
            } else if ((width > 320) && (width < 512)) {
                canvasScale.drawLine(0, 30, 0 + 256, 30, paintScaleDisplay);
                for (int i = 0, j = 0; i < 256; i = i + 64, j++) {
                    for (int k = i; k < (i + 64); k = k + 8) {
                        canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
                    }
                    canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
                    String text = Integer.toString(j) + " KHz";
                    canvasScale.drawText(text, i, 45, paintScaleDisplay);
                }
                canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);
            }

            else if (width < 320) {
                canvasScale.drawLine(0, 30, 256, 30, paintScaleDisplay);
                for (int i = 0, j = 0; i < 256; i = i + 64, j++) {
                    for (int k = i; k < (i + 64); k = k + 8) {
                        canvasScale.drawLine(k, 30, k, 25, paintScaleDisplay);
                    }
                    canvasScale.drawLine(i, 40, i, 25, paintScaleDisplay);
                    String text = Integer.toString(j) + " KHz";
                    canvasScale.drawText(text, i, 45, paintScaleDisplay);
                }
                canvas.drawBitmap(bitmapScale, 0, 0, paintScaleDisplay);
            }

            // canvas.drawBitmap(bitmapScale, 0, 400, paintScaleDisplay);
            // invalidate();
        }

    }

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommandService.STATE_CONNECTED:
                            // mTitle.setText(R.string.title_connected_to);
                            // mTitle.append(mConnectedDeviceName);
                            break;
                        case BluetoothCommandService.STATE_CONNECTING:
                            // mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothCommandService.STATE_LISTEN:
                        case BluetoothCommandService.STATE_NONE:
                            // mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mCommandService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupCommand();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mCommandService.write(BluetoothCommandService.VOL_UP);
            return true;
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            mCommandService.write(BluetoothCommandService.VOL_DOWN);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}