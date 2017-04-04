package org.udoo.udooserial;

/**
 * Created by harlem88 on 03/04/17.
 */

public class DigitalModel {
    public String name;
    /*
    * 0 default, 1 input , 2 output
    * */
    public int pin;
    public short mode;
    public boolean value;

    public static DigitalModel Builder(int pin){
        DigitalModel digitalModel = new DigitalModel();
        digitalModel.name = "D"+pin;
        digitalModel.pin = pin;
        return digitalModel;
    }
}
