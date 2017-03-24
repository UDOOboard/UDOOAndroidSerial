package org.udoo.udooseriallibrary;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.udoo.udooseriallibrary.model.MethodModel;
import org.udoo.udooseriallibrary.model.ServoModel;

import java.io.IOException;
import java.util.concurrent.Callable;

import static android.content.ContentValues.TAG;

/**
 * Created by harlem88 on 23/03/17.
 */

public class UdooASManager {
    private static UdooASManager sUdooASManager;
    private UdooSerialPort mUdooSerialPort;
    private Handler mHandlerNotify;
    private SparseArray<Runnable> mRunnableNotCallback;
    private HandlerThread mSerialHandlerTh;

    private UdooASManager(UdooSerialPort udooUdooSerialPort) {
        mUdooSerialPort = udooUdooSerialPort;
        mSerialHandlerTh = new HandlerThread("serial_handler");
        mSerialHandlerTh.start();
        mHandlerNotify = new Handler(mSerialHandlerTh.getLooper());
        mRunnableNotCallback = new SparseArray<>();
    }

    public interface IReadyManager {
        void onReadyASManager(UdooASManager udooASManager);
    }

    public void setPinMode(int pin, int value) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.PinModeBuilder(pin, value), null);
        }
    }

    public void digitalWrite(int pin, int value) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.DigitalWriteBuilder(pin, value), null);
        }
    }

    public void digitalRead(int pin, final OnResult<Boolean> onResult) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.DigitalReadBuilder(pin), new OnResult<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (jsonObject != null && onResult != null) {
                        try {
                            onResult.onSuccess(jsonObject.getInt("value") == 1);
                        } catch (JSONException e) {
                            onResult.onError(new Throwable("Json parsing error"));
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (onResult != null) {
                        onResult.onError(throwable);
                    }
                }
            });
        }
    }

    public void analogRead(int pin, final OnResult<Integer> onResult) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.AnalogReadBuilder(pin), new OnResult<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (jsonObject != null && onResult != null) {
                        try {
                            onResult.onSuccess(jsonObject.getInt("value"));
                        } catch (JSONException e) {
                            onResult.onError(new Throwable("Json parsing error"));
                        }
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (onResult != null) {
                        onResult.onError(throwable);
                    }
                }
            });
        }
    }

    public void servoAttach(int pin) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(ServoModel.ServoAttachBuilder(pin), null);
        }
    }

    public void servoDetach(int pin) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(ServoModel.ServoDetachBuilder(pin), null);
        }
    }

    public void servoWrite(int pin, int degrees) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(ServoModel.WriteServoBuilder(pin, degrees), null);
        }
    }

    public void subscribeDigitalRead(final int pin,final int interval, final OnResult<Boolean> onResult){
        addNotifyToHandler(pin, interval, new Callable() {
            @Override
            public Object call() throws Exception {
                digitalRead(pin, onResult);
                return null;
            }
        });
    }

    public void subscribeAnalogRead(final int pin, final int interval, final OnResult<Integer> onResult){
        int id = 'a'+pin;
        addNotifyToHandler(id, interval, new Callable() {
            @Override
            public Object call() throws Exception {
                analogRead(pin, onResult);
                return null;
            }
        });
    }

    public void unsubscribeDigitalRead(final int pin){
        removeNotifyFromHandler(pin);
    }

    public void unsubscribeAnalogRead(final int pin){
        int id = 'a'+pin;
        removeNotifyFromHandler(id);
    }

    private void addNotifyToHandler(final int id, int interval, final Callable call){
        if(interval < 50 ) interval = 50;
        final int tmp_interval = interval;
        mRunnableNotCallback.append(id, new Runnable() {
            @Override
            public void run() {
                mHandlerNotify.postDelayed(this, tmp_interval);
                try {
                    call.call();
                } catch (Exception e) {
                    Log.e(TAG, "run: id "+ id + " "+ e.getMessage());
                }
            }
        });
        mHandlerNotify.postDelayed(mRunnableNotCallback.get(id), tmp_interval);
    }

    private void removeNotifyFromHandler(int id){
        if(mRunnableNotCallback.indexOfKey(id) > -1) mHandlerNotify.removeCallbacks(mRunnableNotCallback.get(id));
    }


    public static void Open(IReadyManager iReadyManager) {
        if (sUdooASManager == null) {
            try {
                sUdooASManager = new UdooASManager(new UdooSerialPort("/dev/ttyMCC", 115200, 0));

            } catch (IOException e) {
                Log.e(TAG, "Open: " + e.getMessage());
            }
            if (iReadyManager != null) {
                iReadyManager.onReadyASManager(sUdooASManager);
            }
        } else {
            if (iReadyManager != null) {
                iReadyManager.onReadyASManager(sUdooASManager);
            }
        }
    }

    public void close() {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.close();
            sUdooASManager = null;
            mUdooSerialPort = null;
            for(int i = 0; i < mRunnableNotCallback.size(); i++) {
                int key = mRunnableNotCallback.keyAt(i);
                mHandlerNotify.removeCallbacks(mRunnableNotCallback.get(key));
            }
            mSerialHandlerTh.quit();
            mHandlerNotify = null;
        }
    }
}
