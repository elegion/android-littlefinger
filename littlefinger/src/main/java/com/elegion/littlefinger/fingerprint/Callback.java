package com.elegion.littlefinger.fingerprint;

import android.support.annotation.NonNull;

/**
 * @author Azret Magometov
 */

public interface Callback {
    void onCompleted(@NonNull AuthResult result);
}
