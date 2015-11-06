package edu.asu.jlubawy.bluetoothbridge;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Created by jlubawy on 10/23/2015.
 */
public class ReceiverRunnable implements Runnable {

    /* Success states */
    static final int BT_CONNECT_SUCCESS = 1;
    static final int LINE_AVAILABLE = 2;

    /* Failure states */
    static final int BT_NOT_SUPPORTED = -1;
    static final int BT_DISABLED = -2;
    static final int BT_UNKNOWN_ADDRESS = -3;
    static final int BT_CONNECT_FAIL = -4;
    static final int BT_SOCKET_CLOSED = -5;

    private BridgeTask mBridgeTask;

    public ReceiverRunnable(BridgeTask bridgeTask) {
        mBridgeTask = bridgeTask;
    }

    @Override
    public void run() {
        Context context;
        String deviceAddress;
        BluetoothAdapter adapter;
        BluetoothDevice device;
        BluetoothSocket socket;
        InputStream iStream;
        BufferedReader reader;
        String line;

        final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mBridgeTask.setReceiverThread(Thread.currentThread());

        context = mBridgeTask.getContext();
        deviceAddress = mBridgeTask.getDeviceAddress();

        adapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (adapter == null) {
            // Bluetooth not supported
            mBridgeTask.handleReceiverState(BT_NOT_SUPPORTED);
            return;
        }

        if (adapter.isEnabled() == false) {
            // Bluetooth not enabled
            mBridgeTask.handleReceiverState(BT_DISABLED);
            return;
        }

        /* Try to connect to the Bluetooth device */
        try {
            /* If valid address */
            if ( BluetoothAdapter.checkBluetoothAddress(deviceAddress) ) {
                /* Get Bluetooth device and socket */
                device = adapter.getRemoteDevice(deviceAddress);
                socket = device.createRfcommSocketToServiceRecord(UUID_SPP);
                mBridgeTask.setBluetoothSocket(socket);

                /* Stop Bluetooth discovery, connect to socket, and get the input stream */
                adapter.cancelDiscovery();
                socket.connect();
                iStream = socket.getInputStream();

                mBridgeTask.handleReceiverState(BT_CONNECT_SUCCESS);
            }
            else
            {
                mBridgeTask.handleReceiverState(BT_UNKNOWN_ADDRESS);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            mBridgeTask.handleReceiverState(BT_CONNECT_FAIL);
            return;
        }

        try {
            reader = new BufferedReader(new InputStreamReader(iStream));

            for (;;) {
                line = reader.readLine();

                if (line != null) {
                    mBridgeTask.setCurrentLine(line);
                    mBridgeTask.handleReceiverState(LINE_AVAILABLE);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mBridgeTask.handleReceiverState(BT_SOCKET_CLOSED);
        }
    }
}
