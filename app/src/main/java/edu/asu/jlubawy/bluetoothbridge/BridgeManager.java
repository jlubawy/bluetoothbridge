package edu.asu.jlubawy.bluetoothbridge;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by jlubawy on 10/21/2015.
 */
public class BridgeManager {

    /* Success states */
    static final int BT_CONNECT_SUCCESS = 1;
    static final int LINE_AVAILABLE = 2;
    static final int UPLOAD_DONE = 3;

    /* Failure states */
    static final int BT_NOT_SUPPORTED = -1;
    static final int BT_DISABLED = -2;
    static final int BT_UNKNOWN_ADDRESS = -3;
    static final int BT_CONNECT_FAIL = -4;
    static final int BT_SOCKET_CLOSED = -5;
    static final int UPLOAD_NO_DATA = -6;
    static final int UPLOAD_CONNECTION_ERROR = -7;
    static final int UPLOAD_STATUS_FAIL = -8;
    static final int UPLOAD_UNKNOWN_ERROR = -9;

    private Handler mHandler;

    private ExecutorService mReceiverExecutorService;
    private ExecutorService mUploaderExecutorService;

    private static BridgeTask mBridgeTask;
    private static UploadTask mUploadTask;

    private static Context mContext;

    private static TextView mStatusTextView;
    private static TextView mSerialTextView;
    private static BridgeDatabaseHelper mDatabaseHelper;

    private static BridgeManager sInstance = null;

    static {
        sInstance = new BridgeManager();
    }

    private BridgeManager() {

        mReceiverExecutorService = Executors.newSingleThreadExecutor();
        mUploaderExecutorService = Executors.newSingleThreadExecutor();

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message inMessage) {
                BridgeTask bridgeTask;

                switch(inMessage.what) {
                    case BT_CONNECT_SUCCESS:
                        /* handle connect in main activity */
                        break;
                    case LINE_AVAILABLE:
                        synchronized (mSerialTextView) {
                            bridgeTask = (BridgeTask) inMessage.obj;
                            String line = bridgeTask.getCurrentLine();
                            mSerialTextView.append(line + "\n");
                            mDatabaseHelper.saveLine(line);
                        }
                        break;
                    case UPLOAD_DONE:
                        Toast.makeText(mContext, "Upload done", Toast.LENGTH_SHORT).show();
                        break;
                    case BT_NOT_SUPPORTED:
                        mStatusTextView.setText("Bluetooth not supported");
                        break;
                    case BT_DISABLED:
                        mStatusTextView.setText("Must enable Bluetooth first");
                        break;
                    case BT_UNKNOWN_ADDRESS:
                        bridgeTask = (BridgeTask) inMessage.obj;
                        mStatusTextView.setText("Error unknown address '" + bridgeTask.getDeviceAddress() + "'");
                        break;
                    case BT_CONNECT_FAIL:
                        bridgeTask = (BridgeTask) inMessage.obj;
                        mStatusTextView.setText("Error connecting to '" + bridgeTask.getDeviceAddress() + "'");
                        break;
                    case BT_SOCKET_CLOSED:
                        mStatusTextView.setText("Disconnecting...");
                        break;
                    case UPLOAD_NO_DATA:
                        Toast.makeText(mContext, "No data", Toast.LENGTH_SHORT).show();
                        break;
                    case UPLOAD_CONNECTION_ERROR:
                        Toast.makeText(mContext, "Error connecting to server", Toast.LENGTH_SHORT).show();
                        break;
                    case UPLOAD_STATUS_FAIL:
                        Toast.makeText(mContext, "HTTP error", Toast.LENGTH_SHORT).show();
                        break;
                    case UPLOAD_UNKNOWN_ERROR:
                        Toast.makeText(mContext, "Unknown upload error", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        return;
                }
            }
        };
    }

    static void initialize(Context context, TextView statusTextView, TextView serialTextView, BridgeDatabaseHelper databaseHelper)
    {
        mContext = context;
        mStatusTextView = statusTextView;
        mSerialTextView = serialTextView;
        mDatabaseHelper = databaseHelper;
    }

    static void connect(String deviceAddress) {
        /* Clear the screen */
        mSerialTextView.setText("");
        mBridgeTask = new BridgeTask(mContext, deviceAddress);
        sInstance.mReceiverExecutorService.execute(mBridgeTask.getReceiverRunnable());
    }

    static void disconnect() {

        if (mBridgeTask == null) {
            return;
        }

        BluetoothSocket socket = mBridgeTask.getBluetoothSocket();

        if (socket != null)
        {
            try {
                socket.close();
            } catch (IOException e) {}
        }
    }

    static void upload(URL serverUrl) {
        /* Clear the screen */
        mUploadTask = new UploadTask(mDatabaseHelper, serverUrl);
        sInstance.mUploaderExecutorService.execute(mUploadTask.getUploaderRunnable());
    }

    public static BridgeManager getInstance() {
        return sInstance;
    }

    public void handleBridgeState(BridgeTask bridgeTask, int state) {
        mHandler.obtainMessage(state, bridgeTask).sendToTarget();
    }

    public void handleUploadState(UploadTask uploadTask, int state) {
        mHandler.obtainMessage(state, uploadTask).sendToTarget();
    }
}
