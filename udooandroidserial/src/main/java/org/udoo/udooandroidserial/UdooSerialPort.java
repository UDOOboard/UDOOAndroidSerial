package org.udoo.udooandroidserial;

import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by harlem88 on 23/03/17.
 */

public class UdooSerialPort implements OnResult<JSONObject> {
    private static final String TAG = "SerialPort";
    private FileDescriptor mFd;
    private InputStream mFileInputStream;
    private OutputStream mFileOutputStream;
    private BlockingQueue<Callable> mBlckQueue;
    private SeqObserverQueue mSeqObserverQueue;
    private UdooASReader mReader;
    private SparseArray<OnResult<JSONObject>> mReaderCallback;
    private AtomicBoolean mIsCallbackCalled;


    public UdooSerialPort(String device, int baudrate, int flags) throws SecurityException, IOException {

        mFd = open(device, baudrate, flags);
        if (mFd == null) {
            Log.e("SerialPort", "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
        mBlckQueue = new LinkedBlockingQueue<>(10);
        mSeqObserverQueue = new SeqObserverQueue<>(mBlckQueue);
        mReaderCallback = new SparseArray<>();
        mIsCallbackCalled = new AtomicBoolean();

        mReader = new UdooASReader(mFileInputStream);
        mReader.setReaderCallback(this);
        mSeqObserverQueue.run();
    }

    @Override
    public void onSuccess(JSONObject jsonObject) {
        int id = -1;
        try {
            if (jsonObject != null) {
                id = jsonObject.getInt("id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (id > -1 && mReaderCallback.indexOfKey(id) > -1) {
            OnResult<JSONObject> callback = mReaderCallback.get(id);
            if (callback != null) callback.onSuccess(jsonObject);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }

    public void write(final JSONObject json, final OnResult<JSONObject> callback) {
        final String request = json.toString() + '\n';
        try {
            mBlckQueue.put(new Callable() {
                @Override
                public Object call() throws Exception {
                    try {
                        mIsCallbackCalled.set(false);
                        final CountDownLatch callbackCountDown = new CountDownLatch(1);

                        mFileOutputStream.write(request.getBytes());
                        mFileOutputStream.flush();

//                        if (BuildConfig.DEBUG)
                            Log.i(TAG, "Linux write: " + request);

                        read(json.getInt("id"), new OnResult<JSONObject>() {
                            @Override
                            public void onSuccess(JSONObject o) {
                                if (!mIsCallbackCalled.get()) {
                                    mIsCallbackCalled.set(true);
                                    if (callback != null) {
                                        callback.onSuccess(o);
                                    }
                                    callbackCountDown.countDown();
                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }
                        });

                        callbackCountDown.await(1, TimeUnit.SECONDS);

                        if (!mIsCallbackCalled.get()) {
                            if (callback != null) {
                                callback.onError(new Throwable("Notification Timeout"));
                            }
                            mIsCallbackCalled.set(true);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void read(int id, OnResult<JSONObject> callback){
        mReaderCallback.put(id, callback);
    }

    public void removeRead(int id){
        if(mReaderCallback.indexOfKey(id) >= 0){
            OnResult<JSONObject> jsonObjectOnResult = mReaderCallback.get(id);
            mReaderCallback.delete(id);
            jsonObjectOnResult = null;
        }
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);

    public native void close();

    static {
        System.loadLibrary("udoo_serial_port");
    }
}