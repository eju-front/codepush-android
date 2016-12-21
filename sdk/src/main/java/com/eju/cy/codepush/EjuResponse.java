package com.eju.cy.codepush;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the response.
 *
 * @author SidneyXu
 */
/* package */ class EjuResponse {

    private Map<String, String> headers = new HashMap<String, String>();

    private byte[] body;

    private int statusCode;

    public EjuResponse(int statusCode, Map<String, String> headers, byte[] body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.body = body;
    }

    public boolean isSuccessful() {
        return (statusCode >= 200 && statusCode < 300) || statusCode == 304;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        if (headers.containsKey(EjuRequest.CONTENT_TYPE)) {
            return headers.get(EjuRequest.CONTENT_TYPE);
        }
        return null;
    }

    public byte[] getBody() {
        return body;
    }

    public String getBodyAsString() {
        if (null == body) return null;
        try {
            return new String(body, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(body);
        }
    }

    public JSONObject getBodyAsJSONObject() {
        if (null == body) return null;
        try {
            return new JSONObject(getBodyAsString());
        } catch (JSONException e) {
            return null;
        }
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
