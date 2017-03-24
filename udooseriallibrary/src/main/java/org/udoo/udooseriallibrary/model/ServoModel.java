package org.udoo.udooseriallibrary.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harlem88 on 23/03/17.
 */

public class ServoModel {

    public static JSONObject ServoAttachBuilder(int pin) {
        return ServoBuilder("attach", pin, pin);
    }

    public static JSONObject ServoDetachBuilder(int pin) {
        return ServoBuilder("detach", pin, pin);
    }

    public static JSONObject WriteServoBuilder(int pin, int degrees) {
        JSONObject json =  ServoBuilder("write", pin, pin);
        try {
            json.put("degrees", degrees);
        } catch (JSONException e) {
            Log.e("BuilderPinValue: ", e.getMessage());
        }
        return json;
    }

    private static JSONObject ServoBuilder(String method, int pin, int id) {
        JSONObject json = new JSONObject();
        try {
            json.put("servo", method);
            json.put("pin", pin);
            json.put("id", id);
        } catch (JSONException e) {
            Log.e("BuilderPin: ", e.getMessage());
        }
        return json;
    }
}
