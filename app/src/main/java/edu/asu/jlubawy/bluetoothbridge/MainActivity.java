package edu.asu.jlubawy.bluetoothbridge;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    final private String mDeviceAddress = "30:14:09:03:12:01";

    private TextView mStatusTextView;
    private TextView mSerialTextView;
    private Button mClearButton;
    private Button mUploadButton;
    private Button mConnectButton;

    private BridgeDatabaseHelper mDatabaseHelper;

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            switch (action)
            {
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                {
                    if (device.getAddress().equals(mDeviceAddress)) {
                        mConnectButton.setOnClickListener(mDisconnectClickListener);
                        mStatusTextView.setText("Connected to '" + mDeviceAddress + "'");
                        mConnectButton.setText("Disconnect");
                        mConnectButton.setEnabled(true);
                    }
                    break;
                }

                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                {
                    if (device.getAddress().equals(mDeviceAddress)) {
                        mConnectButton.setOnClickListener(mConnectClickListener);
                        mStatusTextView.setText("Disconnected");
                        mConnectButton.setText("Connect");
                        mConnectButton.setEnabled(true);
                    }
                    break;
                }

                default:
                    /* do nothing */
                    break;
            }
        }
    };


    private View.OnClickListener mClearClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int numCleared = mDatabaseHelper.clearNotSent();
            Toast.makeText(v.getContext(), "Cleared " + numCleared + " rows", Toast.LENGTH_SHORT).show();
        }
    };


    private View.OnClickListener mUploadClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final String urlString = "http://192.168.1.51/";
            URL serverUrl;

            Toast.makeText(v.getContext(), "Building URL", Toast.LENGTH_SHORT);

            try {
                serverUrl = new URL(urlString);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT);
                return;
            }

            Toast.makeText(v.getContext(), "Starting upload", Toast.LENGTH_SHORT);
            BridgeManager.upload(serverUrl);
        }
    };


    private View.OnClickListener mConnectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mStatusTextView.setText("Connecting to '" + mDeviceAddress + "'...");
            ((Button) v).setText("Connecting...");
            v.setEnabled(false);
            BridgeManager.connect(mDeviceAddress);
        }
    };


    private View.OnClickListener mDisconnectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((Button) v).setText("Disconnecting...");
            v.setEnabled(false);
            BridgeManager.disconnect();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatusTextView = (TextView) findViewById(R.id.statusTextView);

        mSerialTextView = (TextView) findViewById(R.id.serialTextView);
        mSerialTextView.setMovementMethod(new ScrollingMovementMethod());

        mDatabaseHelper = new BridgeDatabaseHelper(this);
        mDatabaseHelper.initialize();

        int count = mDatabaseHelper.getNumNotSent();
        Toast.makeText(this, count + " rows pending upload", Toast.LENGTH_SHORT).show();

        BridgeManager.initialize(this, mStatusTextView, mSerialTextView, mDatabaseHelper);

        mClearButton = (Button) findViewById(R.id.clearButton);
        mClearButton.setOnClickListener(mClearClickListener);

        mUploadButton = (Button) findViewById(R.id.uploadButton);
        mUploadButton.setOnClickListener(mUploadClickListener);

        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectButton.setOnClickListener(mConnectClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* Disconnect */
        BridgeManager.disconnect();

        /* Close any open database */
        mDatabaseHelper.close();

        /* Unregister the broadcast receiver */
        unregisterReceiver(mBroadcastReceiver);
    }
}
