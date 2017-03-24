package org.udoo.udooseriallibrary.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harlem88 on 23/03/17.
 */

public class MethodModel {

    public static JSONObject PinModeBuilder(int pin, int value){
        return BuilderPinValue("pinMode", pin, value, pin);
    }

    public static JSONObject DigitalWriteBuilder(int pin, int value){
        return BuilderPinValue("digitalWrite", pin, value, pin);
    }

    public static JSONObject AnalogWriteBuilder(int pin, int value) {
        return BuilderPinValue("analogWrite", pin, value, pin);
    }

    public static JSONObject DigitalReadBuilder(int pin){
        return BuilderPin("digitalRead", pin, pin);
    }

    public static JSONObject AnalogReadBuilder(int pin) {
        return BuilderPin("analogRead", pin, pin);
    }

    private static JSONObject BuilderPinValue(String method, int pin, int value, int id) {
        JSONObject json = BuilderPin(method, pin, id);
        try {
            json.put("value", value);
        } catch (JSONException e) {
            Log.e("BuilderPinValue: ", e.getMessage());
        }
        return json;
    }

    private static JSONObject BuilderPin(String method, int pin, int id) {
        JSONObject json = new JSONObject();
        try {
            json.put("method", method);
            json.put("pin", pin);
            json.put("id", id);
        } catch (JSONException e) {
            Log.e("BuilderPin: ", e.getMessage());
        }
        return json;
    }
}
