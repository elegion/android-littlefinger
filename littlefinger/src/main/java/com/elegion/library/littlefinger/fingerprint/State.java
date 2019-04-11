package com.elegion.library.littlefinger.fingerprint;

/**
 * @author Azret Magometov
 */

public enum State {
    //'prepare to use' states
    NOT_SUPPORTED,
    UNSECURED,
    NO_ENROLLED_FP,
    READY_TO_USE,

    //'auth callback result' states
    SUCCESS,
    HELP,
    FAIL,
    ERROR,

    //exception container
    EXCEPTION,
    UNDEFINED
}

