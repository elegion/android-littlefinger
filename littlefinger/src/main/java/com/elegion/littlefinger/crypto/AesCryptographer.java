package com.elegion.littlefinger.crypto;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author Azret Magometov
 */
@TargetApi(Build.VERSION_CODES.M)
public class AesCryptographer {

    private static final String TAG = AesCryptographer.class.getSimpleName();

    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private static AesCryptographer INSTANCE;
    private KeyStoreManager mKeyStoreManager;
    private AesData mAesData;

    @NonNull
    public synchronized static AesCryptographer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AesCryptographer();
        }
        return INSTANCE;
    }

    private AesCryptographer() {
        mKeyStoreManager = KeyStoreManager.getInstance();
    }

    @NonNull
    public String encode(Cipher cipher) throws Exception {
        try {
            byte[] bytes = cipher.doFinal(mAesData.getBytes());
            byte[] iv = cipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            return AesData.makeString(bytes, iv);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidParameterSpecException exception) {
            throw new Exception("exception while encoding with cipher", exception);
        }
    }

    @NonNull
    public String decode(Cipher cipher) throws Exception {
        try {
            byte[] bytes = mAesData.getBytes();
            return new String(cipher.doFinal(bytes));
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            throw new Exception("exception while decoding with cipher", exception);
        }
    }

    @NonNull
    public FingerprintManager.CryptoObject getCryptoObject(String text, Purpose purpose, String key) throws Exception {
        mAesData = new AesData(purpose, text, key);
        Cipher cipher = null;
        switch (purpose) {
            case DECODE:
                cipher = initDecodeCipher(mAesData.getKey(), mAesData.getIv());
                break;
            case ENCODE:
                cipher = initEncodeCipher(mAesData.getKey());
                break;
        }
        return new FingerprintManager.CryptoObject(cipher);
    }

    @NonNull
    private Cipher initDecodeCipher(String key, byte[] iv) throws Exception {
        SecretKey secretKey = mKeyStoreManager.getSecretKey(key);
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher;
        } catch (KeyPermanentlyInvalidatedException e) {
            mKeyStoreManager.deleteKey(key);
            throw e;
        } catch (GeneralSecurityException e) {
            throw new Exception("An exception happens while initializing Cipher", e);
        }
    }

    @NonNull
    private Cipher initEncodeCipher(String key) throws Exception {
        if (!mKeyStoreManager.containsKey(key)) {
            generateKey(key);
        }

        SecretKey secretKey = mKeyStoreManager.getSecretKey(key);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher;
        } catch (KeyPermanentlyInvalidatedException e) {
            mKeyStoreManager.deleteKey(key);
            throw e;
        } catch (GeneralSecurityException e) {
            Log.e(TAG, "initEncodeCipher: ", e);
            throw new Exception("An exception happens while initializing Cipher", e);
        }
    }

    private void generateKey(String alias) throws Exception {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
            if (keyGenerator != null) {
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(false);
                }

                keyGenerator.init(builder.build());
                keyGenerator.generateKey();
            }
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException e) {
            throw new Exception("Can't generate key " + alias, e);
        }
    }

}
