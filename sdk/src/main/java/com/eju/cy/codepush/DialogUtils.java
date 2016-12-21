package com.eju.cy.codepush;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * This class is used to show dialog.
 *
 * @author SidneyXu
 */

/* package */ class DialogUtils {

    public static void openConfirmDialog(Context context,
                                         String message,
                                         String positiveText,
                                         DialogInterface.OnClickListener positiveHandler,
                                         String negativeText,
                                         DialogInterface.OnClickListener negativeHandler) {
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setNegativeButton(negativeText, negativeHandler)
                .setPositiveButton(positiveText, positiveHandler)
                .create();
        alertDialog.show();
    }

    public static void openDownloadDialog(Context context,
                                          String downloadTitle,
                                          String progressMsg,
                                          String url,
                                          String filePath,
                                          boolean cancelable,
                                          DownloadCallback callback) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(downloadTitle);
        progressDialog.setMessage(progressMsg);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        final DownloadTask downloadTask = new DownloadTask(filePath, callback, progressDialog);
        downloadTask.execute(url);

        if (cancelable) {
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                }
            });
        }
        progressDialog.show();
    }
}
