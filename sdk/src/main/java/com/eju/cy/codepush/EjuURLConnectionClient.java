package com.eju.cy.codepush;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http client with {@code HttpURLConnection}.
 *
 * @author SidneyXu
 */
/* package */ class EjuURLConnectionClient extends EjuHttpClient<HttpURLConnection, HttpURLConnection> {

    public EjuURLConnectionClient(int timeout) {
        super(timeout);
    }

    @Override
    public EjuResponse execute(EjuRequest ejuRequest) throws EjuCodePushException {
        HttpURLConnection connection = null;
        OutputStream outputStream = null;
        try {
            connection = getRequest(ejuRequest);
            if (ejuRequest.getBody() != null) {
                outputStream = connection.getOutputStream();
                Utils.writeBytes(ejuRequest.getBody(), outputStream);
            }
        } catch (IOException e) {
            throw new EjuCodePushException(e);
        } finally {
            Utils.closeQuietly(outputStream);
        }
        return getResponse(connection);
    }

    @Override
    HttpURLConnection getRequest(EjuRequest request) throws IOException {
        URL url = new URL(request.getUrl());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(getMethod(request.getMethod()));
        connection.setConnectTimeout(timeout);
        connection.setReadTimeout(timeout);
        connection.setDoInput(true);

        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setRequestProperty(EjuRequest.CONTENT_TYPE, request.getContentType());

        if (request.getBody() != null) {
            int contentLength = request.getBody().length;
            connection.setRequestProperty(EjuRequest.CONTENT_LENGTH, "" + contentLength);
            connection.setRequestProperty(EjuRequest.CONTENT_TYPE, request.getContentType());
            connection.setFixedLengthStreamingMode(contentLength);
            connection.setDoOutput(true);
        }
        return connection;
    }

    @Override
    EjuResponse getResponse(HttpURLConnection connection) throws EjuCodePushException {
        InputStream bodyStream = null;
        try {
            int statusCode = connection.getResponseCode();
            bodyStream = connection.getErrorStream();
            if (bodyStream == null) {
                bodyStream = connection.getInputStream();
            }
            Map<String, String> headers = new HashMap<String, String>();
            for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet()) {
                if (entry.getKey() != null && entry.getValue().size() > 0) {
                    headers.put(entry.getKey(), entry.getValue() == null ? "" : entry.getValue().get(0));
                }
            }
            byte[] data = Utils.readBytes(bodyStream);
            return new EjuResponse(
                    statusCode,
                    headers,
                    data
            );
        } catch (IOException e) {
            throw new EjuCodePushException(e);
        } finally {
            Utils.closeQuietly(bodyStream);
        }
    }

    private String getMethod(int method) {
        switch (method) {
            case EjuRequest.METHOD_DELETE:
                return "DELETE";
            case EjuRequest.METHOD_GET:
                return "GET";
            case EjuRequest.METHOD_POST:
                return "POST";
            case EjuRequest.METHOD_PUT:
                return "PUT";
            default:
                return "UNKNOWN";
        }
    }
}
