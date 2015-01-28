package at.tuwien.bii;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.luugiathuy.apps.remotebluetooth.BluetoothCommandService;
import com.luugiathuy.apps.remotebluetooth.DeviceListActivity;
import com.luugiathuy.apps.remotebluetooth.R;

public class RemoteBluetooth extends Activity {

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothCommandService bluetoothCommandService = null;

    private Button startStopButton;

    public static boolean started = false;

    private RecordAudio recordTask;

    private ImageView imageViewDisplaySectrum;

    private Canvas canvasDisplaySpectrum;

    private Paint paintSpectrumDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        startStopButton = (Button)findViewById(R.id.startStopButton);
        imageViewDisplaySectrum = (ImageView)findViewById(R.id.imageViewDisplaySectrum);

        Bitmap bitmapDisplaySpectrum = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);

        canvasDisplaySpectrum = new Canvas(bitmapDisplaySpectrum);
        paintSpectrumDisplay = new Paint();
        paintSpectrumDisplay.setColor(Color.RED);
        imageViewDisplaySectrum.setImageBitmap(bitmapDisplaySpectrum);
    }

    public void stopRecording() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // Update UI
                startStopButton.performClick();
                clearDisplay();
            }
        });
    }

    public void OnStartStop(View v) {
        if (started) {
            started = false;
            startStopButton.setText("Start Recording");
            recordTask.cancel(true);
            // recordTask = null;
            canvasDisplaySpectrum.drawColor(Color.BLACK);
        } else {
            if (bluetoothCommandService.getState() == BluetoothCommandService.STATE_CONNECTED) {
                started = true;
                startStopButton.setText("Cancel - Disconnect");
                recordTask = new RecordAudio(this);
                recordTask.execute();
            }
        }
    }

    public void onStop() {
        super.onStop();

        if (recordTask != null) {
            recordTask.cancel(true);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        // If bluetooth off, let user enable it
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BluetoothConstants.REQUEST_ENABLE_BT);
        } else {
            if (bluetoothCommandService == null) {
                setupCommand();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (recordTask != null) {
            recordTask.cancel(true);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // Resume bluetoothCommander
        if (bluetoothCommandService != null) {
            if (bluetoothCommandService.getState() == BluetoothCommandService.STATE_NONE) {
                bluetoothCommandService.start();
            }
        }
    }

    private void setupCommand() {
        bluetoothCommandService = new BluetoothCommandService(this, mHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothCommandService != null) {
            bluetoothCommandService.stop();
        }

        if (recordTask != null) {
            recordTask.cancel(true);
        }
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    // Bluetooth handler
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothConstants.MESSAGE_STATE_CHANGE:
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
                case BluetoothConstants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(BluetoothConstants.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConstants.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BluetoothConstants.TOAST),
                           Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BluetoothConstants.REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    bluetoothCommandService.connect(device);
                }
                break;
            case BluetoothConstants.REQUEST_ENABLE_BT:
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
                // Scan for devices
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, BluetoothConstants.REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Make device discoverable
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    public void refreshDisplay() {
        imageViewDisplaySectrum.invalidate();
    }

    public void clearDisplay() {
        canvasDisplaySpectrum.drawColor(Color.BLACK);
    }

    public void sendSpectrum(byte[] spectrum) {
        bluetoothCommandService.write(spectrum);
    }

    public void drawSpectrum(int x, int downy, int upy) {
        canvasDisplaySpectrum.drawLine(x, downy, x, upy, paintSpectrumDisplay);
    }
}