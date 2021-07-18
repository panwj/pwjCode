package com.mvcdemo.network.base;

import com.yanzhenjie.nohttp.rest.Response;

import org.json.JSONException;


public abstract class BaseResponse {

    private int mStatusCode;

    private int mRequestId;

    private Response<String> mResponse;

    public int getStatusCode() {
        return mStatusCode;
    }

    public void setStatusCode(int statusCode) {
        mStatusCode = statusCode;
    }

    public int getRequestId() {
        return mRequestId;
    }

    public void setRequestId(int id) {
        mRequestId = id;
    }

    public Response<String> getResponse() {
        return mResponse;
    }

    public void setResponse(Response<String> response) {
        mResponse = response;
    }

    public abstract void parseResponse(String json) throws JSONException;

    public abstract void errorResponse(int requestId, Response<String> rawResponse);

}
