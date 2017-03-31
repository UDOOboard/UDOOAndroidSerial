package org.udoo.udooandroidserial.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by harlem88 on 31/03/17.
 */

public class SensorModel {
    public static JSONObject DHT11ReaderBuilder(int pin) {
        JSONObject json =  Builder("DHT11", 0);
        try {
            json.put("pin", pin);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject DHT22ReaderBuilder(int pin) {
        JSONObject json =  Builder("DHT22", 1);
        try {
            json.put("pin", pin);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static JSONObject HUMIDITYBRICKReaderBuilder(int pin) {
        return Builder("HUMIDITY_BRICK", 2);
    }


    public static JSONObject LIGHTBRICKReaderBuilder(int pin) {
        return Builder("LIGHT_BRICK", 3);
    }

    private static JSONObject Builder(String sensor, int id) {
        int tmpId = 's'+id;
        JSONObject json = new JSONObject();
        try {
            json.put("sensor", sensor);
            json.put("id", tmpId);
        } catch (JSONException e) {
            Log.e("BuilderPin: ", e.getMessage());
        }
        return json;
    }

    public static int GetId(JSONObject jsonObject) {
        int id = -1;
        try {
            id = jsonObject.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return id;
    }
}
