package com.eju.cy.codepush;

import org.json.JSONObject;

/**
 * This class represents release info.
 *
 * @author SidneyXu
 */
/* package */ class ReleaseInfo {

    public static final int TYPE_APK = 0;
    public static final int TYPE_H5 = 1;

    private String md5;
    private int version = -1;
    private int type;
    private boolean forceUpdate;

    public ReleaseInfo(JSONObject jsonObject) {
        md5 = jsonObject.optString("md5");
        version = jsonObject.optInt("version");
        type = jsonObject.optInt("type");
        forceUpdate = jsonObject.optBoolean("forceUpdate");
    }

    public ReleaseInfo() {
    }

    public boolean isH5Release() {
        return type == TYPE_H5;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReleaseInfo{");
        sb.append("md5='").append(md5).append('\'');
        sb.append(", version=").append(version);
        sb.append(", type=").append(type);
        sb.append(", forceUpdate=").append(forceUpdate);
        sb.append('}');
        return sb.toString();
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(boolean forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
