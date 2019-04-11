package com.elegion.littlefinger.crypto;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * @author azret.magometov on 08-Nov-16.
 */

@RequiresApi(api = Build.VERSION_CODES.M)
public class RsaCryptographer {
    private static final String TAG = RsaCryptographer.class.getSimpleName();

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private KeyStoreManager mKeyStoreManager;

    private static RsaCryptographer INSTANCE;

    private RsaCryptographer() {
        mKeyStoreManager = KeyStoreManager.getInstance();
    }

    @NonNull
    public synchronized static RsaCryptographer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RsaCryptographer();
        }
        return INSTANCE;
    }

    public String encode(String inputString, String key) throws Exception {
        try {
            Cipher encodingCipher = initEncodeCipher(key);
            byte[] bytes = encodingCipher.doFinal(inputString.getBytes());
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            throw new Exception("Can't encode", exception);
        }
    }

    private void createKey(String key) throws Exception {
        if (!mKeyStoreManager.containsKey(key)) {
            generateKeyPair(key);
        }
    }

    public String decode(String encodedString, Cipher cipher) throws Exception {
        try {
            byte[] bytes = Base64.decode(encodedString, Base64.NO_WRAP);
            return new String(cipher.doFinal(bytes));
        } catch (IllegalBlockSizeException | BadPaddingException exception) {
            throw new Exception("Can't decode", exception);
        }
    }

    @NonNull
    public FingerprintManager.CryptoObject getCryptoObject(String key) throws Exception {
        Cipher cipher = initDecodeCipher(key);
        return new FingerprintManager.CryptoObject(cipher);
    }

    private Cipher initDecodeCipher(String key) throws Exception {
        try {
            PrivateKey privateKey = mKeyStoreManager.getPrivateKey(key);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher;
        } catch (KeyPermanentlyInvalidatedException | UnrecoverableKeyException e) {
            mKeyStoreManager.deleteKey(key);
            if (e instanceof UnrecoverableKeyException) {
                throw new KeyPermanentlyInvalidatedException("An exception happens while getting Key", e);
            }
            throw e;
        } catch (GeneralSecurityException e) {
            throw new Exception("Exception while initializing decoding cipher", e);
        }
    }

    private Cipher initEncodeCipher(String key) throws Exception {
        try {
            createKey(key);

            PublicKey publicKey = mKeyStoreManager.getPublicKey(key);
            // workaround for using public key
            // from https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.html
            PublicKey unrestricted = KeyFactory.getInstance(publicKey.getAlgorithm()).generatePublic(new X509EncodedKeySpec(publicKey.getEncoded()));
            // from https://code.google.com/p/android/issues/detail?id=197719

            OAEPParameterSpec spec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT);

            Cipher cipher;
            cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, unrestricted, spec);

            return cipher;

        } catch (GeneralSecurityException e) {
            throw new Exception("An exception happens while initializing Cipher", e);
        }
    }

    private void generateKeyPair(String alias) throws Exception {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
            if (keyPairGenerator != null) {
                KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                        .setUserAuthenticationRequired(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    builder.setInvalidatedByBiometricEnrollment(false);
                }

                keyPairGenerator.initialize(builder.build());
                keyPairGenerator.generateKeyPair();
            }
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new Exception("An exception happens while generating a new KeyPair", e);
        }
    }

}

