package com.eju.cy.codepush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The {@code EjuCodePush} is used to update apk resource instantly.
 *
 * @author SidneyXu
 */
public class EjuCodePush {

    private static final String BASE_DIR = "EjuCodePush";

    private static final int TIMEOUT = 15000;

    private CodePushPrefs prefs;

    private Option option;
    private EjuHttpClient client;

    private ExecutorService service;
    private Handler mainHandler;

    private static EjuCodePush sInstance;
    private boolean initialized;

    /**
     * Constructs a new {@code EjuCodePush}.
     */
    public EjuCodePush() {
    }

    /**
     * Return the singleton instance.
     */
    public static EjuCodePush getInstance() {
        if (null == sInstance) {
            sInstance = new EjuCodePush();
        }
        return sInstance;
    }

    /**
     * Initialize this object with given context and option.
     *
     * @param context the android context
     * @param option  the option
     */
    public void initialize(Context context, Option option) {
        if (initialized) {
            return;
        }
        this.option = option.clone();
        if (option.appName == null) {
            option.appName = context.getPackageName();
        }
        checkNull(option.htmlDirectory, "htmlDirectory");
        if (option.baseUrl == null && (option.checkVersionUrl == null || option.downloadUrl == null)) {
            checkNull(null, "checkVersionUrl and downloadUrl");
        }
        if (option.checkVersionUrl == null && option.downloadUrl == null) {
            checkNull(option.baseUrl, "baseUrl");
        }

        Context applicationContext = context.getApplicationContext();
        client = EjuHttpClient.newClient(TIMEOUT, context.getApplicationContext());
        mainHandler = new Handler(Looper.getMainLooper());
        prefs = new CodePushPrefs(context);
        service = Executors.newSingleThreadExecutor();

        initialized = true;

        if (prefs.isPendingInstall()) {
            if (prefs.isApk()) {
                CodePushLog.d("Pending apk exists");
                File apkFile = getApkPath(applicationContext, prefs.getVersion());
                installApk(applicationContext, apkFile.getAbsolutePath());
                prefs.clearPendingIntall();

                CodePushLog.d("Update new apk");
            } else {
                CodePushLog.d("Pending h5 resource exists");
                Utils.copyChildren(getDeployPath(applicationContext).getAbsolutePath(), option.htmlDirectory);
                prefs.clearPendingIntall();
                Utils.delete(new File(getDeployPath(applicationContext).getAbsolutePath()));

                CodePushLog.d("Update pending h5 resource");
            }
        }
    }

    /**
     * Sync apk in a background thread.
     *
     * @param context the android context
     */
    public void syncInBackground(Context context) {
        syncInBackground(context, null);
    }

    /**
     * Sync apk in a background thread.
     *
     * @param context      the android context
     * @param syncCallback the callback will be called on ui thread
     */
    public void syncInBackground(Context context, SyncCallback syncCallback) {
        String defaultMessage = "There is newer version of this application available," +
                "click OK to upgrade now?";
        String defaultPositiveText = "OK";
        String defaultNegativeText = "Remind Later";
        String defaultDownloadMessage = "Download";
        String progressMessage = "download...";
        syncInBackground(context, defaultMessage, defaultPositiveText, defaultNegativeText,
                defaultDownloadMessage, progressMessage, syncCallback);
    }

    /**
     * Sync apk in a background thread.
     *
     * @param context         the android context
     * @param hintMessage     a message notice people there has a new release resource.
     * @param positiveText    a text on positive button.
     * @param negativeText    a text on negative button.
     * @param downloadTitle   a title text on download dialog.
     * @param progressMessage a progress text on download dialog.
     * @param syncCallback    the callback will be called on ui thread
     */
    public void syncInBackground(final Context context, final String hintMessage,
                                 final String positiveText, final String negativeText,
                                 final String downloadTitle, final String progressMessage,
                                 final SyncCallback syncCallback) {
        if (!initialized) {
            throw new RuntimeException("Call codepush.initialize() first.");
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final ReleaseInfo releaseInfo = needUpdate(context);
                    if (null == releaseInfo) {
                        if (null != syncCallback) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    syncCallback.onSuccess(false, false);
                                }
                            });
                        }
                        return;
                    }

                    if (releaseInfo.isForceUpdate()) {
                        if (releaseInfo.isH5Release()) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        forceInstallH5(context, releaseInfo,
                                                hintMessage, positiveText, negativeText,
                                                downloadTitle, progressMessage, syncCallback);
                                    } catch (EjuCodePushException e) {
                                        CodePushLog.e(e);
                                        if (null != syncCallback) {
                                            syncCallback.onError(e);
                                        }
                                    }
                                }
                            });
                        } else {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        forceInstallApk(context, releaseInfo,
                                                hintMessage, positiveText, negativeText,
                                                downloadTitle, progressMessage, syncCallback);
                                    } catch (EjuCodePushException e) {
                                        CodePushLog.e(e);
                                        if (null != syncCallback) {
                                            syncCallback.onError(e);
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        if (releaseInfo.isH5Release()) {
                            pendingInstallH5(context, releaseInfo);
                        } else {
                            pendingInstallApk(context, releaseInfo);
                        }
                        if (null != syncCallback) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    syncCallback.onSuccess(true, false);
                                }
                            });
                        }
                    }
                } catch (final EjuCodePushException e) {
                    if (null != syncCallback) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                syncCallback.onError(e);
                            }
                        });
                    }
                } catch (final Exception e) {
                    if (null != syncCallback) {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                syncCallback.onError(new EjuCodePushException(e));
                            }
                        });
                    }
                }
            }
        });
    }

    private void forceInstallApk(final Context context,
                                 final ReleaseInfo releaseInfo,
                                 String hintMessage,
                                 String positiveText,
                                 String negativeText,
                                 final String downloadTitle,
                                 final String progressMessage,
                                 final SyncCallback syncCallback) throws EjuCodePushException {
        CodePushLog.d("forceInstallApk() --- invoked");
        DialogUtils.openConfirmDialog(context,
                hintMessage,
                positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadFile(context,
                                releaseInfo,
                                downloadTitle,
                                progressMessage, new DownloadCallback() {
                                    @Override
                                    public void onSuccess(String downloadDestination) {
                                        CodePushLog.d("forceInstallApk() --- download apk on " + downloadDestination);
                                        try {
                                            checkMd5(releaseInfo, downloadDestination);

                                            CodePushLog.d("forceInstallApk() --- check md5");

                                            installApk(context, downloadDestination);

                                            CodePushLog.d("forceInstallApk() --- install apk");
                                            if (null != syncCallback) {
                                                syncCallback.onSuccess(true, false);
                                            }
                                        } catch (EjuCodePushException e) {
                                            CodePushLog.e(e);
                                            if (null != syncCallback) {
                                                syncCallback.onError(e);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onError(EjuCodePushException e) {
                                        CodePushLog.e(e);
                                        if (null != syncCallback) {
                                            syncCallback.onError(e);
                                        }
                                    }
                                });
                    }
                }, negativeText, null);
    }

    private void pendingInstallApk(Context context, ReleaseInfo releaseInfo) throws EjuCodePushException {
        CodePushLog.d("pendingInstallApk() --- invoked");

        File apkFile = getDownloadDestination(context, releaseInfo);
        String downloadUrl = getDownloadUrl(releaseInfo);
        String error = Utils.download(downloadUrl, apkFile.getAbsolutePath());
        if (null != error) {
            throw new EjuCodePushException(error);
        }

        CodePushLog.d("pendingInstallApk() --- download apk on " + apkFile.getAbsolutePath());

        checkMd5(releaseInfo, apkFile.getAbsolutePath());

        CodePushLog.d("pendingInstallApk() --- check md5");

        prefs.savePendingInstall(ReleaseInfo.TYPE_APK, releaseInfo.getVersion());
        prefs.saveVersion(releaseInfo.getVersion());

        CodePushLog.d("pendingInstallApk() --- prepare pending install");
    }

    private void forceInstallH5(final Context context, final ReleaseInfo releaseInfo,
                                String hintMessage,
                                String positiveText,
                                String negativeText,
                                final String downloadTitle,
                                final String progressMessage,
                                final SyncCallback syncCallback) throws EjuCodePushException {
        CodePushLog.d("forceInstallH5() --- invoked");
        DialogUtils.openConfirmDialog(context,
                hintMessage,
                positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadFile(context, releaseInfo, downloadTitle, progressMessage, new DownloadCallback() {
                            @Override
                            public void onSuccess(String downloadDestination) {
                                CodePushLog.d("forceInstallH5() --- down h5 resource on " + downloadDestination);
                                File zipFile = new File(downloadDestination);
                                try {
                                    checkMd5(releaseInfo, downloadDestination);

                                    CodePushLog.d("forceInstallH5() --- check md5");

                                    Utils.decompressFile(zipFile, getDeployPath(context));

                                    CodePushLog.d("forceInstallH5() --- decompress h5 resource");

                                    Utils.delete(zipFile);

                                    Utils.copyChildren(getDeployPath(context).getAbsolutePath(), option.htmlDirectory);
                                    Utils.delete(new File(getDeployPath(context).getAbsolutePath()));

                                    prefs.saveVersion(releaseInfo.getVersion());

                                    if (null != syncCallback) {
                                        syncCallback.onSuccess(true, true);
                                    }
                                } catch (EjuCodePushException e) {
                                    CodePushLog.e(e);
                                    if (null != syncCallback) {
                                        syncCallback.onError(e);
                                    }
                                } catch (IOException e) {
                                    CodePushLog.e(e);
                                    if (null != syncCallback) {
                                        syncCallback.onError(new EjuCodePushException(e));
                                    }
                                }

                            }

                            @Override
                            public void onError(EjuCodePushException e) {
                                CodePushLog.e(e);
                                if (null != syncCallback) {
                                    syncCallback.onError(new EjuCodePushException(e));
                                }
                            }
                        });
                    }
                }, negativeText, null);
    }

    private void pendingInstallH5(Context context, ReleaseInfo releaseInfo) throws EjuCodePushException, IOException {
        CodePushLog.d("pendingInstallH5() --- invoked");

        File zipFile = getDownloadDestination(context, releaseInfo);
        String downloadUrl = getDownloadUrl(releaseInfo);
        String error = Utils.download(downloadUrl, zipFile.getAbsolutePath());
        if (null != error) {
            throw new EjuCodePushException(error);
        }

        CodePushLog.d("pendingInstallH5() -- download h5 zip file on " + zipFile.getAbsolutePath());

        checkMd5(releaseInfo, zipFile.getAbsolutePath());

        CodePushLog.d("pendingInstallH5() -- check md5");

        Utils.decompressFile(zipFile, getDeployPath(context));

        CodePushLog.d("pendingInstallH5() -- decompress h5 zip file");

        Utils.delete(zipFile);

        prefs.savePendingInstall(ReleaseInfo.TYPE_H5, releaseInfo.getVersion());
        prefs.saveVersion(releaseInfo.getVersion());

        CodePushLog.d("pendingInstallH5() --- prepare pending install");
    }


    public ReleaseInfo checkVersion(Context context) throws EjuCodePushException {
        String checkVersionUrl = getCheckVersionUrl(context);
        CodePushLog.d(String.format("checkVersion() -- CheckVersion url is %s.",
                checkVersionUrl));
        JSONObject result = internalCheckVersion(checkVersionUrl);
        if (null == result) return null;
        return new ReleaseInfo(result);
    }

    public ReleaseInfo needUpdate(Context context) throws EjuCodePushException {
        final int currentVersion = prefs.getVersion();
        CodePushLog.d(String.format("needUpdate() -- Current version is %s.", "" + currentVersion));

        ReleaseInfo releaseInfo = checkVersion(context);
        if (null == releaseInfo) return null;
        if (releaseInfo.isH5Release()) {
            if (releaseInfo.getVersion() != currentVersion) {
                return releaseInfo;
            }
            return null;
        }
        return releaseInfo;
    }

    private JSONObject internalCheckVersion(String url) throws EjuCodePushException {
        EjuRequest request = EjuRequest.newBuilder()
                .url(url)
                .get()
                .build();
        EjuResponse response = client.execute(request);
        if (response.isSuccessful()) {
            JSONObject jsonObject = response.getBodyAsJSONObject();
            return jsonObject.optJSONObject("data");
        }
        String error = response.getBodyAsString();
        throw new EjuCodePushException(error);
    }

    private File getDownloadDestination(Context context, ReleaseInfo releaseInfo) {
        File parentDir = context.getDir(BASE_DIR, Context.MODE_PRIVATE);
        File updateDir = new File(parentDir, "update");
        if (!updateDir.exists()) {
            updateDir.mkdirs();
        }
        if (releaseInfo.isH5Release()) {
            return new File(updateDir, releaseInfo.getVersion() + ".zip");
        } else {
            return getApkPath(context, releaseInfo.getVersion());
        }
    }

    private File getApkPath(Context context, int version) {
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = context.getPackageName();
        return new File(downloadDir, fileName + "-" + version + ".apk");
    }

    private File getDeployPath(Context context) {
        File parentDir = context.getDir(BASE_DIR, Context.MODE_PRIVATE);
        File deployDir = new File(parentDir, "deploy");
        if (!deployDir.exists()) {
            deployDir.mkdirs();
        }
        return deployDir;
    }

    /* package for test */ String getCheckVersionUrl(Context context) {
        if (null != option.checkVersionUrl) {
            return option.checkVersionUrl;
        }
        int appVersion = Utils.getAppVersion(context);
        int h5Version = prefs.getVersion();
        String url = String.format("%s/checkVersion?appVersion=%s&appName=%s&os=android&h5Version=%s",
                option.baseUrl,
                appVersion,
                option.appName,
                h5Version);
        return url;
    }

    /* package for test */ String getDownloadUrl(ReleaseInfo releaseInfo) {
        if (null != option.downloadUrl) {
            return option.downloadUrl;
        }
        String url = String.format("%s/download?appName=%s&version=%s&os=android&type=%s",
                option.baseUrl,
                option.appName,
                releaseInfo.getVersion(),
                releaseInfo.getType());
        return url;
    }

    private void checkMd5(ReleaseInfo releaseInfo, String filePath) throws EjuCodePushException {
        String md5 = Md5.encodeFile(new File(filePath));
        if (!md5.equals(releaseInfo.getMd5())) {
            throw new EjuCodePushException(EjuCodePushException.MD5_NOT_MATCHED,
                    String.format("md5 not matched, excepted is %s, actually is %s",
                            releaseInfo.getMd5(),
                            md5));
        }
    }

    private void installApk(Context context, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void downloadFile(Context activityContext,
                             ReleaseInfo releaseInfo,
                             String downloadTitle,
                             String progressMessage,
                             DownloadCallback callback) {
        File downloadFile = getDownloadDestination(activityContext, releaseInfo);
        String downloadUrl = getDownloadUrl(releaseInfo);

        DialogUtils.openDownloadDialog(activityContext,
                downloadTitle,
                progressMessage,
                downloadUrl,
                downloadFile.getAbsolutePath(),
                false,
                callback);
    }

    private void checkNull(Object object, String prefix) {
        if (null == object) {
            throw new RuntimeException(prefix + " is necessary!");
        }
    }
}