package com.eju.cy.codepush.demo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.eju.cy.codepush.EjuCodePush;
import com.eju.cy.codepush.EjuCodePushException;
import com.eju.cy.codepush.SyncCallback;

/**
 * Created by SidneyXu on 2016/11/28.
 */

public class MainActivity extends FragmentActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private EjuCodePush codePush;
    private WebView webView;

    @SuppressLint("JavascriptInterface")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        Button btnCheckVersion = new Button(this);
        btnCheckVersion.setText("Check Version");
        Button btnDownload = new Button(this);
        btnDownload.setText("Download");
        Button btnSync = new Button(this);
        btnSync.setText("Sync");
        container.addView(btnCheckVersion);
        container.addView(btnDownload);
        container.addView(btnSync);

        webView = new WebView(this);
        container.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(container);

        webView.getSettings().setJavaScriptEnabled(true);
        WebView.setWebContentsDebuggingEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(this, "android");

        App app = (App) getApplication();
        codePush = app.getCodePush();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstStart = preferences.getBoolean("firstStart", true);
        if (firstStart) {
            //data/files
            Utils.copyAssetFolder(getAssets(), "www", app.getHtmlPath());
            preferences.edit().putBoolean("firstStart", false).apply();
        }
        String url = app.getHtmlPath() + "/index.html";
        webView.loadUrl("file:///" + url);

        btnSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sync();
            }
        });
    }

    private void sync() {
        codePush.syncInBackground(this,
                "有新版本的应用",
                "确定",
                "下次提醒",
                "更新",
                "下载中。。。",
                new SyncCallback() {
                    @Override
                    public void onSuccess(boolean isUpdate, boolean needReload) {
                        Log.d(TAG, "sync successful and isUpdate " + isUpdate);
                        if (needReload) {
                            webView.reload();
                        }
                    }

                    @Override
                    public void onError(EjuCodePushException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

}
