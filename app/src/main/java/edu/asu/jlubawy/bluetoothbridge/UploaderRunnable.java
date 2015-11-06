package edu.asu.jlubawy.bluetoothbridge;

import android.os.Process;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

/**
 * Created by jlubawy on 10/23/2015.
 */
public class UploaderRunnable implements Runnable {

    /* Success states */
    static final int UPLOAD_DONE = 1;

    /* Failure states */
    static final int UPLOAD_NO_DATA = -1;
    static final int UPLOAD_CONNECTION_ERROR = -2;
    static final int UPLOAD_STATUS_FAIL = -3;
    static final int UPLOAD_UNKNOWN_ERROR = -4;

    private UploadTask mUploadTask;

    public UploaderRunnable(UploadTask uploadTask) {
        mUploadTask = uploadTask;
    }

    @Override
    public void run() {
        BridgeDatabaseHelper databaseHelper;
        String data;
        HttpURLConnection httpUrlConnection = null;

        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mUploadTask.setUploaderThread(Thread.currentThread());

        databaseHelper = mUploadTask.getDatabaseHelper();
        data = databaseHelper.queryNotSent();
        if (data == null) {
            /* no data to upload */
            mUploadTask.handleUploaderState(UPLOAD_NO_DATA);
            return;
        }

        try {
            httpUrlConnection = (HttpURLConnection) mUploadTask.getServerUrl().openConnection();
            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setFixedLengthStreamingMode(data.getBytes().length);

            OutputStream oStream = new BufferedOutputStream(httpUrlConnection.getOutputStream());
            OutputStreamWriter writer = new OutputStreamWriter(oStream, "UTF-8");
            writer.write(data);
            writer.flush();
            writer.close();
            oStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            mUploadTask.handleUploaderState(UPLOAD_CONNECTION_ERROR);
            return;
        } finally {
            if (httpUrlConnection != null) {
                int responseCode = 0;

                try {
                    responseCode = httpUrlConnection.getResponseCode();
                } catch (IOException e) {}

                httpUrlConnection.disconnect();

                if (responseCode == 200) {
                    databaseHelper.clearNotSent();
                    mUploadTask.handleUploaderState(UPLOAD_DONE);
                } else {
                    mUploadTask.handleUploaderState(UPLOAD_STATUS_FAIL);
                }
            } else {
                mUploadTask.handleUploaderState(UPLOAD_STATUS_FAIL);
            }
        }
    }
}
