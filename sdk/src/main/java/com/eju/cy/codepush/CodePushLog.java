package com.eju.cy.codepush;

import android.util.Log;

/**
 * A log util.
 *
 * @author SidneyXu
 */
public class CodePushLog {

    private static final String TAG = CodePushLog.class.getSimpleName();

    private static boolean debug = false;

    public static void setDebug(boolean debug) {
        CodePushLog.debug = debug;
    }

    public static void d(String message) {
        if (debug) {
            Log.d(TAG, message);
        }
    }

    public static void e(Throwable throwable) {
        if (debug) {
            Log.e(TAG, "error occurs", throwable);
        }
    }

    public static void e(String message, Throwable throwable) {
        if (debug) {
            Log.e(TAG, message, throwable);
        }
    }
}