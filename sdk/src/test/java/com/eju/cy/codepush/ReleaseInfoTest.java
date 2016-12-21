package com.eju.cy.codepush;

import org.json.JSONObject;
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
public class ReleaseInfoTest extends BaseTest {

    @Test
    public void testConstructInstance() throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("md5", "xxx");
        jsonObject.put("version", 2);
        jsonObject.put("type", ReleaseInfo.TYPE_APK);

        ReleaseInfo info = new ReleaseInfo(jsonObject);
        assertThat(info.getVersion()).isEqualTo(2);
        assertThat(info.getMd5()).isEqualTo("xxx");
        assertThat(info.getType()).isEqualTo(ReleaseInfo.TYPE_APK);
    }
}
