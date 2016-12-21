package com.eju.cy.codepush;

/**
 * A {@code SyncCallback} is used to run code after sync in a background thread.
 *
 * @author SidneyXu
 */
public interface SyncCallback {

    void onSuccess(boolean isUpdate, boolean needReload);

    void onError(EjuCodePushException e);
}
