package org.udoo.udooandroidserial;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.udoo.udooandroidserial.model.MethodModel;
import org.udoo.udooandroidserial.model.SensorModel;
import org.udoo.udooandroidserial.model.ServoModel;

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

    public enum DIGITAL_MODE   {INPUT, OUTPUT}
    public enum DIGITAL_VALUE  {LOW, HIGH}
    public enum INTERRUPT_MODE {NONE, DEFAULT, CHANGE, RISING, FALLING}

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

    public void setPinMode(int pin, DIGITAL_MODE mode) {
        setPinMode(pin, mode.ordinal());
    }

    public void digitalWrite(int pin, int value) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.DigitalWriteBuilder(pin, value), null);
        }
    }

    public void digitalWrite(int pin, DIGITAL_VALUE value) {
        digitalWrite(pin, value.ordinal());
    }

    public void digitalRead(int pin, final OnResult<Integer> onResult) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.DigitalReadBuilder(pin), new OnResult<JSONObject>() {
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

    public void attachInterrupt(final int pin, INTERRUPT_MODE mode, final Callable<Void> callable) {
        if (mUdooSerialPort != null) {
            if(mode == INTERRUPT_MODE.DEFAULT) mode = INTERRUPT_MODE.CHANGE;

            mUdooSerialPort.write(MethodModel.AttachInterrupt(pin, mode), new OnResult<JSONObject>() {
                @Override
                public void onSuccess(JSONObject o) {
                    mUdooSerialPort.read(MethodModel.GetInterruptId(pin), new OnResult<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject jsonObject) {
                            if (jsonObject != null && callable != null) {
                                try {
                                    callable.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable throwable) {}
                    });
                }
                @Override
                public void onError(Throwable throwable) {}
            });

        }
    }

    public void detachInterrupt(int pin) {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.DetachInterruptBuilder(pin),null);
            mUdooSerialPort.removeRead(MethodModel.GetInterruptId(pin));
        }
    }

    public void disconnect() {
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(MethodModel.DisconnectBuilder(),null);
        }
    }

    public void dht11Read(int pin, final OnResult<float[]> onResult) {
        readTemperatureHumidity(SensorModel.DHT11ReaderBuilder(pin), onResult);
    }

    public void dht22Read(int pin, final OnResult<float[]> onResult) {
        readTemperatureHumidity(SensorModel.DHT22ReaderBuilder(pin), onResult);
    }

    public void humidityBrickRead(int pin, final OnResult<float[]> onResult) {
        readTemperatureHumidity(SensorModel.HUMIDITYBRICKReaderBuilder(pin), onResult);
    }

    /**
     * * @param onResult [0] visible [1] ir [2] full
     */
    public void lightBrickRead(int pin, final OnResult<int[]> onResult){
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(SensorModel.LIGHTBRICKReaderBuilder(pin), new OnResult<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (jsonObject != null && onResult != null) {
                        try {
                            int[] values = new int[3];
                            values[0] = jsonObject.getInt("visible");
                            values[1] = jsonObject.getInt("ir");
                            values[2] = jsonObject.getInt("full");
                            onResult.onSuccess(values);
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

    /**
     * * @param onResult [0] temperature , [1] humidity
     */
    private void readTemperatureHumidity(JSONObject readJson, final OnResult<float[]> onResult){
        if (mUdooSerialPort != null) {
            mUdooSerialPort.write(readJson, new OnResult<JSONObject>() {
                @Override
                public void onSuccess(JSONObject jsonObject) {
                    if (jsonObject != null && onResult != null) {
                        try {
                            float[] values = new float[2];
                            values[0] =(float) jsonObject.getDouble("temperature");
                            values[1] =(float) jsonObject.getDouble("humidity");
                            onResult.onSuccess(values);
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

    public void subscribeDigitalRead(final int pin,final int interval, final OnResult<Integer> onResult){
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

    public void subscribeDHT11Read(final int pin,final int interval, final OnResult<float[]> onResult){
        int id = SensorModel.GetId(SensorModel.DHT11ReaderBuilder(pin));
        if(id >= 0){
            addNotifyToHandler(id, interval, new Callable() {
                @Override
                public Object call() throws Exception {
                    dht11Read(pin, onResult);
                    return null;
                }
            });
        }
    }

    public void subscribeDHT22Read(final int pin, final int interval, final OnResult<float[]> onResult){
        int id = SensorModel.GetId(SensorModel.DHT22ReaderBuilder(pin));
        if(id >= 0){
            addNotifyToHandler(id, interval, new Callable() {
                @Override
                public Object call() throws Exception {
                    dht22Read(pin, onResult);
                    return null;
                }
            });
        }
    }

    public void subscribeHumidityBrickRead(final int pin,final int interval, final OnResult<float[]> onResult){
        int id = SensorModel.GetId(SensorModel.HUMIDITYBRICKReaderBuilder(pin));
        if(id >= 0){
            addNotifyToHandler(id, interval, new Callable() {
                @Override
                public Object call() throws Exception {
                    humidityBrickRead(pin, onResult);
                    return null;
                }
            });
        }
    }

    public void subscribeLightBrickRead(final int pin, final int interval, final OnResult<int[]> onResult){
        int id = SensorModel.GetId(SensorModel.LIGHTBRICKReaderBuilder(pin));
        if(id >= 0){
            addNotifyToHandler(id, interval, new Callable() {
                @Override
                public Object call() throws Exception {
                    lightBrickRead(pin, onResult);
                    return null;
                }
            });
        }
    }

    public void unsubscribeDigitalRead(final int pin){
        removeNotifyFromHandler(pin);
    }

    public void unsubscribeAnalogRead(final int pin){
        int id = 'a'+pin;
        removeNotifyFromHandler(id);
    }

    public void unsubscribeDHT11Read(final int pin,final int interval, final OnResult<float[]> onResult){
        int id = SensorModel.GetId(SensorModel.DHT11ReaderBuilder(pin));
        if(id >= 0){
            removeNotifyFromHandler(id);
        }
    }

    public void unsubscribeDHT22Read(final int pin, final int interval, final OnResult<float[]> onResult){
        int id = SensorModel.GetId(SensorModel.DHT22ReaderBuilder(pin));
        if(id >= 0){
            removeNotifyFromHandler(id);
        }
    }

    public void unsubscribeHumidityBrickRead(final int pin,final int interval, final OnResult<float[]> onResult){
        int id = SensorModel.GetId(SensorModel.HUMIDITYBRICKReaderBuilder(pin));
        if(id >= 0){
            removeNotifyFromHandler(id);
        }
    }

    public void unsubscribeLightBrickRead(final int pin, final int interval, final OnResult<int[]> onResult){
        int id = SensorModel.GetId(SensorModel.LIGHTBRICKReaderBuilder(pin));
        if(id >= 0){
            removeNotifyFromHandler(id);
        }
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
        mUdooSerialPort.removeRead(id);
    }

    public static void Open(IReadyManager iReadyManager) {
        if (sUdooASManager == null) {
            try {
                if (Build.MODEL.equals("UDOONEO-MX6SX")) {
                    sUdooASManager = new UdooASManager(new UdooSerialPort("/dev/ttyMCC", 115200, 0));
                } else if(Build.MODEL.equals("UDOO-MX6DQ")) {
                    sUdooASManager = new UdooASManager(new UdooSerialPort("/dev/ttymxc3", 115200, 0));
                }
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
