package com.eju.cy.codepush.demo;

import android.app.Application;

import com.eju.cy.codepush.CodePushLog;
import com.eju.cy.codepush.EjuCodePush;
import com.eju.cy.codepush.Option;

import java.io.File;

/**
 * Created by SidneyXu on 2016/11/28.
 */
public class App extends Application {

    private EjuCodePush codePush;
    private String htmlPath;

    @Override
    public void onCreate() {
        super.onCreate();

        File filesDir = getFilesDir();
        File htmlDir = new File(filesDir, "www");
        htmlPath = htmlDir.getAbsolutePath();

        CodePushLog.setDebug(true);

        codePush = new EjuCodePush();
        Option option = new Option();
        option.appName = "demo";
        option.baseUrl = "http://172.29.32.215:10086/app";
        option.htmlDirectory = htmlPath;
        codePush.initialize(this, option);
    }

    public EjuCodePush getCodePush() {
        return codePush;
    }

    public String getHtmlPath() {
        return htmlPath;
    }
}
