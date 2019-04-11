package com.elegion.littlefinger.crypto;

import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * @author Azret Magometov
 */
@RequiresApi(api = Build.VERSION_CODES.M)
class KeyStoreManager {

    private static final String TAG = KeyStoreManager.class.getSimpleName();

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static KeyStoreManager INSTANCE;
    private KeyStore mKeyStore;

    private KeyStoreManager() {
        try {
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            mKeyStore.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.d(TAG, "KeyStoreManager: ", e);
        }
    }

    static synchronized KeyStoreManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new KeyStoreManager();
        }
        return INSTANCE;
    }

    @Nullable
    PrivateKey getPrivateKey(String alias) throws Exception {
        return (PrivateKey) getKey(alias);
    }

    SecretKey getSecretKey(String alias) throws Exception {
        return (SecretKey) getKey(alias);
    }

    @Nullable
    private Key getKey(String alias) throws Exception {
        try {
            mKeyStore.load(null);
            return mKeyStore.getKey(alias, null);
        } catch (UnrecoverableKeyException e) {
            throw e;
        } catch (GeneralSecurityException | IOException e) {
            throw new Exception("An exception happens while getting Key", e);
        }
    }

    void deleteKey(String alias) throws Exception {
        try {
            if (mKeyStore.containsAlias(alias)) {
                mKeyStore.deleteEntry(alias);
            }
        } catch (KeyStoreException e) {
            throw new Exception("Keystore exception while deleting key", e);
        }
    }

    PublicKey getPublicKey(String alias) throws Exception {
        try {
            mKeyStore.load(null);
            return mKeyStore.getCertificate(alias).getPublicKey();
        } catch (GeneralSecurityException | IOException e) {
            throw new Exception("Keystore exception while getting public key", e);
        }
    }

    boolean containsKey(String key) throws Exception {
        try {
            mKeyStore.load(null);
            return mKeyStore.containsAlias(key);
        } catch (GeneralSecurityException | IOException e) {
            throw new Exception("An exception happens while working with KeyStore ", e);
        }
    }
}
