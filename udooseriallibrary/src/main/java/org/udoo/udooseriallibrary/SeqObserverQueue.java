package org.udoo.udooseriallibrary;

/**
 * Created by harlem88 on 23/03/17.
 */

import android.util.Log;

import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by harlem88 on 23/02/16.
 */
public class SeqObserverQueue<T> extends Observable implements Runnable {
    private BlockingQueue<Callable> tBlockingDeque;
    private final static String TAG = "SeqObserverQueue";
    private ExecutorService mExecutorService;
    private AtomicBoolean mBusy;
    private Queue<Observer> observers;
    private boolean changed;
    private int mWAIT;
    private final static int TIMEOUT = 1000;

    public SeqObserverQueue(BlockingQueue<Callable> tBlockingQeque) {
        init(tBlockingQeque, TIMEOUT);
    }

    public SeqObserverQueue(BlockingQueue<Callable> tBlockingQeque, int wait) {
        init(tBlockingQeque, wait);
    }

    private void init(BlockingQueue<Callable> tBlockingQeque, int wait){
        tBlockingDeque = tBlockingQeque;
        mExecutorService = Executors.newSingleThreadExecutor();
        mBusy = new AtomicBoolean(false);
        observers = new ConcurrentLinkedQueue<>();
        changed = false;
        mWAIT = wait;
    }

    public void addObserver(Observer observer) {
        if (observer == null) {
            throw new NullPointerException("observer == null");
        }
        observers.add(observer);
    }

    protected void clearChanged() {
        changed = false;
    }

    public int countObservers() {
        return observers.size();
    }

    public synchronized void deleteObserver(Observer observer) {
        observers.remove(observer);
    }

    public synchronized void deleteObservers() {
        observers.clear();
    }

    public boolean hasChanged() {
        return changed;
    }


    public void notifyObservers() {
        notifyObservers(null);
    }


    @SuppressWarnings("unchecked")
    public void notifyObservers(Object data) {
        int size = 0;
        synchronized (this) {
            if (hasChanged()) {
                clearChanged();
                size = observers.size();
            }
        }
        if (size > 0) {
            for (int i = 0; i< size; i++) {
                observers.poll().update(this, data);
            }
        }
    }

    public void notifyObserver(Object data) {
        observers.poll().update(this, data);
    }

    protected void setChanged() {
        changed = true;
    }

    public void run() {
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        tBlockingDeque.take().call();
                    } catch (Exception e) {
                        setChanged();
                        if (BuildConfig.DEBUG)
                            Log.e(TAG, "run: " + e.getMessage());
                    }
                }
            }
        });
    }
}
