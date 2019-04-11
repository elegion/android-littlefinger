package com.elegion.littlefinger.fingerprint;

import androidx.annotation.NonNull;

/**
 * @author Azret Magometov
 */

public interface Callback {
    void onCompleted(@NonNull AuthResult result);
}
