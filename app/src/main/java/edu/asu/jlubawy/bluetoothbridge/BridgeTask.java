package edu.asu.jlubawy.bluetoothbridge;

import android.bluetooth.BluetoothSocket;
import android.content.Context;

/**
 * Created by jlubawy on 10/23/2015.
 */
public class BridgeTask {

    private final Runnable mReceiverRunnable;

    private Thread mReceiverThread;

    private String mCurrentLine;

    private Context mContext;

    private String mDeviceAddress;

    private BluetoothSocket mBluetoothSocket;

    private static BridgeManager sInstance;

    BridgeTask(Context context, String deviceAddress) {
        mContext = context;
        mDeviceAddress = deviceAddress;
        mReceiverRunnable = new ReceiverRunnable(this);
        sInstance = BridgeManager.getInstance();
    }

    public Context getContext() {
        return mContext;
    }

    public String getDeviceAddress() {
        return mDeviceAddress;
    }

    public BluetoothSocket getBluetoothSocket() {
        return mBluetoothSocket;
    }

    public void setBluetoothSocket(BluetoothSocket mBluetoothSocket) {
        this.mBluetoothSocket = mBluetoothSocket;
    }

    public Runnable getReceiverRunnable() {
        return mReceiverRunnable;
    }

    public Thread getReceiverThread() {
        return mReceiverThread;
    }

    public void setReceiverThread(Thread currentThread) {
        mReceiverThread = currentThread;
    }

    public String getCurrentLine() {
        return mCurrentLine;
    }

    public void setCurrentLine(String currentLine) {
        mCurrentLine = currentLine;
    }

    public void handleReceiverState(int state) {
        int outState;

        switch (state) {
            case ReceiverRunnable.BT_CONNECT_SUCCESS:
                outState = BridgeManager.BT_CONNECT_SUCCESS;
                break;
            case ReceiverRunnable.LINE_AVAILABLE:
                outState = BridgeManager.LINE_AVAILABLE;
                break;
            case ReceiverRunnable.BT_NOT_SUPPORTED:
                outState = BridgeManager.BT_NOT_SUPPORTED;
                break;
            case ReceiverRunnable.BT_DISABLED:
                outState = BridgeManager.BT_DISABLED;
                break;
            case ReceiverRunnable.BT_UNKNOWN_ADDRESS:
                outState = BridgeManager.BT_UNKNOWN_ADDRESS;
                break;
            case ReceiverRunnable.BT_CONNECT_FAIL:
                outState = BridgeManager.BT_CONNECT_FAIL;
                break;
            case ReceiverRunnable.BT_SOCKET_CLOSED:
                outState = BridgeManager.BT_SOCKET_CLOSED;
                break;
            default:
                return;
        }

        sInstance.handleBridgeState(this, outState);
    }
}
