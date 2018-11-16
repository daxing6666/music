package com.music.frame.okhttp;

import com.music.AppDroid;
import com.music.R;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

/**
 * Created by jack
 */

public class OkHttpUtils {

    private static OkHttpUtils okHttpUtils;
    private OkHttpClient okHttpClient;

    private OkHttpUtils(){
        HttpsUtil.SSLParams sslParams = HttpsUtil.getSslSocketFactory(AppDroid.getInstance(),
                new int[0], R.raw.tomcat, "password");
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        okHttpClient = okHttpClientBuilder
                .retryOnConnectionFailure(false)
                .sslSocketFactory(sslParams.sSLSocketFactory)
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return hostname.equals(session.getPeerHost());
                        //return true;
                    }
                }).build();
    }

    public static OkHttpUtils getInstance(){
        if(okHttpUtils == null){
            synchronized (OkHttpUtils.class){
                okHttpUtils = new OkHttpUtils();
            }
        }
        return  okHttpUtils;
    }

    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public void cancel(Object tag) {
        if(okHttpClient != null){
            Dispatcher dispatcher = okHttpClient.dispatcher();
            for (Call call : dispatcher.queuedCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
            for (Call call : dispatcher.runningCalls()) {
                if (tag.equals(call.request().tag())) {
                    call.cancel();
                }
            }
        }
    }

    public void cancelAll(){
        if(okHttpClient != null){
            okHttpClient.dispatcher().cancelAll();
        }
    }

}
