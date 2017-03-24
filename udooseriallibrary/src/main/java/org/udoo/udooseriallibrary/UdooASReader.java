package org.udoo.udooseriallibrary;

/**
 * Created by harlem88 on 23/03/17.
 */

import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;


public class UdooASReader {

    private static final String TAG = "UdooASReader";
    private Thread thread;
    private boolean running;
    private OnResult<JSONObject> mJsonReaderCallback;

    public UdooASReader(InputStream inputStream) {
        this.registerReader(inputStream);
        this.running = true;
    }

    public void setReaderCallback(OnResult<JSONObject> jsonReaderCallback){
        mJsonReaderCallback = jsonReaderCallback;
    }

    private void registerReader(final InputStream inputStream) {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    String readResponse = this.read();
                    if (readResponse != null) {
                        parseJson(readResponse);
                    }
                }
            }

            private void parseJson(String response) {

                if (BuildConfig.DEBUG)
                    Log.i(TAG, "parseJson: "+response);

                try {
                    JSONObject json = new JSONObject(response);
                    if (!json.has("id")) {

                        if (BuildConfig.DEBUG)
                            Log.d("UdooArduinoReader", "Ignored response: " + response);
                        return;
                    }
                    if (json.has("disconnected")) {
                        stop();
                    }

                    if(mJsonReaderCallback != null)
                        mJsonReaderCallback.onSuccess(json);

                } catch (JSONException ex) {
                    Log.e(TAG, "Arduino write: " + ex);

                    if (response.charAt(0) == '}') {
                        parseJson(response.substring(1));
                    }
                }
            }

            private String read() {
                byte[] buffer = new byte[256];
                String message = null;
                int mByteRead = -1;

                try {
                    if (Build.MODEL.equals("UDOONEO-MX6SX")) {
                        message = "";
                        do {
                            if (inputStream.available() > 0) {
                                mByteRead = inputStream.read(buffer, 0, buffer.length);
                                message += new String(Arrays.copyOfRange(buffer, 0, mByteRead));
                            }
                        } while (!message.contains("\n"));
                        message = message.trim();
                    } else {
                        byte[] response;
                        if (inputStream instanceof FileInputStream) {
                            mByteRead = inputStream.read(buffer, 0, buffer.length);
                            if (mByteRead != -1) {
                                response = Arrays.copyOfRange(buffer, 0, mByteRead);
                                message = new String(response).trim();
                            }
                        } else {
                            message = "";
                            do {
                                mByteRead = inputStream.read(buffer, 0, buffer.length);
                                message += new String(Arrays.copyOfRange(buffer, 0, mByteRead));
                            } while (!message.contains("\n"));
                            message = message.trim();
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "ARDUINO IO Exception" + e.getMessage());
                    stop();
                    message = null;
                }

                if (message != null) Log.d("UdooArduinoReader", message);
                return message;
            }
        });

        thread.start();
    }

    void stop() {
        this.running = false;
    }
}
