package com.mvcdemo.network.base;

import android.text.TextUtils;

import com.cipher.CipherUtils;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.OnResponseListener;
import com.yanzhenjie.nohttp.rest.Request;
import com.yanzhenjie.nohttp.rest.Response;
import com.yanzhenjie.nohttp.tools.MultiValueMap;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mvcdemo.common.util.MAD5Util;
import com.mvcdemo.network.NetWorkConstants;


public class BaseRequest<T extends BaseResponse> {

    protected Request<String> mRequest;

    public BaseRequest(String url, RequestMethod method) {
        mRequest = NoHttp.createStringRequest(url, method);
        mRequest.addHeader(NetWorkConstants.HEADER_TAG_PLATFORM, "Android");
    }

    public void setCancelSign(Object sign) {
        mRequest.setCancelSign(sign);
    }

    public void start(int requestId, final T response) {

        calculationParameter();

        CommonHttpManager.getInstance().add(requestId, mRequest, new OnResponseListener<String>() {
            @Override
            public void onStart(int requestId) {

            }

            @Override
            public void onSucceed(int requestId, Response<String> rawResponse) {
                response.setStatusCode(rawResponse.responseCode());
                response.setRequestId(requestId);
                response.setResponse(rawResponse);

                if (rawResponse.responseCode() == NetWorkConstants.STATUS_CODE_OK) {
                    try {
                        String result = rawResponse.get();
                        response.parseResponse(result);
                    } catch (JSONException e) {
                        response.setStatusCode(NetWorkConstants.STATUS_CODE_JSON_ERROR);
                        response.errorResponse(requestId, rawResponse);
                    }
                } else {
                    response.errorResponse(requestId, rawResponse);
                }

            }

            @Override
            public void onFailed(int requestId, Response<String> rawResponse) {
                response.setStatusCode(NetWorkConstants.STATUS_CODE_NETWORK_ERROR);
                response.setRequestId(requestId);
                response.setResponse(rawResponse);
                response.errorResponse(requestId, rawResponse);

            }

            @Override
            public void onFinish(int requestId) {

            }
        });
    }

    /**
     * called when exit app to release cpu。
     */
    public void stop() {
        CommonHttpManager.getInstance().stop();
    }

    private void calculationParameter() {
        if (mRequest == null) return;

        Object cancelSign = mRequest.getCancelSign();
        if (cancelSign != null
                && (TextUtils.equals(String.valueOf(cancelSign), "NetGeocodRequest")
                || TextUtils.equals(String.valueOf(cancelSign), "NetUploadAvatarResponse"))) {
            return;
        }

        MultiValueMap<String, Object> mParams =  mRequest.getParamKeyValues();
        if (mParams == null || mParams.size() <= 0) return;

        Set<Map.Entry<String, List<Object>>> sets = mParams.entrySet();

        List<Map.Entry<String, List<Object>>> list = new ArrayList<Map.Entry<String, List<Object>>>(sets);

        Collections.sort(list,new Comparator<Map.Entry<String, List<Object>>>() {
            //升序排序
            public int compare(Map.Entry<String, List<Object>> o1,
                               Map.Entry<String, List<Object>> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        StringBuffer buffer = new StringBuffer();
        for (Map.Entry<String, List<Object>> data : list) {
            List<Object> values = data.getValue();
            for (int i = 0; i < values.size(); i++) {
                String value = "" + String.valueOf(values.get(i));
                buffer.append(value.trim());
            }
        }

        String source_parameter = buffer.toString() + CipherUtils.getCipherKeyFromJNI();
        String md5 = MAD5Util.getMD5(source_parameter.getBytes());
        mRequest.add("md5", md5);
    }
}
