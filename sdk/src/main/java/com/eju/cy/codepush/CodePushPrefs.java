package com.eju.cy.codepush;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * This class is used to store code push info into shared preferences.
 *
 * @author SidneyXu
 */
/* package */ class CodePushPrefs {

    public static final String PREFERENCES_NAME = "EjuCodePush";
    public static final String PREFS_VERSION = "version";
    public static final String PREFS_PENDING_INSTALL = "pending_install";
    public static final String PREFS_PENDING_INSTALL_VERSION = "pending_install_version";
    public static final String PREFS_INSTALL_MODE = "install_mode";

    private Context context;

    public CodePushPrefs(Context context) {
        this.context = context.getApplicationContext();
    }

    public int getVersion() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREFS_VERSION, -1);
    }

    public CodePushPrefs saveVersion(int version) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putInt(PREFS_VERSION, version)
                .apply();
        return this;
    }

    public CodePushPrefs savePendingInstall(int mode, int pendingInstallVersion) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .putBoolean(PREFS_PENDING_INSTALL, true)
                .putInt(PREFS_INSTALL_MODE, mode)
                .putInt(PREFS_PENDING_INSTALL_VERSION, pendingInstallVersion)
                .apply();
        return this;
    }

    public CodePushPrefs clearPendingIntall() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        preferences.edit()
                .remove(PREFS_PENDING_INSTALL)
                .remove(PREFS_INSTALL_MODE)
                .remove(PREFS_PENDING_INSTALL_VERSION)
                .apply();
        return this;
    }

    public boolean isPendingInstall() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getBoolean(PREFS_PENDING_INSTALL, false);
    }

    public boolean isApk() {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(PREFS_INSTALL_MODE, -1) == ReleaseInfo.TYPE_APK;
    }

}
