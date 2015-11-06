package edu.asu.jlubawy.bluetoothbridge;

import java.net.URL;

/**
 * Created by jlubawy on 10/23/2015.
 */
public class UploadTask {

    private BridgeDatabaseHelper mDatabaseHelper;

    private URL mServerUrl;

    private final Runnable mUploaderRunnable;

    private Thread mUploaderThread;

    private static BridgeManager sInstance;

    UploadTask(BridgeDatabaseHelper databaseHelper, URL serverUrl) {
        mDatabaseHelper = databaseHelper;
        mServerUrl = serverUrl;
        mUploaderRunnable = new UploaderRunnable(this);
        sInstance = BridgeManager.getInstance();
    }

    public BridgeDatabaseHelper getDatabaseHelper() {
        return mDatabaseHelper;
    }

    public URL getServerUrl() {
        return mServerUrl;
    }

    public Runnable getUploaderRunnable() {
        return mUploaderRunnable;
    }

    public Thread getUploaderThread() {
        return mUploaderThread;
    }

    public void setUploaderThread(Thread currentThread) {
        mUploaderThread = currentThread;
    }

    public void handleUploaderState(int state) {
        int outState;

        switch (state) {
            case UploaderRunnable.UPLOAD_DONE:
                outState = BridgeManager.UPLOAD_DONE;
                break;
            case UploaderRunnable.UPLOAD_NO_DATA:
                outState = BridgeManager.UPLOAD_NO_DATA;
                break;
            case UploaderRunnable.UPLOAD_CONNECTION_ERROR:
                outState = BridgeManager.UPLOAD_CONNECTION_ERROR;
                break;
            case UploaderRunnable.UPLOAD_STATUS_FAIL:
                outState = BridgeManager.UPLOAD_STATUS_FAIL;
                break;
            case UploaderRunnable.UPLOAD_UNKNOWN_ERROR:
                outState = BridgeManager.UPLOAD_UNKNOWN_ERROR;
                break;
            default:
                return;
        }

        sInstance.handleUploadState(this, outState);
    }
}
