package com.elegion.littlefinger.fingerprint;

import android.hardware.fingerprint.FingerprintManager;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.support.annotation.NonNull;

/**
 * @author Azret Magometov
 */

public class AuthResult {

    private State mState = State.UNDEFINED;
    private int mErrorCode = -1;
    private boolean mCanceledByUser = false;
    private FingerprintManager.CryptoObject mCryptoObject = null;
    private Throwable mThrowable = null;
    private String mData;

    private AuthResult() {
    }

    private AuthResult(Builder builder) {
        setState(builder.mState);
        setErrorCode(builder.mErrorCode);
        setCanceledByUser(builder.mCanceledByUser);
        setCryptoObject(builder.mCryptoObject);
        setThrowable(builder.mThrowable);
        setData(builder.mData);
    }

    @NonNull
    public static AuthResult getNotSupportedResult() {
        return AuthResult.newBuilder()
                .state(State.NOT_SUPPORTED)
                .throwable(new IllegalStateException("Device does not support finger print"))
                .build();
    }

    @NonNull
    public static AuthResult getUnsecuredResult() {
        return AuthResult.newBuilder()
                .state(State.UNSECURED)
                .throwable(new IllegalStateException("Device is not secured"))
                .build();
    }

    public static AuthResult getNoEnrolledFpResult() {
        return AuthResult.newBuilder()
                .state(State.NO_ENROLLED_FP)
                .throwable(new IllegalStateException("There is no enrolled fingerprints on this device"))
                .build();
    }

    public static AuthResult getReadyToUseResult() {
        return AuthResult.newBuilder()
                .state(State.READY_TO_USE)
                .data("Touch the sensor")
                .build();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static AuthResult getRecognizedResult(FingerprintManager.CryptoObject cryptoObject) {
        return AuthResult.newBuilder()
                .state(State.SUCCESS)
                .data("Recognition success")
                .cryptoObject(cryptoObject)
                .build();
    }

    public static AuthResult getHelpResult(int code, String message) {
        return AuthResult.newBuilder()
                .state(State.HELP)
                .throwable(new IllegalStateException(message))
                .errorCode(code)
                .build();
    }

    public static AuthResult getFailedResult() {
        return AuthResult.newBuilder()
                .state(State.FAIL)
                .throwable(new IllegalStateException("Can't recognize. User should touch sensor again"))
                .build();
    }

    public static AuthResult getErrorResult(int code, String message, boolean canceledByUser) {
        return AuthResult.newBuilder()
                .state(State.ERROR)
                .throwable(new IllegalStateException(message))
                .errorCode(code)
                .canceledByUser(canceledByUser)
                .build();
    }

    public static AuthResult getExceptionResult(Throwable t) {
        return AuthResult.newBuilder()
                .state(State.EXCEPTION)
                .throwable(t)
                .build();
    }

    public static AuthResult getCryptoOperationResult(String dataText) {
        return AuthResult.newBuilder()
                .state(State.SUCCESS)
                .data(dataText)
                .build();
    }

    public State getState() {
        return mState;
    }

    private void setState(State state) {
        mState = state;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    private void setErrorCode(int errorCode) {
        mErrorCode = errorCode;
    }

    public boolean isCanceledByUser() {
        return mCanceledByUser;
    }

    private void setCanceledByUser(boolean canceledByUser) {
        mCanceledByUser = canceledByUser;
    }

    public FingerprintManager.CryptoObject getCryptoObject() {
        return mCryptoObject;
    }

    private void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }

    private void setThrowable(Throwable throwable) {
        mThrowable = throwable;
    }

    public String getData() {
        return mData;
    }

    private void setData(String data) {
        mData = data;
    }

    public boolean isKeyInvalidated() {
        return mState == State.EXCEPTION && mThrowable instanceof KeyPermanentlyInvalidatedException;
    }

    public static final class Builder {
        private State mState;
        private int mErrorCode;
        private boolean mCanceledByUser;
        private FingerprintManager.CryptoObject mCryptoObject;
        private Throwable mThrowable;
        private String mData;

        private Builder() {
        }

        public Builder state(State mState) {
            this.mState = mState;
            return this;
        }

        public Builder errorCode(int mErrorCode) {
            this.mErrorCode = mErrorCode;
            return this;
        }

        public Builder canceledByUser(boolean mCanceledByUser) {
            this.mCanceledByUser = mCanceledByUser;
            return this;
        }

        public Builder cryptoObject(FingerprintManager.CryptoObject mCryptoObject) {
            this.mCryptoObject = mCryptoObject;
            return this;
        }

        public Builder throwable(Throwable mThrowable) {
            this.mThrowable = mThrowable;
            return this;
        }

        public Builder data(String mData) {
            this.mData = mData;
            return this;
        }

        public AuthResult build() {
            return new AuthResult(this);
        }
    }
}
