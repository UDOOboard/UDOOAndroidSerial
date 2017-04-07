# UDOO Android Serial

![alt tag](http://www.udoo.org/wp-content/uploads/2014/12/logoogo.png)

Library for control Arduino on Udoo boards

# Usage

  1. Include the library as local library project or add the dependency in your build.gradle.
       
           repositories {
               maven {
                   url  "http://dl.bintray.com/udooboard/maven"
               }
           }

        dependencies {
            compile 'org.udoo:udooandroidserial:0.2'
        }
      
   2.Open arduino serial manager, and it is ready to return an UdooASManager object.
        
        UdooASManager.Open(new UdooASManager.IReadyManager() {
                       @Override
                       public void onReadyASManager(UdooASManager arduinoManager) {
                       }
                   });
   
  3.(Digital Pin) Configures the specified pin to behave either as an input or an output.
     
  3.1 Input
         
         arduinoManager.setPinMode(0, UdooASManager.DIGITAL_MODE.INPUT);
     
  3.1.1 Read
     
         arduinoManager.digitalRead(0, new OnResult<Integer>() {
                  @Override
                  public void onSuccess(Integer value) {}
                  @Override
                  public void onError(Throwable throwable) {}
                  });                                  
     
     
  3.2 Output
          
          arduinoManager.setPinMode(0, UdooASManager.DIGITAL_MODE.OUTPUT);
     
  3.2.1 Write
          
          arduinoManager.setPinMode(0, UdooASManager.DIGITAL_VALUE.HIGH); 
     
  3.3 Interrupt
  
  3.3.1 Attach interrupt to pin.
        
            arduinoManager.attachInterrupt(2, INTERRUPT_MODE.CHANGE, callback);
  
  3.3.2 Detach 
        
            arduinoManager.detachInterrupt(2);
            
  4.ANALOG
   
  4.1 ANALOG READ
        
        arduinoManager.analogRead(0, new OnResult<Integer>() {
                  @Override
                  public void onSuccess(Integer value) {}
                  @Override
                  public void onError(Throwable throwable) {}
                  });
   
   5. Servo
   
   5.1 Attach the Servo variable to a pin.
        
        arduinoManager.servoAttach(3);
    
   5.2 Writes a value to the servo -> ( pin , degrees) 
        
        arduinoManager.servoWrite(3, 90);
   
   5.3 Detach the Servo on pin.
        
        arduinoManager.servoDetach(3);
    
   6.Bricks
     
   6.1 Read Humidity brick
   
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
   
   6.1 Read Light brick
     
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
                   
                   
   7. Notification for each who can ask arduino to nofify a value with interval.
   
       **subscribeDigitalRead  
       subscribeAnalogRead  
       subscribeHumidityBrickRead  
       subscribeLightBrickRead**
       
        e.g.    
                        
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
        
   
  
