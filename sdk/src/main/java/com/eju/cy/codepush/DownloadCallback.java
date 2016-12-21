package com.eju.cy.codepush;

/**
 * A {@code DownloadCallback} is used to run code after downloading in a background thread.
 *
 * @author SidneyXu
 */
public interface DownloadCallback {

    void onSuccess(String downloadDestination);

    void onError(EjuCodePushException e);
}