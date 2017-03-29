package org.udoo.udooseriallibrary.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.udoo.udooseriallibrary.UdooASManager.INTERRUPT_MODE;

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
        int id = 'a'+pin;
        return BuilderPin("analogRead", pin, id);
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

    public static JSONObject AttachInterrupt(int pin, INTERRUPT_MODE mode) {
        int id = 'i'+pin;
        JSONObject json = BuilderPin("attachInterrupt", pin, id);
        try {
            json.put("mode", mode.ordinal());
            json.put("interrupt_id", id);
        } catch (JSONException e) {
            Log.e("BuilderAttachInt: ", e.getMessage());
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
