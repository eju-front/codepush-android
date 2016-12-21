package com.eju.cy.codepush;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This class is used to download file in a background thread.
 *
 * @author SidneyXu
 */
/* package */ class DownloadTask extends AsyncTask<String, Integer, String> {

    private String downloadDestination;
    private DownloadCallback downloadCallback;
    private ProgressDialog progressDialog;

    public DownloadTask(String downloadDestination,
                        DownloadCallback downloadCallback,
                        ProgressDialog progressDialog) {
        this.downloadDestination = downloadDestination;
        this.downloadCallback = downloadCallback;
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        progressDialog.setProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String exceptionMessage) {
        super.onPostExecute(exceptionMessage);
        if (null == exceptionMessage) {
            downloadCallback.onSuccess(downloadDestination);
        } else {
            downloadCallback.onError(new EjuCodePushException(exceptionMessage));
        }
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // response should contain content-length
            int fileLength = connection.getContentLength();

            CodePushLog.d("file length is " + fileLength);

            // download the file
            input = connection.getInputStream();
            File target = new File(downloadDestination);
            target.createNewFile();
            output = new FileOutputStream(downloadDestination);

            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                // publishing the progress....
                if (fileLength > 0) {
                    publishProgress((int) (total * 100 / fileLength));
                }
                output.write(data, 0, count);
            }
            publishProgress(100);
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }
}