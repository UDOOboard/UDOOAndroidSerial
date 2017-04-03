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
import org.udoo.udooandroidserial.OnResult;
import org.udoo.udooandroidserial.UdooASManager;
import org.udoo.udooandroidserial.UdooASManager.INTERRUPT_MODE;

import java.util.concurrent.Callable;

/**
 * Created by harlem88 on 23/03/17.
 */

public class MainActivity extends Activity {
    private UdooASManager mUdooASManager;
    private MainBinding mViewBinding;
    private Handler mUiHandler;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = DataBindingUtil.setContentView(this, R.layout.main);
        mUiHandler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UdooASManager.Open(new UdooASManager.IReadyManager() {
            @Override
            public void onReadyASManager(final UdooASManager udooASManager) {
                mUdooASManager = udooASManager;
                setListener();
            }
        });
    }

    private void setListener(){
        mUdooASManager.servoAttach(3);
        mUdooASManager.subscribeAnalogRead(0, 2000, new OnResult<Integer>() {
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
        });

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

        mViewBinding.digital.d2IntCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) mUdooASManager.attachInterrupt(2, INTERRUPT_MODE.CHANGE, d2_interrupt());
                else mUdooASManager.detachInterrupt(2);
            }
        });

        mUdooASManager.subscribeLightBrickRead(0, 5000, new OnResult<int[]>() {
            @Override
            public void onSuccess(final int[] values) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mViewBinding.bricks.lightValues.setText(values.toString());
                    }
                });
            }

            @Override
            public void onError(Throwable throwable) {}
        });
    }

    public Callable<Void> d2_interrupt(){
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mUdooASManager.digitalRead(2, new OnResult<Integer>() {
                    @Override
                    public void onSuccess(final Integer o) {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mViewBinding.digital.d2IntValue.setText(o.toString());
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
    protected void onStop() {
        super.onStop();
        mUdooASManager.servoDetach(3);
        if (mUdooASManager != null)
            mUdooASManager.close();
    }
}
