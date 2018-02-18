package com.elegion.littlefinger.fingerprint;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

/**
 * @author Azret Magometov
 */

public class FingerprintManagerHelper {

    private CancellationSignal mCancellationSignal;
    private FingerprintManager mFingerprintManager;
    private KeyguardManager mKeyguardManager;
    private boolean mCanceledByUser = false;

    public static FingerprintManagerHelper getInstance(Context context) {
        return new FingerprintManagerHelper(context);
    }

    private FingerprintManagerHelper(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        } else {
            mFingerprintManager = null;
        }
        mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
    }

    public boolean isFingerprintSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && mFingerprintManager != null
                && mFingerprintManager.isHardwareDetected();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public AuthResult getSensorState() {
        if (!isFingerprintSupported()) {
            return AuthResult.getNotSupportedResult();
        }

        if (mKeyguardManager == null || !mKeyguardManager.isKeyguardSecure()) {
            return AuthResult.getUnsecuredResult();
        }

        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            return AuthResult.getNoEnrolledFpResult();
        }
        return AuthResult.getReadyToUseResult();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startAuth(@Nullable FingerprintManager.CryptoObject cryptoObject, final @NonNull Callback callback) {

        AuthResult sensorState = getSensorState();

        if (!sensorState.getState().equals(State.READY_TO_USE)) {
            callback.onCompleted(AuthResult.getExceptionResult(new IllegalStateException("Sensor isn't ready to use. Check sensor state")));
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mCanceledByUser = false;

        mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0,
                new FingerprintManager.AuthenticationCallback() {

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        mCancellationSignal = null;
                        callback.onCompleted(AuthResult.getErrorResult(errorCode, (String) errString, mCanceledByUser));
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        callback.onCompleted(AuthResult.getHelpResult(helpCode, (String) helpString));
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult authenticationResult) {
                        mCancellationSignal = null;
                        callback.onCompleted(AuthResult.getRecognizedResult(authenticationResult.getCryptoObject()));
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        callback.onCompleted(AuthResult.getFailedResult());
                    }
                },
                null);

    }

    public void cancelAuth(@Nullable CancelCallback callback) {
        if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
            mCanceledByUser = true;
            mCancellationSignal.cancel();
            if (callback != null) {
                callback.onCancel();
            }
        }
    }

}
