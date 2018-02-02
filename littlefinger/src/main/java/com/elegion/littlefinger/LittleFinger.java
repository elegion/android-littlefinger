package com.elegion.littlefinger;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.elegion.littlefinger.crypto.AesCryptographer;
import com.elegion.littlefinger.crypto.CryptoAlgorithm;
import com.elegion.littlefinger.crypto.Purpose;
import com.elegion.littlefinger.crypto.RsaCryptographer;
import com.elegion.littlefinger.fingerprint.AuthResult;
import com.elegion.littlefinger.fingerprint.Callback;
import com.elegion.littlefinger.fingerprint.CancelCallback;
import com.elegion.littlefinger.fingerprint.FingerprintManagerHelper;
import com.elegion.littlefinger.fingerprint.State;

import javax.crypto.Cipher;

import static com.elegion.littlefinger.crypto.Purpose.DECODE;
import static com.elegion.littlefinger.crypto.Purpose.ENCODE;

/**
 * @author Azret Magometov
 */

public class LittleFinger {

    private FingerprintManagerHelper mFingerprintManagerHelper;

    public LittleFinger(@NonNull Context context) {
        mFingerprintManagerHelper = FingerprintManagerHelper.getInstance(context);
    }

    public boolean isFingerprintSupported() {
        return mFingerprintManagerHelper.isFingerprintSupported();
    }

    public boolean isReadyToUse() {
        return mFingerprintManagerHelper.getSensorState().getState().equals(State.READY_TO_USE);
    }

    public AuthResult getSensorState() {
        return mFingerprintManagerHelper.getSensorState();
    }

    public void authenticate(Callback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintManagerHelper.startAuth(null, callback);
        } else {
            callback.onCompleted(AuthResult.getNotSupportedResult());
        }
    }

    public void encode(String textToEncode, String key, CryptoAlgorithm algorithm, Callback callback) {
        if (isReadyToUse()) {
            performCryptoOperation(textToEncode, key, algorithm, ENCODE, callback);
        } else {
            callback.onCompleted(getSensorState());
        }
    }

    public void decode(final String textToDecode, String key, CryptoAlgorithm algorithm, final Callback callback) {
        if (isReadyToUse()) {
            performCryptoOperation(textToDecode, key, algorithm, DECODE, callback);
        } else {
            callback.onCompleted(getSensorState());
        }
    }

    private void performCryptoOperation(String inputText, String key, CryptoAlgorithm algorithm, Purpose purpose, Callback callback) {
        if (algorithm.equals(CryptoAlgorithm.AES)) {
            performCryptoOperationWithAes(purpose, inputText, key, callback);
        }

        if (algorithm.equals(CryptoAlgorithm.RSA)) {
            if (purpose.equals(ENCODE)) {
                encodeWithRsa(inputText, key, callback);
            } else if (purpose.equals(DECODE)) {
                decodeWithRsa(inputText, key, callback);
            }
        }
    }

    public void cancelAuth() {
        mFingerprintManagerHelper.cancelAuth(null);
    }

    public void cancelAuth(@Nullable CancelCallback cancelCallback) {
        mFingerprintManagerHelper.cancelAuth(cancelCallback);
    }

    private void encodeWithRsa(String textToEncode, String key, Callback callback) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            RsaCryptographer cryptographer = RsaCryptographer.getInstance();
            try {
                String encoded = cryptographer.encode(textToEncode, key);
                callback.onCompleted(AuthResult.getCryptoOperationResult(encoded));
            } catch (Exception e) {
                callback.onCompleted(AuthResult.getExceptionResult(e));
            }
        } else {
            callback.onCompleted(AuthResult.getNotSupportedResult());
        }
    }

    private void decodeWithRsa(final String textToDecode, String key, final Callback callback) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final RsaCryptographer cryptographer = RsaCryptographer.getInstance();
            try {
                FingerprintManager.CryptoObject cryptoObject = cryptographer.getCryptoObject(key);
                mFingerprintManagerHelper.startAuth(cryptoObject, result -> {
                    switch (result.getState()) {
                        case SUCCESS:
                            try {
                                String decoded = cryptographer.decode(textToDecode, result.getCryptoObject().getCipher());
                                callback.onCompleted(AuthResult.getCryptoOperationResult(decoded));
                            } catch (Exception e) {
                                callback.onCompleted(AuthResult.getExceptionResult(e));
                            }
                            break;
                        default:
                            callback.onCompleted(result);
                    }
                });
            } catch (Exception e) {
                callback.onCompleted(AuthResult.getExceptionResult(e));
            }
        } else {
            callback.onCompleted(AuthResult.getNotSupportedResult());
        }
    }

    private void performCryptoOperationWithAes(final Purpose purpose, String text, String key, final Callback callback) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            final AesCryptographer aesCryptographer = AesCryptographer.getInstance();
            try {
                FingerprintManager.CryptoObject cryptoObject = aesCryptographer.getCryptoObject(text, purpose, key);
                mFingerprintManagerHelper.startAuth(cryptoObject, result -> {
                    switch (result.getState()) {
                        case SUCCESS:
                            try {
                                Cipher cipher = result.getCryptoObject().getCipher();
                                String resultString = purpose.equals(ENCODE) ?
                                        aesCryptographer.encode(cipher) :
                                        aesCryptographer.decode(cipher);
                                callback.onCompleted(AuthResult.getCryptoOperationResult(resultString));
                            } catch (Exception e) {
                                callback.onCompleted(AuthResult.getExceptionResult(e));
                            }
                            break;
                        default:
                            callback.onCompleted(result);
                    }
                });
            } catch (Exception e) {
                callback.onCompleted(AuthResult.getExceptionResult(e));
            }
        } else {
            callback.onCompleted(AuthResult.getNotSupportedResult());
        }
    }

}
