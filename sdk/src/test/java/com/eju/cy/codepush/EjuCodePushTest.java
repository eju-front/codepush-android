package com.eju.cy.codepush;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Created by SidneyXu on 2016/12/20.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class EjuCodePushTest extends BaseTest {

    private String baseUrl = "http://localhost";
    private EjuCodePush codePush;

    @Override
    public void setUp() {
        super.setUp();
        codePush = new EjuCodePush();
        Option option = new Option();
        option.baseUrl = baseUrl;
        option.htmlDirectory = "www";
        option.appName = "foobar";
        codePush.initialize(application, option);
    }

    @Test
    public void testGetInstanceThenReturnSameObject() {
        EjuCodePush codePush = EjuCodePush.getInstance();
        assertThat(codePush).isNotNull();
        assertThat(EjuCodePush.getInstance()).isEqualTo(codePush);
    }

    @Test
    public void testWithoutInitialize() {
        EjuCodePush codePush = new EjuCodePush();
        try {
            codePush.syncInBackground(application);
            fail("calling method should failed due to not initialized");
        } catch (Exception e) {
            assertThat(e).isNotNull();
        }
    }

    @Test
    public void testSetBaseUrl() throws Exception {
        String checkVersionUrl = codePush.getCheckVersionUrl(application);
        ReleaseInfo releaseInfo = new ReleaseInfo();
        String downloadUrl = codePush.getDownloadUrl(releaseInfo);
        assertThat(checkVersionUrl).startsWith(baseUrl);
        assertThat(downloadUrl).startsWith(downloadUrl);
    }

    @Test
    public void testCustomizeUrl() throws Exception {
        EjuCodePush codePush = new EjuCodePush();
        Option option = new Option();
        option.htmlDirectory = "www";
        option.appName = "foobar";
        option.checkVersionUrl = "https://checkversion";
        option.downloadUrl = "https://download";
        codePush.initialize(application, option);

        String checkVersionUrl = codePush.getCheckVersionUrl(application);
        ReleaseInfo releaseInfo = new ReleaseInfo();
        String downloadUrl = codePush.getDownloadUrl(releaseInfo);
        assertThat(checkVersionUrl).startsWith(option.checkVersionUrl);
        assertThat(downloadUrl).startsWith(option.downloadUrl);
    }

}
