package com.eju.cy.codepush;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by SidneyXu on 2016/12/21.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 23, manifest = Config.NONE)
public class CodePushPrefsTest extends BaseTest {

    private CodePushPrefs prefs;

    @Override
    public void setUp() {
        super.setUp();
        prefs = new CodePushPrefs(application);
    }

    @Test
    public void testSaveVersion() {
        prefs.saveVersion(10);
        int version = prefs.getVersion();
        assertThat(version).isEqualTo(10);
    }

    @Test
    public void testPendingInstall() {
        prefs.savePendingInstall(1, 5);
        assertThat(prefs.isPendingInstall());
        assertThat(prefs.isApk()).isFalse();
    }
}
