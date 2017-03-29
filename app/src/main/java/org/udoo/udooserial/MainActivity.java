package org.udoo.udooserial;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import org.udoo.udooserial.databinding.MainBinding;
import org.udoo.udooseriallibrary.OnResult;
import org.udoo.udooseriallibrary.UdooASManager;
import org.udoo.udooseriallibrary.UdooASManager.INTERRUPT_MODE;

import java.util.concurrent.Callable;

/**
 * Created by harlem88 on 23/03/17.
 */

public class MainActivity extends Activity {
    private UdooASManager mUdooASManager;
    private MainBinding mViewBinding;
    private Handler mUiHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.main);
        mUiHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        UdooASManager.Open(new UdooASManager.IReadyManager() {
            @Override
            public void onReadyASManager(final UdooASManager udooASManager) {
                mUdooASManager = udooASManager;
                setListener();
            }
        });
    }


    private void setListener(){
        //mUdooASManager.servoAttach(3);
        /*mUdooASManager.subscribeAnalogRead(0, 2000, new OnResult<Integer>() {
            @Override
            public void onSuccess(final Integer value) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewBinding.analog.a0Value.setText(value.toString());
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {}
        });*/

        mViewBinding.pwm.ckBox3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) mUdooASManager.servoAttach(3);
                else mUdooASManager.servoDetach(3);
            }
        });

        mViewBinding.pwm.seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(mViewBinding.pwm.ckBox3.isChecked())
                    mUdooASManager.servoWrite(3, seekBar.getProgress());
            }
        });

        mUdooASManager.attachInterrupt(0, INTERRUPT_MODE.CHANGE, blink());
    }

    public Callable<Void> blink(){
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mUdooASManager.digitalRead(0, new OnResult<Integer>() {
                    @Override
                    public void onSuccess(final Integer o) {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mViewBinding.digital.d0IntValue.setText(o.toString());
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("UDOOAndroidSerial", "onError: digitalRead " + throwable.getMessage());
                    }
                });
                return null;
            }
        };
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUdooASManager.servoDetach(3);
        if (mUdooASManager != null)
            mUdooASManager.close();
    }
}
