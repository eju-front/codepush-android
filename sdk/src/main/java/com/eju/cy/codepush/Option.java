package com.eju.cy.codepush;

/**
 * This class is used to make a EjuCodePush configuration.
 *
 * @author SidneyXu
 */
public class Option implements Cloneable {

    public String appName;

    public String baseUrl;

    public String checkVersionUrl;

    public String downloadUrl;

    public String htmlDirectory;

    @Override
    public Option clone() {
        Option option = new Option();
        option.baseUrl = baseUrl;
        option.htmlDirectory = htmlDirectory;
        option.appName = appName;
        option.downloadUrl = downloadUrl;
        option.checkVersionUrl = checkVersionUrl;
        return option;
    }
}
