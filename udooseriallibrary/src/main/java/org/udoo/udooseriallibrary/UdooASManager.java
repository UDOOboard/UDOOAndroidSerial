package org.udoo.udooseriallibrary;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.udoo.udooseriallibrary.model.MethodModel;
import org.udoo.udooseriallibrary.model.ServoModel;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by harlem88 on 23/03/17.
 */

public class UdooASManager {
    private static UdooASManager sUdooASManager;
    private UdooSerialPort mUdooUdooSerialPort;

    private UdooASManager(UdooSerialPort udooUdooSerialPort) {
        mUdooUdooSerialPort = udooUdooSerialPort;
    }

    public interface IReadyManager {
        void onReadyASManager(UdooASManager udooASManager);
    }

    public void setPinMode(int pin, int value) {
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(MethodModel.PinModeBuilder(pin, value), null);
        }
    }

    public void digitalWrite(int pin, int value) {
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(MethodModel.DigitalWriteBuilder(pin, value), null);
        }
    }

    public void digitalRead(int pin, final OnResult<Boolean> onResult) {
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(MethodModel.DigitalReadBuilder(pin), new OnResult<JSONObject>() {
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
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(MethodModel.AnalogReadBuilder(pin), new OnResult<JSONObject>() {
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
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(ServoModel.ServoAttachBuilder(pin), null);
        }
    }

    public void servoDetach(int pin) {
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(ServoModel.ServoDetachBuilder(pin), null);
        }
    }

    public void servoWrite(int pin, int degrees) {
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.write(ServoModel.WriteServoBuilder(pin, degrees), null);
        }
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
        if (mUdooUdooSerialPort != null) {
            mUdooUdooSerialPort.close();
            sUdooASManager = null;
            mUdooUdooSerialPort = null;
        }
    }
}
