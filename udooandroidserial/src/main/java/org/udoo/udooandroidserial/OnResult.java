package org.udoo.udooandroidserial;

/**
 * Created by harlem88 on 17/03/16.
 */

public interface OnResult<T> {
    void onSuccess(T o);
    void onError(Throwable throwable);
}
