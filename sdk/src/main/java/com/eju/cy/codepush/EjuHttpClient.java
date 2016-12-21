package com.eju.cy.codepush;


import android.content.Context;

import java.io.IOException;

public abstract class EjuHttpClient<ExternalRequest, ExternalResponse> {

    protected int timeout;

    public EjuHttpClient(int timeout) {
        this.timeout = timeout;
    }

    public static EjuHttpClient newClient(int timeout, Context context) {
        EjuHttpClient client = new EjuURLConnectionClient(timeout);
        return client;
    }

    public abstract EjuResponse execute(EjuRequest ejuRequest) throws EjuCodePushException;

    abstract ExternalRequest getRequest(EjuRequest ejuRequest) throws IOException;

    abstract EjuResponse getResponse(ExternalResponse externalResponse) throws EjuCodePushException;

}
