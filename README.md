# UDOO Android Serial

![alt tag](http://www.udoo.org/wp-content/uploads/2014/12/logoogo.png)

Thanks to this library you can control the Arduino side of the Udoo boards directly from an Android App.

This library communicate with this [Arduino sketch](https://github.com/fmntf/belzedoo), through a serial JSON protocol.

# Usage

You can include the library as local library project or add the dependency in your build.gradle.

## Gradle

Add this into your dependencies **build.gradle** file.

        repositories {
           maven {
               url  "http://dl.bintray.com/udooboard/maven"
           }
        }

        dependencies {
            compile 'org.udoo:udooandroidserial:0.2.1'
        }

## Code

### Android Serial Manager
Open arduino serial manager, and it is ready to return an UdooASManager object.
```java
UdooASManager.Open(new UdooASManager.IReadyManager() {
               @Override
               public void onReadyASManager(UdooASManager arduinoManager) {
               }
           });
```        


### Digital Pins
Configures the specified pin to behave either as an input or an output.

#### Input
```java
arduinoManager.setPinMode(0, UdooASManager.DIGITAL_MODE.INPUT);

```

#### Read
```java
arduinoManager.digitalRead(0, new OnResult<Integer>() {
         @Override
         public void onSuccess(Integer value) {}
         @Override
         public void onError(Throwable throwable) {}
         });

```     

#### Output
```java
arduinoManager.setPinMode(0, UdooASManager.DIGITAL_MODE.OUTPUT);
```

#### Write
You can set the value of a Pin directly from the `setPinMode()` method
```java
arduinoManager.setPinMode(0, UdooASManager.DIGITAL_VALUE.HIGH);
```
Or you can use the standard `digitalWrite()`
```java
mUdooASManager.digitalWrite(2,  UdooASManager.DIGITAL_VALUE.HIGH);
```        


#### Interrupt

Attach interrupt to a pin.
```java
arduinoManager.attachInterrupt(2, INTERRUPT_MODE.CHANGE, callback);
```          

Detach
```java
arduinoManager.detachInterrupt(2);
```  

### ANALOG

#### ANALOG READ
```java
arduinoManager.analogRead(0, new OnResult<Integer>() {
          @Override
          public void onSuccess(Integer value) {}
          @Override
          public void onError(Throwable throwable) {}
          });
```        

### Servo

Attach the Servo function to a pin.
```java
arduinoManager.servoAttach(3);
```    
Writes a value to the servo -> ( pin , degrees)
```java
arduinoManager.servoWrite(3, 90);
```    
Detach the Servo on pin.
```java
arduinoManager.servoDetach(3);
```    


### Bricks
UDOO BRICKS are modules you can use to augment your UDOO BOARDS and make them more versatile.

At this stage you can use Bricks only with the **UDOO NEO**
#### Humidity brick
```java
arduinoManager.humidityBrickRead(new OnResult<float[]>() {
            @Override
            public void onSuccess(final float[] values) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        values[0]  //temperature
                        values[1]; //humidity                             
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {}
        });
```

#### Light brick
```java
arduinoManager.lightBrickRead(new OnResult<float[]>() {
                    @Override
                    public void onSuccess(final int[] values) {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                values[0]  //visible
                                values[1]; //ir
                                values[2]; //full
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {}
                });
```     

### Notification
For each pin you can ask arduino to notify a reading with a certain interval.

 **subscribeDigitalRead  
 subscribeAnalogRead  
 subscribeHumidityBrickRead  
 subscribeLightBrickRead**

e.g.    

```java

mUdooASManager.subscribeLightBrickRead(5000, new OnResult<int[]>() {
 @Override
 public void onSuccess(final int[] values) {
     mUiHandler.post(new Runnable() {
         @Override
         public void run() {
             mViewBinding.bricks.lightValues.setText(values[2] +"");
         }
     });
 }

 @Override
 public void onError(Throwable throwable) {}
});
```         
