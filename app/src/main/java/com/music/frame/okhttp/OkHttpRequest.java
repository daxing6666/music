package com.music.frame.okhttp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jack
 */

public abstract class OkHttpRequest<T> {

    private int readTimeOut = 25;//数据读取超时时间，默认为30s
    private int writeTimeOut = 25; //写超时时间，默认为15s
    private int connectTimeOut = 25; //连接超时时间，默认为25s
    private List<KeyValue> requestParams;//请求参数
    private boolean header = false;//是否有请求头
    private int tag;//请求标识
    private String url;
    private boolean file;//是否有文件的提交
    public boolean isSingleFile = false;

    public OkHttpRequest(int tag, String url){
        requestParams = new ArrayList<>();
        this.url = url;
        this.tag = tag;
    }

    public void addParams(String key, String value) {
        requestParams.add(new KeyValue(key, value));
    }

    public void addParams(String key, long value) {
        requestParams.add(new KeyValue(key, String.valueOf(value)));
    }

    public void addParams(String key, int value) {
        requestParams.add(new KeyValue(key, String.valueOf(value)));
    }

    public void addParams(String key, short value) {
        requestParams.add(new KeyValue(key, String.valueOf(value)));
    }

    public void addParams(String key, float value) {
        requestParams.add(new KeyValue(key, String.valueOf(value)));
    }

    public void addParams(String key, double value) {
        requestParams.add(new KeyValue(key, String.valueOf(value)));
    }

    public void addParams(String key, File value) {
        requestParams.add(new KeyValue(key, value));
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    protected abstract T getResult(String responseBody);

    public void setReadTimeOut(int readTimeOut) {
        this.readTimeOut = readTimeOut;
    }

    public int getWriteTimeOut() {
        return writeTimeOut;
    }

    public void setWriteTimeOut(int writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public void setConnectTimeOut(int connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public List<KeyValue> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<KeyValue> requestParams) {
        this.requestParams = requestParams;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public boolean isSingleFile() {
        return isSingleFile;
    }

    public void setSingleFile(boolean singleFile) {
        isSingleFile = singleFile;
    }
}
