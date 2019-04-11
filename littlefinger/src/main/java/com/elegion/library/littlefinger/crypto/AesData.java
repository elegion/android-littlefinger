package com.elegion.library.littlefinger.crypto;

import android.util.Base64;

import androidx.annotation.NonNull;

/**
 * @author Azret Magometov
 */

class AesData {

    private static final String SEPARATOR = "-SEPARATOR-";

    private String mKey;
    private Purpose mPurpose;
    private byte[] mBytes = null;
    private byte[] mIv = null;

    @NonNull
    static String makeString(byte[] bytes, byte[] iv) {
        String encodedString = Base64.encodeToString(bytes, Base64.NO_WRAP);
        String initialVector = Base64.encodeToString(iv, Base64.NO_WRAP);
        return String.format("%1$s%2$s%3$s", encodedString, SEPARATOR, initialVector);
    }

    AesData(Purpose purpose, String inputText, String key) {
        mKey = key;
        mPurpose = purpose;
        switch (purpose) {
            case DECODE:
                if (inputText.contains(SEPARATOR)) {
                    String[] temp = inputText.split(SEPARATOR);
                    mBytes = Base64.decode(temp[0], Base64.NO_WRAP);
                    mIv = Base64.decode(temp[1], Base64.NO_WRAP);
                } else {
                    throw new IllegalArgumentException("Input string isn't valid. Missing SEPARATOR. Are you trying to decode not encoded string?");
                }
                break;
            case ENCODE:
            default:
                mBytes = inputText.getBytes();
                break;
        }
    }

    String getKey() {
        return mKey;
    }

    Purpose getPurpose() {
        return mPurpose;
    }

    byte[] getBytes() {
        return mBytes;
    }

    byte[] getIv() {
        return mIv;
    }

}